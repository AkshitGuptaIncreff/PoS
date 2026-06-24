package com.example.pos.controller;

import com.example.pos.dto.ClientDto;
import com.example.pos.models.ClientData;
import com.example.pos.models.ClientForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientDto clientDto;

    @RequestMapping(method=RequestMethod.POST)
    public ClientData createClient(@Valid @RequestBody ClientForm clientForm){
        return clientDto.createClient(clientForm);
    }

    @RequestMapping(method=RequestMethod.GET)
    public List<ClientData> getAllClients() {
        return clientDto.getAllClients();
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/update")
    public ClientData updateClient(@Valid @RequestBody ClientForm clientForm){
        return clientDto.updateClient(clientForm);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/filter")
    public List<ClientData> filterClientByName(@Valid @RequestParam String name){
        return clientDto.filterClientsByName(name);
    }
}
