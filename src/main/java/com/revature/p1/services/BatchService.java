package com.revature.p1.services;


import com.revature.p1.datasource.documents.AppUser;
import com.revature.p1.datasource.documents.Batch;
import com.revature.p1.datasource.repos.BatchRepository;
import com.revature.p1.datasource.repos.UserRepository;
import com.revature.p1.util.exceptions.InvalidRequestException;
import com.revature.p1.util.exceptions.ResourcePersistenceException;
import org.json.simple.JSONObject;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class BatchService {

    private final UserRepository userRepo;
    private final BatchRepository batchRepo;

    public BatchService(UserRepository userRepo, BatchRepository batchRepo) {
        this.userRepo = userRepo;
        this.batchRepo = batchRepo;
    }

    /**
     * Validates a batch by checking for empty fields
     *
     * @param batch A user object
     * @return true if user object is filled out, false if missing fields
     */
    public boolean isBatchValid(Batch batch) {
        if (batch == null) return false;
        if (batch.getShortName() == null || batch.getShortName().trim().equals("")) return false;
        if (batch.getName() == null || batch.getName().trim().equals("")) return false;
        if (batch.getDescription() == null || batch.getStatus().trim().equals("")) return false;
        if (batch.getStatus() == null || batch.getStatus().trim().equals("")) return false;
        if (batch.getRegistrationEnd() == null || batch.getRegistrationEnd().equals("")) return false;
        return batch.getRegistrationStart() != null && !batch.getRegistrationStart().equals("");
    }

    /**
     * Returns all Batches in a List.
     *
     */
    public List<Batch> listAllBatches() {return batchRepo.listAllBatches();}

    /**
     * Adds a new batch, after checking validity and redundancy
     *
     * @param newBatch A batch object
     * @return Batch object if batch is valid, or throws an exception
     */
    public Batch addBatch(Batch newBatch){
        if (!isBatchValid(newBatch)) {
            throw new InvalidRequestException("Invalid batch data provided!");
        }

        if (batchRepo.findById(newBatch.getShortName()) != null) {
            throw new ResourcePersistenceException("Provided batch shortname is already taken!");
        }

        return batchRepo.save(newBatch);
    }

    /**
     * Prints all batches that are enabled and registerable to the terminal
     *
     */
    public List<Batch> listUsableBatches(){
        List<Batch> allBatches = batchRepo.listAllBatches();
        List<Batch> usableBatches = new ArrayList<>();
        for (Batch batch : allBatches) {
            int val1 = batch.getRegistrationEnd().compareTo(LocalDate.now());
            int val2 = batch.getRegistrationStart().compareTo(LocalDate.now());
            if (batch.getStatus().equals("Enabled") && val1>0 && val2<0 )
                usableBatches.add(batch);
        }
        return usableBatches;
    }

    /**
     * Fetches a batch with its shortName
     *
     * @param shortName A batch shortName
     * @return Batch object if found, or throws an exception
     */
    public Batch getBatchByID(String shortName){
        return batchRepo.findById(shortName);
    }

    /**
     * Edits a batch by uploading a new object in its place
     *
     * @param shortName A batch shortName
     * @param json A JSON object containing the desired changes to batch fields
     */

    public void editBatch(String shortName, JSONObject json){

        Batch batch = getBatchByID(shortName);
        List<String> fieldList = Arrays.asList("shortName","name","status","description","registrationStart", "registrationEnd");
        Set<String> keys = json.keySet();
        for(String key: keys)
            if(fieldList.indexOf(key)==-1)
                throw new InvalidRequestException("Request to update nonexistent field, denied.");
        if (json.containsKey("name"))
            batch.setName(json.get("name").toString());
        if (json.containsKey("status"))
            batch.setStatus(json.get("status").toString());
        if (json.containsKey("description"))
            batch.setDescription(json.get("description").toString());
        if (json.containsKey("registrationStart"))
            batch.setRegistrationStart(LocalDate.parse(json.get("registrationStart").toString()));
        if (json.containsKey("registrationEnd"))
            batch.setRegistrationEnd(LocalDate.parse(json.get("registrationEnd").toString()));
        batchRepo.update(batch, batch.getShortName());
    }

    /**
     * Withdraws all users from the batch before deleting the batch.
     *
     * @param shortName A batch shortName
     */
    public void removeBatch(String shortName){
        if(batchRepo.findById(shortName) == null)
            throw new InvalidRequestException("Batch name does not exist in repository.");
        List<AppUser> usersByBatch = userRepo.findUsersByBatch(shortName);
        for (AppUser user : usersByBatch) {
            user.getBatchRegistrations().remove(shortName);
            userRepo.update(user, user.getUsername());
        }
        batchRepo.deleteById(shortName);
    }

    /**
     * Adds the current user's name to the batch's Users Registered list (if not already registered)
     *
     * @param shortName A batch shortName
     */
/*    public void enrollBatch(String shortName){
        Batch a = batchRepo.findById(shortName);
        a.addUsersRegistered(session.getCurrentUser().getUsername());
        batchRepo.update(a, shortName);
    }*/

    /**
     * Removes the current user's name from the batch's Users Registered list
     *
     * @param shortName A batch shortName
     */
/*    public void withdrawBatch(String shortName) {
        Batch a = batchRepo.findById(shortName);
        a.removeBatchRegistrations(session.getCurrentUser().getUsername());
        batchRepo.update(a, shortName);
    }*/
}
