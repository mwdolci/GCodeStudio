import java.nio.file.Paths;
import javax.swing.SwingUtilities;

// Pour lancer en bash:
//javac *.java
//java Main.java

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new MainWindow();  // Lance la fenêtre
        });
    }
}
