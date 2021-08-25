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
import com.revature.p1.web.filters.AuthFilter;
import com.revature.p1.web.servlets.AuthServlet;
import com.revature.p1.web.servlets.BatchServlet;
import com.revature.p1.web.servlets.StudentServlet;
import com.revature.p1.web.servlets.UserServlet;
import com.revature.p1.web.util.security.JwtConfig;
import com.revature.p1.web.util.security.TokenGenerator;
import com.revature.p1.web.servlets.HealthCheckServlet;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.util.EnumSet;

public class ContextLoaderListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        MongoClient mongoClient = MongoClientFactory.getInstance().getConnection();
        PasswordUtils passwordUtils = new PasswordUtils();
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        JwtConfig jwtConfig = new JwtConfig();
        TokenGenerator tokenGenerator = new TokenGenerator(jwtConfig);

        UserRepository userRepo = new UserRepository(mongoClient);
        BatchRepository batchRepo = new BatchRepository(mongoClient);
        UserService userService = new UserService(userRepo, batchRepo, passwordUtils);
        BatchService batchService = new BatchService(batchRepo);

        AuthFilter authFilter = new AuthFilter(jwtConfig);

        HealthCheckServlet healthCheckServlet = new HealthCheckServlet();
        UserServlet userServlet = new UserServlet(userService, mapper, tokenGenerator);
        AuthServlet authServlet = new AuthServlet(userService, mapper, tokenGenerator);
        BatchServlet batchServlet = new BatchServlet(userService, batchService, mapper);
        StudentServlet studentServlet = new StudentServlet(userService, mapper);

        ServletContext servletContext = sce.getServletContext();
        servletContext.addFilter("AuthFilter", authFilter).addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
        servletContext.addServlet("UserServlet", userServlet).addMapping("/user/*");
        servletContext.addServlet("AuthServlet", authServlet).addMapping("/auth");
        servletContext.addServlet("BatchServlet", batchServlet).addMapping("/batch/*");
        servletContext.addServlet("StudentServlet", studentServlet).addMapping("/student/*");
        servletContext.addServlet("HealthCheckServlet", healthCheckServlet).addMapping("/health");

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
