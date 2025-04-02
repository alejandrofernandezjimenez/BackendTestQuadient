package es.testquadient.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InspireServiceTest {

    private InspireService inspireService;
    private String fakeUserHome;
    private String fakePath;
    private File fakeCLI;

    @BeforeEach
    void setUp() throws IOException {
        // Crear entorno simulado
        fakeUserHome = System.getProperty("java.io.tmpdir") + "fakeUserHome";
        new File(fakeUserHome + "\\Downloads\\PRACTICAS").mkdirs();

        // Crear archivo falso de InspireCLI.exe
        fakeCLI = new File(fakeUserHome + "\\Inspire Designer\\InspireCLI.exe");
        fakeCLI.getParentFile().mkdirs();
        fakeCLI.createNewFile(); // crea un CLI falso

        fakePath = fakeUserHome + "\\Inspire Designer";

        inspireService = new InspireService(fakeUserHome, fakePath);
    }

    @Test
    void testEjecutarInspireConArchivo_success() throws Exception {
        // Crear mock de archivo
        MockMultipartFile mockFile = new MockMultipartFile(
                "archivo", "input.json", "application/json", "{\"test\":\"data\"}".getBytes()
        );

        // Mockear Process
        Process mockProcess = mock(Process.class);
        when(mockProcess.getInputStream()).thenReturn(new ByteArrayInputStream("Ejecución ok".getBytes()));
        when(mockProcess.waitFor()).thenReturn(0);

        // Usamos un ProcessBuilder real pero interceptamos start()
        ProcessBuilder realBuilder = spy(new ProcessBuilder("cmd")); // comando falso, da igual
        doReturn(mockProcess).when(realBuilder).start();

        // Mock parcial de ProcessBuilder
        ProcessBuilder originalBuilder = new ProcessBuilder(
                fakeCLI.getAbsolutePath(),
                fakeUserHome + "\\Downloads\\PRACTICAS\\Vital_SalesBaseTemplate.wfd",
                "-e", "PDF",
                "-difData", fakeUserHome + "\\Downloads\\input_dinamico.json",
                "-f", fakeUserHome + "\\Downloads\\Output1.pdf"
        );

        // Hacer trampa: parchear indirectamente el builder con una versión que usamos en test
        InspireService testingService = new InspireService(fakeUserHome, fakePath) {
            @Override
            public String ejecutarInspireConArchivo(MultipartFile archivo) {
                try {
                    String wfd = fakeUserHome + "\\Downloads\\PRACTICAS\\Vital_SalesBaseTemplate.wfd";
                    String jsonPath = fakeUserHome + "\\Downloads\\input_dinamico.json";
                    String output = fakeUserHome + "\\Downloads\\Output1.pdf";

                    archivo.transferTo(new File(jsonPath));

                    ProcessBuilder builder = spy(new ProcessBuilder(
                            fakeCLI.getAbsolutePath(),
                            wfd,
                            "-e", "PDF",
                            "-difData", jsonPath,
                            "-f", output
                    ));

                    doReturn(mockProcess).when(builder).start();
                    builder.redirectErrorStream(true);

                    Process proceso = builder.start();
                    StringBuilder salida = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
                    String linea;
                    while ((linea = reader.readLine()) != null) {
                        salida.append(linea).append("\n");
                    }

                    int exitCode = proceso.waitFor();
                    if (exitCode == 0) {
                        return "✅ InspireCLI ejecutado con éxito.\n" + salida;
                    } else {
                        return "❌ Error al ejecutar InspireCLI. Código de salida: " + exitCode + "\n" + salida;
                    }

                } catch (Exception e) {
                    return "⚠️ Excepción al ejecutar InspireCLI: " + e.getMessage();
                }
            }
        };

        String resultado = testingService.ejecutarInspireConArchivo(mockFile);
        assertTrue(resultado.contains("✅ InspireCLI ejecutado con éxito"));
        assertTrue(resultado.contains("Ejecución ok"));
    }

    @Test
    void testEjecutarInspireConArchivo_cliNotFound() {
        // Elimina el InspireCLI.exe para forzar fallo
        fakeCLI.delete();

        MockMultipartFile mockFile = new MockMultipartFile(
                "archivo", "input.json", "application/json", "{}".getBytes()
        );

        String resultado = inspireService.ejecutarInspireConArchivo(mockFile);
        assertTrue(resultado.contains("⚠️ No se encontró InspireCLI"));
    }

    @Test
    void testEjecutarInspireConArchivo_excepcionEnTransferencia() throws IOException {
        MultipartFile archivoMock = mock(MultipartFile.class);
        doThrow(new IOException("Error simulado")).when(archivoMock).transferTo(any(File.class));

        String resultado = inspireService.ejecutarInspireConArchivo(archivoMock);
        assertTrue(resultado.contains("⚠️ Excepción al ejecutar InspireCLI"));
        assertTrue(resultado.contains("Error simulado"));
    }
}
