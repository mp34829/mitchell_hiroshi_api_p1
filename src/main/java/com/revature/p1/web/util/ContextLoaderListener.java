package com.revature.p1.web.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.revature.p1.datasource.repos.BatchRepository;
import com.revature.p1.datasource.repos.UserRepository;
import com.revature.p1.datasource.util.MongoClientFactory;
import com.revature.p1.services.BatchService;
import com.revature.p1.services.UserService;
import com.revature.p1.util.PasswordUtils;
import com.revature.p1.web.servlets.AuthServlet;
import com.revature.p1.web.servlets.BatchServlet;
import com.revature.p1.web.servlets.StudentServlet;
import com.revature.p1.web.servlets.UserServlet;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;

public class ContextLoaderListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        MongoClient mongoClient = MongoClientFactory.getInstance().getConnection();
        PasswordUtils passwordUtils = new PasswordUtils();
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

        UserRepository userRepo = new UserRepository(mongoClient);
        BatchRepository batchRepo = new BatchRepository(mongoClient);
        UserService userService = new UserService(userRepo, passwordUtils);
        BatchService batchService = new BatchService(batchRepo);

        UserServlet userServlet = new UserServlet(userService, mapper);
        AuthServlet authServlet = new AuthServlet(userService, mapper);
        BatchServlet batchServlet = new BatchServlet(batchService, mapper);
        StudentServlet studentServlet = new StudentServlet(userService, mapper);

        ServletContext servletContext = sce.getServletContext();
        servletContext.addServlet("UserServlet", userServlet).addMapping("/user/*");
        servletContext.addServlet("AuthServlet", authServlet).addMapping("/auth");
        servletContext.addServlet("BatchServlet", batchServlet).addMapping("/batch/*");
        servletContext.addServlet("StudentServlet", studentServlet).addMapping("/student/*");

        configureLogback(servletContext);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        MongoClientFactory.getInstance().cleanUp();
    }

    private void configureLogback(ServletContext servletContext) {

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator logbackConfig = new JoranConfigurator();
        logbackConfig.setContext(loggerContext);
        loggerContext.reset();

        String logbackConfigFilePath = servletContext.getRealPath("") + File.separator + servletContext.getInitParameter("logback-config");

        try {
            logbackConfig.doConfigure(logbackConfigFilePath);
        } catch (JoranException e) {
            e.printStackTrace();
            System.out.println("An unexpected exception occurred. Unable to configure Logback.");
        }

    }

}
