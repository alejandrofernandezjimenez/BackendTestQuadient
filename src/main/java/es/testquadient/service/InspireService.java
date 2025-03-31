package es.testquadient.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class InspireService {
    public String ejecutarInspire() {
        String comando = "InspireCLI.exe";
        String wfd = "C:\\Users\\alejandro.fernandezj.PLEXUS\\Downloads\\PRACTICAS\\Vital_SalesBaseTemplate.wfd";
        String json = "C:\\Users\\alejandro.fernandezj.PLEXUS\\Downloads\\PRACTICAS\\testInput.json";
        String output = "C:\\Users\\alejandro.fernandezj.PLEXUS\\Downloads\\Output1.pdf";

        ProcessBuilder builder = new ProcessBuilder(
                comando,
                wfd,
                "-e", "PDF",
                "-difData", json,
                "-f", output
        );

        builder.redirectErrorStream(true);

        try {
            System.out.println("Comando completo: " + String.join(" ", builder.command()));
            Process proceso = builder.start();
            // Leer la salida
            StringBuilder salida = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()));
            String linea;
            while ((linea = reader.readLine()) != null) {
                System.out.println(salida);

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
