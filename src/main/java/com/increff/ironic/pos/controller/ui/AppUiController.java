package com.increff.ironic.pos.controller.ui;

import com.increff.ironic.pos.pojo.EmployeePojo;
import com.increff.ironic.pos.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class AppUiController extends AbstractUiController {

    @Autowired
    EmployeeService service;

    @RequestMapping(value = "/ui/home")
    public ModelAndView home() {
        return mav("home.html");
    }

    @RequestMapping(value = "/ui/employee")
    public ModelAndView employee() {
        ModelAndView mav = mav("employee.html");

        List<EmployeePojo> employees = service.getAll();
        mav.addObject("employees", employees);
        return mav;
    }

    @RequestMapping(value = "/ui/admin")
    public ModelAndView admin() {
        return mav("user.html");
    }

}
