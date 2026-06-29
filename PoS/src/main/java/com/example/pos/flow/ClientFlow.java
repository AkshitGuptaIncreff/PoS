package com.example.pos.flow;

import com.example.pos.api.ClientApi;
import com.example.pos.api.CounterApi;
import com.example.pos.models.db.ClientPojo;
import com.example.pos.models.db.CounterType;
import com.example.pos.util.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class ClientFlow {

    @Autowired
    ClientApi clientApi;

    @Autowired
    CounterApi counterApi;

    @Autowired
    AuthFlow authFlow;

    @Transactional
    public ClientPojo createClient(ClientPojo clientPojo) {
        authFlow.checkSupervisor();

        if (clientApi.existsByEmail(clientPojo.getEmail())) {
            throw new ApiException("Email already exists");
        }

        long sequence = counterApi.getNextSequence(CounterType.CLIENT);
        String publicId = String.format("CL%06d", sequence);
        clientPojo.setClientId(publicId);

        return clientApi.createClient(clientPojo);
    }

    @Transactional
    public ClientPojo updateClient(String clientId, ClientPojo updatePojo) {
        authFlow.checkSupervisor();

        ClientPojo existing = clientApi.getClientByClientId(clientId);

        boolean emailChanged = !Objects.equals(existing.getEmail(), updatePojo.getEmail());

        if (emailChanged && clientApi.existsByEmail(updatePojo.getEmail())) {
            throw new ApiException("Email already exists");
        }

        if (emailChanged) {
            existing.setEmail(updatePojo.getEmail());
        }

        existing.setName(updatePojo.getName());

        return clientApi.updateClient(existing);
    }
}
