package com.increff.ironic.pos.controller.ui;

import com.increff.ironic.pos.model.auth.UserRole;
import com.increff.ironic.pos.model.data.InfoData;
import com.increff.ironic.pos.util.SecurityUtil;
import com.increff.ironic.pos.model.auth.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

@Controller
public abstract class AbstractUiController {

    @Autowired
    private InfoData info;

    @Value("${app.baseUrl}")
    private String baseUrl;

    protected ModelAndView mav(String page) {
        // Get current user
        UserPrincipal principal = SecurityUtil.getPrincipal();

        String email = principal == null ? "" : principal.getEmail();
        info.setEmail(email);

        UserRole role = SecurityUtil.getCurrentUserRole();
        info.setRole(role);

        // Set info
        ModelAndView mav = new ModelAndView(page);
        mav.addObject("info", info);
        mav.addObject("baseUrl", baseUrl);
        return mav;
    }

}
