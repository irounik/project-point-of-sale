package com.increff.ironic.pos.model.data;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Getter
@Setter
public class InfoData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String message;
    private String email;

    public InfoData() {
        message = "No message";
        email = "No email";
    }

}
