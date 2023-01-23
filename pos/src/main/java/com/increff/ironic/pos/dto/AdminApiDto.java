package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.UserData;
import com.increff.ironic.pos.model.form.UserForm;
import com.increff.ironic.pos.pojo.User;
import com.increff.ironic.pos.service.UserService;
import com.increff.ironic.pos.util.ConversionUtil;
import com.increff.ironic.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AdminApiDto {

    private final UserService service;

    @Autowired
    public AdminApiDto(UserService service) {
        this.service = service;
    }

    public User add(UserForm userForm) throws ApiException {
        validateForm(userForm);
        User user = ConversionUtil.convertFormToPojo(userForm);
        return service.add(user);
    }

    private void validateForm(UserForm userForm) throws ApiException {
        if (!ValidationUtil.isValidEmail(userForm.getEmail())) {
            throw new ApiException("Invalid email!");
        }

        if (ValidationUtil.isBlank(userForm.getPassword())) {
            throw new ApiException("Password must not be blank!");
        }
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

    public String getRoleByEmail(String email) {
        try {
            return service.get(email).getRole();
        } catch (Exception ex) {
            return "operator";
        }
    }

}
