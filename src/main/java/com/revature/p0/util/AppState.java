package com.revature.p0.util;

import com.revature.p0.repos.BatchRepository;
import com.revature.p0.repos.UserRepository;
import com.revature.p0.screens.adminscreens.AdminBatchesScreen;
import com.revature.p0.screens.adminscreens.AdminDashboardScreen;
import com.revature.p0.screens.userscreens.*;
import com.revature.p0.services.BatchService;
import com.revature.p0.services.UserService;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AppState {

    private static boolean appRunning;
    private final ScreenRouter router;

    public AppState() {

        appRunning = true;
        router = new ScreenRouter();
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

        UserSession userSession = new UserSession();
        UserRepository userRepo = new UserRepository();
        BatchRepository batchRepo = new BatchRepository();
        UserService userService = new UserService(userRepo, userSession);
        BatchService batchService = new BatchService(batchRepo, userSession);


        router.addScreen(new WelcomeScreen(consoleReader, router))
              .addScreen(new LoginScreen(consoleReader, router, userService))
              .addScreen(new RegisterScreen(consoleReader, router, userService))
              .addScreen(new DashboardScreen(consoleReader, router, userService))
              .addScreen(new BatchesScreen(consoleReader, router, userService, batchService))
              .addScreen(new AdminDashboardScreen(consoleReader, router, userService))
              .addScreen(new AdminBatchesScreen(consoleReader, router, userService, batchService))
                ;

    }

    public void startup() {
        router.navigate("/welcome");

        while (appRunning) {
            try {
                router.getCurrentScreen().render();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Switches 'appRunning' to false, ending the while loop and
    // closing the app through super's main.
    public static void shutdown() {
        appRunning = false;
        MongoClientFactory.getInstance().cleanUp();
    }

}
