import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Paths;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MainWindow extends JFrame {

    private boolean GCodeIsOpen = false;
    private boolean STLIsOpen = false;
    private String fullPathSTL = "";
    private String fullPathGCode = "";
    private java.util.List<String> tempData;  // variable temporaire pour stocker les données CSV
    private JTextArea gcodeEditor;
    private DefaultTableModel tableModel;
    private JTable table;

    DefaultListModel<String> listModel = new DefaultListModel<>();  // Modèle liste pour stockage des données
    JList<String> itemList = new JList<>(listModel);                // Création de la liste pour affichage

    public MainWindow() {
        super("GCodeStudio");
        initializeWindow();
        setupMenu();
        JSplitPane mainSplit = setupPanels();
        setupListForGCode(mainSplit);
        setVisible(true);
    }

    private void initializeWindow() {
        // Logo
        Image icon = Toolkit.getDefaultToolkit().getImage("../../img/logo.png");
        setIconImage(icon);

        // Configuration de la fenêtre
        setExtendedState(JFrame.MAXIMIZED_BOTH);    // Prend tout l'écran
        setDefaultCloseOperation(EXIT_ON_CLOSE);    // Si je ferme avec la croix, ça stop le process
        setLocationRelativeTo(null);              // Centrer
    }

    private void setupMenu() {
        // Menu bar
        JMenuBar menuBar = new JMenuBar();

        // *Liste Fichier*
        JMenu menuFile = new JMenu("Fichier");
        JMenuItem itemOpenGCode = new JMenuItem("Ouvrir GCode");
        JMenuItem itemOpenSTL = new JMenuItem("Ouvrir 3D");
        JMenuItem itemSaveGCode = new JMenuItem("Enregistrer GCode");
        JMenuItem itemSaveGCodeAs = new JMenuItem("Enregistrer GCode sous");

        itemOpenGCode.addActionListener(e -> openGCodeFile());
        itemOpenSTL.addActionListener(e -> openSTLFile());
        // itemSaveGCode.addActionListener(e -> saveGCode());
        // itemSaveGCodeAs.addActionListener(e -> saveGCodeAs());

        menuFile.add(itemOpenGCode);
        menuFile.add(itemOpenSTL);
        menuFile.add(itemSaveGCode);
        menuFile.add(itemSaveGCodeAs);

        // *Liste Fonctions*
        JMenu menuFunctions = new JMenu("Fonctions");
        JMenuItem itemCalculate = new JMenuItem("Recalculer");
        menuFunctions.add(itemCalculate);

        // itemCalculate.addActionListener(e -> calculation());

        // *Liste Options*
        JMenu menuOptions = new JMenu("Options");

        menuBar.add(menuFile);
        menuBar.add(menuFunctions);
        menuBar.add(menuOptions);

        setJMenuBar(menuBar);
    }

    private JSplitPane setupPanels() {
        Color backgroundColor = new Color(30, 30, 30);
        Color borderColor = Color.WHITE;

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

        return mainSplit;
    }

    private void setupListForGCode(JSplitPane mainSplit) {
        JPanel bottomLeft = (JPanel) ((JSplitPane) mainSplit.getBottomComponent()).getLeftComponent();  // Liste GCode
        JPanel bottomRight = (JPanel) ((JSplitPane) mainSplit.getBottomComponent()).getRightComponent(); // Détail ligne

        // Création du JTextArea pour l'édition du G-code
        gcodeEditor = new JTextArea();
        gcodeEditor.setFont(new Font("Monospaced", Font.PLAIN, 14));
        gcodeEditor.setLineWrap(false); // Pas de retour à la ligne automatique
        gcodeEditor.setWrapStyleWord(false);
        gcodeEditor.setMargin(new Insets(5, 5, 5, 5));

        // Ajout d’un scroll
        JScrollPane editorScrollPane = new JScrollPane(gcodeEditor);
        bottomLeft.setLayout(new BorderLayout());
        bottomLeft.add(editorScrollPane, BorderLayout.CENTER);

        // Zone de détail dans le panneau de droite
        JTextArea detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane detailScroll = new JScrollPane(detailArea);
        bottomRight.setLayout(new BorderLayout());
        bottomRight.add(detailScroll, BorderLayout.CENTER);

        // Initialisation à partir du listModel (ancien contenu)
        for (int i = 0; i < listModel.size(); i++) {
            gcodeEditor.append(listModel.getElementAt(i) + "\n");
        }

        // Affichage de la ligne courante dans le panneau de droite
        gcodeEditor.addCaretListener(e -> {
            try {
                int caretPos = gcodeEditor.getCaretPosition();
                int line = gcodeEditor.getLineOfOffset(caretPos);
                int start = gcodeEditor.getLineStartOffset(line);
                int end = gcodeEditor.getLineEndOffset(line);
                String currentLine = gcodeEditor.getText().substring(start, end).trim();
                detailArea.setText("Détail de la ligne sélectionnée :\n\n" + currentLine);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void openGCodeFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Ouvrir un fichier GCode");

        // Filtre pour limiter les fichiers affichés aux extensions reconnues
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Fichiers .anc .nc .txt", "anc", "nc", "txt"));

        int result = fileChooser.showOpenDialog(this); // this = parent JFrame

        if (result == JFileChooser.APPROVE_OPTION) { // Si fichier sélectionné 
            File selectedFile = fileChooser.getSelectedFile();
            fullPathGCode = selectedFile.getAbsolutePath();

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // Sablier

            // Travail en arrière-plan
            SwingWorker<Void, Void> worker = new SwingWorker<>() { // Classe absraite pour tâche en arrière plan sans figer swing
                @Override
                protected Void doInBackground() throws Exception {
                    String currentDir = Paths.get("").toAbsolutePath().toString();
                    String pythonScriptPath = Paths.get(currentDir, "..", "..", "Back-GCodeStudio", "main.py").normalize().toString();

                    GCodeIsOpen = true;

                    PythonCaller.runScript(fullPathGCode, fullPathSTL, pythonScriptPath, STLIsOpen);

                    Path tempFolder = Paths.get(System.getenv().getOrDefault("TEMP", "/tmp"));
                    Path tempGCodePath = tempFolder.resolve(Paths.get(fullPathGCode).getFileName().toString() + "_gcode.csv");

                    tempData = loadCSVColumnToList(tempGCodePath.toFile());

                    return null;
                }

                @Override
                protected void done() {
                    gcodeEditor.setText(""); // Vide le contenu
                    for (String item : tempData) {
                        gcodeEditor.append(item + "\n");
                    }
                    setCursor(Cursor.getDefaultCursor());
}
            };

            worker.execute();
        }
    }

    // Charge les données CSV dans une liste String sans toucher à l'UI (thread swing) --> évite les erreurs sporadique au chargement du GCode
    private java.util.List<String> loadCSVColumnToList(File csvFile) {
        java.util.List<String> dataList = new java.util.ArrayList<>(); //Création de la liste 
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) { //Ouverutre csv pour lecture
            String header = br.readLine();  // Ignore entête

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // Séparateur
                if (values.length > 0) {
                    dataList.add(values[0]); // Ajoute colonne 1
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur de lecture du fichier CSV.");
        }
        return dataList;
    }

    private void openSTLFile() {
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
    }
}