import java.io.File;
import java.nio.file.Paths;
import java.awt.Desktop;
import javax.swing.*;

public class Help {

    public static void openHelpPDF() {
        String currentDir = Paths.get("").toAbsolutePath().toString();
        String helpFilePath = Paths.get(currentDir, "..", "..", "doc", "user_manual_fr.pdf").normalize().toString();

        File helpFile = new File(helpFilePath);
        if (helpFile.exists()) {
            try {
                Desktop.getDesktop().open(helpFile);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Impossible d'ouvrir le fichier d'aide.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Fichier d'aide introuvable.");
        }
    }

    public static void openTutorialMovie() {
        String currentDir = Paths.get("").toAbsolutePath().toString();
        String tutorialPath = Paths.get(currentDir, "..", "..", "doc", "tutorial.mp4").normalize().toString();

        File tutorialFile = new File(tutorialPath);
        if (tutorialFile.exists()) {
            try {
                Desktop.getDesktop().open(tutorialFile);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Impossible d'ouvrir le tutoriel vidéo.");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Fichier de tutoriel introuvable.");
        }
    }

    public static void openAboutWindow(JFrame parent) {
        String aboutText = "GCode Studio V1.0\n\n" +
                "Développé par Dolci Marco & Toussaint Guillaume\n" +
                "Dans le cadre de la formation MAS-RAD à la HE-Arc Ingénierie (Neuchâtel)\n\n" +
                "Ce logiciel est gratuit et peut être utilisé librement à vos propres risques.\n" +
                "Les auteurs ne peuvent être tenus responsables de tout dommage direct ou indirect\n" +
                "résultant de son utilisation.\n\n" +
                "Aucune licence spécifique n'est appliquée.";

        JOptionPane.showMessageDialog(parent, aboutText, "À propos", JOptionPane.INFORMATION_MESSAGE);
    }
}
