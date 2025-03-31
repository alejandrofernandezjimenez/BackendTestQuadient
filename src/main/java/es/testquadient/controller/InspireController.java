package es.testquadient.controller;

import es.testquadient.service.InspireService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InspireController {

    @Autowired
    private InspireService inspireService;

    @GetMapping("/generar-pdf")
    public String generarPDF() {
        return inspireService.ejecutarInspire();
    }
}
