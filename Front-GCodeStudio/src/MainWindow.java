import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Locale;
import java.nio.file.Path;

public class MainWindow extends JFrame {

    private boolean GCodeIsOpen = false;
    private boolean STLIsOpen = false;
    private String fullPathSTL = "";
    private String fullPathGCode = "";
    private java.util.List<String[]> datasGCode;  // variable temporaire pour stocker toutes les colonnes CSV
    private java.util.List<String[]> datasProgram;
    private java.util.List<String[]> datasTools;
    private Path tempGCodePath;
    private Path tempInfoProgramPath;
    private Path tempInfoToolsPath;
    private JPanel welcomePanel;
    private JTextArea gcodeEditor;
    private JTextArea lineInfoArea;
    private JTextArea bottomLeftTextArea;
    private JTextArea bottomRightTextArea;
    private JSplitPane mainSplit;
    private int selectedToolNumber = -1; // -1 = aucun outil sélectionné
    Color backgroundColor = new Color(30, 30, 30);
    Color borderColor = Color.WHITE;

    DefaultListModel<String> listModel = new DefaultListModel<>();  // Modèle liste pour stockage des données
    JList<String> itemList = new JList<>(listModel);                // Création de la liste pour affichage

    public MainWindow() {
        super("GCodeStudio");
        initializeWindow();
        setupMenu();
        setupWelcomePanel();
        getContentPane().removeAll();
        getContentPane().add(welcomePanel, BorderLayout.CENTER);
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
        menuFile.add(itemOpenGCode);
        menuFile.add(itemOpenSTL);

        itemOpenGCode.addActionListener(e -> openGCodeFile());
        itemOpenSTL.addActionListener(e -> openSTLFile());
        
        // *Liste Fonctions*
        JMenu menuFunctions = new JMenu("Fonctions");
        JMenuItem itemCalculate = new JMenuItem("Recalculer");
        JMenuItem itemViewer3D = new JMenuItem("Lancer le viewer 3D");
        menuFunctions.add(itemCalculate);
        menuFunctions.add(itemViewer3D);

        itemCalculate.addActionListener(e -> recalculation());
        itemViewer3D.addActionListener(e -> startViewer3D());

        // *Liste Aide*
        JMenu menuHelp = new JMenu("Aide");
        JMenuItem itemOpenHelpPDF = new JMenuItem("Aide");
        JMenuItem itemOpenWindowAbout = new JMenuItem("A propos");
        menuHelp.add(itemOpenHelpPDF);
        menuHelp.add(itemOpenWindowAbout);

        itemOpenHelpPDF.addActionListener(e -> openHelpPDF());
        itemOpenWindowAbout.addActionListener(e -> openAboutWindow());

        menuBar.add(menuFile);
        menuBar.add(menuFunctions);
        menuBar.add(menuHelp);

        setJMenuBar(menuBar);
    }

