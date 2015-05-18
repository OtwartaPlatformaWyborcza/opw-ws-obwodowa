/*
 * The MIT License
 *
 * Copyright 2015 Adam Kowalewski.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package pl.otwartapw.opw.ws.obwodowa.bean;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.otwartapw.opw.ws.obwodowa.MailConfigDto;
import pl.otwartapw.opw.ws.obwodowa.MailContentDto;
import pl.otwartapw.opw.ws.obwodowa.OpwWsConfigStatic;
import pl.otwartapw.opw.ws.obwodowa.entity.OpwUser;

/**
 * Provides reusable logic related to E-Mail in- and outbound.
 *
 * @author Adam Kowalewski
 */
@Stateless
public class MailBean {

    private static final Logger logger = LoggerFactory.getLogger(MailBean.class);

    @Resource(name = "mail/opw")
    private Session mailSession;
    @EJB
    ConfigBean configBean;

    public boolean sendMailWelcome(OpwUser user, String passwordPlain, boolean useFacesMsg) {
        String subject = "Otwarta Platforma Wyborcza - rejestracja";
        String actLink = prepareActivationLink(user, configBean.readConfigValue(OpwWsConfigStatic.CFG_KEY_BASE_URL));

        MailContentDto payload = new MailContentDto(user.getEmail(), passwordPlain, actLink);
        try {
            String content = getMailContent(payload, "welcome_plain.ftl");
            sendMail(user, subject, content, prepareMailConfig());
            return true;
        } catch (IOException | TemplateException | MessagingException ex) {
            logger.error(null, ex);
        }
        return false;
    }

    public boolean sendMailPasswordNew(OpwUser user, String passwordPlain) {
        String subject = "Otwarta Platforma Wyborcza - nowe hasło";
        String actLink = prepareActivationLink(user, prepareMailConfig().getHostname());
        MailContentDto payload = new MailContentDto(user.getEmail(), passwordPlain, actLink);
        try {
            String content = getMailContent(payload, "passwordNew_plain.ftl");
            sendMail(user, subject, content, prepareMailConfig());
            return true;
        } catch (IOException | TemplateException | MessagingException ex) {
            logger.error(null, ex);
        }
        return false;
    }

    private boolean sendMail(OpwUser user, String subject, String content, MailConfigDto mailConfigDto) throws AddressException, MessagingException, UnsupportedEncodingException {
        boolean outboudEnabled = Boolean.valueOf(configBean.readConfigValue(OpwWsConfigStatic.CFG_KEY_EMAIL_OUTBOUND));

        if (!outboudEnabled) {
            logger.info("E-Mail outbound disabled");
            return false;
        }

        Message message = new MimeMessage(mailSession);

        Address singleReceiver = new InternetAddress(user.getEmail());
        message.setFrom(new InternetAddress(mailConfigDto.getFromAddress(), mailConfigDto.getFromLabel()));
        message.setRecipient(Message.RecipientType.TO, singleReceiver);
        message.setSubject(subject);
        message.setContent(content, "text/plain; charset=UTF-8");
        Transport.send(message);
        return true;
    }

    /**
     * Returns E-Mail content generated with FreeMarker template.
     *
     * @param payload DTO instance.
     * @param templateName name of template to use.
     * @return String representation of content.
     * @throws IOException
     * @throws TemplateException
     * @author Adam Kowalewski
     * @version 2015.03.29
     */
    public String getMailContent(MailContentDto payload, String templateName) throws IOException, TemplateException {
        Configuration cfg = prepareFreemarkerConfig();
        Template templ = cfg.getTemplate(templateName);
        Writer out = new StringWriter();
        templ.process(payload, out);

        return out.toString();
    }

    /**
     * Creates default FreeMarker configuration instance.
     *
     * @return FreeMarker instance.
     * @author Adam Kowalewski
     * @version 2015.03.30
     */
    private Configuration prepareFreemarkerConfig() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setClassForTemplateLoading(MailBean.class, "templates");
        return cfg;
    }

    private String prepareActivationLink(OpwUser user, String hostname) {
        String result = hostname + "/verify.xhtml"
                + "?login=" + user.getEmail()
                + "&code=" + user.getToken();
        return result;
    }

    private MailConfigDto prepareMailConfig() {

        return new MailConfigDto(
                configBean.readConfigValue(OpwWsConfigStatic.CFG_KEY_EMAIL_FROM_LABEL),
                configBean.readConfigValue(OpwWsConfigStatic.CFG_KEY_EMAIL_FROM),
                configBean.readConfigValue(OpwWsConfigStatic.CFG_KEY_BASE_URL));
    }

}
