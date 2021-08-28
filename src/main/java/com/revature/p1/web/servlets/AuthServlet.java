package com.revature.p1.web.servlets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.p1.datasource.documents.AppUser;
import com.revature.p1.services.UserService;
import com.revature.p1.util.exceptions.AuthenticationException;
import com.revature.p1.web.dtos.Credentials;
import com.revature.p1.web.dtos.ErrorResponse;
import com.revature.p1.web.dtos.Principal;
import com.revature.p1.web.util.security.TokenGenerator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class AuthServlet extends HttpServlet {
    private final UserService userService;
    private final ObjectMapper mapper;
    private final TokenGenerator tokenGenerator;

    public AuthServlet(UserService userService, ObjectMapper mapper, TokenGenerator tokenGenerator) {
        this.userService = userService;
        this.mapper = mapper;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println(req.getAttribute("filtered"));
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        try {
            respWriter.write("Reached");
            Credentials creds = mapper.readValue(req.getInputStream(), Credentials.class);
            AppUser user = userService.login(creds.getUsername(), creds.getPassword());
            Principal principal =new Principal(user);
            String payload = mapper.writeValueAsString(principal);
            respWriter.write(payload);
            respWriter.write("Reached again");

//            String token = tokenGenerator.createToken(principal);
//            resp.setHeader(tokenGenerator.getJwtConfig().getHeader(), token);


        } catch (AuthenticationException ae) {
            ae.printStackTrace();
            resp.setStatus(401);
            ErrorResponse errResp = new ErrorResponse(401, ae.getMessage());
            respWriter.write(mapper.writeValueAsString(errResp));
        } catch (IOException io) {
            io.printStackTrace();
            resp.setStatus(400);
            ErrorResponse errResp = new ErrorResponse(400, "IO exception from object mapper");
            respWriter.write(mapper.writeValueAsString(errResp));
        } catch (NullPointerException npe){
            npe.printStackTrace();
            resp.setStatus(409);
            ErrorResponse errResp = new ErrorResponse(409, "Null pointer exception");
            respWriter.write(mapper.writeValueAsString(errResp));
        }  catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500); // server's fault
            ErrorResponse errResp = new ErrorResponse(500, "The server experienced an issue, please try again later.");
            respWriter.write(mapper.writeValueAsString(errResp));
        }

    }
}
