package com.mcmcg.ico.bluefin.controller;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.mcmcg.ico.bluefin.configuration.properties.ApplicationProperties;

@Controller
public class HomeController {
    @Autowired
    private ApplicationProperties applicationProperties;

    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String home(Model model) {
        model.addAttribute("date", Calendar.getInstance().getTime());
        model.addAttribute("applicationProperties", applicationProperties);

        return "home";
    }
}