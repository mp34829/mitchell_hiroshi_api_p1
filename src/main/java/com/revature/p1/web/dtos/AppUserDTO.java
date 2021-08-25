package com.revature.p1.web.dtos;

import com.revature.p1.datasource.documents.AppUser;
import io.jsonwebtoken.Claims;


import java.util.List;
import java.util.Objects;

public class AppUserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private List<String> batchRegistrations;
    private String userPrivileges;


    public AppUserDTO(AppUser user) {
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.batchRegistrations = user.getBatchRegistrations();
        this.userPrivileges = user.getUserPrivileges();
    }

    public AppUserDTO(Claims jwtClaims){
        this.firstName = (String) jwtClaims.get("firstName");
        this.lastName = (String) jwtClaims.get("lastName");
        this.email = (String) jwtClaims.get("email");
        this.username = (String) jwtClaims.get("username");
        this.batchRegistrations = (List<String>) jwtClaims.get("batchRegistrations");
        this.userPrivileges = (String) jwtClaims.get("privilege");
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

    public List<String> getBatchRegistrations() {
        return batchRegistrations;
    }

    public void setBatchRegistrations(List<String> batchRegistrations) {
        this.batchRegistrations = batchRegistrations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppUserDTO that = (AppUserDTO) o;
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(email, that.email) && Objects.equals(username, that.username) && Objects.equals(batchRegistrations, that.batchRegistrations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, email, username, batchRegistrations);
    }

    @Override
    public String toString() {
        return "AppUserDTO{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", batchRegistrations=" + batchRegistrations +
                '}';
    }
}
