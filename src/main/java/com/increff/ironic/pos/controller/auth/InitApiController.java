package com.increff.ironic.pos.controller.auth;

import com.increff.ironic.pos.controller.webapp.AbstractUiController;
import com.increff.ironic.pos.model.data.InfoData;
import com.increff.ironic.pos.model.form.UserForm;
import com.increff.ironic.pos.pojo.User;
import com.increff.ironic.pos.service.ApiException;
import com.increff.ironic.pos.service.UserService;
import com.increff.ironic.pos.util.ConversionUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class InitApiController extends AbstractUiController {

    private final UserService service;
    private final InfoData info;

    @Autowired
    public InitApiController(UserService service, InfoData info) {
        this.service = service;
        this.info = info;
    }

    @ApiOperation(value = "Initializes application")
    @RequestMapping(path = "/site/init", method = RequestMethod.GET)
    public ModelAndView showPage() {
        info.setMessage("");
        return mav("init.html");
    }

    @ApiOperation(value = "Initializes application")
    @RequestMapping(path = "/site/init", method = RequestMethod.POST)
    public ModelAndView initSite(UserForm form) throws ApiException {
        List<User> list = service.getAll();

        if (list.isEmpty()) {
            form.setRole("admin");
            User p = ConversionUtil.convertFormToPojo(form);
            service.add(p);
            info.setMessage("Application initialized");
        } else {
            info.setMessage("Application already initialized. Please use existing credentials");
        }

        return mav("init.html");
    }

}
