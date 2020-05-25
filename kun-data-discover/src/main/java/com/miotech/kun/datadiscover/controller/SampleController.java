package com.miotech.kun.datadiscover.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: Melo
 * @created: 5/26/20
 */

@RestController
public class SampleController {
    @RequestMapping("/")
    public String index() {
        return "Greetings from Kun - Data Discovery!";
    }
}
