package com.increff.ironic.pos.controller.ui;

import com.increff.ironic.pos.util.SecurityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/ui")
public class AppUiController extends AbstractUiController {

    @RequestMapping(value = "/home")
    public ModelAndView home() {
        String page = SecurityUtil.isAuthenticated() ? "home.html" : "redirect:/site/login";
        return mav(page);
    }

    @RequestMapping(value = "/brands")
    public ModelAndView brand() {
        return mav("brand.html");
    }

    @RequestMapping(value = "/products")
    public ModelAndView product() {
        return mav("product.html");
    }

    @RequestMapping(value = "/admin")
    public ModelAndView admin() {
        return mav("user.html");
    }

    @RequestMapping(value = "/inventory")
    public ModelAndView inventory() {
        return mav("inventory.html");
    }

    @RequestMapping(value = "/orders")
    public ModelAndView order() {
        return mav("order.html");
    }

    @RequestMapping(value = "/reports")
    public ModelAndView reports() {
        return mav("reports.html");
    }

    @RequestMapping(value = "/reports/sales")
    public ModelAndView salesReport() {
        return mav("sales.html");
    }

    @RequestMapping(value = "/reports/per-day-sale")
    public ModelAndView perDaySalesReport() {
        return mav("per-day-sale.html");
    }

    @RequestMapping(value = "/reports/brand")
    public ModelAndView brandReport() {
        return mav("brand-report.html");
    }

    @RequestMapping(value = "/reports/inventory")
    public ModelAndView inventoryReport() {
        return mav("inventory-report.html");
    }

}
