package com.example.pos.api;

import com.example.pos.dao.ClientDao;
import com.example.pos.models.db.ClientPojo;
import com.example.pos.util.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Service
public class ClientApi {

    @Autowired
    ClientDao clientDao;

    @Transactional
    public ClientPojo createClient(ClientPojo clientPojo){
        return clientDao.save(clientPojo);
    }

    @Transactional
    public ClientPojo updateClient(ClientPojo clientPojo){
        return clientDao.save(clientPojo);
    }

    public Page<ClientPojo> getAllClients(int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        return clientDao.findAll(pageable);
    }

    public Page<ClientPojo> filterClientsByName(String clientName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return clientDao.findByNameContainingIgnoreCase(clientName, pageable);
    }

    public List<ClientPojo> findByNames(Collection<String> names){
        return clientDao.findByNameIn(names);
    }

    public ClientPojo getClientByName(String clientName) {
        ClientPojo client = clientDao.findByName(clientName);
        if (client == null) {
            throw new ApiException("Client not found");
        }
        return client;
    }

    public ClientPojo getClientByClientId(String clientId) {
        if (clientId == null) {
            throw new ApiException("Client not found");
        }
        return clientDao.findByClientId(clientId)
                .orElseThrow(() -> new ApiException("Client not found"));
    }

    public boolean existsByEmail(String email) {
        return clientDao.existsByEmail(email);
    }

    public boolean existsByName(String name) {
        return clientDao.existsByName(name);
    }
}
