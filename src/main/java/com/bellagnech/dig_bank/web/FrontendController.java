package com.bellagnech.dig_bank.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

// Controller to handle Angular routes and serve the SPA - This forwards all unmatched routes to index.html for client-side routing
@Controller
public class FrontendController {

    @RequestMapping(value = { "/", "/{x:[\\w\\-]+}", "/{x:[\\w\\-]+}/**" })
    public String index() {
        return "forward:/index.html";
    }
}
