import java.nio.file.Paths;

import javax.swing.SwingUtilities;

// Pour lancer en bash:
//javac *.java
//java Main "C:\Users\mdolci\Desktop\GCodeStudio\datas_testing\Levier.anc"

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new MainWindow();  // Lance la fenêtre
        });

        if (args.length < 1) {
            System.out.println("Veuillez fournir le chemin du fichier G-code.");
            return;
        }

        String gcodeFilePath = args[0];

        // Chemin du script Python : remonter de 2 niveaux + entrer dans le dossier Back-GCodeStudio
        String currentDir = Paths.get("").toAbsolutePath().toString();
        String pythonScriptPath = Paths.get(currentDir, "..", "..", "Back-GCodeStudio", "main.py").normalize().toString();

        PythonCaller.runScript(gcodeFilePath, pythonScriptPath);
    }
}
