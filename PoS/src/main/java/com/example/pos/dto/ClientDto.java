package com.example.pos.dto;

import com.example.pos.api.ClientApi;
import com.example.pos.models.db.ClientPojo;
import com.example.pos.util.Utils;
import com.example.pos.models.ClientData;
import com.example.pos.models.ClientForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientDto {

    @Autowired
    private ClientApi clientApi;

    public ClientData createClient(ClientForm clientForm){
        String clientName = Utils.trimAndLowercase(clientForm.getName());
        ClientPojo clientPojo = clientApi.createClient(clientName,clientForm.getEmail());
        return Utils.clientPojoToData(clientPojo);
    }

    public List<ClientData> getAllClients(){
        List<ClientPojo> clientPojos = clientApi.getAllClients();
        return Utils.clientPojoListToDataList(clientPojos);
    }

    public ClientData updateClient(ClientForm clientForm){
        String clientName = Utils.trimAndLowercase(clientForm.getName());
        ClientPojo clientPojo = clientApi.updateClient(clientName,clientForm.getEmail());
        return Utils.clientPojoToData(clientPojo);
    }

    public List<ClientData> filterClientsByName(String clientName){
        String newClientName = Utils.trimAndLowercase(clientName);
        List<ClientPojo> clientPojos = clientApi.filterClientsByName(newClientName);
        return Utils.clientPojoListToDataList(clientPojos);
    }
}