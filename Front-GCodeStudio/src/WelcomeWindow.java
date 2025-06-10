import javax.swing.*;
import java.awt.*;

public class WelcomeWindow {
    public static JPanel createWelcomePanel(Runnable openGCodeAction) {
        JPanel welcomePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int width = getWidth();
                int height = getHeight();
                float[] fractions = {0.0f, 1.0f};
                Color[] colors = {new Color(30, 30, 30), new Color(70, 70, 70)};
                LinearGradientPaint lgp = new LinearGradientPaint(0, 0, 0, height, fractions, colors);
                g2d.setPaint(lgp);
                g2d.fillRect(0, 0, width, height);
                g2d.dispose();
            }
        };
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));

        JLabel welcomeLabel = new JLabel("Bienvenue dans GCode Studio", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel startLabel = new JLabel("Ouvrez un programme pour commencer", SwingConstants.CENTER);
        startLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        startLabel.setForeground(Color.WHITE);
        startLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton openFileButton = new JButton("+");
        openFileButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        openFileButton.setFont(new Font("Arial", Font.BOLD, 92));
        openFileButton.setBorder(BorderFactory.createEmptyBorder());
        openFileButton.setFocusPainted(false);
        openFileButton.setToolTipText("Ouvrir GCode");
        openFileButton.setBackground(Color.GRAY);
        openFileButton.setForeground(Color.BLACK);
        openFileButton.setOpaque(true);

        int buttonSize = 100;
        openFileButton.setPreferredSize(new Dimension(buttonSize, buttonSize));
        openFileButton.setMaximumSize(new Dimension(buttonSize, buttonSize));
        openFileButton.setMinimumSize(new Dimension(buttonSize, buttonSize));

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

        openFileButton.addActionListener(e -> openGCodeAction.run());

        welcomePanel.add(Box.createVerticalGlue());
        welcomePanel.add(welcomeLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        welcomePanel.add(startLabel);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 50)));
        welcomePanel.add(openFileButton);
        welcomePanel.add(Box.createVerticalGlue());

        return welcomePanel;
    }
}
