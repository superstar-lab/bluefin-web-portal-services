package com.mcmcg.ico.bluefin.controller;

import java.util.Calendar;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @Value("${info.build.artifact}")
    private String infoBuildArtifact;
    @Value("${info.build.name}")
    private String infoBuildName;
    @Value("${info.build.description}")
    private String infoBuildDescription;
    @Value("${info.build.version}")
    private String infoBuildVersion;

    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("artifact", infoBuildArtifact);
        model.addAttribute("name", infoBuildName);
        model.addAttribute("description", infoBuildDescription);
        model.addAttribute("version", infoBuildVersion);
        model.addAttribute("date", Calendar.getInstance().getTime());
        return "home";
    }
}