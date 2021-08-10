package com.revature.p0.screens.adminscreens;

import com.revature.p0.documents.AppUser;
import com.revature.p0.documents.Batch;
import com.revature.p0.screens.Screen;
import com.revature.p0.services.BatchService;
import com.revature.p0.services.UserService;
import com.revature.p0.util.ScreenRouter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.BufferedReader;
import java.time.Instant;

import static com.revature.p0.util.AppState.shutdown;

public class AdminBatchesScreen  extends Screen {

    private final Logger logger = LogManager.getLogger(AdminBatchesScreen.class);
    private final UserService userService;
    private final BatchService batchService;

    public AdminBatchesScreen(BufferedReader consoleReader, ScreenRouter router, UserService userService, BatchService batchService) {
        super("AdminDashboardScreen", "/adminbatches", consoleReader, router);
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


        String menu = "\nWelcome to p0 Registration Application Privileged Batches Screen!\n" +
                "1) View Batches\n" +
                "2) Add Batch\n" +
                "3) Edit Batch\n" +
                "4) Delete Batch\n" +
                "5) Exit application\n" +
                "> ";

        System.out.print(menu);

        String userSelection = consoleReader.readLine();

        switch (userSelection) {

            case "1":
                try {
                    batchService.listAllBatches();
                    logger.info("Batches listed successfully!");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    logger.debug("Failed to list batches!");
                }
                break;
            case "2":
                System.out.print("Batch ID: ");
                String id = consoleReader.readLine();

                System.out.print("Batch Name: ");
                String name = consoleReader.readLine();

                System.out.print("Batch Status: ");
                String status = consoleReader.readLine();

                System.out.print("Batch Registration Start Date: ");
                Instant registrationStart = Instant.parse(consoleReader.readLine());

                System.out.print("Batch Registration End Date:");
                Instant registrationEnd = Instant.parse(consoleReader.readLine());

                Batch newBatch = new Batch(id, name, status, registrationStart, registrationEnd);

                try {
                    batchService.addBatch(newBatch);
                    logger.info("Batch added successfully!");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    logger.debug("Failed to add batch!");
                }
                break;
            case "3":
                try {
                    System.out.print("Which batch (by ID number) do you wish to edit?");
                    String batchID = consoleReader.readLine();
                    batchService.editBatch(batchID);
                    logger.info("Batch edited successfully!");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    logger.debug("Failed to edit batch!");
                }
                break;
            case "4":
                try {
                    System.out.print("Which batch (by ID number) do you wish to remove?");
                    String batchID = consoleReader.readLine();
                    batchService.removeBatch(batchID);
                    userService.removeBatch(batchID);
                    logger.info("Batch removed successfully!");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    logger.debug("Failed to remove batch!");
                }
                break;
            case "5":
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
