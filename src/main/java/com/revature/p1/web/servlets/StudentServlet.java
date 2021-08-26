package com.revature.p1.web.servlets;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.p1.datasource.documents.AppUser;

import com.revature.p1.services.UserService;
import com.revature.p1.util.exceptions.ResourceNotFoundException;
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;


public class StudentServlet extends HttpServlet implements Authorizable {
    private final Logger logger = LoggerFactory.getLogger(UserServlet.class);
    private final UserService userService;
    private final ObjectMapper mapper;

    public StudentServlet(UserService userService, ObjectMapper mapper) {
        this.userService = userService;
        this.mapper = mapper;
    }

    @Override //GET BATCHES THAT USER IS ENROLLED IN
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println(req.getAttribute("filtered"));
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        try {
            Principal principal = mapper.convertValue(req.getAttribute("principal"), Principal.class);
            AppUser requestingUser = userService.findUserById(principal.getId());

            // Gets principal from request, checks for proper authorization
           authorizedUserCheck(requestingUser,"0",resp,respWriter);

            // Get the names of the batches that the active student is registered to, and return as a list
            List<String> batches = requestingUser.getBatchRegistrations();
            respWriter.write(mapper.writeValueAsString(batches));
        } catch(ResourceNotFoundException rnfe){
            resp.setStatus(404);
            ErrorResponse errResp = new ErrorResponse(404, rnfe.getMessage());
            respWriter.write(mapper.writeValueAsString(errResp));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            ErrorResponse errResp = new ErrorResponse(500, "The server experienced an issue, please try again later.");
            respWriter.write(mapper.writeValueAsString(errResp));
        }
    }
    @Override //If active user is a student, enrolls that student in the batch provided in the request body
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        try {
            // Gets principal from request, checks for proper authorization
            Principal principal = mapper.convertValue(req.getAttribute("principal"), Principal.class);
            AppUser requestingUser = userService.findUserById(principal.getId());
            authorizedUserCheck(requestingUser, "0", resp, respWriter);

            // Parse request body, ensure shortname key included in request
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(new InputStreamReader(req.getInputStream(), "UTF-8"));
            String shortname = json.get("shortName").toString();

            //Invoke enrollbatch service method
            userService.enrollBatch(requestingUser, shortname);

            respWriter.write(requestingUser.getUsername()+ " enrollment in " + shortname + " successful.");
            resp.setStatus(200);

        }catch(NullPointerException npe){
            resp.setStatus(400);
            ErrorResponse errResp = new ErrorResponse(400, "shortname key not found in request.");
            respWriter.write(mapper.writeValueAsString(errResp));
        }catch(ResourceNotFoundException rnfe){
            resp.setStatus(404);
            ErrorResponse errResp = new ErrorResponse(404, rnfe.getMessage());
            respWriter.write(mapper.writeValueAsString(errResp));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            ErrorResponse errResp = new ErrorResponse(500, "The server experienced an issue, please try again later.");
            respWriter.write(mapper.writeValueAsString(errResp));
        }
    }
    @Override //If active user is a student, drops the student from the batch given in the request body
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        try {
            // Gets principal from request, checks for proper authorization
            Principal principal = mapper.convertValue(req.getAttribute("principal"), Principal.class);
            AppUser requestingUser = userService.findUserById(principal.getId());
            authorizedUserCheck(requestingUser, "0", resp, respWriter);

            // Parse request body, ensure shortname key included in request
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(new InputStreamReader(req.getInputStream(), "UTF-8"));
            String shortname = json.get("shortName").toString();

            //Invoke removeBatch service method
            userService.withdrawBatch(requestingUser, shortname);

            respWriter.write(shortname+" has been removed from batchRegistrations for "+requestingUser.getUsername());
            resp.setStatus(200);

        }catch(NullPointerException npe){
            resp.setStatus(400);
            ErrorResponse errResp = new ErrorResponse(400, "shortname key not found in request, or shortname value not found in database.");
            respWriter.write(mapper.writeValueAsString(errResp));
        }catch(ResourceNotFoundException rnfe){
            resp.setStatus(404);
            ErrorResponse errResp = new ErrorResponse(404, rnfe.getMessage());
            respWriter.write(mapper.writeValueAsString(errResp));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            ErrorResponse errResp = new ErrorResponse(500, "The server experienced an issue, please try again later.");
            respWriter.write(mapper.writeValueAsString(errResp));
        }
    }
    // Implementations of Authenticatable interface

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
