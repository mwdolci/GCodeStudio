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

class ToolUsage {
    int toolNumber;
    double usageTime;

    public ToolUsage(int toolNumber, double usageTime) {
        this.toolNumber = toolNumber;
        this.usageTime = usageTime;
    }
}

public class LinearGanttPanel extends JPanel {

    private java.util.List<ToolUsage> timeline = new ArrayList<>(); // liste des outils et leur temps d'utilisation
    private double totalTime = 0;
	private int selectedToolIndex = -1; // aucun sélectionné
	private java.util.List<Rectangle> toolRects = new ArrayList<>(); // rectangles pour chaque outil

    private final Map<Integer, Color> toolColors = new HashMap<>(); // map pour stocker les couleurs des outils
    
    // Constructeur qui charge le CSV et initialise les couleurs
    public LinearGanttPanel(String csvPath) {
        loadCSV(csvPath);
        assignColors();
        setToolTipText("");
		
        // Ecouteur de clics de souris
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Point p = e.getPoint();
				for (int i = 0; i < toolRects.size(); i++) {
					if (toolRects.get(i).contains(p)) {
						selectedToolIndex = i;
						ToolUsage selectedTool = timeline.get(i);
						System.out.println("Clicked Tool T" + selectedTool.toolNumber);
						repaint(); // redessine pour mise en évidence
						break;
					}
				}
			}
		});
    }

    private void loadCSV(String path) {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(path))) {
            String line = br.readLine(); // Prend pas en compte l'entête
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 2) continue;
                int toolNumber = Integer.parseInt(parts[0].trim());
                double usageTime = Double.parseDouble(parts[1].trim());
                timeline.add(new ToolUsage(toolNumber, usageTime));
                totalTime += usageTime;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void assignColors() {
        Random rand = new Random(42); // Couleur aléatoire
        for (ToolUsage usage : timeline) {
            if (!toolColors.containsKey(usage.toolNumber)) {
                Color color = new Color(rand.nextInt(200) + 30, rand.nextInt(200) + 30, rand.nextInt(200) + 30);
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
        int y = panelHeight / 2 - barHeight / 2;

        Graphics2D g2 = (Graphics2D) g; // Cast pour utiliser les méthodes avancées de Graphics2D
		g2.setFont(new Font("SansSerif", Font.BOLD, 14));
		toolRects.clear();

        // Dessin des barres de Gantt
		for (int i = 0; i < timeline.size(); i++) {
			ToolUsage usage = timeline.get(i);
			int barWidth = (int) ((usage.usageTime / totalTime) * (panelWidth - 20));
			Rectangle rect = new Rectangle(x, y, barWidth, barHeight);
			toolRects.add(rect);

			// Mise en évidence de l'outil sélectionné
			if (i == selectedToolIndex) {
				g2.setColor(toolColors.get(usage.toolNumber).darker());
				g2.fillRect(x, y, barWidth, barHeight);
				g2.setColor(Color.YELLOW);
				g2.setStroke(new BasicStroke(3));
				g2.draw(rect);
			} else {
				g2.setColor(toolColors.get(usage.toolNumber));
				g2.fillRect(x, y, barWidth, barHeight);
				g2.setColor(Color.BLACK);
				g2.setStroke(new BasicStroke(1));
				g2.draw(rect);
			}

			// Dessin label outil
			String label = "T" + usage.toolNumber;
			FontMetrics fm = g2.getFontMetrics();
			int textWidth = fm.stringWidth(label);
			int textX = x + (barWidth - textWidth) / 2;
			int textY = y + (barHeight - fm.getHeight()) / 2 + fm.getAscent();
			g2.drawString(label, textX, textY);

			x += barWidth;
		}
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        Point p = e.getPoint();
        for (int i = 0; i < toolRects.size(); i++) {
            if (toolRects.get(i).contains(p)) {
                ToolUsage usage = timeline.get(i);
                return "Outil T" + usage.toolNumber + " : " + usage.usageTime + "s";
            }
        }
        return null; // pas de tooltip
    }
}
