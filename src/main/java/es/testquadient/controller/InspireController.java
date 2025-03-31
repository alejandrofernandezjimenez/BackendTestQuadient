package es.testquadient.controller;

import es.testquadient.service.InspireService;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;


import java.io.File;
import java.io.FileInputStream;

@RestController
public class InspireController {

    @Autowired
    private InspireService inspireService;

    @GetMapping("/generar-pdf")
    public String generarPDF() {
        return inspireService.ejecutarInspire();
    }

    @GetMapping("/descargar-pdf")
    public ResponseEntity<Resource> descargarPDF() {
        try {
            String userHome = System.getProperty("user.home");
            File archivo = new File(userHome + "\\Downloads\\Output1.pdf");

            if (!archivo.exists()) {
                return ResponseEntity.notFound().build();
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(archivo));

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=Output1.pdf")
                    .contentLength(archivo.length())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body((Resource) resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
