package com.revature.p0.repos;



import com.revature.p0.documents.Batch;
import org.bson.Document;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;

import com.revature.p0.util.exceptions.DataSourceException;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class BatchRepository implements CrudRepository<Batch> {

    private final Logger logger = LoggerFactory.getLogger(BatchRepository.class);
    private final MongoCollection<Batch> batchCollection;

    public BatchRepository(MongoClient mongoClient) {
        this.batchCollection = mongoClient.getDatabase("p0").getCollection("batches", Batch.class);
    }

    /**
     * Fetches a batch with its shortName
     *
     * @param shortName A batch shortName
     * @return Batch object if found, or throws an exception
     */
    @Override
    public Batch findById(String shortName) {
        try {
            return batchCollection.find(new Document("shortName", shortName)).first();
        } catch (Exception e) {
            logger.error("An unexpected exception occurred.", e);
            throw new DataSourceException("An unexpected exception occurred.", e);
        }
    }

    /**
     * Adds a new batch object to the collection
     *
     * @param newBatch A batch object
     * @return Batch object if batch is valid, or throws an exception
     */
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

    /**
     * Edits a batch by uploading a new object in its place
     *
     * @param updatedResource A batch object
     * @param shortName A batch shortName
     *
     * @return true if MongoDB connection intact, false otherwise
     */
    @Override
    public boolean update(Batch updatedResource, String shortName) {
        try {
            return batchCollection.replaceOne(eq("shortName", shortName), updatedResource).wasAcknowledged();
        } catch (Exception e) {
            logger.error("An unexpected exception occurred.", e);
            throw new DataSourceException("An unexpected exception occurred.", e);
        }
    }

    /**
     * Removes a batch of the given shortName
     *
     * @param shortName A batch shortName
     * @return true if MongoDB connection intact, false otherwise
     */
    @Override
    public boolean deleteById(String shortName) {
        try {
            Document queryDoc = new Document("shortName", shortName);
            return batchCollection.deleteOne(queryDoc).wasAcknowledged();
        } catch (Exception e) {
            logger.error("An unexpected exception occurred.", e);
            throw new DataSourceException("An unexpected exception occurred.", e);
        }
    }

    /**
     * Fetches all batches and places them into an ArrayList
     *
     * @return List of all batch objects
     */
    public List<Batch> listAllBatches() {
        try {
            return batchCollection.find().into(new ArrayList<>());
        } catch (Exception e) {
            logger.error("An unexpected exception occurred.", e);
            throw new DataSourceException("An unexpected exception occurred.", e);
        }

    }
}
