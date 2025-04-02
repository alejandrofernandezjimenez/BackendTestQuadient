package es.testquadient.controller;

import es.testquadient.service.InspireService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileOutputStream;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class InspireControllerTest {

    private InspireController controller;
    private InspireService inspireService;

    @BeforeEach
    void setUp() {
        inspireService = mock(InspireService.class);
        controller = new InspireController();

        // Inyectamos a mano porque esto no es magia negra de Spring, es Mockito puro
        try {
            var field = controller.getClass().getDeclaredField("inspireService");
            field.setAccessible(true);
            field.set(controller, inspireService);
        } catch (Exception e) {
            fail("Error inyectando InspireService: " + e.getMessage());
        }
    }

    @Test
    void testGenerarPDF() {
        MockMultipartFile mockFile = new MockMultipartFile(
                "archivo", "test.txt", "text/plain", "contenido de prueba".getBytes());

        when(inspireService.ejecutarInspireConArchivo(mockFile))
                .thenReturn("PDF generado con éxito");

        String respuesta = controller.generarPDF(mockFile);

        assertEquals("PDF generado con éxito", respuesta);
        verify(inspireService, times(1)).ejecutarInspireConArchivo(mockFile);
    }

    @Test
    void testDescargarPDF_successful() {
        try {
            String userHome = System.getProperty("user.home");
            File fakePDF = new File(userHome + "\\Downloads\\Output1.pdf");
            fakePDF.getParentFile().mkdirs();
            try (FileOutputStream out = new FileOutputStream(fakePDF)) {
                out.write("PDF de prueba".getBytes());
            }

            ResponseEntity<Resource> respuesta = controller.descargarPDF();

            assertEquals(200, respuesta.getStatusCodeValue());
            assertEquals("application/pdf", respuesta.getHeaders().getContentType().toString());
            assertTrue(respuesta.getHeaders().getFirst("Content-Disposition").contains("Output1.pdf"));

            fakePDF.delete();

        } catch (Exception e) {
            fail("Excepción durante el test de descarga: " + e.getMessage());
        }
    }

    @Test
    void testDescargarPDF_fileNotFound() {
        String userHome = System.getProperty("user.home");
        File archivo = new File(userHome + "\\Downloads\\Output1.pdf");

        // Intenta eliminarlo varias veces si hace falta (Windows a veces lo bloquea)
        for (int i = 0; i < 5 && archivo.exists(); i++) {
            archivo.delete();
            try {
                Thread.sleep(100); // Deja respirar al sistema operativo
            } catch (InterruptedException ignored) {}
        }

        assertFalse(archivo.exists(), "El archivo Output1.pdf sigue existiendo, no se puede testear 404.");

        ResponseEntity<Resource> respuesta = controller.descargarPDF();
        assertEquals(404, respuesta.getStatusCodeValue());
    }


}
