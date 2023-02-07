package com.increff.ironic.pos.dto;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.UserData;
import com.increff.ironic.pos.model.form.LoginForm;
import com.increff.ironic.pos.model.form.SignUpForm;
import com.increff.ironic.pos.model.form.UserForm;
import com.increff.ironic.pos.pojo.UserPojo;
import com.increff.ironic.pos.service.UserService;
import com.increff.ironic.pos.util.ConversionUtil;
import com.increff.ironic.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.increff.ironic.pos.util.ValidationUtil.validateForm;

@Component
public class UserApiDto {

    private final UserService userService;

    @Autowired
    public UserApiDto(UserService userService) {
        this.userService = userService;
    }

    public UserPojo add(UserForm userForm) throws ApiException {
        ValidationUtil.validateForm(userForm);
        UserPojo user = ConversionUtil.convertFormToPojo(userForm);
        return userService.add(user);
    }

    public UserPojo add(SignUpForm signUpForm) throws ApiException {
        validateForm(signUpForm);
        UserPojo userPojo = ConversionUtil.convertFormToPojo(signUpForm);
        return userService.add(userPojo);
    }

    public void delete(int id) throws ApiException {
        userService.delete(id);
    }

    public List<UserData> getAll() {
        return userService
                .getAll()
                .stream()
                .map(ConversionUtil::convertPojoToData)
                .collect(Collectors.toList());
    }

    public UserPojo getAuthenticatedUser(LoginForm loginForm) throws ApiException {
        validateForm(loginForm);
        UserPojo userPojo = userService.getByEmail(loginForm.getEmail());
        boolean authenticated = userPojo.getPassword().equals(loginForm.getPassword());

        if (!authenticated) {
            throw new ApiException("Invalid email or password");
        }

        return userPojo;
    }
}
