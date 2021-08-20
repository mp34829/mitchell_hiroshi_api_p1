package com.revature.p1.web.servlets;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.revature.p1.datasource.documents.AppUser;
import com.revature.p1.datasource.documents.Batch;
import com.revature.p1.services.BatchService;

import com.revature.p1.util.exceptions.InvalidRequestException;
import com.revature.p1.util.exceptions.ResourceNotFoundException;
import com.revature.p1.util.exceptions.ResourcePersistenceException;
import com.revature.p1.web.dtos.ErrorResponse;
import com.revature.p1.web.dtos.Principal;
import org.bson.json.JsonObject;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
import java.util.HashMap;
import java.util.List;

public class BatchServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(UserServlet.class);
    private final BatchService batchService;
    private final ObjectMapper mapper;

    public BatchServlet(BatchService batchService, ObjectMapper mapper) {
        this.batchService = batchService;
        this.mapper = mapper;
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        // Get the session from the request, if it exists (do not create one)
        HttpSession session = req.getSession(false);

        // If the session is not null, then grab the Principal attribute from it
        Principal requestingUser = (session == null) ? null : (Principal) session.getAttribute("Principal");

        // Check to see if there was a valid auth-user attribute
        if (requestingUser == null) {
            String msg = "No session found, please login.";
            logger.info(msg);
            resp.setStatus(401);
            ErrorResponse errResp = new ErrorResponse(401, msg);
            respWriter.write(mapper.writeValueAsString(errResp));
            return;
        }

        //return a list of all batches available for registration. Will not work for null registration start and end values, but replacing method with listAllBatches() will work.
        try {
            List<Batch> batches = batchService.listUsableBatches();
            respWriter.write(mapper.writeValueAsString(batches));
        } catch(ResourceNotFoundException rnfe){
            resp.setStatus(404);
            ErrorResponse errResp = new ErrorResponse(404, rnfe.getMessage());
            respWriter.write(mapper.writeValueAsString(errResp));
        }catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(500);
            ErrorResponse errResp = new ErrorResponse(500, "The server experienced an issue, please try again later.");
            respWriter.write(mapper.writeValueAsString(errResp));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        // Get the session from the request, if it exists (do not create one)
        HttpSession session = req.getSession(false);

        // If the session is not null, then grab the AppUser attribute from it
        AppUser requestingUser = (session == null) ? null : (AppUser) session.getAttribute("AppUser");

        // Check to see if an active session exists, and has proper authorization to add batches
        if (requestingUser == null) {
            String msg = "No session found, please login.";
            logger.info(msg);
            resp.setStatus(401);
            ErrorResponse errResp = new ErrorResponse(401, msg);
            respWriter.write(mapper.writeValueAsString(errResp));
            return;
        } else if (!requestingUser.getUserPrivileges().equals("1")) {
            String msg = "Request by non-faculty user " + requestingUser.getUsername() + ", denied.";
            logger.info(msg);
            resp.setStatus(403);
            ErrorResponse errResp = new ErrorResponse(403, msg);
            respWriter.write(mapper.writeValueAsString(errResp));
            return;
        }

        //add batch
        try {
            Batch batch = mapper.readValue(req.getInputStream(), Batch.class);
            batchService.addBatch(batch);
            String payload = mapper.writeValueAsString(batch);
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
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        // Get the session from the request, if it exists (do not create one)
        HttpSession session = req.getSession(false);

        // If the session is not null, then grab the AppUser attribute from it
        AppUser requestingUser = (session == null) ? null : (AppUser) session.getAttribute("AppUser");

        // Check to see if an active session exists, and has proper authorization to add batches
        if (requestingUser == null) {
            String msg = "No session found, please login.";
            logger.info(msg);
            resp.setStatus(401);
            ErrorResponse errResp = new ErrorResponse(401, msg);
            respWriter.write(mapper.writeValueAsString(errResp));
            return;
        } else if (!requestingUser.getUserPrivileges().equals("1")) {
            String msg = "Request by non-faculty user " + requestingUser.getUsername() + ", denied.";
            logger.info(msg);
            resp.setStatus(403);
            ErrorResponse errResp = new ErrorResponse(403, msg);
            respWriter.write(mapper.writeValueAsString(errResp));
            return;
        }
        try {
            //TODO Remove batch may not delete the batch itself
            JSONParser jsonParser = new JSONParser();
            JSONObject json = (JSONObject) jsonParser.parse(new InputStreamReader(req.getInputStream(), "UTF-8"));
            String shortname = json.get("shortname").toString();

            batchService.removeBatch(shortname);
            respWriter.write(shortname +" has been removed.");
            resp.setStatus(201);

        } catch (InvalidRequestException | MismatchedInputException | ParseException |NullPointerException e) {
            e.printStackTrace();
            resp.setStatus(400); // client's fault
            ErrorResponse errResp = new ErrorResponse(400, "Invalid request. Please try again.");
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

}
