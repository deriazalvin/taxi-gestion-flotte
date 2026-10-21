package com.example.taxi.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(annotations = Controller.class)
public class WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public String handleBusiness(BusinessException ex, RedirectAttributes ra) {
        ra.addFlashAttribute("error", ex.getMessage());
        return "redirect:/dashboard";
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAuthz(AuthorizationDeniedException ex, Model model) {
        log.warn("Accès refusé MVC : {}", ex.getMessage());
        model.addAttribute("errorTitle", "Accès refusé");
        model.addAttribute("errorMessage", "Vous n'avez pas les droits suffisants pour cette action.");
        model.addAttribute("backUrl", "/dashboard");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception ex, Model model) {
        log.warn("Erreur MVC capturée : {}, {}", ex.getClass().getSimpleName(), ex.getMessage());
        model.addAttribute("errorTitle", "Erreur applicative");
        model.addAttribute("errorMessage", "Une erreur inattendue est survenue. Veuillez réessayer.");
        model.addAttribute("backUrl", "/dashboard");
        return "error";
    }
}