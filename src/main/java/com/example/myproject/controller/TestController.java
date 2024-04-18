package com.example.myproject.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @GetMapping("/test/logging")
    public String testLogging() {
        logger.debug("디버그 로그 테스트");
        logger.info("인포 로그 테스트");
        return "로깅 테스트 완료";
    }
}
