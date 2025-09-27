package com.example.otel_kotlin_api_usage_with_spring_starter

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Controller {
    @GetMapping("/test")
    fun ping(): String {
        return "pong"
    }
}
