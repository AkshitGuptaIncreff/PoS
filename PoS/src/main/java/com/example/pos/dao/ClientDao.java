package com.example.pos.dao;

import com.example.pos.models.db.ClientPojo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientDao extends MongoRepository<ClientPojo, String> {

    Optional<ClientPojo> findById(String id);

    boolean existsByName(String name);

    List<ClientPojo> findByNameContainingIgnoreCase(String name);

    List<ClientPojo> findByNameIn(Collection<String> names);

    ClientPojo findByName(String name);
}