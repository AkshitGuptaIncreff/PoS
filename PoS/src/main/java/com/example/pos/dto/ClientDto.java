package com.example.pos.dto;

import com.example.pos.api.ClientApi;
import com.example.pos.flow.ClientFlow;
import com.example.pos.models.ClientData;
import com.example.pos.models.ClientForm;
import com.example.pos.models.PageData;
import com.example.pos.models.db.ClientPojo;
import com.example.pos.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ClientDto {

    @Autowired
    private ClientFlow clientFlow;

    @Autowired
    private ClientApi clientApi;

    public ClientData createClient(ClientForm clientForm){

        ClientForm form = Helper.clientFormValidate(clientForm);
        ClientPojo clientPojo = Helper.clientFormToPojo(form);

        ClientPojo newClient = clientFlow.createClient(clientPojo);
        return Helper.clientPojoToData(newClient);
    }

    public ClientData updateClient(String clientId, ClientForm clientForm){

        ClientForm form = Helper.clientFormValidate(clientForm);
        ClientPojo clientPojo = Helper.clientFormToPojo(form);

        ClientPojo updateClient = clientFlow.updateClient(clientId, clientPojo);
        return Helper.clientPojoToData(updateClient);
    }

    public PageData<ClientData> getAllClients(int page, int size){
        Page<ClientPojo> pageResult = clientApi.getAllClients(page, size);
        return Helper.clientDataPagination(pageResult, page, size);
    }

    public PageData<ClientData> filterClientsByName(String clientName, int page, int size){
        String newClientName = Helper.trimAndLowercase(clientName);

        Page<ClientPojo> pageResult = clientApi.filterClientsByName(newClientName, page, size);
        return Helper.clientDataPagination(pageResult, page, size);
    }
}
