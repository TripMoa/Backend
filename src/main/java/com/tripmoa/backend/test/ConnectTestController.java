package com.tripmoa.backend.test;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConnectTestController {

    @GetMapping("/connect-test")
    public String connectTest() {
        return "backend connected";
    }
}
