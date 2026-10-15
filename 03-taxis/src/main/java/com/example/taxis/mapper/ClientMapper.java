package com.example.taxis.mapper;

import com.example.taxis.entity.Client;
import com.example.taxis.dto.request.ClientRequest;
import com.example.taxis.dto.response.ClientResponse;
public final class ClientMapper {
    private ClientMapper() {}
    public static ClientResponse response(Client e) { return new ClientResponse(e.getId(), e.getVersion(), e.getName(), e.getEmail(), e.getPhone(), e.getAddress()); }
    public static void apply(ClientRequest r, Client e) {
        e.setName(r.name());
        e.setEmail(r.email());
        e.setPhone(r.phone());
        e.setAddress(r.address());
    }
}
