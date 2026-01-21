package com.animefan.controller.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controller for error pages
 */
@Slf4j
@Controller
public class CustomErrorController implements ErrorController {

    @GetMapping("/error/403")
    public String accessDenied() {
        log.warn("Access denied - 403");
        return "error/403";
    }

    @GetMapping("/error/404")
    public String notFound() {
        log.warn("Page not found - 404");
        return "error/404";
    }

    @GetMapping("/error/500")
    public String serverError() {
        log.error("Internal server error - 500");
        return "error/500";
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, HttpServletResponse response, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object uri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        int statusCode = status != null ? Integer.parseInt(status.toString()) : response.getStatus();

        log.warn("Error occurred: status={}, uri={}, message={}", statusCode, uri, message);

        model.addAttribute("statusCode", statusCode);
        model.addAttribute("path", uri != null ? uri.toString() : request.getRequestURI());

        if (message != null && !message.toString().isEmpty()) {
            model.addAttribute("message", message.toString());
        } else {
            model.addAttribute("message", "Страница не найдена");
        }

        if (statusCode == HttpStatus.NOT_FOUND.value() || statusCode == 0) {
            return "error/404";
        } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
            return "error/403";
        } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            return "error/500";
        } else if (statusCode == HttpStatus.BAD_REQUEST.value()) {
            return "error/404";
        }

        return "error/500";
    }
}
