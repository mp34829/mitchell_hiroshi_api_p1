package com.revature.p1.web.servlets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.revature.p1.datasource.documents.AppUser;
import com.revature.p1.web.dtos.ErrorResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

public interface Authorizable {

    void authorizedUserCheck(AppUser user, String privilege, HttpServletResponse resp, PrintWriter respWriter) throws JsonProcessingException;


}
