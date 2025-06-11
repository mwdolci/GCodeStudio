import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Paths;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import java.nio.file.Path;

public class MainWindow extends JFrame {

    public boolean GCodeIsOpen = false;
    public String fullPathSTL = "";
    public String fullPathGCode = "";
    public String messageGCodeNotOpen = "Aucun fichier GCode ouvert !";
    private java.util.List<String[]> datasGCode;  // variable temporaire pour stocker toutes les colonnes CSV
    private java.util.List<String[]> datasProgram;
    private java.util.List<String[]> datasTools;
    private Path tempGCodePath;
    private Path tempInfoProgramPath;
    private Path tempInfoToolsPath;
    private JPanel welcomePanel;
    private JPanel ganttAndSliderPanel;
    private JPanel sliderPanel;
    public JMenu menuParameters;
    private JTextArea gcodeEditor;
    private JTextArea lineInfoArea;
    private JTextArea bottomLeftTextArea;
    private JTextArea bottomRightTextArea;
    private JSplitPane mainSplit;
    private JSlider timeSlider;
    private int selectedToolNumber = -1; // -1 = aucun outil sélectionné
    private int padding = 20;  // position à laquelle commencent les valeurs
    private LinearGanttPanel linearGanttPanel;
    private Color backgroundColor = new Color(244, 244, 244);
    private Color backgroundColorEditor;
    private Color textColor = Color.BLACK;
    private Color borderColor = Color.WHITE;
    private Color backgroundColorTopRight;
    private Color backgroundColorBottomLeft; 
    private Color backgroundColorBottomRight;
    public Viewer3D fileViewer3d;
    
    // Getter et setter pour couleurs
    public Color getBackgroundColor() { return backgroundColor;}
    public void setBackgroundColor(Color color) {this.backgroundColor = color;}
    public Color getBackgroundColorTopRight() {return backgroundColorTopRight;}
    public void setBackgroundColorTopRight(Color color) {this.backgroundColorTopRight = color;}
    public Color getBackgroundColorBottomLeft() {return backgroundColorBottomLeft;}
    public void setBackgroundColorBottomLeft(Color color) {this.backgroundColorBottomLeft = color;}
    public Color getBackgroundColorBottomRight() {return backgroundColorBottomRight;}
    public void setBackgroundColorBottomRight(Color color) {this.backgroundColorBottomRight = color;}
    public Color getBackgroundColorEditor() {return backgroundColorEditor;}
    public void setBackgroundColorEditor(Color color) {this.backgroundColorEditor = color;}
    public Color getTextColor() {return textColor;}
    public void setTextColor(Color color) {this.textColor = color;}

    // Getters pour les composants
    public JTextArea getLineInfoArea() {return lineInfoArea;}
    public JTextArea getBottomLeftTextArea() {return bottomLeftTextArea;}
    public JTextArea getBottomRightTextArea() {return bottomRightTextArea;}
    public LinearGanttPanel getLinearGanttPanel() {return linearGanttPanel;}
    public JPanel getSliderPanel() {return sliderPanel;}
    public JPanel getGanttAndSliderPanel() {return ganttAndSliderPanel;}
    public JTextArea getGcodeEditor() {return gcodeEditor;}

    public MainWindow() {
        super("GCodeStudio");
        fileViewer3d = new Viewer3D(this);
        initializeWindow();
        setJMenuBar(Menu.createMenuBar(this));
        welcomePanel = WelcomeWindow.createWelcomePanel(this::openGCodeFile);
        menuParameters.setEnabled(false); // Désactiver le menu paramètres tant que pas sur page principale
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
                                value = FormatUtils.formatDuration(value);
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
                    value = FormatUtils.formatDuration(value);
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
                String value = FormatUtils.shortenPath(fullPathSTL, numberOfCharacters); // Limite nombre de caractères affichés à l'écran

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
                    value = FormatUtils.shortenPath(value, numberOfCharacters); // Limite nombre de caractères affichés à l'écran
                } else if (label.startsWith("Durée")) {
                    value = FormatUtils.formatDuration(value);
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

    public void openGCodeFile() {

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

                datasGCode = CSVUtils.loadCSVToList(tempGCodePath.toFile()); // Charge toutes les colonnes
                datasProgram = CSVUtils.loadCSVToList(tempInfoProgramPath.toFile());
                datasTools = CSVUtils.loadCSVToList(tempInfoToolsPath.toFile());

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

    public void openSTLFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Ouvrir un fichier 3D");

        // Filtre pour limiter les fichiers affichés aux extensions reconnues
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Fichiers .stl", "stl"));

        int result = fileChooser.showOpenDialog(this); // this = parent JFrame

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            fullPathSTL = selectedFile.getAbsolutePath();
            
            if (GCodeIsOpen) {
                setupBottomRight(mainSplit); // On recharge le panel pour affichage du nom du stl
            }
        }
    }

    public void recalculation() {

        if (fullPathGCode != null && !fullPathGCode.isEmpty()) {
            File f = new File(fullPathGCode);
            if (f.exists()) {
                processLoadGCodeFile(fullPathGCode);
            } else {
                JOptionPane.showMessageDialog(this, "Fichier GCode déplacé ou inaccessible !");
            }
        } else {
            JOptionPane.showMessageDialog(this, messageGCodeNotOpen);
        }
    }
}