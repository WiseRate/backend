package com.wiserate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    // Add an exclusion for static assets in FrontendController to prevent forwarding loop
    @GetMapping("/{path:^(?!api)(?!.*\\.(js|css|png|ico|jpg|jpeg|gif|svg|webp)$).*$}")
    public String forward() {
        return "forward:/index.html";
    }
}
