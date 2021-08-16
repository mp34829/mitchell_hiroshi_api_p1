package com.revature.p1.datasource.util;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import com.revature.p1.util.exceptions.DataSourceException;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;


public class MongoClientFactory {

    private final MongoClient mongoClient;
    private static final MongoClientFactory mongoClientFactory = new MongoClientFactory();


    private MongoClientFactory() {



        try {
            String ipAddress = System.getProperty("ipAddress");
            int port = Integer.parseInt(System.getProperty("port"));
            String dbName = System.getProperty("dbName");
            String username = System.getProperty("username");
            char[] password = System.getProperty("password").toCharArray();

            List<ServerAddress> hosts = Collections.singletonList(new ServerAddress(ipAddress, port));
            MongoCredential credentials = MongoCredential.createScramSha1Credential(username, dbName, password);
            CodecRegistry defaultCodecRegistry = getDefaultCodecRegistry();
            PojoCodecProvider pojoCodecProvider= PojoCodecProvider.builder().automatic(true).build();
            CodecRegistry pojoCodecRegistry = fromRegistries(defaultCodecRegistry, fromProviders(pojoCodecProvider));

            MongoClientSettings settings = MongoClientSettings.builder()
                                                              .applyToClusterSettings(builder -> builder.hosts(hosts))
                                                              .credential(credentials)
                                                              .codecRegistry(pojoCodecRegistry)
                                                              .build();

            this.mongoClient = MongoClients.create(settings);

        } catch(Exception e){
            e.printStackTrace(); // TODO log this to a file
            throw new DataSourceException("An unexpected exception occurred.", e);
        }

    }

    public void cleanUp(){
        mongoClient.close();
    }

    public static MongoClientFactory getInstance(){
        return mongoClientFactory;
    }

    public MongoClient getConnection(){
        return mongoClient;
    }

}
