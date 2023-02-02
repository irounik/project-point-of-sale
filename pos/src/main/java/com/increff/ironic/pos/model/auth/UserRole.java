package com.increff.ironic.pos.model.auth;

public enum UserRole {

    OPERATOR, SUPERVISOR, NONE;

    public static UserRole getRole(String roleString) {
        if (roleString == null) {
            return NONE;
        }
        roleString = roleString.trim();
        if (roleString.equalsIgnoreCase(OPERATOR.toString())) {
            return OPERATOR;
        }
        if (roleString.equalsIgnoreCase(SUPERVISOR.toString())) {
            return OPERATOR;
        }
        return NONE;
    }

}
