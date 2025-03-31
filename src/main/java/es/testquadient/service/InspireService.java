package es.testquadient.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;

@Service
public class InspireService {

    public String ejecutarInspire() {
        String userHome = System.getProperty("user.home");
        String wfd = userHome + "\\Downloads\\PRACTICAS\\Vital_SalesBaseTemplate.wfd";
        String json = userHome + "\\Downloads\\PRACTICAS\\testInput.json";
        String output = userHome + "\\Downloads\\Output1.pdf";

        // Buscar la ruta que contiene "Inspire Designer" en el PATH
        String path = System.getenv("PATH");
        String[] rutas = path.split(";");
        String rutaInspireCLI = Arrays.stream(rutas)
                .filter(r -> r.contains("Inspire Designer"))
                .findFirst()
                .map(r -> r + "\\InspireCLI.exe")
                .orElse(null);

        if (rutaInspireCLI == null || !new File(rutaInspireCLI).exists()) {
            return "⚠️ No se encontró InspireCLI en el PATH del sistema (¿falta Inspire Designer en el PATH?).";
        }

        ProcessBuilder builder = new ProcessBuilder(
                rutaInspireCLI,
                wfd,
                "-e", "PDF",
                "-difData", json,
                "-f", output
        );

        builder.redirectErrorStream(true);

        try {
            System.out.println("🛠️ Ejecutando: " + String.join(" ", builder.command()));

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
