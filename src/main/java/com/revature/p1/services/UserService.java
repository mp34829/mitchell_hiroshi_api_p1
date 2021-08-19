package com.revature.p1.services;

import com.revature.p1.datasource.documents.AppUser;
import com.revature.p1.datasource.repos.UserRepository;
import com.revature.p1.util.PasswordUtils;
import com.revature.p1.util.exceptions.AuthenticationException;
import com.revature.p1.util.exceptions.InvalidRequestException;
import com.revature.p1.util.exceptions.ResourcePersistenceException;
import com.revature.p1.web.dtos.AppUserDTO;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class UserService {

    private final UserRepository userRepo;
    private final PasswordUtils passwordUtils;

    public UserService(UserRepository userRepo, PasswordUtils passwordUtils) {
        this.userRepo = userRepo;
        this.passwordUtils = passwordUtils;

    }

    /**
     * Generates a list of all AppUsers in the users collection, then converts each user to its DTO equivalent
     *
     * @return A list of all AppUsers as AppUserDTOs
     */
    public List<AppUserDTO> findAll() {
        return userRepo.findAll()
                .stream()
                .map(AppUserDTO::new)
                .collect(Collectors.toList());
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
            System.out.println("Provided username is already taken!");
            throw new ResourcePersistenceException("Provided username is already taken!");
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
    public AppUser login(String username, String password) {

        if (username == null || username.trim().equals("") || password == null || password.trim().equals("")) {
            throw new InvalidRequestException("Invalid user credentials provided!");
        }

        String encryptedPassword = passwordUtils.generateSecurePassword(password);
        AppUser authUser = userRepo.findUserByCredentials(username, encryptedPassword);

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

    public void enrollBatch(String batchID, HttpSession session){
        AppUser currentUser = (AppUser) session.getAttribute("AppUser");
        AppUser a = userRepo.findUserByUsername(currentUser.getUsername());
        currentUser.addBatchRegistrations(batchID);
        a.addBatchRegistrations(batchID);
        userRepo.update(a, a.getUsername());
    }

    public AppUser findUserById(String userIdParam) {
        return userRepo.findById(userIdParam);
    }

    public void updateCurrentUser(HttpSession session){
        AppUser currentUser = (AppUser) session.getAttribute("AppUser");
        AppUser a = userRepo.findById(currentUser.getId());

    }

    public void updateUserByField(AppUser user, HashMap hash) {
        try{

        } catch (Exception e) {
            throw new InvalidRequestException("Request to update nonexistent field, denied.");
        }
    }


    /**
     * Removes a batchID to a user's Batch Registrations
     *
     * @param batchID A batch shortname
     */
    public void withdrawBatch(String batchID, HttpSession session){
        AppUser currentUser = (AppUser) session.getAttribute("AppUser");
        currentUser.removeBatchRegistrations(batchID);
        userRepo.update(currentUser, currentUser.getUsername());
    }
}
