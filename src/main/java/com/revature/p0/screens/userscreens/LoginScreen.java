package com.revature.p0.screens.userscreens;

import com.revature.p0.documents.AppUser;
import com.revature.p0.screens.Screen;
import com.revature.p0.services.UserService;
import com.revature.p0.util.ScreenRouter;
import com.revature.p0.util.exceptions.AuthenticationException;

import java.io.BufferedReader;

public class LoginScreen extends Screen {

    private final UserService userService;

    public LoginScreen(BufferedReader consoleReader, ScreenRouter router, UserService userService) {
        super("LoginScreen", "/login", consoleReader, router);
        this.userService = userService;
    }

    @Override
    public void render() throws Exception {
        System.out.print("Username: ");
        String username = consoleReader.readLine();

        System.out.print("Password: ");
        String password = consoleReader.readLine();

        try {
            AppUser authUser = userService.login(username, password);
            System.out.println("Login successful!");
            if (authUser.getUserPrivileges() == "0")
                router.navigate("/dashboard");
            else if (authUser.getUserPrivileges() == "1")
                router.navigate("/admindashboard");

        } catch (AuthenticationException ae) {
            System.out.println("No user found with provided credentials!");
            System.out.println("Navigating back to welcome screen...");
            router.navigate("/welcome");
        }


    }

}
