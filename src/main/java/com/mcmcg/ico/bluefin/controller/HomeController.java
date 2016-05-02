package com.mcmcg.ico.bluefin.controller;

import java.util.Calendar;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @RequestMapping("/")
    public String greeting(Model model) {
        model.addAttribute("version", "1.0.0-SNAPSHOT");
        model.addAttribute("date", Calendar.getInstance().getTime());
        return "home";
    }
}