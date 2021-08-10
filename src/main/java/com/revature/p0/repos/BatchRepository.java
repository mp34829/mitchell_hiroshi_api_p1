package com.revature.p0.repos;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.revature.p0.documents.Batch;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.revature.p0.documents.AppUser;
import com.revature.p0.util.MongoClientFactory;
import com.revature.p0.util.exceptions.DataSourceException;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BatchRepository implements CrudRepository<Batch> {

    private final Logger logger = LoggerFactory.getLogger(BatchRepository.class);
    private final MongoCollection<Batch> batchCollection;

    public BatchRepository(MongoClient mongoClient) {
        this.batchCollection = mongoClient.getDatabase("p0").getCollection("batches", Batch.class);
    }
    @Override
    public Batch findById(String id) {
        try {
            return batchCollection.find(new Document("id", id)).first();
        } catch (Exception e) {
            logger.error("An unexpected exception occurred.", e);
            throw new DataSourceException("An unexpected exception occurred.", e);
        }
    }

    @Override
    public Batch save(Batch newBatch) {
        try {
            newBatch.setId(new ObjectId().toString());
            batchCollection.insertOne(newBatch);

            return newBatch;

        }catch (Exception e) {
            e.printStackTrace(); // TODO log this to a file
            throw new DataSourceException("An unexpected exception occurred.", e);
        }

    }

    @Override
    public boolean update(Batch updatedResource) {
        return false;
    }

    @Override
    public boolean deleteById(String id) {
        return false;
    }


    public List<Batch> listAllBatches() {
        try {
            return batchCollection.find().into(new ArrayList<>());
        } catch (Exception e) {
            logger.error("An unexpected exception occurred.", e);
            throw new DataSourceException("An unexpected exception occurred.", e);
        }

    }
    public Batch findBatchByID(String id){return null;}
    public void enroll(String batchID){return;}
    public void withdraw(String batchID){return;}
}
