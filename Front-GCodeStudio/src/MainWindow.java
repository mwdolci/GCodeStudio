import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Paths;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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

    DefaultListModel<String> listModel = new DefaultListModel<>();  // Modèle liste pour stockage des données
    JList<String> itemList = new JList<>(listModel);                // Création de la liste pour affichage

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

                        loadCSVColumn(tempGCodePath.toFile());

                        return null;
                    }

                    @Override
                    protected void done() {
                        setCursor(Cursor.getDefaultCursor()); // Curseur normal
                    }
                };

                worker.execute();
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













        // Création d'une liste de string
        // DefaultListModel<String> listModel = new DefaultListModel<>();  // Modèle liste pour stockage des données
        // JList<String> itemList = new JList<>(listModel);                // Création de la liste pour affichage
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Mode sélection d'un seul élément à la fois

        // Scrollbar panel pour la liste
        JScrollPane scrollPane = new JScrollPane(itemList);
        //scrollPane.setPreferredSize(new Dimension(300, 400));

        // Zone de texte pour afficher ou éditer la ligne sélectionnée
        JTextField selectedItemField = new JTextField();
        //selectedItemField.setPreferredSize(new Dimension(280, 30));

        // Bouton pour modifier l'élément sélectionné
        JButton editButton = new JButton("Modifier");

        // Evénement bouton Modifier
        editButton.addActionListener(e -> {
            int indexListGCode = itemList.getSelectedIndex();
            if (indexListGCode >= 0) { // On fait quelque chose seulement si un élément est sélectionné
                listModel.set(indexListGCode, selectedItemField.getText()); // On remplace le texte d'origine
            }
        });

        // Ecoute jusqu'au clic dans la liste
        itemList.addListSelectionListener(new ListSelectionListener() { // Ecouteur d'événement à la liste
            public void valueChanged(ListSelectionEvent e) { // Méthode déclenché sur changement de sélection
                if (!e.getValueIsAdjusting()) { // !! sécurité pour n'avoir qu'un seul événement à la fois
                    String selected = itemList.getSelectedValue();  // Récupère le texte
                    selectedItemField.setText(selected);            // Affiche le texte
                }
            }
        });

        // Disposition des éléments
        bottomLeft.setLayout(new BorderLayout()); // 5 zones (Nord Sud Centre Est Ouest)
        bottomLeft.add(scrollPane, BorderLayout.CENTER); //Centre le G-Code

        JPanel bottomControls = new JPanel(); // sous conteneur
        bottomControls.setLayout(new BorderLayout());
        bottomControls.add(selectedItemField, BorderLayout.CENTER);
        bottomControls.add(editButton, BorderLayout.EAST);

        bottomLeft.add(bottomControls, BorderLayout.SOUTH);

    }

    public void loadCSVColumn(File csvFile) {
        listModel.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            String header = br.readLine(); // pour ne pas charger l'entête de colonne

            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); // Séparateur
                if (values.length > 0) {
                    listModel.addElement(values[0]); // Ajoute la 1re colonne
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erreur de lecture du fichier CSV.");
        }
    }
}