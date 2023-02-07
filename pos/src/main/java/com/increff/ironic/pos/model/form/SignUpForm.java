package com.increff.ironic.pos.model.form;

import lombok.Data;

@Data
public class SignUpForm {

    private String email;

    private String password;

    private String confirmPassword;

}
