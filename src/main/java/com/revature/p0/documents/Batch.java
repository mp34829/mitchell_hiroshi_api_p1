package com.revature.p0.documents;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class Batch {

    private String id;
    private String name;
    private String status;
    private String description;
    private Instant registrationStart;
    private Instant registrationEnd;
    private List<String> usersRegistered;


    public void setUsersRegistered(List<String> usersRegistered) {
        this.usersRegistered = usersRegistered;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getUsersRegistered() {
        return usersRegistered;
    }

    public void addUsersRegistered(String toAdd) {
        this.usersRegistered.add(toAdd);
    }

    public void removeUsersRegistered(String toRemove) {
        this.usersRegistered.remove(toRemove);
    }

    public Batch(String id, String name, String status, Instant registrationStart, Instant registrationEnd) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getRegistrationStart() {
        return registrationStart;
    }

    public void setRegistrationStart(Instant registrationStart) {
        this.registrationStart = registrationStart;
    }

    public Instant getRegistrationEnd() {
        return registrationEnd;
    }

    public void setRegistrationEnd(Instant registrationEnd) {
        this.registrationEnd = registrationEnd;
    }
}
