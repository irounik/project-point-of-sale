package com.increff.ironic.pos.controller.auth;

import com.increff.ironic.pos.controller.webapp.AbstractUiController;
import com.increff.ironic.pos.dto.AdminApiDto;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.InfoData;
import com.increff.ironic.pos.model.form.UserForm;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SignUpApiController extends AbstractUiController {

    private final AdminApiDto dto;
    private final InfoData info;

    @Autowired
    public SignUpApiController(AdminApiDto dto, InfoData info) {
        this.info = info;
        this.dto = dto;
    }

    @ApiOperation(value = "Initializes application")
    @RequestMapping(path = "/site/signup", method = RequestMethod.GET)
    public ModelAndView showPage() {
        info.setMessage("");
        return mav("signup.html");
    }

    @ApiOperation(value = "Initializes application")
    @RequestMapping(path = "/site/signup", method = RequestMethod.POST)
    public ModelAndView initSite(UserForm form) {
        try {
            dto.add(form);
        } catch (ApiException ex) {
            info.setMessage("User with email: " + form.getEmail() + " already exists!");
        }
        return mav("signup.html");
    }

}
