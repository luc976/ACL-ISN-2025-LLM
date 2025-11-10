package MazeGen;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MazeGenAcces extends JPanel {

    private static final int WALL = 1;
    private static final int PATH = 0;
    private static final int HERO = 2;
    private static final int ENEMY = 3;

    private static int rows = 32;
    private static int cols = 32;
    private static int[][] maze;

    private static int herosX = 1;
    private static int herosY = 1;
    private static int ennemiX = rows/2;
    private static int ennemiY = cols/2;
    private static boolean jeuEnCours = true;

    private static final int CELL_SIZE = 15;
    private static JFrame frame;
    private static MazeGenAcces panel;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> afficherMenu());
    }

    private static void afficherMenu() {
        JFrame menuFrame = new JFrame("🎮 Menu du jeu - MazeGen");
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setSize(300, 250);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setLayout(new GridLayout(6, 1, 10, 10));

        JLabel lblNiveau = new JLabel("Choisissez un niveau (1-10) :", SwingConstants.CENTER);
        JComboBox<Integer> niveauBox = new JComboBox<>();
        for (int i = 1; i <= 10; i++) niveauBox.addItem(i);

        JButton jouerBtn = new JButton("🚀 Jouer !");
        jouerBtn.addActionListener(e -> {
            int niveauChoisi = (int) niveauBox.getSelectedItem();

            // Charger la map prédéfinie depuis maps_facile
            maze = new int[rows][cols];
            String[] mapSelectionnee = maps_facile.maps_facile[niveauChoisi - 1];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    char c = mapSelectionnee[i].charAt(j);
                    maze[i][j] = (c == '0') ? WALL : PATH;
                }
            }

            // Initialiser héros et ennemi
            herosX = 1; herosY = 1;
            maze[herosY][herosX] = HERO;
            int[] ennemiCoord = creerEnnemi(rows/2, cols/2, maze);
            ennemiX = ennemiCoord[0];
            ennemiY = ennemiCoord[1];

            menuFrame.dispose();
            createAndShowGUI();
        });

        menuFrame.add(new JLabel(""));
        menuFrame.add(lblNiveau);
        menuFrame.add(niveauBox);
        menuFrame.add(new JLabel(""));
        menuFrame.add(jouerBtn);
        menuFrame.add(new JLabel(""));

        menuFrame.setVisible(true);
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Maze Game 🧩 - Utilisez les flèches");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new MazeGenAcces();
        panel.setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE + 50));
        panel.setFocusable(true);

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!jeuEnCours) return;

                int keyCode = e.getKeyCode();
                int[] n;

                switch (keyCode) {
                    case KeyEvent.VK_UP: n = mouvement(herosX, herosY, KeyEvent.VK_UP, maze); break;
                    case KeyEvent.VK_DOWN: n = mouvement(herosX, herosY, KeyEvent.VK_DOWN, maze); break;
                    case KeyEvent.VK_LEFT: n = mouvement(herosX, herosY, KeyEvent.VK_LEFT, maze); break;
                    case KeyEvent.VK_RIGHT: n = mouvement(herosX, herosY, KeyEvent.VK_RIGHT, maze); break;
                    case KeyEvent.VK_ESCAPE: System.exit(0); return;
                    default: return;
                }
                herosX = n[0]; herosY = n[1];

                int[] nE = deplacementEnnemi(ennemiX, ennemiY, herosX, herosY, maze);
                ennemiX = nE[0]; ennemiY = nE[1];

                if (verifierMort(herosX, herosY, ennemiX, ennemiY)) {
                    jeuEnCours = false;
                    JOptionPane.showMessageDialog(frame, "💀 GAME OVER 💀", "Défaite", JOptionPane.ERROR_MESSAGE);
                }

                if (herosX == cols - 2 && herosY == rows - 2) {
                    jeuEnCours = false;
                    JOptionPane.showMessageDialog(frame, "🎉 VICTOIRE 🎉", "Bravo !", JOptionPane.INFORMATION_MESSAGE);
                }

                panel.repaint();
            }
        });

        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int x = j * CELL_SIZE;
                int y = i * CELL_SIZE;
                switch (maze[i][j]) {
                    case WALL: g.setColor(Color.BLACK); g.fillRect(x, y, CELL_SIZE, CELL_SIZE); break;
                    case PATH: g.setColor(Color.WHITE); g.fillRect(x, y, CELL_SIZE, CELL_SIZE); break;
                    case HERO:
                        g.setColor(Color.WHITE); g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        g.setColor(Color.BLUE); g.fillOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
                        break;
                    case ENEMY:
                        g.setColor(Color.WHITE); g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        g.setColor(Color.RED); g.fillOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
                        break;
                }
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }
        g.setColor(Color.BLACK);
        g.drawString("Héros: (" + herosX + ", " + herosY + ")  Ennemi: (" + ennemiX + ", " + ennemiY + ")", 10, rows * CELL_SIZE + 20);
    }

    // === MÉCANISMES DU JEU ===
    public static boolean blocage(int x, int y, int keyCode, int[][] carte) {
        int xSuiv = x, ySuiv = y;
        switch (keyCode) {
            case KeyEvent.VK_UP: ySuiv--; break;
            case KeyEvent.VK_DOWN: ySuiv++; break;
            case KeyEvent.VK_LEFT: xSuiv--; break;
            case KeyEvent.VK_RIGHT: xSuiv++; break;
            default: return true;
        }
        return ySuiv < 0 || ySuiv >= carte.length || xSuiv < 0 || xSuiv >= carte[0].length || carte[ySuiv][xSuiv] == WALL;
    }

    public static int[] mouvement(int herosX, int herosY, int keyCode, int[][] carte) {
        int newX = herosX, newY = herosY;
        if (!blocage(herosX, herosY, keyCode, carte)) {
            carte[herosY][herosX] = PATH;
            switch (keyCode) {
                case KeyEvent.VK_UP: newY--; break;
                case KeyEvent.VK_DOWN: newY++; break;
                case KeyEvent.VK_LEFT: newX--; break;
                case KeyEvent.VK_RIGHT: newX++; break;
            }
            carte[newY][newX] = HERO;
        }
        return new int[]{newX, newY};
    }

    public static int[] creerEnnemi(int x, int y, int[][] carte) {
        carte[y][x] = ENEMY;
        return new int[]{x, y};
    }

    public static int[] deplacementEnnemi(int ex, int ey, int hx, int hy, int[][] carte) {
        int newEx = ex, newEy = ey;
        String direction = "";
        if (Math.abs(hx - ex) > Math.abs(hy - ey)) {
            if (hx < ex) direction = "gauche"; else if (hx > ex) direction = "droite";
        } else {
            if (hy < ey) direction = "haut"; else if (hy > ey) direction = "bas";
        }
        if (!direction.isEmpty() && !blocageString(ex, ey, direction, carte)) {
            carte[ey][ex] = PATH;
            switch (direction) {
                case "haut": newEy--; break;
                case "bas": newEy++; break;
                case "gauche": newEx--; break;
                case "droite": newEx++; break;
            }
            carte[newEy][newEx] = ENEMY;
        }
        return new int[]{newEx, newEy};
    }

    private static boolean blocageString(int x, int y, String direction, int[][] carte) {
        int xSuiv = x, ySuiv = y;
        switch (direction.toLowerCase()) {
            case "haut": ySuiv--; break;
            case "bas": ySuiv++; break;
            case "gauche": xSuiv--; break;
            case "droite": xSuiv++; break;
            default: return true;
        }
        return ySuiv < 0 || ySuiv >= carte.length || xSuiv < 0 || xSuiv >= carte[0].length || carte[ySuiv][xSuiv] == WALL;
    }

    public static boolean verifierMort(int herosX, int herosY, int ennemiX, int ennemiY) {
        return herosX == ennemiX && herosY == ennemiY;
    }
}
