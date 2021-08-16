package com.revature.p1.datasource.repos;

public interface CrudRepository<T> {

    T findById(String id);
    T save(T newResource);
    boolean update(T updatedResource, String id);
    boolean deleteById(String id);

}
