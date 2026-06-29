package com.example.pos.dao;

import com.example.pos.models.db.ClientPojo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientDao extends MongoRepository<ClientPojo, String> {

    Optional<ClientPojo> findById(String id);

    Optional<ClientPojo> findByClientId(String clientId);

    boolean existsByClientId(String clientId);

    boolean existsByEmail(String email);

    boolean existsByName(String name);

    Page<ClientPojo> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<ClientPojo> findByNameIn(Collection<String> names);

    ClientPojo findByName(String name);
}
