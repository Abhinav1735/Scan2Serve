package com.scan2serve.controller;

import com.scan2serve.dto.CustomerCategoryResponse;
import com.scan2serve.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/menu")
    public List<CustomerCategoryResponse> getCustomerMenu() {

        return customerService.getCustomerMenu();

    }

}