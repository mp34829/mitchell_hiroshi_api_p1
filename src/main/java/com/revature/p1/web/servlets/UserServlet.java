package com.revature.p1.web.servlets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.revature.p1.datasource.documents.AppUser;
import com.revature.p1.services.UserService;
import com.revature.p1.util.exceptions.DataSourceException;
import com.revature.p1.util.exceptions.InvalidRequestException;
import com.revature.p1.util.exceptions.ResourceNotFoundException;
import com.revature.p1.util.exceptions.ResourcePersistenceException;
import com.revature.p1.web.dtos.AppUserDTO;
import com.revature.p1.web.dtos.ErrorResponse;
import com.revature.p1.web.dtos.Principal;
import com.revature.p1.web.util.security.TokenGenerator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;


public class UserServlet extends HttpServlet implements Authorizable {
    private final Logger logger = LoggerFactory.getLogger(UserServlet.class);
    private final UserService userService;
    private final ObjectMapper mapper;
    private final TokenGenerator tokenGenerator;

    public UserServlet(UserService userService, ObjectMapper mapper, TokenGenerator tokenGenerator) {
        this.userService = userService;
        this.mapper = mapper;
        this.tokenGenerator = tokenGenerator;
    }

    @Override //GETS ALL FIELDS FOR REQUESTING USER
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");


        try {
            // Gets principal from request, converts to AppUserDTO
            Principal principal = mapper.convertValue(req.getAttribute("principal"), Principal.class);
            AppUser requestingUser = userService.findUserById(principal.getId());
            AppUserDTO dto = new AppUserDTO(requestingUser);

            //Provides AppUserDTO as payload for response
            String payload = mapper.writeValueAsString(dto);
            respWriter.write(payload);
            resp.setStatus(201);

        } catch (ResourceNotFoundException rnfe) {
            rnfe.printStackTrace();
            resp.setStatus(404);
            ErrorResponse errResp = new ErrorResponse(404, rnfe.getMessage());
            respWriter.write(mapper.writeValueAsString(errResp));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500); // server's fault
            ErrorResponse errResp = new ErrorResponse(500, "The server experienced an issue, please try again later.");
            respWriter.write(mapper.writeValueAsString(errResp));
        }

    }

    @Override //REGISTERS USER
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //System.out.println(req.getAttribute("filtered"));
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        try {
            AppUser newUser = mapper.readValue(req.getInputStream(), AppUser.class);
            //validating request body, checking for null inputs
            if(userService.isUserValid(newUser)==false)
                throw new InvalidRequestException("Invalid user credentials entered. Please try again.");
            respWriter.write("REACHED");
            //registers new user and creates a Principle object
            Principal principal = new Principal(userService.register(newUser)); // after this, the newUser should have a new id

            //Upon registration, create a token
            String token = tokenGenerator.createToken(principal);
            resp.setHeader(tokenGenerator.getJwtConfig().getHeader(), token);

            //Sends response confirming successful registration
            String payload = mapper.writeValueAsString(principal);
            respWriter.write(payload);
            resp.setStatus(201);

        } catch (NullPointerException | InvalidRequestException | MismatchedInputException e) {
            e.printStackTrace();
            resp.setStatus(401);
            ErrorResponse errResp = new ErrorResponse(401, e.getMessage());
            respWriter.write(mapper.writeValueAsString(errResp));
        }catch (DataSourceException dse){
            dse.printStackTrace();
            resp.setStatus(403);
            ErrorResponse errResp = new ErrorResponse(403, dse.getMessage());
            respWriter.write(mapper.writeValueAsString(errResp));
        } catch (ResourcePersistenceException rpe) {
            rpe.printStackTrace();
            resp.setStatus(409);
            ErrorResponse errResp = new ErrorResponse(409, rpe.getMessage());
            respWriter.write(mapper.writeValueAsString(errResp));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500); // server's fault
        }

    }

    @Override//UPDATES USER FIELDS
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        try {
            //Grab principal from request, generate AppUser from principal
            Principal principal = mapper.convertValue(req.getAttribute("principal"), Principal.class);
            AppUser requestingUser = userService.findUserById(principal.getId());


            //Parse request body and cast it to a JSONObject
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(new InputStreamReader(req.getInputStream(), "UTF-8"));
            userService.updateUserByField(requestingUser, json);

            respWriter.write("Changes made to user's requested fields.");

        } catch (InvalidRequestException ire){
            ire.printStackTrace();
            resp.setStatus(401);
            ErrorResponse errResp = new ErrorResponse(401, "Invalid request. Please try again.");
            respWriter.write(mapper.writeValueAsString(errResp));
        } catch (Exception e){
            e.printStackTrace();
            resp.setStatus(500);
        }

    }

    // Implementations of Authorizable interface

    @Override
    public void authorizedUserCheck(AppUser user, String privilege, HttpServletResponse resp, PrintWriter respWriter) throws JsonProcessingException {
        if (!user.getUserPrivileges().equals(privilege)) {
            String msg = "Request by non-authorized user " + user.getUsername() + ", denied.";
            logger.info(msg);
            resp.setStatus(403);
            ErrorResponse errResp = new ErrorResponse(403, msg);
            respWriter.write(mapper.writeValueAsString(errResp));
            return;
        }
    }


}
