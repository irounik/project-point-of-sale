package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.model.data.UserData;
import com.increff.ironic.pos.model.form.UserForm;
import com.increff.ironic.pos.pojo.User;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.service.UserService;
import com.increff.ironic.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class AdminApiDto {

    private final UserService service;

    @Autowired
    public AdminApiDto(UserService service) {
        this.service = service;
    }


    public void add(UserForm form) throws ApiException {
        User user = ConversionUtil.convertFormToPojo(form);
        service.add(user);
    }

    public void delete(int id) throws ApiException {
        service.delete(id);
    }

    public List<UserData> getAll() {
        return service
                .getAll()
                .stream()
                .map(ConversionUtil::convertPojoToData)
                .collect(Collectors.toList());
    }

}
