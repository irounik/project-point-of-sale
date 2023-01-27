package com.increff.ironic.pos.controller.auth;

import com.increff.ironic.pos.controller.webapp.AbstractUiController;
import com.increff.ironic.pos.dto.UserApiDto;
import com.increff.ironic.pos.exceptions.ApiException;
import com.increff.ironic.pos.model.data.InfoData;
import com.increff.ironic.pos.model.form.UserForm;
import com.increff.ironic.pos.pojo.User;
import com.increff.ironic.pos.util.ConversionUtil;
import com.increff.ironic.pos.util.SecurityUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
public class SignUpApiController extends AbstractUiController {

    private final UserApiDto userApiDto;
    private final InfoData infoData;

    @Autowired
    public SignUpApiController(UserApiDto userApiDto, InfoData infoData) {
        this.infoData = infoData;
        this.userApiDto = userApiDto;
    }

    @ApiOperation(value = "Initializes application")
    @RequestMapping(path = "/site/signup", method = RequestMethod.POST)
    public ModelAndView signUp(UserForm form, HttpServletRequest request) {
        try {
            User user = userApiDto.add(form);
            // Create authentication object
            Authentication authentication = ConversionUtil.convertToAuth(user);

            // Create new session
            HttpSession session = request.getSession(true);

            // Attach Spring SecurityContext to this new session
            SecurityUtil.createContext(session);

            // Attach Authentication object to the Security Context
            SecurityUtil.setAuthentication(authentication);

            return mav("redirect:/ui/home");
        } catch (ApiException apiException) {
            infoData.setMessage(apiException.getMessage());
        }
        return mav("signup.html");
    }

}
