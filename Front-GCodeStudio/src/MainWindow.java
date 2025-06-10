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
    private String fullPathSTL = "";
    private String fullPathGCode = "";
    private java.util.List<String[]> datasGCode;  // variable temporaire pour stocker toutes les colonnes CSV
    private java.util.List<String[]> datasProgram;
    private java.util.List<String[]> datasTools;
    private Path tempGCodePath;
    private Path tempInfoProgramPath;
    private Path tempInfoToolsPath;
    private JPanel welcomePanel;
    private JPanel ganttAndSliderPanel;
    private JPanel sliderPanel;
    private JMenu menuParameters;
    private JTextArea gcodeEditor;
    private JTextArea lineInfoArea;
    private JTextArea bottomLeftTextArea;
    private JTextArea bottomRightTextArea;
    private JSplitPane mainSplit;
    private JSlider timeSlider;
    private int selectedToolNumber = -1; // -1 = aucun outil sélectionné
    private LinearGanttPanel linearGanttPanel;
    private Color backgroundColor = new Color(244, 244, 244);
    private Color backgroundColorEditor;
    private Color textColor = Color.BLACK;
    private Color borderColor = Color.WHITE;
    private Color backgroundColorTopRight;
    private Color backgroundColorBottomLeft; 
    private Color backgroundColorBottomRight;
    private int padding = 20;  // position à laquelle commencent les valeurs

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
        JMenuItem itemViewer3D = new JMenuItem("Simulation 3D");
        menuFunctions.add(itemCalculate);
        menuFunctions.add(itemViewer3D);

        itemCalculate.addActionListener(e -> recalculation());
        itemViewer3D.addActionListener(e -> startViewer3D());

        // *Liste Paramètres*
        menuParameters = new JMenu("Paramètres");
        JMenuItem itemTheme = new JMenuItem("Thème");
        menuParameters.add(itemTheme);
        itemTheme.addActionListener(e -> showThemeSelectionDialog());

        // *Liste Aide*
        JMenu menuHelp = new JMenu("Aide");
        JMenuItem itemOpenHelpPDF = new JMenuItem("Manuel utilisateur");
        JMenuItem itemOpenTutorialMovie = new JMenuItem("Tutoriel vidéo");
        JMenuItem itemOpenWindowAbout = new JMenuItem("A propos");
        menuHelp.add(itemOpenHelpPDF);
        menuHelp.add(itemOpenTutorialMovie);
        menuHelp.add(itemOpenWindowAbout);

        itemOpenHelpPDF.addActionListener(e -> openHelpPDF());
        itemOpenTutorialMovie.addActionListener(e -> openTutorialMovie());
        itemOpenWindowAbout.addActionListener(e -> AboutWindow.openAboutWindow(this));

        menuBar.add(menuFile);
        menuBar.add(menuFunctions);
        menuBar.add(menuParameters);
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
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // centrer dans BoxLayout

        JLabel startLabel = new JLabel("Ouvrez un programme pour commencer", SwingConstants.CENTER);
        startLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
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

        if (menuParameters != null) {menuParameters.setEnabled(false); } // Désactiver le menu paramètres sur la page d'accueil
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
        JSplitPane splitBottom = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bottomLeft, bottomRight);
        splitBottom.setResizeWeight(0.83); 
        splitBottom.setDividerSize(2);    // épaisseur de la ligne
        splitBottom.setBorder(null);

        // Top
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(top, BorderLayout.CENTER);

        // Split principal (haut/bas)
        mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, splitBottom);
        mainSplit.setResizeWeight(0.5); // Partie haute 50%
        mainSplit.setDividerSize(2);
        mainSplit.setBorder(null);

        // Ajouter le split principal à la fenêtre
        getContentPane().add(mainSplit, BorderLayout.CENTER);

        return mainSplit;
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
        gcodeEditor.setFont(new Font("Monospaced", Font.PLAIN, 20));
        gcodeEditor.setLineWrap(false); // Pas de retour à la ligne automatique
        gcodeEditor.setWrapStyleWord(false);
        gcodeEditor.setMargin(new Insets(5, 10, 5, 5));
        gcodeEditor.setBackground(Color.WHITE);
        gcodeEditor.setEditable(false); // Verrouiller dans version 1.0
        JScrollPane gcodeScrollPane = new JScrollPane(gcodeEditor);

        // Infos ligne (droite)
        lineInfoArea = new JTextArea();
        lineInfoArea.setEditable(false);
        lineInfoArea.setFont(new Font("Consolas", Font.PLAIN, 18));
        lineInfoArea.setBackground(backgroundColor);
        lineInfoArea.setForeground(textColor);
        lineInfoArea.setMargin(new Insets(10, 10, 2, 10));
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
                    int[] columnIndices = {1, -1, 11, 10, -1, 9, 12, -1, 2, 7, 3, 4, 5, 6};

                    for (int i = 0; i < labels.length; i++) {
                        if (columnIndices[i] == -1) {
                            details.append("\n");  // ligne vide
                        } else if (columnIndices[i] < row.length) {
                            String label = labels[i];
                            String value = row[columnIndices[i]];

                            if (linearGanttPanel != null && timeSlider != null)  {
                                double currentTime = Double.parseDouble(row[11]);
                                linearGanttPanel.setCurrentTime(currentTime); // Déplace la ligne dans le Gantt
                                double totalTime = linearGanttPanel.getTotalTime();
                                int sliderValue = (int) ((currentTime / totalTime) * 1000);
                                timeSlider.setValue(sliderValue); // Déplace le curseur dans le Gantt
                            }

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
		bottomLeftTextArea.setForeground(textColor);
		bottomLeftTextArea.setBackground(backgroundColor);
		bottomLeftTextArea.setFont(new Font("Consolas", Font.PLAIN, 18));
		bottomLeftTextArea.setMargin(new Insets(10, 10, 2, 10));
        bottomLeftTextArea.setMinimumSize(new Dimension(60, 100));
		JScrollPane scrollPaneBottomLeft = new JScrollPane(bottomLeftTextArea);

        // Création du LinearGanttPanel
        if (linearGanttPanel == null) {
            linearGanttPanel = new LinearGanttPanel(tempInfoToolsPath.toString());
        } else {
            linearGanttPanel.loadCSV(tempInfoToolsPath.toString());
        }
        linearGanttPanel.setMinimumSize(new Dimension(60, 100));
        linearGanttPanel.setActiveToolNumber(selectedToolNumber);

        // Création du slider
        if (timeSlider == null) {
            timeSlider = new JSlider(0, 1000, 0);
            timeSlider.setPreferredSize(new Dimension(200, 40));
            timeSlider.setBackground(backgroundColor);
            timeSlider.setOpaque(false);
            timeSlider.setEnabled(false); // Désactive le slider pour l'utilisateur
        }

        // Slider qui met à jour la ligne de temps dans le Gantt
        timeSlider.addChangeListener(e -> {
            double percent = timeSlider.getValue() / 1000.0;
            double currentTime = percent * linearGanttPanel.getTotalTime();
            linearGanttPanel.setCurrentTime(currentTime);
        });
        
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

        // Panel contenant le gantt
        sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.setBorder(BorderFactory.createEmptyBorder(0, 2, 5, 5));
        sliderPanel.setBackground(backgroundColor);
        sliderPanel.add(timeSlider, BorderLayout.CENTER);

        // Panel contenant le gantt et le slider
        ganttAndSliderPanel = new JPanel(new BorderLayout());
        ganttAndSliderPanel.add(sliderPanel, BorderLayout.NORTH);
        ganttAndSliderPanel.add(linearGanttPanel, BorderLayout.CENTER);
        
        // SplitPane invisible, avec le texte en haut et gantt+slider en bas
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPaneBottomLeft, ganttAndSliderPanel);
        split.setResizeWeight(0.8);
        split.setDividerSize(0);
        split.setEnabled(false);

		// Supprimer les bordures
		scrollPaneBottomLeft.setBorder(null);
		linearGanttPanel.setBorder(null);
		linearGanttPanel.setBackground(backgroundColor);

		bottomLeft.add(split, BorderLayout.CENTER);

        bottomLeft.revalidate();
        bottomLeft.repaint();
	}

    // Panel en bas à droite
    private void setupBottomRight(JSplitPane mainSplit) {
        // Récupération du panneau bas droite à partir du JSplitPane principal
        JPanel bottomRight = (JPanel) ((JSplitPane) mainSplit.getBottomComponent()).getRightComponent();

        bottomRight.removeAll();
        
        // Config panneau bas droite
        bottomRight.setLayout(new BorderLayout());
        bottomRightTextArea = new JTextArea();
        bottomRightTextArea.setEditable(false);
        bottomRightTextArea.setForeground(textColor);
        bottomRightTextArea.setBackground(backgroundColor);
        bottomRightTextArea.setFont(new Font("Consolas", Font.PLAIN, 18));
        bottomRightTextArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPaneBottomRight = new JScrollPane(bottomRightTextArea);
        bottomRight.add(scrollPaneBottomRight, BorderLayout.CENTER);

        if (datasProgram == null || datasProgram.isEmpty()) {
            bottomRightTextArea.setText("Aucune donnée disponible.");
            return;
        }

        String[] row = datasProgram.get(0);

        String[] labels = {"Fichier G-Code", "Fichier 3D", "", "", "Nombre de lignes", "Durée du programme", "Durée productive", "Durée imporductive"};
        int[] columnIndices = {0, -2, -1, -1, 2, 3, 4, 5};

        int numberOfCharacters = 30; // Nombre de caractères maximum pour le nom du fichier
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < labels.length; i++) {
            if (columnIndices[i] == -1) {
                builder.append("\n");  // ligne vide
            } else if (columnIndices[i] == -2) {
                // Cas spécial : on récupère le chemin du stl si chargé
                String label = labels[i];
                //String value = fullPathSTL;
                String value = shortenPath(fullPathSTL, numberOfCharacters); // Limite nombre de caractères affichés à l'écran

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

                if (label.contains("Fichier")) {
                    value = shortenPath(value, numberOfCharacters); // Limite nombre de caractères affichés à l'écran
                } else if (label.startsWith("Durée")) {
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

        bottomRightTextArea.setText(builder.toString());

        bottomRight.revalidate();
        bottomRight.repaint();
    }

    private void openGCodeFile() {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Ouvrir un fichier GCode");

        // Filtre pour limiter les fichiers affichés aux extensions reconnues
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Fichiers .anc .nc .txt", "anc", "nc", "txt"));

        int result = fileChooser.showOpenDialog(this); // this = parent JFrame

        if (result == JFileChooser.APPROVE_OPTION) {

            // Première construction graphique
            if (mainSplit == null) {
                mainSplit = setupPanels();
                setupTop(mainSplit);
            }

            File selectedFile = fileChooser.getSelectedFile();
            fullPathGCode = selectedFile.getAbsolutePath();
            processLoadGCodeFile(fullPathGCode);
        }
    }

    private void processLoadGCodeFile(String filePath){
        System.out.println("Charge depuis MainWindow");
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

                if (menuParameters != null) {menuParameters.setEnabled(true); } // Réactiver le menu paramètres après chargement du GCode
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
        if (hours > 0) sb.append(hours).append(" h ");
        if (minutes > 0 || hours > 0) sb.append(minutes).append(" min. ");
        sb.append(seconds).append(" sec.");

        return sb.toString().trim();
    }

    private void recalculation() {

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

    private void openTutorialMovie() {
        String currentDir = Paths.get("").toAbsolutePath().toString();
        String tutorialPath = Paths.get(currentDir, "..", "..", "doc", "tutorial.mp4").normalize().toString();

        File tutorialFile = new File(tutorialPath);
        if (tutorialFile.exists()) {
            try {
                Desktop.getDesktop().open(tutorialFile);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Impossible d'ouvrir le tutoriel vidéo.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Fichier de tutoriel introuvable.");
        }
    }

    private void startViewer3D() {
        if (GCodeIsOpen) {
            new Thread(() -> { // Lance le script Python dans un thread séparé --> évite de bloquer l'UI
                String currentDir = Paths.get("").toAbsolutePath().toString();
                String pythonScriptPath = Paths.get(currentDir, "..", "..", "Back-GCodeStudio", "main.py").normalize().toString();
                PythonCaller.runScript(fullPathGCode, fullPathSTL, pythonScriptPath, GCodeIsOpen);
            }).start();
        } else {
            JOptionPane.showMessageDialog(this, "Aucun fichier GCode ouvert !");
        }
    }

    private String shortenPath(String path, int maxLength) {
        if (path == null || path.length() <= maxLength) {
            return path;
        }

        path = path.replace("\\", "/"); // Normalise les séparateurs : remplace tous les \ par /

        String fileName = new File(path).getName();

        if (fileName.length() + 5 >= maxLength) {
            return "..." + fileName.substring(fileName.length() - (maxLength - 3));
        }

        int keepLength = maxLength - fileName.length() - 5; // 5 pour /.../
        String start = path.substring(0, Math.min(keepLength, path.length()));
        
        start = start.replaceAll("/+$", ""); // Supprime les éventuels / en trop à la fin

        return start + "/.../" + fileName;
    }

    //*************************************************************************************
    // #region : Gestion des thèmes 
    //*************************************************************************************
    private void showThemeSelectionDialog() {
        JDialog dialog = new JDialog(this, "Sélection du thème", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(this);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JRadioButton lightButton = new JRadioButton("Light");
        JRadioButton darkButton = new JRadioButton("Dark");
        JRadioButton harlequinButton = new JRadioButton("Harlequin");

        // Sélection actuelle
        if (backgroundColor.equals(new Color(34, 34, 34))) {
            darkButton.setSelected(true);
        } else if (backgroundColor.equals(new Color(0, 120, 255))) {
            harlequinButton.setSelected(true);
        } else {
            lightButton.setSelected(true);
        }

        ButtonGroup group = new ButtonGroup();
        group.add(lightButton);
        group.add(darkButton);
        group.add(harlequinButton);

        centerPanel.add(lightButton);
        centerPanel.add(darkButton);
        centerPanel.add(harlequinButton);

        JPanel buttonPanel = new JPanel();
        JButton applyButton = new JButton("Appliquer");
        JButton cancelButton = new JButton("Annuler");

        applyButton.addActionListener(e -> {
            if (lightButton.isSelected()) {
                applyLightTheme();
            } else if (darkButton.isSelected()) {
                applyDarkTheme();
            } else if (harlequinButton.isSelected()) {
                applyHarlequinTheme();
            }
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);

        dialog.add(centerPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void applyLightTheme() {
        backgroundColor = new Color(244, 244, 244);
        backgroundColorTopRight = backgroundColor;
        backgroundColorBottomLeft = backgroundColor;
        backgroundColorBottomRight = backgroundColor;
        backgroundColorEditor = Color.WHITE;
        textColor = Color.BLACK;
        applyThemeColors();
    }

    private void applyDarkTheme() {
        backgroundColor = new Color(34, 34, 34);
        backgroundColorTopRight = backgroundColor;
        backgroundColorBottomLeft = backgroundColor;
        backgroundColorBottomRight = backgroundColor;
        backgroundColorEditor = new Color(200, 200, 200);
        textColor = Color.WHITE;
        applyThemeColors();
    }

    private void applyHarlequinTheme() {
        backgroundColor = new Color(0, 120, 255);
        backgroundColorTopRight = new Color(140, 180, 255);
        backgroundColorBottomLeft = new Color(255, 170, 200);
        backgroundColorBottomRight = new Color(150, 230, 150);
        backgroundColorEditor = new Color(255, 250, 210);
        textColor = Color.BLACK;
        applyThemeColors();
    }

    private void applyThemeColors() {
        getContentPane().setBackground(backgroundColor);
        lineInfoArea.setBackground(backgroundColorTopRight);
        lineInfoArea.setForeground(textColor);
        bottomLeftTextArea.setBackground(backgroundColorBottomLeft);
        bottomLeftTextArea.setForeground(textColor);
        bottomRightTextArea.setBackground(backgroundColorBottomRight);
        bottomRightTextArea.setForeground(textColor);
        linearGanttPanel.setBackground(backgroundColorBottomLeft);
        sliderPanel.setBackground(backgroundColorBottomLeft);
        ganttAndSliderPanel.setBackground(backgroundColorBottomLeft);
        gcodeEditor.setBackground(backgroundColorEditor);
        revalidate();
        repaint();
    }
}