package com.increff.ironic.pos.controller.auth;

import java.util.List;

import com.increff.ironic.pos.controller.webapp.AbstractUiController;
import com.increff.ironic.pos.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.increff.ironic.pos.model.InfoData;
import com.increff.ironic.pos.model.UserForm;
import com.increff.ironic.pos.service.ApiException;
import com.increff.ironic.pos.service.UserService;

import io.swagger.annotations.ApiOperation;

@Controller
public class InitApiController extends AbstractUiController {

	@Autowired
	private UserService service;
	@Autowired
	private InfoData info;

	@ApiOperation(value = "Initializes application")
	@RequestMapping(path = "/site/init", method = RequestMethod.GET)
	public ModelAndView showPage(UserForm form) throws ApiException {
		info.setMessage("");
		return mav("init.html");
	}

	@ApiOperation(value = "Initializes application")
	@RequestMapping(path = "/site/init", method = RequestMethod.POST)
	public ModelAndView initSite(UserForm form) throws ApiException {

		List<User> list = service.getAll();
		if (list.size() > 0) {
			info.setMessage("Application already initialized. Please use existing credentials");
		} else {
			form.setRole("admin");
			User p = convert(form);
			service.add(p);
			info.setMessage("Application initialized");
		}
		return mav("init.html");

	}

	private static User convert(UserForm f) {
		User p = new User();
		p.setEmail(f.getEmail());
		p.setRole(f.getRole());
		p.setPassword(f.getPassword());
		return p;
	}

}
