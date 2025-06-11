import javax.swing.*;
import java.nio.file.Paths;

public class Viewer3D {
    private final MainWindow mainWindow;

    public Viewer3D(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public void startViewer3D() {
        if (mainWindow.GCodeIsOpen) {
            new Thread(() -> {
                String currentDir = Paths.get("").toAbsolutePath().toString();
                String pythonScriptPath = Paths.get(currentDir, "..", "..", "Back-GCodeStudio", "main.py").normalize().toString();
                PythonCaller.runScript(mainWindow.fullPathGCode, mainWindow.fullPathSTL, pythonScriptPath, mainWindow.GCodeIsOpen);
            }).start();
        } else {
            JOptionPane.showMessageDialog(mainWindow, mainWindow.messageGCodeNotOpen);
        }
    }
}
