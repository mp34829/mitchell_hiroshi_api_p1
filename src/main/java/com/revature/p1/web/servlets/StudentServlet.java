package com.revature.p1.web.servlets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.p1.datasource.documents.AppUser;
import com.revature.p1.datasource.documents.Batch;
import com.revature.p1.services.BatchService;
import com.revature.p1.util.exceptions.ResourceNotFoundException;
import com.revature.p1.web.dtos.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class StudentServlet extends HttpServlet implements Authenticatable {
    private final Logger logger = LoggerFactory.getLogger(UserServlet.class);
    private final BatchService batchService;
    private final ObjectMapper mapper;


    public StudentServlet(BatchService batchService, ObjectMapper mapper) {
        this.batchService = batchService;
        this.mapper = mapper;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        // Get the session from the request, if it exists (do not create one)
        HttpSession session = req.getSession(false);

        // If the session is not null, then grab the AppUser attribute from it
        AppUser requestingUser = (session == null) ? null : (AppUser) session.getAttribute("AppUser");

        try {
            // Check to see if an active session exists, and active user is a student
            activeSessionCheck(requestingUser, resp, respWriter);
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
