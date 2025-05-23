import java.nio.file.Paths;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

// Pour lancer en bash:
//javac *.java
//java Main.java

public class Main {

    public static void main(String[] args) {
    	
    	try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Erreur lors de l'application du thème FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            new MainWindow();  // Lance la fenêtre
        });
    }
}
