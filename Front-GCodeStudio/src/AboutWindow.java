import javax.swing.*;

public class AboutWindow {
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
