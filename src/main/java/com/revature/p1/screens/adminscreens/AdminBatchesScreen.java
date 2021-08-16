package com.revature.p1.screens.adminscreens;

import com.revature.p1.datasource.documents.AppUser;
import com.revature.p1.datasource.documents.Batch;
import com.revature.p1.screens.Screen;
import com.revature.p1.services.BatchService;
import com.revature.p1.services.UserService;
import com.revature.p1.util.ScreenRouter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.BufferedReader;
import java.time.Instant;
import java.util.Collections;

import static com.revature.p1.util.AppState.shutdown;

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
        if (!currentUser.getUserPrivileges().equals("1"))
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
                "5) Return to dashboard\n" +
                "6) Exit application\n" +
                "> ";

        System.out.print(menu);

        String userSelection = consoleReader.readLine();
        Batch newBatch;

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
                System.out.print("Are you sure you want to add a class? (Leave empty to abort): ");
                String addCheck = consoleReader.readLine();
                if(addCheck.isEmpty())
                    break;

                System.out.print("Batch Shortname/ID: ");
                String shortName = consoleReader.readLine();

                System.out.print("Batch Name: ");
                String name = consoleReader.readLine();

                System.out.print("Batch Status: ");
                String status = consoleReader.readLine();

                System.out.print("Batch Description: ");
                String description = consoleReader.readLine();

                System.out.print("Batch Registration Start Date: ");
                Instant registrationStart = Instant.parse(consoleReader.readLine());

                System.out.print("Batch Registration End Date:");
                Instant registrationEnd = Instant.parse(consoleReader.readLine());

                newBatch = new Batch(shortName, name, status, description, registrationStart, registrationEnd, Collections.EMPTY_LIST);

                try {
                    batchService.addBatch(newBatch);
                    System.out.println("Batch added successfully!");
                    logger.info("Batch added successfully!");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    logger.debug("Failed to add batch!");
                }
                break;
            case "3":
                try {
                    System.out.print("Which batch (by short name) do you wish to edit? (Leave empty to abort): ");
                    String batchID = consoleReader.readLine();
                    if(batchID.isEmpty())
                        break;
                    Batch oldBatch = batchService.getBatchByID(batchID);
                    String newShortname,newName,newStatus,newDescription;
                    Instant newRegistrationStart,newRegistrationEnd;
                    String line;
                    System.out.print("Current Batch Shortname/ID: " + oldBatch.getShortName() + "\n" + "Insert new value or leave empty to keep the same:");
                    line = consoleReader.readLine();
                    newShortname = line.isEmpty() ? oldBatch.getShortName() : line;

                    System.out.print("Current Batch Name: " + oldBatch.getName() + "\n" + "Insert new value or leave empty to keep the same:");
                    line = consoleReader.readLine();
                    newName = line.isEmpty() ? oldBatch.getName() : line;

                    System.out.print("Current Batch Status: " + oldBatch.getStatus() + "\n" + "Insert new value or leave empty to keep the same:");
                    line = consoleReader.readLine();
                    newStatus = line.isEmpty() ? oldBatch.getStatus() : line;

                    System.out.print("Current Batch Description: " + oldBatch.getDescription() + "\n" + "Insert new value or leave empty to keep the same:");
                    line = consoleReader.readLine();
                    newDescription = line.isEmpty() ? oldBatch.getDescription() : line;

                    System.out.print("Current Batch Start Date: " + oldBatch.getRegistrationStart() + "\n" + "Insert new value or leave empty to keep the same:");
                    line = consoleReader.readLine();
                    newRegistrationStart = line.isEmpty() ? oldBatch.getRegistrationStart() : Instant.parse(line);

                    System.out.print("Current Batch End Date: " + oldBatch.getRegistrationEnd() + "\n" + "Insert new value or leave empty to keep the same:");
                    line = consoleReader.readLine();
                    newRegistrationEnd = line.isEmpty() ? oldBatch.getRegistrationEnd() : Instant.parse(line);

                    newBatch = new Batch(oldBatch.getId(), newShortname, newName, newStatus, newDescription, newRegistrationStart, newRegistrationEnd, oldBatch.getUsersRegistered());
                    batchService.editBatch(newBatch, batchID);
                    System.out.println("Batch edited successfully!");
                    logger.info("Batch edited successfully!");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    logger.debug("Failed to edit batch!");
                }
                break;
            case "4":
                try {
                    System.out.print("Which batch (by short name) do you wish to remove? (Leave empty to abort)");
                    String batchID = consoleReader.readLine();
                    if(batchID.isEmpty())
                        break;
                    batchService.removeBatch(batchID);
                    userService.removeBatch(batchID);
                    System.out.println("Batch removed successfully!");
                    logger.info("Batch removed successfully!");
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    logger.debug("Failed to remove batch!");
                }
                break;
            case "5":
                router.navigate("/admindashboard");
                break;
            case "6":
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
