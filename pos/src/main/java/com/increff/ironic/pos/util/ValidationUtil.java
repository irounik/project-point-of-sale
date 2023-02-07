package com.increff.ironic.pos.util;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.form.BrandForm;
import com.increff.ironic.pos.model.form.LoginForm;
import com.increff.ironic.pos.model.form.SignUpForm;
import com.increff.ironic.pos.model.form.UserForm;

public class ValidationUtil {

    private static final String EMAIL_PATTERN = "[a-z\\d.]+@[a-z]+\\.[a-z]{2,3}";

    private static final String PASSWORD_PATTERN = "(?=.*?[A-Z])(?=.*?[a-z])(?=.*?\\d)(?=.*?[#?!@$%^&*-]).{8,}";

    public static boolean isBlank(String input) {
        return input == null || input.trim().isEmpty();
    }

    public static boolean isNegativeNumber(Double number) {
        return number == null || number <= 0;
    }

    public static boolean isPositiveNumber(Integer number) {
        return number != null && number > 0;
    }

    public static boolean isNegative(Integer number) {
        return number == null || number < 0;
    }

    public static boolean isValidEmail(String email) {
        return email.matches(EMAIL_PATTERN);
    }

    public static boolean isValidBarcode(String barcode) {
        return barcode.trim().matches("\\w+");
    }

    public static void validate(BrandForm form) throws ApiException {
        if (ValidationUtil.isBlank(form.getName())) {
            ApiException.throwCantBeBlank("brand name");
        }

        if (ValidationUtil.isBlank(form.getCategory())) {
            ApiException.throwCantBeBlank("category");
        }
    }

    public static void validateForm(LoginForm userForm) throws ApiException {
        if (!ValidationUtil.isValidEmail(userForm.getEmail())) {
            throw new ApiException("Invalid email!");
        }

        if (ValidationUtil.isBlank(userForm.getPassword())) {
            ApiException.throwCantBeBlank("Password");
        }
    }

    public static void validateForm(SignUpForm userForm) throws ApiException {
        if (!ValidationUtil.isValidEmail(userForm.getEmail())) {
            throw new ApiException("Invalid email!");
        }

        validatePassword(userForm.getPassword());

        if (!userForm.getPassword().equals(userForm.getConfirmPassword())) {
            throw new ApiException("Please make sure that the passwords are same!");
        }

    }


    private static void validatePassword(String password) throws ApiException {
        if (password.length() < 8) {
            throw new ApiException("Password must have at least 8 characters!");
        }

        if (!password.matches(PASSWORD_PATTERN)) {
            throw new ApiException("Password must have at least one capital, small & special character!");
        }
    }

    public static void validateForm(UserForm userForm) throws ApiException {
        if (!ValidationUtil.isValidEmail(userForm.getEmail())) {
            throw new ApiException("Invalid email!");
        }

        validatePassword(userForm.getPassword());
    }
}
