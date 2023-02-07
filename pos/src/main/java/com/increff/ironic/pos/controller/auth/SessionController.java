package com.increff.ironic.pos.controller.auth;

import com.increff.ironic.pos.controller.ui.AbstractUiController;
import com.increff.ironic.pos.dto.UserApiDto;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.form.LoginForm;
import com.increff.ironic.pos.model.form.SignUpForm;
import com.increff.ironic.pos.model.form.UserForm;
import com.increff.ironic.pos.pojo.UserPojo;
import com.increff.ironic.pos.util.SecurityUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping(path = "/session")
public class SessionController extends AbstractUiController {

    private final UserApiDto userApiDto;

    @Autowired
    public SessionController(UserApiDto userApiDto) {
        this.userApiDto = userApiDto;
    }

    @ApiOperation(value = "User signup")
    @RequestMapping(path = "/signup", method = RequestMethod.POST)
    public void signUp(@RequestBody SignUpForm form, HttpServletRequest request) throws ApiException {
        UserPojo userPojo = userApiDto.add(form);
        SecurityUtil.createAuthSession(userPojo, request);
    }

    @ApiOperation(value = "User login")
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public void login(@RequestBody LoginForm loginForm, HttpServletRequest req) throws ApiException {
        UserPojo userPojo = userApiDto.getAuthenticatedUser(loginForm);
        SecurityUtil.createAuthSession(userPojo, req);
    }

    @ApiOperation(value = "User logout")
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.getSession().invalidate();
        response.sendRedirect(request.getContextPath() + "/site/login");
    }

}
