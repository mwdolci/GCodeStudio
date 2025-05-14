import java.io.*;

public class PythonCaller {

    public static void runScript(String gcodeFilePath, String pythonScriptPath) {
        String pythonExe = "python";
        ProcessBuilder processBuilder = new ProcessBuilder(pythonExe, pythonScriptPath, gcodeFilePath);
        processBuilder.redirectErrorStream(true); // Combine stdout + stderr

        try {
            Process process = processBuilder.start();

            // Affiche la sortie Python
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            int exitCode = process.waitFor();
            System.out.println("Le processus s'est terminé avec le code : " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

