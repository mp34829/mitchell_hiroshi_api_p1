package com.revature.p0.services;

import com.revature.p0.documents.AppUser;
import com.revature.p0.documents.Batch;
import com.revature.p0.repos.UserRepository;
import com.revature.p0.util.UserSession;
import com.revature.p0.util.exceptions.AuthenticationException;
import com.revature.p0.util.exceptions.InvalidRequestException;
import com.revature.p0.util.exceptions.ResourcePersistenceException;

import java.time.Instant;
import java.util.List;

public class UserService {

    private final UserRepository userRepo;
    private final UserSession session;

    public UserService(UserRepository userRepo, UserSession session) {
        this.userRepo = userRepo;
        this.session = session;
    }

    public UserSession getSession() {
        return session;
    }

    /**
     * Registers a user, after checking validity and redundancy
     *
     * @param newUser
     * @return User object if user is valid or throws an exception
     */
    public AppUser register(AppUser newUser) {

        if (!isUserValid(newUser)) {
            throw new InvalidRequestException("Invalid user data provided!");
        }

        if (userRepo.findUserByUsername(newUser.getUsername()) != null) {
            throw new ResourcePersistenceException("Provided username is already taken!");
        }

        if (userRepo.findUserByEmail(newUser.getEmail()) != null) {
            throw new ResourcePersistenceException("Provided username is already taken!");
        }

        return userRepo.save(newUser);

    }

    /**
     * Authorizes a user by checking their credentials
     *
     * @param username  A user's username
     * @param password  A user's password
     * @return User object if user exists, or throws an exception
     */
    public AppUser login(String username, String password) {

        if (username == null || username.trim().equals("") || password == null || password.trim().equals("")) {
            throw new InvalidRequestException("Invalid user credentials provided!");
        }

        AppUser authUser = userRepo.findUserByCredentials(username, password);

        if (authUser == null) {
            throw new AuthenticationException("Invalid credentials provided!");
        }

        session.setCurrentUser(authUser);

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
     * Removes a batch from the collection by removing all registrations from users
     *
     * @param batchID A batch shortname
     */
    public void removeBatch(String batchID){
        List<AppUser> usersByBatch = userRepo.findUsersByBatch(batchID);
        for (AppUser user : usersByBatch) {
            user.removeBatchRegistrations(batchID);
            userRepo.update(user, user.getUsername());
        }
    }

    /**
     * Adds a batchID to a user's Batch Registrations, if it does not already exist
     *
     * @param batchID A batch shortname
     */
    public void enrollBatch(String batchID){
        AppUser a = userRepo.findUserByUsername(session.getCurrentUser().getUsername());
        session.getCurrentUser().addBatchRegistrations(batchID);
        a.addBatchRegistrations(batchID);
        userRepo.update(a, a.getUsername());
    }

    /**
     * Removes a batchID to a user's Batch Registrations
     *
     * @param batchID A batch shortname
     */
    public void withdrawBatch(String batchID){
        AppUser a = userRepo.findUserByUsername(session.getCurrentUser().getUsername());
        session.getCurrentUser().removeBatchRegistrations(batchID);
        a.removeBatchRegistrations(batchID);
        userRepo.update(a, a.getUsername());}
}
