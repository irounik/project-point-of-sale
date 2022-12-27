package com.increff.ironic.pos.controller.webapp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class AppUiController extends AbstractUiController {

    @RequestMapping(value = "/ui/brands")
    public ModelAndView brand() {
        return mav("brand.html");
    }

    @RequestMapping(value = "/ui/admin")
    public ModelAndView admin() {
        return mav("user.html");
    }

}
