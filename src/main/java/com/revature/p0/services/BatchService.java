package com.revature.p0.services;

import com.revature.p0.documents.AppUser;
import com.revature.p0.documents.Batch;
import com.revature.p0.repos.BatchRepository;
import com.revature.p0.util.UserSession;
import com.revature.p0.util.exceptions.DataSourceException;
import com.revature.p0.util.exceptions.InvalidRequestException;
import com.revature.p0.util.exceptions.ResourcePersistenceException;
import org.bson.Document;

import java.time.Instant;
import java.util.List;

public class BatchService {
    private final UserSession session;
    private final BatchRepository batchRepo;

    public BatchService(BatchRepository batchRepo, UserSession session) {
        this.session = session;
        this.batchRepo = batchRepo;
    }

    public boolean isBatchValid(Batch batch) {
        if (batch == null) return false;
        if (batch.getShortName() == null || batch.getShortName().trim().equals("")) return false;
        if (batch.getName() == null || batch.getName().trim().equals("")) return false;
        if (batch.getDescription() == null || batch.getStatus().trim().equals("")) return false;
        if (batch.getStatus() == null || batch.getStatus().trim().equals("")) return false;
        if (batch.getRegistrationEnd() == null || batch.getRegistrationEnd().equals("")) return false;
        return batch.getRegistrationStart() != null && !batch.getRegistrationStart().equals("");
    }

    public void listAllBatches()
    {
       List<Batch> allBatches = batchRepo.listAllBatches();
       for (Batch batch : allBatches)
           System.out.println(batch);
    }
    public Batch addBatch(Batch newBatch){
        if (!isBatchValid(newBatch)) {
            throw new InvalidRequestException("Invalid user data provided!");
        }

        if (batchRepo.findById(newBatch.getShortName()) != null) {
            throw new ResourcePersistenceException("Provided username is already taken!");
        }

        return batchRepo.save(newBatch);
    }
    public void listUsableBatches(){
        List<Batch> allBatches = batchRepo.listAllBatches();
        for (Batch batch : allBatches) {
            int val1 = batch.getRegistrationEnd().compareTo(Instant.now());
            int val2 = batch.getRegistrationStart().compareTo(Instant.now());

            if (batch.getStatus().equals("Enabled") && val1>0 && val2<0 )
                System.out.println(batch);
        }
    }

    public Batch getBatchByID(String batchID){
        return batchRepo.findById(batchID);
    }

    public void editBatch(Batch newBatch, String batchID){batchRepo.update(newBatch, batchID); }
    public void removeBatch(String batchID){batchRepo.deleteById(batchID);}
    public void enrollBatch(String batchID){
        Batch a = batchRepo.findById(batchID);
        a.addUsersRegistered(session.getCurrentUser().getUsername());
        batchRepo.update(a, batchID);
    }
    public void withdrawBatch(String batchID) {
        Batch a = batchRepo.findById(batchID);
        a.removeBatchRegistrations(session.getCurrentUser().getUsername());
        batchRepo.update(a, batchID);
    }
}
