package com.revature.p0.services;

import com.revature.p0.documents.AppUser;
import com.revature.p0.documents.Batch;
import com.revature.p0.repos.BatchRepository;
import com.revature.p0.repos.UserRepository;
import com.revature.p0.util.UserSession;
import com.revature.p0.util.exceptions.AuthenticationException;
import com.revature.p0.util.exceptions.InvalidRequestException;
import com.revature.p0.util.exceptions.ResourcePersistenceException;

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
        if (batch.getId() == null || batch.getId().trim().equals("")) return false;
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

        if (batchRepo.findBatchByID(newBatch.getId()) != null) {
            throw new ResourcePersistenceException("Provided username is already taken!");
        }

        return batchRepo.save(newBatch);
    }
    public void editBatch(String batchID){return;}
    public void removeBatch(String batchID){batchRepo.deleteById(batchID);}
    public void enrollBatch(String batchID){batchRepo.enroll(batchID);}
    public void withdrawBatch(String batchID){batchRepo.withdraw(batchID);}

}
