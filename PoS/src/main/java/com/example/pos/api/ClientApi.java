package com.example.pos.api;

import com.example.pos.dao.ClientDao;
import com.example.pos.flow.AuthFlow;
import com.example.pos.models.db.ClientPojo;
import com.example.pos.util.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class ClientApi {

    @Autowired
    ClientDao clientDao;

    @Autowired
    AuthFlow authFlow;

    public ClientPojo createClient(String clientName, String email){
        authFlow.checkSupervisor();

        boolean clientPojoExists = clientDao.existsByName(clientName);

        if(clientPojoExists) {
            throw new ApiException("Client already exists");
        }

        ClientPojo clientPojo = new ClientPojo();
        clientPojo.setName(clientName);
        clientPojo.setEmail(email);

        return clientDao.save(clientPojo);
    }

    public List<ClientPojo> getAllClients(){
        return clientDao.findAll();
    }

    public ClientPojo updateClient(String clientName, String email){
        authFlow.checkSupervisor();

        ClientPojo clientPojo = clientDao.findByName(clientName);
        if(clientPojo == null){
            throw new ApiException("Client not found");
        }
        clientPojo.setEmail(email);

        return clientDao.save(clientPojo);
    }

    public List<ClientPojo> filterClientsByName(String clientName) {
        List<ClientPojo> clientPojoList = clientDao.findByNameContainingIgnoreCase(clientName);
        if(clientPojoList.isEmpty()){
            throw new ApiException("Clients not found");
        }
        return clientPojoList;
    }

    public ClientPojo getClientById(String clientId){
        ClientPojo clientPojoById =clientDao.findById(clientId)
                .orElseThrow(() -> new ApiException("Client not found"));
        return clientPojoById;
    }

    public List<ClientPojo> findByNames(Collection<String> names){
        List<ClientPojo> clientPojoList = clientDao.findByNameIn(names);
        return clientPojoList;
    }

    public ClientPojo getClientByName(String clientName) {
        return clientDao.findByName(clientName);
    }
}
