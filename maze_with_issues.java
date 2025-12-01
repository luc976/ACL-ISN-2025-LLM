package MazeGen;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class MazeGenAcces extends JPanel {

    private static final String WALL = "1";
    private static final String PATH = "0";
    private static final String HERO = "2";
    private static final String ENEMY = "3";

    private static String[][] maze;
    private static int rows;
    private static int cols;
    private static int herosX;
    private static int herosY;
    private static final ArrayList<int[]> ennemis = new ArrayList<>();
    private static boolean jeuEnCours = true;

    private static final int CELL_SIZE = 20;
    private static JFrame frame;
    private static MazeGenAcces panel;

    private static JLabel timerLabel;
    private static int secondesEcoulees = 0;
    private static Timer timerAffichage;
    private static Timer timerSpawnEnnemi;

    public static void main(String[] args) {

        int choix = 1; // par défaut
        try {
            choix = Integer.parseInt(JOptionPane.showInputDialog(
                    "Choisir la map:\n1. Facile\n2. Moyenne\n3. Difficile"));
        } catch (Exception e) { }

        switch (choix) {
            case 1:
                maze = maps_facile.maps_facile[0]; // première map facile
                break;
            case 2:
                maze = maps_moyen.maps_moyen[0];  // première map moyenne
                break;
            case 3:
                maze = maps_difficile.maps_difficile[0]; // première map difficile
                break;
            default:
                maze = maps_facile.maps_facile[0];
        }

        rows = maze.length;
        cols = maze[0].length;

        herosX = 1;
        herosY = 1;
        maze[herosY][herosX] = HERO;

        creerEnnemi(cols / 2, rows / 2, maze);

        SwingUtilities.invokeLater(MazeGenAcces::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Maze Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new MazeGenAcces();
        panel.setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE + 50));
        panel.setFocusable(true);

        timerLabel = new JLabel("Temps: 0s | Ennemis: " + ennemis.size(), SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setForeground(Color.DARK_GRAY);
        frame.add(timerLabel, BorderLayout.NORTH);

        timerAffichage = new Timer(1000, e -> {
            secondesEcoulees++;
            timerLabel.setText("Temps: " + secondesEcoulees + "s | Ennemis: " + ennemis.size());
        });
        timerAffichage.start();

        timerSpawnEnnemi = new Timer(10000, e -> {
            if (jeuEnCours) {
                spawnNouvelEnnemi();
                panel.repaint();
            }
        });
        timerSpawnEnnemi.start();

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!jeuEnCours) return;
                int keyCode = e.getKeyCode();
                int[] newPos = mouvement(herosX, herosY, keyCode, maze);
                herosX = newPos[0];
                herosY = newPos[1];

                for (int[] ennemi : ennemis) {
                    int[] newEnnemi = deplacementEnnemi(ennemi[0], ennemi[1], herosX, herosY, maze);
                    ennemi[0] = newEnnemi[0];
                    ennemi[1] = newEnnemi[1];
                }

                if (verifierMort(herosX, herosY)) {
                    jeuEnCours = false;
                    timerAffichage.stop();
                    timerSpawnEnnemi.stop();
                    JOptionPane.showMessageDialog(frame,
                            "GAME OVER!\nTemps: " + secondesEcoulees + "s\nEnnemis: " + ennemis.size(),
                            "Defaite", JOptionPane.ERROR_MESSAGE);
                }

                if (herosX == cols - 2 && herosY == rows - 2) {
                    jeuEnCours = false;
                    timerAffichage.stop();
                    timerSpawnEnnemi.stop();
                    JOptionPane.showMessageDialog(frame,
                            "VICTOIRE!\nTemps: " + secondesEcoulees + "s\nEnnemis evites: " + ennemis.size(),
                            "Bravo!", JOptionPane.INFORMATION_MESSAGE);
                }

                panel.repaint();
            }
        });

        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void spawnNouvelEnnemi() {
        Random random = new Random();
        int tentatives = 0;
        while (tentatives < 100) {
            int x = random.nextInt(cols - 2) + 1;
            int y = random.nextInt(rows - 2) + 1;
            int distanceHeros = Math.abs(x - herosX) + Math.abs(y - herosY);
            if (maze[y][x].equals(PATH) && distanceHeros > 5) {
                creerEnnemi(x, y, maze);
                return;
            }
            tentatives++;
        }
        int centreX = cols / 2;
        int centreY = rows / 2;
        if (maze[centreY][centreX].equals(PATH)) {
            creerEnnemi(centreX, centreY, maze);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int x = j * CELL_SIZE;
                int y = i * CELL_SIZE;
                switch (maze[i][j]) {
                    case WALL:
                        g.setColor(Color.BLACK);
                        g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        break;
                    case PATH:
                        g.setColor(Color.WHITE);
                        g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        break;
                    case HERO:
                        g.setColor(Color.BLUE);
                        g.fillOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
                        break;
                    case ENEMY:
                        g.setColor(Color.RED);
                        g.fillOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
                        break;
                }
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    public static boolean blocage(int x, int y, int keyCode, String[][] carte) {
        int xSuiv = x, ySuiv = y;
        switch (keyCode) {
            case KeyEvent.VK_UP: ySuiv = y - 1; break;
            case KeyEvent.VK_DOWN: ySuiv = y + 1; break;
            case KeyEvent.VK_LEFT: xSuiv = x - 1; break;
            case KeyEvent.VK_RIGHT: xSuiv = x + 1; break;
        }
        if (ySuiv < 0 || ySuiv >= carte.length || xSuiv < 0 || xSuiv >= carte[0].length) return true;
        return carte[ySuiv][xSuiv].equals(WALL);
    }

    public static int[] mouvement(int herosX, int herosY, int keyCode, String[][] carte) {
        int newX = herosX, newY = herosY;
        if (!blocage(herosX, herosY, keyCode, carte)) {
            carte[herosY][herosX] = PATH;
            switch (keyCode) {
                case KeyEvent.VK_UP: newY = herosY - 1; break;
                case KeyEvent.VK_DOWN: newY = herosY + 1; break;
                case KeyEvent.VK_LEFT: newX = herosX - 1; break;
                case KeyEvent.VK_RIGHT: newX = herosX + 1; break;
            }
            carte[newY][newX] = HERO;
        }
        return new int[]{newX, newY};
    }

    public static void creerEnnemi(int x, int y, String[][] carte) {
        if (!carte[y][x].equals(HERO) && !carte[y][x].equals(ENEMY)) {
            carte[y][x] = ENEMY;
            ennemis.add(new int[]{x, y});
        }
    }

    public static int[] deplacementEnnemi(int ex, int ey, int hx, int hy, String[][] carte) {
        int newEx = ex, newEy = ey;
        String direction = "";
        if (Math.abs(hx - ex) > Math.abs(hy - ey)) {
            direction = (hx < ex) ? "gauche" : "droite";
        } else {
            direction = (hy < ey) ? "haut" : "bas";
        }

        if (!blocageString(ex, ey, direction, carte)) {
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

    private static boolean blocageString(int x, int y, String direction, String[][] carte) {
        int xSuiv = x, ySuiv = y;
        switch (direction) {
            case "haut": ySuiv = y - 1; break;
            case "bas": ySuiv = y + 1; break;
            case "gauche": xSuiv = x - 1; break;
            case "droite": xSuiv = x + 1; break;
        }
        if (ySuiv < 0 || ySuiv >= carte.length || xSuiv < 0 || xSuiv >= carte[0].length) return true;
        return carte[ySuiv][xSuiv].equals(WALL);
    }

    public static boolean verifierMort(int herosX, int herosY) {
        for (int[] ennemi : ennemis) {
            if (herosX == ennemi[0] && herosY == ennemi[1]) return true;
        }
        return false;
    }
}
