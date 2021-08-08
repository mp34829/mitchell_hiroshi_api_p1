package com.revature.p0.util;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.revature.p0.util.exceptions.ResourcePersistenceException;

import java.io.FileReader;
import java.util.Arrays;
import java.util.Properties;

public class Database {
    private MongoClient mongoClient;

    public Database()
    {
        Properties appProperties = new Properties();

        try {
            appProperties.load(new FileReader("src/main/resources/application.properties"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResourcePersistenceException("Unable to load properties file.");
        }

        String ipAddress = appProperties.getProperty("ipAddress");
        int port = Integer.parseInt(appProperties.getProperty("port"));
        String dbName = appProperties.getProperty("dbName");
        String dbUsername = appProperties.getProperty("username");
        String dbPassword = appProperties.getProperty("password");

        // TODO obfuscate DB credentials
        // TODO abstract connection logic from here
        try (MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(ipAddress, port))))
                        .credential(MongoCredential.createScramSha1Credential(dbUsername, dbName, dbPassword.toCharArray()))
                        .build()
        )) {
            this.mongoClient = mongoClient;
        }
    }
}
