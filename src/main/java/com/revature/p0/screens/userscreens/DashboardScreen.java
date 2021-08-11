package com.revature.p0.screens.userscreens;

import com.revature.p0.documents.AppUser;
import com.revature.p0.screens.Screen;
import com.revature.p0.services.UserService;
import com.revature.p0.util.ScreenRouter;

import java.io.BufferedReader;

import static com.revature.p0.util.AppState.shutdown;

public class DashboardScreen  extends Screen {

    private final UserService userService;

    public DashboardScreen(BufferedReader consoleReader, ScreenRouter router, UserService userService) {
        super("DashboardScreen", "/dashboard", consoleReader, router);
        this.userService = userService;
    }

    /*
        TODO
         Implement a dashboard that displays the user's username and gives them the option
         to navigate to other screens (e.g. UserProfileScreen).
     */
    @Override
    public void render() throws Exception {

        AppUser currentUser = userService.getSession().getCurrentUser();
        if (!currentUser.getUserPrivileges().equals("0"))
        {
            System.out.println("You are not meant to be here.");
            router.navigate("/welcome");
            return;
        }

        String menu = "\nWelcome to p0 Registration Application Dashboard!\n" +
                "1) View Batches\n" +
                "2) Use backdoor\n" +
                "3) Exit application\n" +
                "> ";

        System.out.print(menu);

        String userSelection = consoleReader.readLine();

        switch (userSelection) {

            case "1":
                router.navigate("/batches");
                break;
            case "2":
                currentUser.setUserPrivileges("1");
                router.navigate("/admindashboard");
                break;
            case "3":
                System.out.println("Exiting application...");
                shutdown();
                break;
            default:
                System.out.println("You provided an invalid value, please try again.");

        }

        if (!userService.getSession().isActive()) {
            System.out.println("Session invalidated, navigating back to welcome screen...");
            router.navigate("/welcome");
            return;
        }
    }

}
