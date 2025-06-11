import javax.swing.*;
import java.awt.*;

public class Theme {
    public enum ThemeType { LIGHT, DARK, HARLEQUIN }
    private static ThemeType currentTheme = ThemeType.LIGHT;

    public static ThemeType getCurrentTheme() {
        return currentTheme;
    }
    public static void setCurrentTheme(ThemeType theme) {
        currentTheme = theme;
    }

    public static void showThemeSelectionDialog(MainWindow window) {

        JDialog dialog = new JDialog(window, "Sélection du thème", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(300, 150);
        dialog.setLocationRelativeTo(window);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JRadioButton lightButton = new JRadioButton("Light");
        JRadioButton darkButton = new JRadioButton("Dark");
        JRadioButton harlequinButton = new JRadioButton("Harlequin");

        // Sélection actuelle
        if (window.getBackgroundColor().equals(new Color(34, 34, 34))) {
            darkButton.setSelected(true);
        } else if (window.getBackgroundColor().equals(new Color(0, 120, 255))) {
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
                applyLightTheme(window);
            } else if (darkButton.isSelected()) {
                applyDarkTheme(window);
            } else if (harlequinButton.isSelected()) {
                applyHarlequinTheme(window);
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

    public static void applyLightTheme(MainWindow window) {
        setCurrentTheme(ThemeType.LIGHT);
        window.setBackgroundColor(new Color(244, 244, 244));
        window.setBackgroundColorTopRight(window.getBackgroundColor());
        window.setBackgroundColorBottomLeft(window.getBackgroundColor());
        window.setBackgroundColorBottomRight(window.getBackgroundColor());
        window.setBackgroundColorEditor(Color.WHITE);
        window.setTextColor(Color.BLACK);
        applyThemeColors(window);
    }

    public static void applyDarkTheme(MainWindow window) {
        setCurrentTheme(ThemeType.DARK);
        window.setBackgroundColor(new Color(34, 34, 34));
        window.setBackgroundColorTopRight(window.getBackgroundColor());
        window.setBackgroundColorBottomLeft(window.getBackgroundColor());
        window.setBackgroundColorBottomRight(window.getBackgroundColor());
        window.setBackgroundColorEditor(new Color(200, 200, 200));
        window.setTextColor(Color.WHITE);
        applyThemeColors(window);
    }

    public static void applyHarlequinTheme(MainWindow window) {
        setCurrentTheme(ThemeType.HARLEQUIN);
        window.setBackgroundColor(new Color(0, 120, 255));
        window.setBackgroundColorTopRight(new Color(140, 180, 255));
        window.setBackgroundColorBottomLeft(new Color(255, 170, 200));
        window.setBackgroundColorBottomRight(new Color(150, 230, 150));
        window.setBackgroundColorEditor(new Color(255, 250, 210));
        window.setTextColor(Color.BLACK);
        applyThemeColors(window);
    }

    public static void applyThemeColors(MainWindow window) {
        window.getContentPane().setBackground(window.getBackgroundColor());
        window.getLineInfoArea().setBackground(window.getBackgroundColorTopRight());
        window.getLineInfoArea().setForeground(window.getTextColor());
        window.getBottomLeftTextArea().setBackground(window.getBackgroundColorBottomLeft());
        window.getBottomLeftTextArea().setForeground(window.getTextColor());
        window.getBottomRightTextArea().setBackground(window.getBackgroundColorBottomRight());
        window.getBottomRightTextArea().setForeground(window.getTextColor());
        window.getLinearGanttPanel().setBackground(window.getBackgroundColorBottomLeft());
        window.getSliderPanel().setBackground(window.getBackgroundColorBottomLeft());
        window.getGanttAndSliderPanel().setBackground(window.getBackgroundColorBottomLeft());
        window.getGcodeEditor().setBackground(window.getBackgroundColorEditor());
        window.revalidate();
        window.repaint();
    }

    // Pour réappliquer le thème actuel au recalcul du programme
    public static void reapplyCurrentTheme(MainWindow window) {
        switch (currentTheme) {
            case LIGHT:
                applyLightTheme(window);
                break;
            case DARK:
                applyDarkTheme(window);
                break;
            case HARLEQUIN:
                applyHarlequinTheme(window);
                break;
        }
    }
}
