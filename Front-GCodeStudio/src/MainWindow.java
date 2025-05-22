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
import javax.swing.text.DefaultHighlighter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Locale;
import java.nio.file.Path;

public class MainWindow extends JFrame {

    private boolean GCodeIsOpen = false;
    private boolean STLIsOpen = false;
    private String fullPathSTL = "";
    private String fullPathGCode = "";
    private java.util.List<String[]> tempData;  // variable temporaire pour stocker toutes les colonnes CSV
    private java.util.List<String[]> tempData2;
    private java.util.List<String[]> tempData3;
    private JTextArea gcodeEditor;
    private JTextArea lineInfoArea;
    private JTextArea topLeftTextArea;
    private JTextArea topRightTextArea;
    private JTextArea bottomRightTextArea;
    private JSplitPane mainSplit;
    Color backgroundColor = new Color(30, 30, 30);
    Color borderColor = Color.WHITE;

    DefaultListModel<String> listModel = new DefaultListModel<>();  // Modèle liste pour stockage des données
    JList<String> itemList = new JList<>(listModel);                // Création de la liste pour affichage

    public MainWindow() {
        super("GCodeStudio");
        initializeWindow();
        setupMenu();
        mainSplit = setupPanels();
        setupTopLeft(mainSplit);
        setupTopRight(mainSplit);
        setupBottomLeft(mainSplit);
        setupBottomRight(mainSplit);
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

        //Pour version évoluée
        //menuFile.add(itemSaveGCode);
        //menuFile.add(itemSaveGCodeAs);

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

    // 4 Panels
    private JSplitPane setupPanels() {
        // Création des 4 panneaux
        JPanel topLeft = new JPanel();
        JPanel topRight = new JPanel();
        JPanel bottomLeft = new JPanel();
        JPanel bottomRight = new JPanel();

        // Apparence des 4 panneaux
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

    // Panel en haut à gauche
    private void setupTopLeft(JSplitPane mainSplit) {
        // Récupération du panneau haut gauche à partir du JSplitPane principal
        JPanel topLeft = (JPanel) ((JSplitPane) mainSplit.getTopComponent()).getLeftComponent();

        topLeft.removeAll();
        
        // Config panneau haut gauche
        topLeft.setLayout(new BorderLayout());
        topLeftTextArea = new JTextArea();
        topLeftTextArea.setEditable(false);
        topLeftTextArea.setForeground(Color.WHITE);
        topLeftTextArea.setBackground(backgroundColor);
        topLeftTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        topLeftTextArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPaneTopLeft = new JScrollPane(topLeftTextArea);
        topLeft.add(scrollPaneTopLeft, BorderLayout.CENTER);

        if (tempData2 == null || tempData2.isEmpty()) {
            topLeftTextArea.setText("Aucune donnée disponible.");
            return;
        }

        String[] row = tempData2.get(0);

        String[] labels = {
            "Fichier G-Code",
            "Fichier 3D",
            "",
            "",
            "Nombre de lignes",
            "Durée du programme",
            "Durée productive",
            "Durée imporductive",
        };

        int[] columnIndices = {
            0, -2, -1, -1, 2, 3, 4, 5  
        };

        int padding = 20;
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < labels.length; i++) {
            if (columnIndices[i] == -1) {
                builder.append("\n");  // ligne vide
            } else if (columnIndices[i] == -2) {
                // Cas spécial : on récupère le chemin du stl si chargé
                String label = labels[i];
                String value = fullPathSTL;
                
                int spaces = Math.max(1, padding - label.length());
                builder.append(label)
                    .append(" ".repeat(spaces))
                    .append(": ")
                    .append(value)
                    .append("\n");
            } else if (columnIndices[i] < row.length) {
                String label = labels[i];
                String value = row[columnIndices[i]];

                if (label.startsWith("Durée")) {
                    value = formatDuration(value);
                }

                int spaces = Math.max(1, padding - label.length());
                builder.append(label)
                    .append(" ".repeat(spaces))
                    .append(": ")
                    .append(value)
                    .append("\n");
            } else {
                builder.append(labels[i]).append(" : (donnée manquante)\n");
            }
        }

        topLeftTextArea.setText(builder.toString());

        // !! Forcer le rafraichissement de la page sinon fonctionne pas
        topLeft.revalidate();
        topLeft.repaint();
    }

    // Panel en bas à gauche
    private void setupBottomLeft(JSplitPane mainSplit) {
        // Colonne gauche = éditeur G-code + infos
        JSplitPane leftSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        leftSplit.setDividerSize(2);

        JPanel bottomLeft = (JPanel) ((JSplitPane) mainSplit.getBottomComponent()).getLeftComponent();
        bottomLeft.setLayout(new BorderLayout());
        bottomLeft.add(leftSplit, BorderLayout.CENTER);

        // Éditeur G-code (gauche)
        gcodeEditor = new JTextArea();
        gcodeEditor.setFont(new Font("Monospaced", Font.PLAIN, 14));
        gcodeEditor.setLineWrap(false); // Pas de retour à la ligne automatique
        gcodeEditor.setWrapStyleWord(false);
        gcodeEditor.setMargin(new Insets(5, 5, 5, 5));
        gcodeEditor.setBackground(Color.LIGHT_GRAY);
        gcodeEditor.setEditable(false); // Verrouiller dans version 1.0
        JScrollPane gcodeScrollPane = new JScrollPane(gcodeEditor);

        // Infos ligne (droite)
        lineInfoArea = new JTextArea();
        lineInfoArea.setEditable(false);
        lineInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        lineInfoArea.setBackground(new Color(30, 30, 30));
        lineInfoArea.setForeground(Color.WHITE);
        lineInfoArea.setCaretColor(Color.WHITE);
        JScrollPane lineInfoScroll = new JScrollPane(lineInfoArea);

        leftSplit.setTopComponent(gcodeScrollPane);
        leftSplit.setBottomComponent(lineInfoScroll);
        leftSplit.setResizeWeight(0.6); // 60% éditeur, 40% infos

        // Initialisation à partir du listModel (ancien contenu)
        for (int i = 0; i < listModel.size(); i++) {
            gcodeEditor.append(listModel.getElementAt(i) + "\n");
        }

        // Affichage de la ligne courante dans le panneau de droite
        gcodeEditor.addCaretListener(e -> {
            try {
                if (tempData == null) {
                    lineInfoArea.setText("Aucune donnée chargée.");
                    return;
                }

                int caretPos = gcodeEditor.getCaretPosition();
                int lineNum = gcodeEditor.getLineOfOffset(caretPos);

                // Surlignage ligne active
                gcodeEditor.getHighlighter().removeAllHighlights();
                int startOffset = gcodeEditor.getLineStartOffset(lineNum);
                int endOffset = gcodeEditor.getLineEndOffset(lineNum);
                gcodeEditor.getHighlighter().addHighlight(startOffset, endOffset, new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 255, 150)));

                if (lineNum >= 0 && lineNum < tempData.size()) {
                    String[] row = tempData.get(lineNum);
                    StringBuilder details = new StringBuilder();

                    //Substitution textes
                    if (row.length > 2 && "RAPID_MOVE".equals(row[2])) {row[2] = "Rapide";}
                    if (row.length > 2 && "LINEAR_MOVE".equals(row[2])) {row[2] = "Linéaire";}
                    if (row.length > 2 && "CIRCULAR_MOVE_CW".equals(row[2])) {row[2] = "Circulaire (horaire)";}
                    if (row.length > 2 && "CIRCULAR_MOVE_CCW".equals(row[2])) {row[2] = "Circulaire (anti-horaire)";}

                    // Nom des champs
                    String[] labels = {
                        //"Ligne active:",
                        //"",
                        "N° outil",
                        "",
                        "Temps",
                        "Durée",
                        "",
                        "Avance",
                        "Rotation",
                        "",
                        "Mouvement",
                        "Distance",
                        "Position X",
                        "Position Y",
                        "Position Z",
                        "Rayon"
                    };
                    int[] columnIndices = {
                        //0,     // Ligne
                        //-1,    // Vide
                        1,     // Numéro outil
                        -1,    // Vide
                        11,    // Temps
                        10,    // Durée
                        -1,    // Vide
                        9,     // Avance
                        12,    // Rotation
                        -1,    // Vide
                        2,     // Mouvement
                        7,     // Distance
                        3,     // X
                        4,     // Y
                        5,     // Z
                        6      // Rayon
                    };

                    int padding = 14;  // position à laquelle commencent les valeurs

                    for (int i = 0; i < labels.length; i++) {
                        if (columnIndices[i] == -1) {
                            details.append("\n");  // ligne vide
                        } else if (columnIndices[i] < row.length) {
                            String label = labels[i];
                            String value = row[columnIndices[i]];

                            if ("Temps".equals(label) || "Durée".equals(label)) {
                                value = formatDuration(value);
                            } else if ("Avance".equals(label)) {
                                value = value + " mm/min";
                            } else if ("Distance".equals(label)) {
                                value = value + " mm";
                            } else if ("Rotation".equals(label)) {
                                value = value + " rpm";
                            } else if (label.startsWith("Position")) {
                                value = value + " mm";
                            } else if ("Rayon".equals(label)) {
                                value = value + " mm";
                            }
                            
                            // calculer le nombre d'espaces à ajouter après le label
                            int spacesToAdd = Math.max(1, padding - label.length());
                            StringBuilder line = new StringBuilder(label);
                            for (int s = 0; s < spacesToAdd; s++) {
                                line.append(' '); // ajoute le nombre d'espaces nécessaires
                            }
                            line.append(": ").append(value).append("\n");

                            details.append(line.toString());
                        } else {
                            details.append(labels[i]).append(" : (donnée manquante)\n");
                        }
                    }

                    lineInfoArea.setText(details.toString());
                } else {
                    lineInfoArea.setText("Aucune donnée pour cette ligne.");
                }

            } catch (BadLocationException ex) {
                lineInfoArea.setText("Impossible de récupérer la ligne.");
            }
        });
    }

    // Panel en haut à droite
    private void setupTopRight(JSplitPane mainSplit) {
        // Récupération du panneau haut droit à partir du JSplitPane principal
        JPanel topRight = (JPanel) ((JSplitPane) mainSplit.getTopComponent()).getRightComponent();
 
        // Config panneau haut droite
        topRight.setLayout(new BorderLayout());
        topRightTextArea = new JTextArea();
        topRightTextArea.setEditable(false);
        topRightTextArea.setForeground(Color.WHITE);
        topRightTextArea.setBackground(backgroundColor);
        topRightTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        topRightTextArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPaneTopRight = new JScrollPane(topRightTextArea);
        topRight.add(scrollPaneTopRight, BorderLayout.CENTER);
        topRightTextArea.setText("texte en haut à droite");
    }

    // Panel en bas à droite
    private void setupBottomRight(JSplitPane mainSplit) {
        // Récupération du panneau haut droit à partir du JSplitPane principal
        JPanel bottomRight = (JPanel) ((JSplitPane) mainSplit.getBottomComponent()).getRightComponent();

        // Config panneau bas droite
        bottomRight.setLayout(new BorderLayout());
        bottomRightTextArea = new JTextArea();
        bottomRightTextArea.setEditable(false);
        bottomRightTextArea.setForeground(Color.WHITE);
        bottomRightTextArea.setBackground(backgroundColor);
        bottomRightTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        bottomRightTextArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPaneBottomRight = new JScrollPane(bottomRightTextArea);
        bottomRight.add(scrollPaneBottomRight, BorderLayout.CENTER);
        bottomRightTextArea.setText("texte en bas à droite");
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
                    Path tempInfoProgramPath = tempFolder.resolve(Paths.get(fullPathGCode).getFileName().toString() + "_program.csv");
                    Path tempInfoToolsPath = tempFolder.resolve(Paths.get(fullPathGCode).getFileName().toString() + "_tool.csv");

                    tempData = loadCSVToList(tempGCodePath.toFile()); // Charge toutes les colonnes
                    tempData2 = loadCSVToList(tempInfoProgramPath.toFile());
                    tempData3 = loadCSVToList(tempInfoToolsPath.toFile());

                    return null;
                }

                @Override
                protected void done() {
                    gcodeEditor.setText("");
                    for (String[] row : tempData) {
                        if (row.length > 0) {
                            gcodeEditor.append(row[0] + "\n");  // affiche uniquement la colonne 0 (GCode) dans l'éditeur
                        }
                    }
                    gcodeEditor.setCaretPosition(0); // affiche curseur gcode tout en haut
                    setCursor(Cursor.getDefaultCursor());
                    setupTopLeft(mainSplit);
                }
            };

            worker.execute();
        }
    }

    // Charge les données CSV dans une liste String sans toucher à l'UI (thread swing) --> évite les erreurs sporadique au chargement du GCode
    private java.util.List<String[]> loadCSVToList(File csvFile) {
        java.util.List<String[]> dataList = new java.util.ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Ignore entête
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                dataList.add(values);  // Ajoute la ligne complète (tableau de colonnes)
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

            setupTopLeft(mainSplit); // On recharge le panel pour affichage du nom du stl

            PythonCaller.runScript(fullPathGCode, fullPathSTL, pythonScriptPath, GCodeIsOpen); // Si un GCode est déjà ouvert on lance aussi le viewer
        }
    }

    private String formatDuration(String valueSeconds) {
        double totalSecondsDouble = Double.parseDouble(valueSeconds);

        if (totalSecondsDouble < 60.0) {
            // Détecter le nombre de décimales dans la chaîne d'origine
            int decimals = 0;
            int indexOfDot = valueSeconds.indexOf('.');
            if (indexOfDot >= 0) {
                decimals = valueSeconds.length() - indexOfDot - 1;
            }

            // Limiter les décimales à 3 max
            if (decimals == 1) {
                return String.format(Locale.US, "%.1f sec.", totalSecondsDouble);
            } else if (decimals == 2) {
                return String.format(Locale.US, "%.2f sec.", totalSecondsDouble);
            } else {
                return String.format(Locale.US, "%.3f sec.", totalSecondsDouble);
            }
        }

        int totalSeconds = (int) Math.round(totalSecondsDouble);

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) sb.append(hours).append("h ");
        if (minutes > 0 || hours > 0) sb.append(minutes).append("min. ");
        sb.append(seconds).append("sec.");

        return sb.toString().trim();
    }
}