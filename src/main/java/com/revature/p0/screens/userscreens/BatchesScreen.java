package com.revature.p0.screens.userscreens;

import com.revature.p0.documents.AppUser;
import com.revature.p0.screens.Screen;
import com.revature.p0.screens.adminscreens.AdminBatchesScreen;
import com.revature.p0.services.BatchService;
import com.revature.p0.services.UserService;
import com.revature.p0.util.ScreenRouter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.BufferedReader;

import static com.revature.p0.util.AppState.shutdown;

public class BatchesScreen extends Screen {

    private final Logger logger = LogManager.getLogger(AdminBatchesScreen.class);
    private final UserService userService;
    private final BatchService batchService;

    public BatchesScreen(BufferedReader consoleReader, ScreenRouter router, UserService userService, BatchService batchService) {
        super("BatchesScreen", "/batches", consoleReader, router);
        this.userService = userService;
        this.batchService = batchService;
    }

    /*
        TODO
         Implement a dashboard that displays the user's username and gives them the option
         to navigate to other screens (e.g. UserProfileScreen).
     */
    @Override
    public void render() throws Exception {

        AppUser currentUser = userService.getSession().getCurrentUser();
        if (currentUser.getUserPrivileges() != "1")
        {
            System.out.println("You are not meant to be here.");
            router.navigate("/welcome");
            return;
        }


        String menu = "\nWelcome to p0 Registration Application Unprivileged Batches Screen!\n" +
                "1) Enroll in a Batch\n" +
                "2) Withdraw from existing enrollment\n" +
                "3) Exit application\n" +
                "> ";

        System.out.print(menu);

        String userSelection = consoleReader.readLine();

        switch (userSelection) {

            case "1":
                try {
                    System.out.print("Which batch (by ID number) do you wish to enroll in?");
                    String batchID = consoleReader.readLine();
                    userService.enrollBatch(batchID);
                    batchService.enrollBatch(batchID);
                    logger.info("Enrolled in batch successfully!");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    logger.debug("Failed to enroll in batch!");
                }
                break;
            case "2":
                try {
                    System.out.print("Which batch (by ID number) do you wish to withdraw from?");
                    String batchID = consoleReader.readLine();
                    userService.withdrawBatch(batchID);
                    batchService.withdrawBatch(batchID);
                    logger.info("Withdrew from batch successfully!");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    logger.debug("Failed to withdraw from batch!");
                }
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
