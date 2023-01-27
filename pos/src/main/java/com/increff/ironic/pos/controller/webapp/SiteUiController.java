package com.increff.ironic.pos.controller.webapp;

import com.increff.ironic.pos.model.data.InfoData;
import com.increff.ironic.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class SiteUiController extends AbstractUiController {

    private final InfoData infoData;

    @Autowired
    public SiteUiController(InfoData infoData) {
        this.infoData = infoData;
    }

    private boolean isAuthenticated() {
        return !ValidationUtil.isBlank(infoData.getEmail());
    }

    // WEBSITE PAGES
    @RequestMapping(value = "")
    public ModelAndView index() {
        String page = isAuthenticated() ? "redirect:/ui/home" : "login.html";
        return mav(page);
    }

    @RequestMapping(path = "/site/signup", method = RequestMethod.GET)
    public ModelAndView signup() {
        String page = isAuthenticated() ? "redirect:/ui/home" : "signup.html";
        infoData.setMessage(""); // Setting error message
        return mav(page);
    }

    @RequestMapping(path = "/site/login", method = RequestMethod.GET)
    public ModelAndView login() {
        String page = isAuthenticated() ? "redirect:/ui/home" : "login.html";
        infoData.setMessage(""); // Setting error message
        return mav(page);
    }

    @RequestMapping(value = "/site/logout")
    public ModelAndView logout() {
        return mav("logout.html");
    }

}
