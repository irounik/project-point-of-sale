package com.increff.ironic.pos.util;

import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.form.BrandForm;

public class ValidationUtil {

    private static final String EMAIL_PATTERN = "[a-z\\d]+@[a-z]+\\.[a-z]{2,3}";

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

}
