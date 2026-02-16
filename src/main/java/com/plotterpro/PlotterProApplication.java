package com.plotterpro;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.ui.Model;

@SpringBootApplication
public class PlotterProApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlotterProApplication.class, args);
    }
}

@ControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        model.addAttribute("error", e.getClass().getSimpleName());
        model.addAttribute("message", e.getMessage());
        return "error";
    }
}
