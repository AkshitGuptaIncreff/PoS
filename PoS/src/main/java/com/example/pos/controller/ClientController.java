package com.example.pos.controller;

import com.example.pos.dto.ClientDto;
import com.example.pos.models.ClientData;
import com.example.pos.models.ClientForm;
import com.example.pos.models.PageData;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*")
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
    public PageData<ClientData> getAllClients(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return clientDto.getAllClients(page,size);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/{clientId}")
    public ClientData updateClient(@PathVariable String clientId,
                                   @Valid @RequestBody ClientForm clientForm){
        return clientDto.updateClient(clientId, clientForm);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/filter")
    public PageData<ClientData> filterClientByName(@Valid @RequestParam String name,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size){
        return clientDto.filterClientsByName(name,page,size);
    }
}
