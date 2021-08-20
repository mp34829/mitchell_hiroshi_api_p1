package com.revature.p1.web.servlets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.revature.p1.datasource.documents.AppUser;
import com.revature.p1.services.UserService;
import com.revature.p1.util.exceptions.InvalidRequestException;
import com.revature.p1.util.exceptions.ResourceNotFoundException;
import com.revature.p1.util.exceptions.ResourcePersistenceException;
import com.revature.p1.web.dtos.AppUserDTO;
import com.revature.p1.web.dtos.ErrorResponse;
import com.revature.p1.web.dtos.Principal;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

public class UserServlet extends HttpServlet implements Authenticatable{
    private final Logger logger = LoggerFactory.getLogger(UserServlet.class);
    private final UserService userService;
    private final ObjectMapper mapper;

    public UserServlet(UserService userService, ObjectMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @Override //GETS ALL FIELDS FOR REQUESTING USER (doesn't do that, yet)
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        // Get the session from the request, if it exists (do not create one)
        HttpSession session = req.getSession(false);

        // If the session is not null, then grab the AppUser attribute from it
        AppUser requestingUser = (session == null) ? null : (AppUser) session.getAttribute("AppUser");

        // Check to see if there was a valid auth-user attribute
        activeSessionCheck(requestingUser,resp,respWriter);
        authorizedUserCheck(requestingUser, "admin", resp, respWriter);

        String userIdParam = req.getParameter("id");

        try {

            if (userIdParam == null) {
                List<AppUserDTO> users = userService.findAll();
                respWriter.write(mapper.writeValueAsString(users));
            } else {
                AppUser user = userService.findUserById(userIdParam);
                respWriter.write(mapper.writeValueAsString(new AppUserDTO(user)));
            }

        } catch (ResourceNotFoundException rnfe) {
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

            //registers new user and creates a Principle object
            Principal principal = new Principal(userService.register(newUser)); // after this, the newUser should have a new id

            //Upon registration, setting the session's Principal and AppUser attributes
            HttpSession session = req.getSession();
            session.setAttribute("AppUser", newUser);
            session.setAttribute("Principal", principal);

            //Sends response confirming successful registration
            String payload = mapper.writeValueAsString(principal);
            respWriter.write(payload);
            resp.setStatus(201);

        } catch (InvalidRequestException | MismatchedInputException e) {
            e.printStackTrace();
            resp.setStatus(400); // client's fault
            ErrorResponse errResp = new ErrorResponse(400, e.getMessage());
            respWriter.write(mapper.writeValueAsString(errResp));
        } catch (ResourcePersistenceException rpe) {
            resp.setStatus(409);
            ErrorResponse errResp = new ErrorResponse(409, rpe.getMessage());
            respWriter.write(mapper.writeValueAsString(errResp));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500); // server's fault
        }

    }
    //TODO figure out where to put request for displaying current user's fields. Before updating user fields or as part of doGET method, maybe in a new servlet??.
//    @Override
//    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException{
//        PrintWriter respWriter = resp.getWriter();
//        resp.setContentType("application/json");
//
//        // Get the session from the request, if it exists (do not create one)
//        HttpSession session = req.getSession(false);
//
//        // If the session is not null, then grab the AppUser attribute from it
//        AppUser requestingUser = (session == null) ? null : (AppUser) session.getAttribute("AppUser");
//
//        //If requesting user is null, return an error response to user
//        if (requestingUser == null) {
//            String msg = "No session found, please login.";
//            logger.info(msg);
//            resp.setStatus(401);
//            ErrorResponse errResp = new ErrorResponse(401, msg);
//            respWriter.write(mapper.writeValueAsString(errResp));
//            return;
//        }
//
//        //View requesting user's fields
//        AppUserDTO dto = new AppUserDTO(requestingUser);
//        String payload = mapper.writeValueAsString(dto);
//        respWriter.write(payload);
//        resp.setStatus(201);
//
//    }
    @Override//UPDATES USER FIELDS
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        // Get the session from the request, if it exists (do not create one)
        HttpSession session = req.getSession(false);

        // If the session is not null, then grab the AppUser attribute from it
        AppUser requestingUser = (session == null) ? null : (AppUser) session.getAttribute("AppUser");

        try {
            //If requesting user is null, return an error response to user
            activeSessionCheck(requestingUser, resp, respWriter);

            //Parse request body and cast it to a JSONObject, then update the requesting user's fields based on the contents of the JSONObject instance
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(new InputStreamReader(req.getInputStream(), "UTF-8"));
            userService.updateUserByField((AppUser) session.getAttribute("AppUser"), json);
            respWriter.write("Changes made to user's requested fields.");

        } catch (InvalidRequestException ire){
            ire.printStackTrace();
            resp.setStatus(400);
        } catch (Exception e){
            e.printStackTrace();
            resp.setStatus(500);
        }

    }

    // Implementations of Authenticatable interface
    @Override
    public void activeSessionCheck(AppUser user, HttpServletResponse resp, PrintWriter respWriter) throws JsonProcessingException {
        if (user == null) {
            String msg = "No session found, please login.";
            logger.info(msg);
            resp.setStatus(401);
            ErrorResponse errResp = new ErrorResponse(401, msg);
            respWriter.write(mapper.writeValueAsString(errResp));
            return;
        }
    }

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
