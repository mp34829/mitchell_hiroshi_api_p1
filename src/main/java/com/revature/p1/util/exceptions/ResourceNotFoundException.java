package com.revature.p1.util.exceptions;


public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException() {
            super("No resource found using provided search criteria.");
        }

}


