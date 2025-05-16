import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Paths;

public class MainWindow extends JFrame {

    boolean GCodeIsOpen = false;
    boolean STLIsOpen = false;
    String fullPathSTL = "";
    String fullPathGCode = "";

    public MainWindow() {
        super("GCodeStudio");

        // Logo
        Image icon = Toolkit.getDefaultToolkit().getImage("../../img/logo.png");
        setIconImage(icon);

        // Couleurs
        Color backgroundColor = new Color(30, 30, 30);
        Color borderColor = Color.WHITE;
        Color foregroundColor = Color.WHITE;

        // Configuration de la fenêtre
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Prend tout l'écran
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Si je ferme avec la croix, ça stop le process
        setLocationRelativeTo(null); // Centrer
        
        // Menu bar
        JMenuBar menuBar = new JMenuBar();

        // Liste Fichier
        JMenu menuFile = new JMenu("Fichier");
        JMenuItem itemOpenGCode = new JMenuItem("Ouvrir GCode");
        JMenuItem itemOpenSTL = new JMenuItem("Ouvrir 3D");
        JMenuItem itemSaveGCode = new JMenuItem("Enregistrer GCode");
        JMenuItem itemSaveGCodeAs = new JMenuItem("Enregistrer GCode sous");

        menuFile.add(itemOpenGCode);
        menuFile.add(itemOpenSTL);
        menuFile.add(itemSaveGCode);
        menuFile.add(itemSaveGCodeAs);

        // Evénement "Ouvrir GCode"
        itemOpenGCode.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Ouvrir un fichier GCode");

            // Filtre pour limiter les fichiers affichés aux extensions reconnues
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Fichiers .anc .nc .txt", "anc", "nc", "txt"));

            int result = fileChooser.showOpenDialog(this); // this = parent JFrame

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                fullPathGCode = selectedFile.getAbsolutePath();
                
                String currentDir = Paths.get("").toAbsolutePath().toString();
                String pythonScriptPath = Paths.get(currentDir, "..", "..", "Back-GCodeStudio", "main.py").normalize().toString();

                GCodeIsOpen = true;

                PythonCaller.runScript(fullPathGCode, fullPathSTL, pythonScriptPath, STLIsOpen); // Si un stl est déjà ouvert on lance aussi le viewer
                
            }
        });

        // Evénement "Ouvrir STL"
        itemOpenSTL.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Ouvrir un fichier 3D");

            // Filtre pour limiter les fichiers affichés aux extensions reconnues
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Fichiers .stl", "stl"));

            int result = fileChooser.showOpenDialog(this); // this = parent JFrame

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                fullPathSTL = selectedFile.getAbsolutePath();
                
                String currentDir = Paths.get("").toAbsolutePath().toString();
                String pythonScriptPath = Paths.get(currentDir, "..", "..", "Back-GCodeStudio", "main.py").normalize().toString();

                STLIsOpen = true;

                PythonCaller.runScript(fullPathGCode, fullPathSTL, pythonScriptPath, GCodeIsOpen); // Si un GCode est déjà ouvert on lance aussi le viewer
            }
        });

        // itemSaveGCode.addActionListener(...);
        // itemSaveGCodeAs.addActionListener(...);

        // Liste Fonctions
        JMenu menuFunctions = new JMenu("Fonctions");
        JMenuItem itemCalculate = new JMenuItem("Recalculer");

        menuFunctions.add(itemCalculate);

        // itemCalculate.addActionListener(...);

        // Liste Options
        JMenu menuOptions = new JMenu("Options");

        menuBar.add(menuFile);
        menuBar.add(menuFunctions);
        menuBar.add(menuOptions);

        setJMenuBar(menuBar);

        // Création des 4 panneaux
        JPanel topLeft = new JPanel();
        JPanel topRight = new JPanel();
        JPanel bottomLeft = new JPanel();
        JPanel bottomRight = new JPanel();

        JPanel[] panels = { topLeft, topRight, bottomLeft, bottomRight };
        for (JPanel panel : panels) {
            panel.setBackground(backgroundColor);
            panel.setBorder(BorderFactory.createLineBorder(borderColor, 1));
        }

        // Split haut (gauche/droite)
        JSplitPane splitTop = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, topLeft, topRight);
        splitTop.setResizeWeight(0.5);   // moitié-moitié
        splitTop.setDividerSize(2);    // épaisseur de la ligne
        splitTop.setBorder(null);

        // Split bas (gauche/droite)
        JSplitPane splitBottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bottomLeft, bottomRight);
        splitBottom.setResizeWeight(0.5);
        splitBottom.setDividerSize(2);
        splitBottom.setBorder(null);

        // Split principal (haut/bas)
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitTop, splitBottom);
        mainSplit.setResizeWeight(0.5);
        mainSplit.setDividerSize(2);
        mainSplit.setBorder(null);

        // Ajouter le split principal à la fenêtre
        getContentPane().add(mainSplit, BorderLayout.CENTER);

        setVisible(true);


    }
}