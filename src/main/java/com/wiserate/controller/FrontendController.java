package com.wiserate.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class FrontendController {

    // Path not start with /api
    @GetMapping(value = "/{path:[^\\.]*}")
    public String forward(@PathVariable String path, HttpServletRequest request) {
        if (request.getRequestURI().startsWith("/api/h2-console")) {
            return "forward:/api/h2-console";
        }
        return "forward:/index.html";
    }
}
