package com.revature.p0.documents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UserRegistrationList {

    private String id;
    private String username;
    private List<Batch> wishlist = new ArrayList<>();

    public UserRegistrationList(String username) {
        this.username = username;
    }

    public UserRegistrationList(String id, String username) {
        this(username);
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Batch> getWishlist() {
        return wishlist;
    }

    public void setWishlist(List<Batch> wishlist) {
        this.wishlist = wishlist;
    }

    public UserRegistrationList addToWishlist(Batch... books) {
        wishlist.addAll(Arrays.asList(books));
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserRegistrationList that = (UserRegistrationList) o;
        return Objects.equals(id, that.id) && Objects.equals(username, that.username) && Objects.equals(wishlist, that.wishlist);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, wishlist);
    }

    @Override
    public String toString() {
        return "UserWishlist{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", wishlist=" + wishlist +
                '}';
    }

}
