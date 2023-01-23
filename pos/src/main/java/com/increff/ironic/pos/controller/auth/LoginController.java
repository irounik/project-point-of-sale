package com.increff.ironic.pos.controller.auth;

import com.increff.ironic.pos.controller.webapp.AbstractUiController;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.InfoData;
import com.increff.ironic.pos.model.form.LoginForm;
import com.increff.ironic.pos.pojo.User;
import com.increff.ironic.pos.service.UserService;
import com.increff.ironic.pos.util.ConversionUtil;
import com.increff.ironic.pos.util.SecurityUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Objects;

@Controller
public class LoginController extends AbstractUiController {

    private final UserService service;
    private final InfoData info;

    @Autowired
    public LoginController(UserService service, InfoData info) {
        this.service = service;
        this.info = info;
    }

    @RequestMapping(path = "/site/login", method = RequestMethod.GET)
    public ModelAndView showPage() {
        info.setMessage("");
        return mav("login.html");
    }

    @ApiOperation(value = "Logs in a user")
    @RequestMapping(path = "/session/login", method = RequestMethod.POST, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ModelAndView login(HttpServletRequest req, LoginForm loginForm) throws ApiException {
        User user = service.get(loginForm.getEmail());
        boolean authenticated = (user != null && Objects.equals(user.getPassword(), loginForm.getPassword()));

        if (!authenticated) {
            info.setMessage("Invalid username or password");
            return mav("login.html");
        }

        // Create authentication object
        Authentication authentication = ConversionUtil.convertToAuth(user);

        // Create new session
        HttpSession session = req.getSession(true);

        // Attach Spring SecurityContext to this new session
        SecurityUtil.createContext(session);

        // Attach Authentication object to the Security Context
        SecurityUtil.setAuthentication(authentication);

        return mav("redirect:/ui/brands");
    }

    @RequestMapping(path = "/session/logout", method = RequestMethod.GET)
    public ModelAndView logout(HttpServletRequest request, HttpServletResponse response) {
        request.getSession().invalidate();
        return new ModelAndView("redirect:/site/logout");
    }

}
