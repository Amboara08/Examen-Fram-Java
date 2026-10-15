package com.example.parking.mapper;

import com.example.parking.entity.Customer;
import com.example.parking.dto.request.CustomerRequest;
import com.example.parking.dto.response.CustomerResponse;
public final class CustomerMapper {
    private CustomerMapper() {}
    public static CustomerResponse response(Customer e) { return new CustomerResponse(e.getId(), e.getVersion(), e.getName(), e.getEmail(), e.getPhone(), e.getType()); }
    public static void apply(CustomerRequest r, Customer e) {
        e.setName(r.name());
        e.setEmail(r.email());
        e.setPhone(r.phone());
        e.setType(r.type());
    }
}
