import javax.swing.*;
import java.awt.*;

public class LoadingDialog {
    private final JDialog dialog;

    public LoadingDialog(JFrame parent, String message) {
        dialog = new JDialog(parent, "Calcul en cours...", false); 
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setUndecorated(true);

        ImageIcon gearIcon = new ImageIcon("../../img/gear.png");

        Image img = gearIcon.getImage();
        int newHeight = 40;  // hauteur fixe en pixels
        int newWidth = (img.getWidth(null) * newHeight) / img.getHeight(null);
        Image scaledImg = img.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImg);

        JLabel label = new JLabel(message, scaledIcon, SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 14f));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setHorizontalTextPosition(SwingConstants.RIGHT);
        label.setIconTextGap(10);

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(15));
        panel.add(label);
        panel.add(Box.createVerticalStrut(15));

        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
    }

    public void showDialog() {
        SwingUtilities.invokeLater(() -> dialog.setVisible(true));
    }

    public void closeDialog() {
        SwingUtilities.invokeLater(() -> dialog.setVisible(false));
    }
}
