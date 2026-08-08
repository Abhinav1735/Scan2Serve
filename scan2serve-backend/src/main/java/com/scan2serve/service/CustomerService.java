package com.scan2serve.service;

import com.scan2serve.dto.CustomerCategoryResponse;
import com.scan2serve.dto.CustomerMenuItemResponse;
import com.scan2serve.entity.Menu;
import com.scan2serve.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerService {

    @Autowired
    private MenuRepository menuRepository;

    public List<CustomerCategoryResponse> getCustomerMenu() {

        List<Menu> menus = menuRepository.findByAvailableTrue();

        Map<String, List<CustomerMenuItemResponse>> groupedMenu = new LinkedHashMap<>();

        for (Menu menu : menus) {

            String categoryName = menu.getCategory().getName();

            groupedMenu.putIfAbsent(categoryName, new ArrayList<>());

            groupedMenu.get(categoryName).add(

                    new CustomerMenuItemResponse(

                            menu.getId(),
                            menu.getName(),
                            menu.getDescription(),
                            menu.getPrice()

                    )

            );

        }

        List<CustomerCategoryResponse> response = new ArrayList<>();

        for (Map.Entry<String, List<CustomerMenuItemResponse>> entry : groupedMenu.entrySet()) {

            response.add(

                    new CustomerCategoryResponse(

                            entry.getKey(),
                            entry.getValue()

                    )

            );

        }

        return response;

    }

}