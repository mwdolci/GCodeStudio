import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Paths;

public class MainWindow extends JFrame {

    public MainWindow() {
        super("GCodeStudio");

        // Configuration de la fenêtre
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Prend tout l'écran
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Si je ferme avec la croix, ça stop le process
        setLocationRelativeTo(null); // Centrer

        // Boutons pour charger fichiers
        JButton buttonLoadGCode = new JButton("Ouvrir GCode");
        JButton buttonLoadSTL = new JButton("Ouvrir 3D");

        // Ajouter les boutons au panneau
        JPanel panel = new JPanel();
        panel.add(buttonLoadGCode);
        panel.add(buttonLoadSTL);
        add(panel);

        setVisible(true);
    }
}