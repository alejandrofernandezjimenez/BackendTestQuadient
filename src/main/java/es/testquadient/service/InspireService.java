package es.testquadient.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Arrays;

@Service
public class InspireService {

    private final String userHome;
    private final String path;

    public InspireService() {
        this.userHome = System.getProperty("user.home");
        this.path = System.getenv("PATH");
    }

    // constructor para inyectar en tests
    public InspireService(String userHome, String path) {
        this.userHome = userHome;
        this.path = path;
    }

    public String ejecutarInspireConArchivo(MultipartFile archivo) {
        try {
            String wfd = userHome + "\\Downloads\\PRACTICAS\\Vital_SalesBaseTemplate.wfd";
            String jsonPath = userHome + "\\Downloads\\input_dinamico.json";
            String output = userHome + "\\Downloads\\Output1.pdf";
            String jobConfig = userHome + "\\Downloads\\PRACTICAS\\jobconfig.job";

            File destino = new File(jsonPath);
            archivo.transferTo(destino);

            String[] rutas = path.split(";");
            String rutaInspireCLI = Arrays.stream(rutas)
                    .filter(r -> r.contains("Inspire Designer"))
                    .findFirst()
                    .map(r -> r + "\\InspireCLI.exe")
                    .orElse(null);

            if (rutaInspireCLI == null || !new File(rutaInspireCLI).exists()) {
                return "⚠️ No se encontró InspireCLI en el PATH del sistema.";
            }



            ProcessBuilder builder = new ProcessBuilder(
                    rutaInspireCLI,
                    wfd,
                    "-e", "PDF",
                    "-difData", jsonPath,
                    "-f", output,
                    "-c", jobConfig
            );
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
}

