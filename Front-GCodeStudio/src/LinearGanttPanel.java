import javax.swing.*;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.FontMetrics;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.awt.event.*;
import java.awt.Rectangle;
import java.awt.Point;
import java.awt.BasicStroke;

// Classe pour représenter l'utilisation d'un outil
class ToolUsage {
    int toolNumber;
    double usageTime;

    public ToolUsage(int toolNumber, double usageTime) {
        this.toolNumber = toolNumber;
        this.usageTime = usageTime;
    }
}

// Classe pour dessiner un panneau de Gantt linéaire
public class LinearGanttPanel extends JPanel {
    private java.util.List<ToolUsage> timeline = new ArrayList<>(); // liste des outils et leur temps d'utilisation
    private double totalTime = 0;
	private int selectedToolIndex = -1; // aucun sélectionné par défaut
	private java.util.List<Rectangle> toolRects = new ArrayList<>(); // rectangles pour chaque outil

    private double currentTime = 0; 

    private final Map<Integer, Color> toolColors = new HashMap<>(); // Equivalent à un dictionnaire numéro outil - couleur | Final = non modifiable

    // Constructeur qui charge le CSV et initialise les couleurs
    public LinearGanttPanel(String csvPath) {
        loadCSV(csvPath);
        assignColors();
        setToolTipText("");
		
        // Ecouteur de clics de souris
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
                
				Point p = e.getPoint(); // Récupère la position du clic en XY
				for (int i = 0; i < toolRects.size(); i++) { // Parcourt les rectangles des outils
					if (toolRects.get(i).contains(p)) { // Si le clic est dans un rectangle
						selectedToolIndex = i;
						ToolUsage selectedTool = timeline.get(i);
						System.out.println("Clicked Tool T" + selectedTool.toolNumber);
						
                        if (toolSelectionListener != null) {
                            toolSelectionListener.onToolSelected(selectedTool.toolNumber);
                        }

                        repaint(); // redessine pour mise en évidence
                        
                        break;
					}
				}
			}
		});
    }

    public void setCurrentTime(double seconds) {
        System.out.println("currentTime = " + currentTime + ", totalTime = " + totalTime + ", seconds = " + seconds);
        this.currentTime = Math.max(0, Math.min(seconds, totalTime)); // entre 0 et totalTime
        repaint();
    }

    public double getTotalTime() {
        return totalTime;
    }

    // Méthode pour changer le numéro de l'outil actif dans le gantt
    public interface ToolSelectionListener {
        void onToolSelected(int toolNumber);
    }

    // Permet de notifier le changement d'outil sélectionné
    private ToolSelectionListener toolSelectionListener;
    public void setToolSelectionListener(ToolSelectionListener listener) {
        this.toolSelectionListener = listener;
    }

    public void setActiveToolNumber(int toolNumber) {
        for (int i = 0; i < timeline.size(); i++) {
            if (timeline.get(i).toolNumber == toolNumber) {
                selectedToolIndex = i;
                repaint();
                return;
            }
        }
        selectedToolIndex = -1; // aucun sélectionné si non trouvé
        repaint();
    }

    public void loadCSV(String path) {
        System.out.println("Charge depuis LinearGanttPanel");
        timeline.clear(); // Vide la liste avant de charger
        totalTime = 0; // Réinitialise le temps total
        toolColors.clear(); // Vide les couleurs des outils
        selectedToolIndex = -1; // Réinitialise l'outil sélectionné

        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
            String line = br.readLine(); // Prend pas en compte l'entête
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 2) continue; // Ignore les éventuelles lignes incorèctes
                int toolNumber = Integer.parseInt(parts[0].trim()); // Récupère le numéro de l'outil
                double usageTime = Double.parseDouble(parts[1].trim()); // Récupère le temps d'utilisation
                timeline.add(new ToolUsage(toolNumber, usageTime)); // Ajoute à la liste
                totalTime += usageTime;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        assignColors();
        repaint();
    }

    private void assignColors() {
        Random rand = new Random(42); // Couleur aléatoire, 42 = pour la reproductibilité
        for (ToolUsage usage : timeline) {
            if (!toolColors.containsKey(usage.toolNumber)) {
                Color color = new Color(rand.nextInt(200) + 30, rand.nextInt(200) + 30, rand.nextInt(200) + 30); //Couleur aléatoire en évitant couleur sombre
                toolColors.put(usage.toolNumber, color);
            }
        }
    }

    // Rafraîchit l'affichage
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (timeline.isEmpty()) return;

        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int barHeight = 50;
        int x = 10;
        int minBottomMargin = 20;
        int y = panelHeight - barHeight - minBottomMargin;

        Graphics2D g2 = (Graphics2D) g; // Cast pour utiliser les méthodes avancées de Graphics2D
		g2.setFont(new Font("SansSerif", Font.BOLD, 14));
		toolRects.clear();

        Rectangle selectedRect = null;
        ToolUsage selectedUsage = null;

        // Dessin des barres de Gantt
		for (int i = 0; i < timeline.size(); i++) {
			ToolUsage usage = timeline.get(i);
			int barWidth = (int) ((usage.usageTime / totalTime) * (panelWidth - 20));
			Rectangle rect = new Rectangle(x, y, barWidth, barHeight);
			toolRects.add(rect);

			if (i == selectedToolIndex) {
                selectedRect = rect;
                selectedUsage = usage;
            } else {
                g2.setColor(toolColors.get(usage.toolNumber));
                g2.fillRect(x, y, barWidth, barHeight);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(1));
                g2.draw(rect);

                // Dessin du label outil
                String label = "T" + usage.toolNumber;
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(label);
                int textX = x + (barWidth - textWidth) / 2;
                int textY = y + (barHeight - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(label, textX, textY);
            }

			x += barWidth;
		}

        // Dessin de la barre sélectionnée en dernier
        if (selectedRect != null && selectedUsage != null) {
            g2.setColor(toolColors.get(selectedUsage.toolNumber).darker());
            g2.fillRect(selectedRect.x, selectedRect.y, selectedRect.width, selectedRect.height);
            g2.setColor(Color.YELLOW);
            g2.setStroke(new BasicStroke(3));
            g2.draw(selectedRect);

            // Dessin du label outil sélectionné
            String label = "T" + selectedUsage.toolNumber;
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(label);
            int textX = selectedRect.x + (selectedRect.width - textWidth) / 2;
            int textY = selectedRect.y + (selectedRect.height - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(label, textX, textY);
        }

        // Dessine la ligne verticale rouge représentant le temps courant
        int timeX = 10 + (int)((currentTime / totalTime) * (panelWidth - 20));
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(timeX, y, timeX, y + barHeight);
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        Point p = e.getPoint();
        for (int i = 0; i < toolRects.size(); i++) {
            if (toolRects.get(i).contains(p)) {
                ToolUsage usage = timeline.get(i);

                double totalTime = getTotalUsageTime();

                if (totalTime > 0) {
                    double percentage = (usage.usageTime / totalTime) * 100;
                    return String.format("T%d : %.1f%%", usage.toolNumber, percentage);
                } else {
                    return "T" + usage.toolNumber + " : 0%";
                }
            }
        }
        return null; // pas de tooltip
    }

    // Méthode pour obtenir le temps total d'utilisation de tous les outils
    private double getTotalUsageTime() {
        return timeline.stream().mapToDouble(usage -> usage.usageTime).sum();
    }
}
