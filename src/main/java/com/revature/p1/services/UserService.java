package com.revature.p1.services;

import com.revature.p1.datasource.documents.AppUser;
import com.revature.p1.datasource.documents.Batch;
import com.revature.p1.datasource.repos.BatchRepository;
import com.revature.p1.datasource.repos.UserRepository;
import com.revature.p1.util.PasswordUtils;
import com.revature.p1.util.exceptions.*;

import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.*;
import java.util.stream.Collectors;

public class UserService {

    private final UserRepository userRepo;
    private final BatchRepository batchRepo;
    private final PasswordUtils passwordUtils;

    public UserService(UserRepository userRepo, BatchRepository batchRepo, PasswordUtils passwordUtils) {
        this.userRepo = userRepo;
        this.passwordUtils = passwordUtils;
        this.batchRepo = batchRepo;
    }


    /**
     * Registers a user, after checking validity and redundancy
     *
     * @param newUser
     * @return User object if user is valid or throws an exception
     */
    public AppUser register(AppUser newUser) {

        if (!isUserValid(newUser)) {
            System.out.println("Invalid user data provided!");
            throw new InvalidRequestException("Invalid user data provided!");
        }
        if (userRepo.findUserByUsername(newUser.getUsername()) != null) {
            System.out.println("Provided username is already taken!");
            throw new ResourcePersistenceException("Provided username is already taken!");
        }
        if (userRepo.findUserByEmail(newUser.getEmail()) != null) {
            System.out.println("Provided email is already taken!");
            throw new ResourcePersistenceException("Provided email is already taken!");
        }
        String encryptedPassword = passwordUtils.generateSecurePassword(newUser.getPassword());
        newUser.setPassword(encryptedPassword);

        return userRepo.save(newUser);

    }

    /**
     * Authorizes a user by checking their credentials
     *
     * @param username  A user's username
     * @param password  A user's password
     * @return User object if user exists, or throws an exception
     */
    public AppUser login(String username, String password) throws DataSourceException {

        if (username == null || username.trim().equals("") || password == null || password.trim().equals("")) {
            throw new InvalidRequestException("Invalid user credentials provided!");
        }

        //String encryptedPassword = passwordUtils.generateSecurePassword(password);
       // AppUser authUser = userRepo.findUserByCredentials(username, encryptedPassword);
        AppUser authUser = userRepo.findUserByCredentials(username, password);
        if (authUser == null) {
            throw new AuthenticationException("Invalid credentials provided!");
        }

        return authUser;
    }

    /**
     * Validates a user by checking for empty fields
     *
     * @param user A user object
     * @return true if user object is filled out, false if missing fields
     */
    public boolean isUserValid(AppUser user) {
        if (user == null) return false;
        if (user.getFirstName() == null || user.getFirstName().trim().equals("")) return false;
        if (user.getLastName() == null || user.getLastName().trim().equals("")) return false;
        if (user.getEmail() == null || user.getEmail().trim().equals("")) return false;
        if (user.getUsername() == null || user.getUsername().trim().equals("")) return false;
        return user.getPassword() != null && !user.getPassword().trim().equals("");
    }


    /**
     * Adds a batchID to a user's Batch Registrations, if it does not already exist
     *
     * @param queryUser The AppUser requesting batch enrollment
     * @param shortname A batch shortname
     */

    public void enrollBatch(AppUser queryUser, String shortname) {
        Batch batch = batchRepo.findById(shortname);
        if (batch == null)
            throw new ResourceNotFoundException("Requested batch not found in database");
        if (queryUser.getBatchRegistrations().contains(batch.getShortName()))
            throw new ResourceNotFoundException("You're already registered to this batch!");
        queryUser.getBatchRegistrations().add(shortname);
        batch.getUsersRegistered().add(queryUser.getUsername());
        userRepo.update(queryUser, queryUser.getUsername());
        batchRepo.update(batch, batch.getShortName());
    }


    public AppUser findUserById(String userIdParam) {return userRepo.findById(userIdParam);}

    public void updateUserByField(AppUser user, JSONObject json) {
        List<String> fieldList = Arrays.asList("firstName","lastName","email", "password");
        Set<String> keys = json.keySet();
        for(String key: keys)
            if(fieldList.indexOf(key)==-1)
                throw new InvalidRequestException("Request to update nonexistent field, denied.");
        if (json.containsKey("firstName"))
            user.setFirstName(json.get("firstName").toString());
        if (json.containsKey("lastName"))
            user.setLastName(json.get("lastName").toString());
        if (json.containsKey("email"))
            user.setEmail(json.get("email").toString());
        if (json.containsKey("password"))
            user.setPassword(json.get("password").toString());
        userRepo.update(user, user.getUsername());
    }

    /**
     * Removes a batchID from a user's Batch Registrations, and the user's username from the batch's Users Registered
     *
     * @param requestingUser The AppUser requesting batch removal
     * @param shortname A batch shortname
     */
    public void withdrawBatch(AppUser requestingUser, String shortname){
        Batch batch = batchRepo.findById(shortname);
        if(!requestingUser.getBatchRegistrations().contains(shortname))
            throw new ResourceNotFoundException("You are not registered to the batch! Batch withdrawal failed.");
        requestingUser.getBatchRegistrations().remove(shortname);

        if(batch.getUsersRegistered().contains(requestingUser.getUsername()))
            batch.getUsersRegistered().remove(requestingUser.getUsername());

        userRepo.update(requestingUser, requestingUser.getUsername());
        batchRepo.update(batch, batch.getShortName());
    }

}
