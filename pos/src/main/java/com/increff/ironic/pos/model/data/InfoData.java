package com.increff.ironic.pos.model.data;

import com.increff.ironic.pos.model.auth.UserRole;
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
    private UserRole role;

    public InfoData() {
        message = "";
        email = "";
        role = UserRole.NONE;
    }

}
