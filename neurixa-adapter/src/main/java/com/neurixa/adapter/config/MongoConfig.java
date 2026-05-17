package com.neurixa.adapter.config;

import com.mongodb.client.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
public class MongoConfig {

    /**
     * Enables Spring @Transactional support for MongoDB.
     *
     * IMPORTANT — MongoDB multi-document transactions require a replica set (or sharded cluster).
     * A standalone mongod instance does NOT support transactions.
     *
     * Dev setup with replica set (Docker):
     *   docker run -d -p 27017:27017 --name neurixa-mongo \
     *     mongo:latest --replSet rs0 --bind_ip_all
     *   docker exec neurixa-mongo mongosh --eval "rs.initiate()"
     *
     * If running standalone mongod (no replica set), @Transactional annotations are silently
     * ignored — writes still happen but without atomicity guarantees.
     * See: done/ARCHITECTURE.md § Transactional Boundaries
     */
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }
}
