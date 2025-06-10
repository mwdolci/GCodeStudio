import javax.swing.*;

public class Menu {
    public static JMenuBar createMenuBar(MainWindow window) {
        JMenuBar menuBar = new JMenuBar();

        // Fichier
        JMenu menuFile = new JMenu("Fichier");
        JMenuItem itemOpenGCode = new JMenuItem("Ouvrir GCode");
        JMenuItem itemOpenSTL = new JMenuItem("Ouvrir 3D");
        menuFile.add(itemOpenGCode);
        menuFile.add(itemOpenSTL);

        itemOpenGCode.addActionListener(e -> window.openGCodeFile());
        itemOpenSTL.addActionListener(e -> window.openSTLFile());

        // Fonctions
        JMenu menuFunctions = new JMenu("Fonctions");
        JMenuItem itemCalculate = new JMenuItem("Recalculer");
        JMenuItem itemViewer3D = new JMenuItem("Simulation 3D");
        menuFunctions.add(itemCalculate);
        menuFunctions.add(itemViewer3D);

        itemCalculate.addActionListener(e -> window.recalculation());
        itemViewer3D.addActionListener(e -> window.startViewer3D());

        // Paramètres
        window.menuParameters = new JMenu("Paramètres");
        JMenuItem itemTheme = new JMenuItem("Thème");
        window.menuParameters.add(itemTheme);
        itemTheme.addActionListener(e -> Theme.showThemeSelectionDialog(window));

        // Aide
        JMenu menuHelp = new JMenu("Aide");
        JMenuItem itemOpenHelpPDF = new JMenuItem("Manuel utilisateur");
        JMenuItem itemOpenTutorialMovie = new JMenuItem("Tutoriel vidéo");
        JMenuItem itemOpenWindowAbout = new JMenuItem("A propos");
        menuHelp.add(itemOpenHelpPDF);
        menuHelp.add(itemOpenTutorialMovie);
        menuHelp.add(itemOpenWindowAbout);

        itemOpenHelpPDF.addActionListener(e -> window.openHelpPDF());
        itemOpenTutorialMovie.addActionListener(e -> window.openTutorialMovie());
        itemOpenWindowAbout.addActionListener(e -> AboutWindow.openAboutWindow(window));

        menuBar.add(menuFile);
        menuBar.add(menuFunctions);
        menuBar.add(window.menuParameters);
        menuBar.add(menuHelp);

        return menuBar;
    }
}
