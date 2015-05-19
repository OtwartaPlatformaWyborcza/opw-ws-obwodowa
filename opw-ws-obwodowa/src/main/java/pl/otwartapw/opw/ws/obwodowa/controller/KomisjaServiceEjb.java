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
package pl.otwartapw.opw.ws.obwodowa.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.core.GenericEntity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.validation.Valid;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static javax.ws.rs.core.Response.Status.OK;
import pl.otwartapw.opw.ws.obwodowa.SecurityHandler;
import pl.otwartapw.opw.ws.obwodowa.bean.KandydatBean;
import pl.otwartapw.opw.ws.obwodowa.bean.ObwodowaBean;
import pl.otwartapw.opw.ws.obwodowa.bean.UserBean;
import pl.otwartapw.opw.ws.obwodowa.bean.WynikBean;
import pl.otwartapw.opw.ws.obwodowa.dto.GResultDto;
import pl.otwartapw.opw.ws.obwodowa.dto.KandydatDto;
import pl.otwartapw.opw.ws.obwodowa.dto.KomisjaDto;
import pl.otwartapw.opw.ws.obwodowa.dto.OkregowaDto;
import pl.otwartapw.opw.ws.obwodowa.dto.WynikDto;
import pl.otwartapw.opw.ws.obwodowa.dto.WynikShortDto;
import pl.otwartapw.opw.ws.obwodowa.entity.OpwKandydat;
import pl.otwartapw.opw.ws.obwodowa.entity.OpwObwodowaKomisja;
import pl.otwartapw.opw.ws.obwodowa.entity.OpwUser;
import pl.otwartapw.opw.ws.obwodowa.entity.OpwWynik;

/**
 * @author Adam Kowalewski
 */
@Stateless
public class KomisjaServiceEjb implements Serializable {

    private final static Logger logger = LoggerFactory.getLogger(KomisjaServiceEjb.class);

    @EJB
    SecurityHandler securityHandler;
    @EJB
    UserBean userBean;
    @EJB
    ObwodowaBean obwodowaBean;
    @EJB
    KandydatBean kandydatBean;
    @EJB
    WynikBean wynikBean;

    public KomisjaServiceEjb() {
    }

    public GResultDto<KomisjaDto> loadObwodowa(String pkwId, String login, String token) {
        if (!securityHandler.checkUser(login, token)) {
            logger.error("unauthorized access  {} - {}", login, token);
            return GResultDto.invalidResult(UNAUTHORIZED.getStatusCode());
        }
        OpwObwodowaKomisja obw = obwodowaBean.findObwodowa(pkwId);
        OpwUser user = userBean.findUser(login);

        if (obw == null) {
            return GResultDto.invalidResult(NOT_FOUND.getStatusCode());
        }

        updateRelation(obw, user);

        List<KandydatDto> kandydatList = findKandydatAllDto();

        // TODO ADAM 
        OkregowaDto okr = new OkregowaDto("obw.getOpwOkregowaKomisjaId().getPkwId().toString()", "obw.getOpwOkregowaKomisjaId().getName()", "obw.getOpwOkregowaKomisjaId().getAddress()");

        KomisjaDto komisja = new KomisjaDto(obw.getPkwId(), obw.getName(), obw.getAddress(), okr, kandydatList);

        return GResultDto.validResult(OK.getStatusCode(), komisja);
    }

    public GResultDto<GenericEntity<List<WynikShortDto>>> loadWynik(String pkwId, String login, String token) {
        if (!securityHandler.checkUser(login, token)) {
            logger.error("unauthorized access  {} - {}", login, token);
            return GResultDto.invalidResult(UNAUTHORIZED.getStatusCode());
        }

        OpwObwodowaKomisja obw = obwodowaBean.findObwodowa(pkwId);

        if (obw == null) {
            return GResultDto.invalidResult(NOT_FOUND.getStatusCode());
        }

        List<WynikShortDto> wynikShortList = new ArrayList<>();
        List<OpwWynik> wynikList = wynikBean.find(obw);

        for (OpwWynik single : wynikList) {
            WynikShortDto wynikShort = new WynikShortDto(single.getId(), String.valueOf(single.getDateCreated().getTime()), single.getRatedPositiv(), single.getRatedNegativ());
            wynikShortList.add(wynikShort);
        }

        GenericEntity<List<WynikShortDto>> resultList = new GenericEntity<List<WynikShortDto>>(wynikShortList) {
        };

        logger.trace("obwodowa {} found {} wynik records", pkwId, wynikShortList.size());

        return GResultDto.validResult(OK.getStatusCode(), resultList);
    }

    public GResultDto uploadWynik(String pkwId, String login, String token, @Valid WynikDto wynik) {
        if (!securityHandler.checkUser(login, token)) {
            logger.error("unauthorized access  {} - {}", login, token);
            return GResultDto.invalidResult(UNAUTHORIZED.getStatusCode());
        }

        OpwUser user = userBean.findUser(login);
        OpwObwodowaKomisja obwodowa = obwodowaBean.findObwodowa(pkwId);

        OpwWynik w = new OpwWynik();
        w.setOpwUserId(user);
        w.setOpwObwodowaKomisjaId(obwodowa);

        w.setLUprawnionych(wynik.getUprawnionych());
        w.setLKartWydanych(wynik.getGlosujacych());
        w.setLKartWaznych(wynik.getKartWaznych());
        w.setLGlosowNiewaznych(wynik.getGlosowNieWaznych());
        w.setLGlosowWaznych(wynik.getGlosowWaznych());
        w.setActive(Boolean.TRUE);
        w.setDateCreated(new Date());

        w.setK1(wynik.getK1());
        w.setK2(wynik.getK2());
        w.setK3(wynik.getK3());
        w.setK4(wynik.getK4());
        w.setK5(wynik.getK5());
        w.setK6(wynik.getK6());
        w.setK7(wynik.getK7());
        w.setK8(wynik.getK8());
        w.setK9(wynik.getK9());
        w.setK10(wynik.getK10());
        w.setK11(wynik.getK11());
        w.setRatedNegativ(0);
        w.setRatedPositiv(0);

        wynikBean.create(w);

        return GResultDto.validResult(OK.getStatusCode());
    }

    public List<KandydatDto> findKandydatAllDto() {
        List<OpwKandydat> kandydatList = kandydatBean.findAll();
        List<KandydatDto> result = new ArrayList<>(kandydatList.size());

        for (OpwKandydat kandydat : kandydatList) {
            KandydatDto k = new KandydatDto(kandydat.getPkwId(), kandydat.getFirstname(), kandydat.getLastname());
            result.add(k);
        }
        return result;
    }

    /**
     * Creates, only if missing, a persistent relation between user and Komisja
     * Obwodowa.
     *
     * @param obwodowa instance of Komisja Obwodowa.
     * @param user instance of user.
     * @author Adam Kowalewski
     * @version 2015.04.03
     */
    public void updateRelation(OpwObwodowaKomisja obwodowa, OpwUser user) {

        if (!user.getOpwObwodowaKomisjaList().contains(obwodowa)) {
            // TODO transaction & reconsider this combo
            user.getOpwObwodowaKomisjaList().add(obwodowa);
            obwodowa.getOpwUserList().add(user);

            logger.trace("add Obwodowa {} to user {}", obwodowa.getPkwId(), user.getEmail());
            userBean.edit(user);
            obwodowaBean.edit(obwodowa);
        }

    }

}
