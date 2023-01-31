package com.increff.ironic.pos.exceptions;

public class ApiException extends Exception {

    public ApiException(String string) {
        super(string);
    }

    public static void throwCantBeBlank(String field) throws ApiException {
        throw new ApiException("Invalid input: " + field + " can't be blank!");
    }

}
