package com.revature.p0.repos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import com.revature.p0.documents.AppUser;
import com.revature.p0.documents.Batch;
import com.revature.p0.util.MongoClientFactory;
import com.revature.p0.util.exceptions.DataSourceException;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

public class UserRepository implements CrudRepository<AppUser> {

    private final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    private final MongoCollection<AppUser> usersCollection;

    public UserRepository(MongoClient mongoClient) {
        this.usersCollection = mongoClient.getDatabase("p0").getCollection("users", AppUser.class);
    }

    /**
     * Find an existing user by searching for username/password
     *
     * @param username  A user's username
     * @param password  A user's password
     * @return User object if user exists, or throws an exception
     */
    public AppUser findUserByCredentials(String username, String password) {

        try {
            Document queryDoc = new Document("username", username).append("password", password);
            return usersCollection.find(queryDoc).first();

        } catch (Exception e) {
            logger.error("An unexpected exception occurred.", e);
            throw new DataSourceException("An unexpected exception occurred.", e);
        }
    }

    /**
     * Find an existing user by searching for username
     *
     * @param username  A user's username
     * @return User object if user exists, or throws an exception
     */
    public AppUser findUserByUsername(String username) {
        try {
            return usersCollection.find(new Document("username", username)).first();
        } catch (Exception e) {
            logger.error("An unexpected exception occurred.", e);
            throw new DataSourceException("An unexpected exception occurred.", e);
        }
    }

    /**
     * Find an existing user by searching for email
     *
     * @param email  A user's email
     * @return User object if user exists, or throws an exception
     */
    public AppUser findUserByEmail(String email) {
        try {
            return usersCollection.find(new Document("email", email)).first();
        } catch (Exception e) {
            logger.error("An unexpected exception occurred.", e);
            throw new DataSourceException("An unexpected exception occurred.", e);
        }
    }

    /**
     * Find an existing user by searching for their ID
     *
     * @param id  A user's id
     * @return User object if user exists, or throws an exception
     */
    @Override
    public AppUser findById(String id) {
        try {

            Document queryDoc = new Document("_id", id);
            return usersCollection.find(queryDoc).first();

        } catch (Exception e) {
            logger.error("An unexpected exception occurred.", e);
            throw new DataSourceException("An unexpected exception occurred.", e);
        }
    }

    /**
     * Adds a new user object to the collection
     *
     * @param newUser A user object
     * @return User object, or throws an exception
     */
    @Override
    public AppUser save(AppUser newUser) {
        try {
            newUser.setId(new ObjectId().toString());
            usersCollection.insertOne(newUser);

            return newUser;

        } catch (Exception e) {
            e.printStackTrace(); // TODO log this to a file
            throw new DataSourceException("An unexpected exception occurred.", e);
        }

    }

    /**
     * Replaces a user object in the collection
     *
     * @param updatedResource A user object
     * @param username A user's username
     * @return true if MongoDB connection intact, false otherwise
     */
    @Override
    public boolean update(AppUser updatedResource, String username) {
        try {
            return usersCollection.replaceOne(eq("username", username), updatedResource).wasAcknowledged();
        } catch (Exception e) {
            logger.error("An unexpected exception occurred.", e);
            throw new DataSourceException("An unexpected exception occurred.", e);
        }
    }

    /**
     * Deletes a user object in the collection
     *
     * @param id A user's id
     * @return true if MongoDB connection intact, false otherwise
     */
    @Override
    public boolean deleteById(String id) {
        try {
            Document queryDoc = new Document("_id", id);
            return usersCollection.deleteOne(queryDoc).wasAcknowledged();
        } catch (Exception e) {
            logger.error("An unexpected exception occurred.", e);
            throw new DataSourceException("An unexpected exception occurred.", e);
        }
    }

    /**
     * Lists all users registered in a batch
     *
     * @param batchId A batch's shortName
     * @return List<AppUser> A list of all users registered to the batch
     */
    public List<AppUser> findUsersByBatch(String batchId)
    {
        try {
            Document queryDoc = new Document("batchRegistrations", batchId);
            return usersCollection.find().into(new ArrayList<>());
        } catch (Exception e) {
            logger.error("An unexpected exception occurred.", e);
            throw new DataSourceException("An unexpected exception occurred.", e);
        }
    }

}