    private void setupWelcomePanel() {
        welcomePanel = new JPanel() {
            // Redéfinition de paintComponent pour le fond dégradé
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();

                // Anti-aliasing pour un rendu plus lisse
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                // Dégradé vertical du sombre au moins sombre
                float[] fractions = {0.0f, 1.0f};
                Color[] colors = {new Color(30, 30, 30), new Color(70, 70, 70)};

                LinearGradientPaint lgp = new LinearGradientPaint(0, 0, 0, height, fractions, colors);

                g2d.setPaint(lgp);
                g2d.fillRect(0, 0, width, height);

                g2d.dispose();
            }
        };
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS)); // disposition verticale

        JLabel welcomeLabel = new JLabel("Bienvenue dans GCode Studio", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 48));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // centrer dans BoxLayout

        JLabel startLabel = new JLabel("Ouvrez un programme pour commencer", SwingConstants.CENTER);
        startLabel.setFont(new Font("Arial", Font.BOLD, 24));
        startLabel.setForeground(Color.WHITE);
        startLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton openFileButton = new JButton("+");
        openFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        openFileButton.setFont(new Font("Arial", Font.BOLD, 92));
        openFileButton.setBorder(BorderFactory.createEmptyBorder()); //enlever la bordure
        openFileButton.setFocusPainted(false);
        openFileButton.setToolTipText("Ouvrir GCode");
        openFileButton.setBackground(Color.GRAY); 
        openFileButton.setForeground(Color.BLACK);
        openFileButton.setOpaque(true);            

        // Bouton carré
        int buttonSize = 100;
        openFileButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        openFileButton.setMaximumSize(new Dimension(buttonSize, buttonSize));
        openFileButton.setMinimumSize(new Dimension(buttonSize, buttonSize));

        // Effet au survol
        openFileButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                openFileButton.setBackground(openFileButton.getBackground().brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                openFileButton.setBackground(Color.GRAY);
            }
        });

        openFileButton.addActionListener(e -> openGCodeFile());

        welcomePanel.add(Box.createVerticalGlue());
        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        welcomePanel.add(startLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 50)));
        welcomePanel.add(openFileButton);
        welcomePanel.add(Box.createVerticalGlue());
    }

    // 3 Panels
    private JSplitPane setupPanels() {
        // Création des 3 panneaux
        JPanel bottomLeft = new JPanel();
        JPanel bottomRight = new JPanel();
        JPanel top = new JPanel();

        // Apparence des 3 panneaux
        JPanel[] panels = { bottomLeft, bottomRight, top };
        for (JPanel panel : panels) {
            panel.setBackground(backgroundColor);
            panel.setBorder(BorderFactory.createLineBorder(borderColor, 1));
        }

        // Split bas (gauche/droite)
        JSplitPane splitBottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bottomRight, bottomLeft);
        splitBottom.setResizeWeight(0.5);   // moitié-moitié
        splitBottom.setDividerSize(2);    // épaisseur de la ligne
        splitBottom.setBorder(null);

        // Top
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(top, BorderLayout.CENTER);

        // Split principal (haut/bas)
        mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, splitBottom);
        mainSplit.setResizeWeight(0.6); // Partie haute 60%
        mainSplit.setDividerSize(2);
        mainSplit.setBorder(null);

        // Ajouter le split principal à la fenêtre
        getContentPane().add(mainSplit, BorderLayout.CENTER);

        return mainSplit;
    }


    // Panel en bas à droite
    private void setupBottomRight(JSplitPane mainSplit) {
        // Récupération du panneau bas droite à partir du JSplitPane principal
        JPanel bottomRight = (JPanel) ((JSplitPane) mainSplit.getBottomComponent()).getRightComponent();

        bottomRight.removeAll();
        
        // Config panneau bas droite
        bottomRight.setLayout(new BorderLayout());
        bottomLeftTextArea = new JTextArea();
        bottomLeftTextArea.setEditable(false);
        bottomLeftTextArea.setForeground(Color.WHITE);
        bottomLeftTextArea.setBackground(backgroundColor);
        bottomLeftTextArea.setFont(new Font("Monospaced", Font.PLAIN, 18));
        bottomLeftTextArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPaneBottomLeft = new JScrollPane(bottomLeftTextArea);
        bottomRight.add(scrollPaneBottomLeft, BorderLayout.CENTER);

        if (datasProgram == null || datasProgram.isEmpty()) {
            bottomLeftTextArea.setText("Aucune donnée disponible.");
            return;
        }

        String[] row = datasProgram.get(0);

        String[] labels = {"Fichier G-Code", "Fichier 3D", "", "", "Nombre de lignes", "Durée du programme", "Durée productive", "Durée imporductive"};
        int[] columnIndices = {0, -2, -1, -1, 2, 3, 4, 5};

        int padding = 20;
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < labels.length; i++) {
            if (columnIndices[i] == -1) {
                builder.append("\n");  // ligne vide
            } else if (columnIndices[i] == -2) {
                // Cas spécial : on récupère le chemin du stl si chargé
                String label = labels[i];
                String value = fullPathSTL;

                if (value == "") {value = "-";}
                
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

        bottomLeftTextArea.setText(builder.toString());

        // !! Forcer le rafraichissement de la page sinon fonctionne pas
        bottomRight.revalidate();
        bottomRight.repaint();
    }

    // Panel en haut
    private void setupTop(JSplitPane mainSplit) {
        // Colonne gauche = éditeur G-code + infos
        JSplitPane leftSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        leftSplit.setDividerSize(2);

        // JPanel du haut
        JPanel topPanel = (JPanel) mainSplit.getTopComponent();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(leftSplit, BorderLayout.CENTER);

        // Éditeur G-code (gauche)
        gcodeEditor = new JTextArea();
        gcodeEditor.setFont(new Font("Monospaced", Font.PLAIN, 14));
        gcodeEditor.setLineWrap(false); // Pas de retour à la ligne automatique
        gcodeEditor.setWrapStyleWord(false);
        gcodeEditor.setMargin(new Insets(5, 10, 5, 5));
        gcodeEditor.setBackground(Color.WHITE);
        gcodeEditor.setEditable(false); // Verrouiller dans version 1.0
        JScrollPane gcodeScrollPane = new JScrollPane(gcodeEditor);

        // Infos ligne (droite)
        lineInfoArea = new JTextArea();
        lineInfoArea.setEditable(false);
        lineInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 18));
        lineInfoArea.setBackground(backgroundColor);
        lineInfoArea.setForeground(Color.WHITE);
        lineInfoArea.setMargin(new Insets(10, 10, 10, 10));
        lineInfoArea.getCaret().setVisible(false);
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
                if (datasGCode == null) {
                    lineInfoArea.setText("Aucune donnée chargée.");
                    return;
                }

                int caretPos = gcodeEditor.getCaretPosition();
                int lineNum = gcodeEditor.getLineOfOffset(caretPos);

                // Surlignage ligne active
                gcodeEditor.getHighlighter().removeAllHighlights();
                int startOffset = gcodeEditor.getLineStartOffset(lineNum);
                int endOffset = gcodeEditor.getLineEndOffset(lineNum);
                gcodeEditor.getHighlighter().addHighlight(startOffset, endOffset, new DefaultHighlighter.DefaultHighlightPainter(new Color(150, 240, 210)));

                if (lineNum >= 0 && lineNum < datasGCode.size()) {
                    String[] row = datasGCode.get(lineNum);
                    StringBuilder details = new StringBuilder();

                    //Substitution textes
                    if (row.length > 2 && "RAPID_MOVE".equals(row[2])) {row[2] = "Rapide";}
                    if (row.length > 2 && "LINEAR_MOVE".equals(row[2])) {row[2] = "Linéaire";}
                    if (row.length > 2 && "CIRCULAR_MOVE_CW".equals(row[2])) {row[2] = "Circulaire (horaire)";}
                    if (row.length > 2 && "CIRCULAR_MOVE_CCW".equals(row[2])) {row[2] = "Circulaire (anti-horaire)";}

                    // Nom des champs
                    String[] labels = {"N° outil", "", "Temps", "Durée", "", "Avance", "Rotation", "", "Mouvement", "Distance", "Position X", "Position Y", "Position Z", "Rayon"};
                    int[] columnIndices = {
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

                    int padding = 30;  // position à laquelle commencent les valeurs

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

	// Panel en bas à gauche
	private void setupBottomLeft(JSplitPane mainSplit) {
        JPanel bottomLeft = (JPanel) ((JSplitPane) mainSplit.getBottomComponent()).getLeftComponent();
		
        bottomLeft.removeAll();
        
        bottomLeft.setLayout(new BorderLayout());

		bottomLeftTextArea = new JTextArea();
		bottomLeftTextArea.setEditable(false);
		bottomLeftTextArea.setForeground(Color.WHITE);
		bottomLeftTextArea.setBackground(backgroundColor);
		bottomLeftTextArea.setFont(new Font("Monospaced", Font.PLAIN, 18));
		bottomLeftTextArea.setMargin(new Insets(10, 10, 10, 10));
        bottomLeftTextArea.setMinimumSize(new Dimension(60, 100));
		JScrollPane scrollPaneBottomLeft = new JScrollPane(bottomLeftTextArea);

		LinearGanttPanel linearGanttPanel = new LinearGanttPanel(tempInfoToolsPath.toString());
        linearGanttPanel.setMinimumSize(new Dimension(60, 100));
        linearGanttPanel.setActiveToolNumber(selectedToolNumber);
        
        // Écouteur pour la sélection d'outil dans le diagramme de Gantt
        linearGanttPanel.setToolSelectionListener(toolNumber -> {
            selectedToolNumber = toolNumber;
            setupBottomLeft(mainSplit);
        });

        if (datasTools == null || datasTools.isEmpty()) {
            bottomLeftTextArea.setText("Aucune donnée disponible.");
            return;
        }

        String[] row = null;
        if (selectedToolNumber == -1) {
            row = datasTools.get(0); // Par défaut, premier outil
        } else {
            for (String[] r : datasTools) {
                if (r.length > 0 && Integer.toString(selectedToolNumber).equals(r[0])) {
                    row = r;
                    break;
                }
            }
            if (row == null) row = datasTools.get(0); // fallback si non trouvé
        }

        String[] labels = {"Numéro outil", "", "Durée", "Durée productive", "Durée improductive", "", "Distance", "Distance en matière"};
        int[] columnIndices = {0, -1, 1, 2, 3, -1, 4, 5};

        int padding = 20;
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < labels.length; i++) {
            if (columnIndices[i] == -1) {
                builder.append("\n");  // ligne vide
            } else if (columnIndices[i] < row.length) {
                String label = labels[i];
                String value = row[columnIndices[i]];

                if (label.startsWith("Durée")) {
                    value = formatDuration(value);
                }
                else if (label.startsWith("Distance")) {
                    value = value + " mm";
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

        bottomLeftTextArea.setText(builder.toString());

		// SplitPane invisible
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPaneBottomLeft, linearGanttPanel);
		split.setResizeWeight(0.8); // 80% texte, 20% diagramme
		split.setDividerSize(0);   // Enlève la barre de séparation
		split.setEnabled(false);   // Désactive la possibilité de la déplacer

		// Supprimer les bordures
		scrollPaneBottomLeft.setBorder(null);
		linearGanttPanel.setBorder(null);
		linearGanttPanel.setBackground(backgroundColor);

		bottomLeft.add(split, BorderLayout.CENTER);

        // !! Forcer le rafraichissement de la page sinon fonctionne pas
        bottomLeft.revalidate();
        bottomLeft.repaint();
	}

    private void openGCodeFile() {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Ouvrir un fichier GCode");

        // Filtre pour limiter les fichiers affichés aux extensions reconnues
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Fichiers .anc .nc .txt", "anc", "nc", "txt"));

        int result = fileChooser.showOpenDialog(this); // this = parent JFrame

        if (result == JFileChooser.APPROVE_OPTION) {

            // Première construction graphique de la fenêtre principale
            if (mainSplit == null) {
                 mainSplit = setupPanels();
                 //setupTopLeft(mainSplit);
                 setupTop(mainSplit);
                 //setupTopRight(mainSplit);
                 //setupBottomRight(mainSplit);
             }

            File selectedFile = fileChooser.getSelectedFile();
            fullPathGCode = selectedFile.getAbsolutePath();
            processLoadGCodeFile(fullPathGCode);
        }
    }

    private void processLoadGCodeFile(String filePath){
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)); // Sablier

        // Travail en arrière-plan
        SwingWorker<Void, Void> worker = new SwingWorker<>() { // Classe absraite pour tâche en arrière plan sans figer swing
            @Override
            protected Void doInBackground() throws Exception {
                String currentDir = Paths.get("").toAbsolutePath().toString();
                String pythonScriptPath = Paths.get(currentDir, "..", "..", "Back-GCodeStudio", "main.py").normalize().toString();

                GCodeIsOpen = true;

                PythonCaller.runScript(fullPathGCode, fullPathSTL, pythonScriptPath, false);

                Path tempFolder = Paths.get(System.getenv().getOrDefault("TEMP", "/tmp"));
                tempGCodePath = tempFolder.resolve(Paths.get(fullPathGCode).getFileName().toString() + "_gcode.csv");
                tempInfoProgramPath = tempFolder.resolve(Paths.get(fullPathGCode).getFileName().toString() + "_program.csv");
                tempInfoToolsPath = tempFolder.resolve(Paths.get(fullPathGCode).getFileName().toString() + "_tool.csv");

                datasGCode = loadCSVToList(tempGCodePath.toFile()); // Charge toutes les colonnes
                datasProgram = loadCSVToList(tempInfoProgramPath.toFile());
                datasTools = loadCSVToList(tempInfoToolsPath.toFile());

                return null;
            }

            @Override
            protected void done() {
                gcodeEditor.setText("");
                for (String[] row : datasGCode) {
                    if (row.length > 0) {
                        gcodeEditor.append(row[0] + "\n");  // affiche uniquement la colonne 0 (GCode) dans l'éditeur
                    }
                }
                getContentPane().removeAll();
                getContentPane().add(mainSplit, BorderLayout.CENTER);
                revalidate();
                repaint();

                gcodeEditor.setCaretPosition(0); // affiche curseur gcode tout en haut
                setCursor(Cursor.getDefaultCursor());
                
                setupBottomRight(mainSplit);
                setupBottomLeft(mainSplit);
            }
        };

        worker.execute();
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

            setupBottomRight(mainSplit); // On recharge le panel pour affichage du nom du stl
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

    private void recalculation() {
        STLIsOpen = false; // Pour ne pas ouvrir le viwer sur recalcul

        if (fullPathGCode != null && !fullPathGCode.isEmpty()) {
            File f = new File(fullPathGCode);
            if (f.exists()) {
                processLoadGCodeFile(fullPathGCode);
            } else {
                JOptionPane.showMessageDialog(this, "Fichier GCode déplacé ou inaccessible !");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Aucun fichier GCode chargé !");
        }
    }

    private void openHelpPDF() {
        String currentDir = Paths.get("").toAbsolutePath().toString();
        String helpFilePath = Paths.get(currentDir, "..", "..", "doc", "user_manual_fr.pdf").normalize().toString();

        File helpFile = new File(helpFilePath);
        if (helpFile.exists()) {
            try {
                Desktop.getDesktop().open(helpFile);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Impossible d'ouvrir le fichier d'aide.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Fichier d'aide introuvable.");
        }
    }

    private void openAboutWindow() {
        String aboutText = "GCode Studio V1.0\n\n" +
                "Développé par Dolci Marco & Toussaint Guillaume\n" +
                "Dans le cadre de la formation MAS-RAD à la HE-Arc Ingénierie (Neuchâtel)\n\n" +
                "Ce logiciel est gratuit et peut être utilisé librement à vos propres risques.\n" +
                "Les auteurs ne peuvent être tenus responsables de tout dommage direct ou indirect\n" +
                "résultant de son utilisation.\n\n" +
                "Aucune licence spécifique n'est appliquée.";

        JOptionPane.showMessageDialog(this, aboutText, "À propos", JOptionPane.INFORMATION_MESSAGE);
    }

    private void startViewer3D() {
        if (GCodeIsOpen) {
                String currentDir = Paths.get("").toAbsolutePath().toString();
                String pythonScriptPath = Paths.get(currentDir, "..", "..", "Back-GCodeStudio", "main.py").normalize().toString();
                PythonCaller.runScript(fullPathGCode, fullPathSTL, pythonScriptPath, GCodeIsOpen);
            } else {
                JOptionPane.showMessageDialog(this, "Aucun fichier GCode ouvert !");
        }
    }
}