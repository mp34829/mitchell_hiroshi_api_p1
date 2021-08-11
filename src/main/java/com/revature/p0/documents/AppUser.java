package com.revature.p0.documents;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AppUser {

    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private String password;
    private List<String> batchRegistrations;
    private String userPrivileges;

    public AppUser() {
        super();
    }

    public AppUser(String firstName, String lastName, String email, String username, String password, String userPrivileges) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.username = username;
        this.password = password;
        this.userPrivileges = userPrivileges;
        this.batchRegistrations = Collections.EMPTY_LIST;
    }

    public AppUser(String firstName, String lastName, String email, String username, String password, String userPrivileges, List<String> batchRegistrations) {
        this(firstName, lastName, email, username, password, userPrivileges);
        this.batchRegistrations = batchRegistrations;
    }


    public AppUser(String id, String firstName, String lastName, String email, String username, String password, String userPrivileges) {
        this(firstName, lastName, email, username, password, userPrivileges);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getBatchRegistrations() {
        return batchRegistrations;
    }

    public void setBatchRegistrations(List<String> batchRegistrations) {
        this.batchRegistrations = batchRegistrations;
    }

    public void addBatchRegistrations(String toAdd) {
        if (!this.batchRegistrations.contains(toAdd))
            this.batchRegistrations.add(toAdd);
    }

    public void removeBatchRegistrations(String toRemove) {
        this.batchRegistrations.remove(toRemove);
    }

    public String getUserPrivileges() {
        return userPrivileges;
    }

    public void setUserPrivileges(String userPrivileges) {
        this.userPrivileges = userPrivileges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppUser appUser = (AppUser) o;
        return Objects.equals(id, appUser.id) && Objects.equals(firstName, appUser.firstName) && Objects.equals(lastName, appUser.lastName) && Objects.equals(email, appUser.email) && Objects.equals(username, appUser.username) && Objects.equals(password, appUser.password) && Objects.equals(batchRegistrations, appUser.batchRegistrations) && Objects.equals(userPrivileges, appUser.userPrivileges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, email, username, password, batchRegistrations, userPrivileges);
    }
}
