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
package pl.otwartapw.opw.ws.obwodowa;

import javax.ejb.EJB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import pl.otwartapw.opw.ws.obwodowa.controller.UserServiceEjb;
import pl.otwartapw.opw.ws.obwodowa.dto.UserRegisterDto;

/**
 * Represents user perspective.
 *
 * @author Adam Kowalewski
 */
@Path("/user")
public class UserService extends AbstractService {

    private final static Logger logger = LoggerFactory.getLogger(UserService.class);

    @EJB
    UserServiceEjb userServiceEjb;

    @GET
    @Path("/login")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response login(
            @HeaderParam(OPW_HEADER_DEBUG_ERROR500) String debug,
            @NotNull @HeaderParam(OPW_HEADER_LOGIN) String login,
            @NotNull @HeaderParam(OPW_HEADER_PASSWORD) String password) {

        if (debug != null) {
            logger.debug(LOG_DBG_500);
            return mockServerError();
        }

        logger.trace("Login {} ", login);
        return buildResponse(userServiceEjb.login(login, password));
    }

    @DELETE
    @Path("/{userId}/obwodowa/{pkwId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response delObwodowa(
            @HeaderParam(OPW_HEADER_DEBUG_ERROR500) String debug,
            @NotNull @PathParam("userId") int userId,
            @NotNull @PathParam("pkwId") String pkwId,
            @NotNull @HeaderParam(OPW_HEADER_LOGIN) String login,
            @NotNull @HeaderParam(OPW_HEADER_TOKEN) String token) {
        if (debug != null) {
            logger.debug(LOG_DBG_500);
            return mockServerError();
        }

        logger.trace("del obodowa {} ", pkwId);
        return buildResponse(userServiceEjb.delObwodowa(userId, pkwId, login, token));
    }

    @PUT
    @Path("/{userId}/obwodowa/{pkwId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response putObwodowa(
            @HeaderParam(OPW_HEADER_DEBUG_ERROR500) String debug,
            @NotNull @PathParam("userId") int userId,
            @NotNull @PathParam("pkwId") String pkwId,
            @NotNull @HeaderParam(OPW_HEADER_LOGIN) String login,
            @NotNull @HeaderParam(OPW_HEADER_TOKEN) String token) {
        if (debug != null) {
            logger.debug(LOG_DBG_500);
            return mockServerError();
        }

        logger.trace("add obodowa {} ", pkwId);
        return buildResponse(userServiceEjb.addObwodowa(userId, pkwId, login, token));
    }

    @GET
    @Path("/{userId}/obwodowa")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response loadObwodowaShortList(
            @HeaderParam(OPW_HEADER_DEBUG_ERROR500) String debug,
            @NotNull @PathParam("userId") int userId,
            @NotNull @HeaderParam(OPW_HEADER_LOGIN) String login,
            @NotNull @HeaderParam(OPW_HEADER_TOKEN) String token) {

        if (debug != null) {
            logger.debug(LOG_DBG_500);
            return mockServerError();
        }

        logger.trace("user {} ", login);
        return buildResponse(userServiceEjb.loadObwodowaShortList(userId, login, token));
    }

    @GET
    @Path("/logout")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response logout(
            @HeaderParam(OPW_HEADER_DEBUG_ERROR500) String debug,
            @NotNull @HeaderParam(OPW_HEADER_LOGIN) String login,
            @NotNull @HeaderParam(OPW_HEADER_TOKEN) String token
    ) {

        if (debug != null) {
            logger.debug(LOG_DBG_500);
            return mockServerError();
        }

        return buildResponse(userServiceEjb.logout(login, token));
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response register(
            @HeaderParam(OPW_HEADER_DEBUG_ERROR500) String debug,
            @NotNull @HeaderParam(OPW_HEADER_API_CLIENT) String apiClient,
            @NotNull @HeaderParam(OPW_HEADER_API_TOKEN) String apiToken,
            @NotNull UserRegisterDto newUser) {

        if (debug != null) {
            logger.debug(LOG_DBG_500);
            return mockServerError();
        }

        logger.trace("API client {} ", apiClient);
        return buildResponse(userServiceEjb.register(apiClient, apiToken, newUser));
    }

    @GET
    @Path("/available/{email}")
    public Response checkEmail(
            @NotNull @PathParam("email") String email,
            @NotNull @HeaderParam(OPW_HEADER_API_CLIENT) String apiClient,
            @NotNull @HeaderParam(OPW_HEADER_API_TOKEN) String apiToken,
            @HeaderParam(OPW_HEADER_DEBUG_ERROR500) String debug) {
        
        if (debug != null) {
            logger.debug(LOG_DBG_500);
            return mockServerError();
        }

        logger.trace("CheckEmail request for {} ", email);

        return buildResponse(userServiceEjb.checkEmail(apiClient, apiToken, email));
    }

}
