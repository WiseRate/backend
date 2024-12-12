package com.wiserate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class FrontendController {

    // Path not start with /api
    @GetMapping(value = "/{path:[^\\.]*}")
    public String forward(@PathVariable String path) {
        return "forward:/index.html";
    }
}
