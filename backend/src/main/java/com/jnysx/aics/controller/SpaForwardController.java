package com.jnysx.aics.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * SPA路由转发 - 所有非API路由返回index.html
 */
@Controller
public class SpaForwardController {

    @GetMapping({"/admin/**", "/chat", "/agent-workbench", "/login", "/license"})
    public String forward() {
        return "forward:/index.html";
    }
}
