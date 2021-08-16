package com.revature.p1.screens.userscreens;

import com.revature.p1.datasource.documents.AppUser;
import com.revature.p1.screens.Screen;
import com.revature.p1.services.UserService;
import com.revature.p1.util.ScreenRouter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;

public class RegisterScreen extends Screen {

    private final Logger logger = LogManager.getLogger(RegisterScreen.class);
    private final UserService userService;

    public RegisterScreen(BufferedReader consoleReader, ScreenRouter router, UserService userService) {
        super("RegisterScreen", "/register", consoleReader, router);
        this.userService = userService;
    }

    @Override
    public void render() throws Exception {
        System.out.println("\nRegister for a new account!");

        System.out.print("First name: ");
        String firstName = consoleReader.readLine();

        System.out.print("Last name: ");
        String lastName = consoleReader.readLine();

        System.out.print("Email: ");
        String email = consoleReader.readLine();

        System.out.print("Username: ");
        String username = consoleReader.readLine();

        System.out.print("Password: ");
        String password = consoleReader.readLine();

        AppUser newUser = new AppUser(firstName, lastName, email, username, password, "0");

        try {
            userService.register(newUser);
            System.out.println("User successfully registered!");
            router.navigate("/welcome");
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.debug("User not registered!");
            router.navigate("/welcome");
        }

    }

}
