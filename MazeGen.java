package MazeGen;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class MazeGenAcces extends JPanel {

    // === CONSTANTES DU JEU ===
    private static final int WALL = 1;
    private static final int PATH = 0;
    private static final int HERO = 2;
    private static final int ENEMY = 3;

    // === VARIABLES GLOBALES ===
    private static int rows;
    private static int cols;
    private static double initialWallChance = 0.45;
    private static int[][] maze;
    private static Random random = new Random();
    
    // === VARIABLES DE JEU ===
    private static int herosX = 1;
    private static int herosY = 1;
    private static int ennemiX = 5;
    private static int ennemiY = 5;
    private static boolean jeuEnCours = true;
    
    // === VARIABLES D'AFFICHAGE ===
    private static final int CELL_SIZE = 15; // Taille d'une case en pixels
    private static JFrame frame;
    private static MazeGenAcces panel;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // === CHOIX DE LA TAILLE DU LABYRINTHE ===
        System.out.println("Choose cave map size:");
        System.out.println("1. Small (32x32)");
        System.out.println("2. Medium (64x64)");
        System.out.println("3. Large (128x128)");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();

        switch (choice) {
            case 1:
                rows = 32;
                cols = 32;
                break;
            case 2:
                rows = 64;
                cols = 64;
                break;
            case 3:
                rows = 128;
                cols = 128;
                break;
            default:
                rows = 21;
                cols = 21;
                break;
        }

        maze = new int[rows][cols];

        // === GÉNÉRATION DU LABYRINTHE ===
        fillRandomMaze();

        int iterations = 5;
        for (int k = 0; k < iterations; k++) {
            maze = doSimulationStep(maze);
        }

        maze[1][1] = PATH;
        maze[rows - 2][cols - 2] = PATH;

        connectRegions();

        // === INITIALISATION DU HÉROS ET DE L'ENNEMI ===
        herosX = 1;
        herosY = 1;
        maze[herosY][herosX] = HERO;

        int[] ennemiCoord = creerEnnemi(5, 5, maze);
        ennemiX = ennemiCoord[0];
        ennemiY = ennemiCoord[1];

        scanner.close();

        // === CRÉATION DE LA FENÊTRE GRAPHIQUE ===
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Maze Game - Utilisez les flèches pour vous déplacer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        panel = new MazeGenAcces();
        panel.setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE + 50));
        panel.setFocusable(true);
        
        // === AJOUT DU LISTENER POUR LES TOUCHES ===
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!jeuEnCours) return;
                
                int keyCode = e.getKeyCode();
                
                switch (keyCode) {
                    case KeyEvent.VK_UP:
                        int[] newPosUp = mouvement(herosX, herosY, KeyEvent.VK_UP, maze);
                        herosX = newPosUp[0];
                        herosY = newPosUp[1];
                        break;
                    case KeyEvent.VK_DOWN:
                        int[] newPosDown = mouvement(herosX, herosY, KeyEvent.VK_DOWN, maze);
                        herosX = newPosDown[0];
                        herosY = newPosDown[1];
                        break;
                    case KeyEvent.VK_LEFT:
                        int[] newPosLeft = mouvement(herosX, herosY, KeyEvent.VK_LEFT, maze);
                        herosX = newPosLeft[0];
                        herosY = newPosLeft[1];
                        break;
                    case KeyEvent.VK_RIGHT:
                        int[] newPosRight = mouvement(herosX, herosY, KeyEvent.VK_RIGHT, maze);
                        herosX = newPosRight[0];
                        herosY = newPosRight[1];
                        break;
                    case KeyEvent.VK_ESCAPE:
                        System.exit(0);
                        break;
                    default:
                        return;
                }
                
                // === DÉPLACEMENT DE L'ENNEMI ===
                int[] nouvellesCoordEnnemi = deplacementEnnemi(ennemiX, ennemiY, herosX, herosY, maze);
                ennemiX = nouvellesCoordEnnemi[0];
                ennemiY = nouvellesCoordEnnemi[1];
                
                // === VÉRIFICATIONS ===
                if (verifierMort(herosX, herosY, ennemiX, ennemiY)) {
                    jeuEnCours = false;
                    JOptionPane.showMessageDialog(frame, "💀 GAME OVER! 💀", "Défaite", JOptionPane.ERROR_MESSAGE);
                }
                
                if (herosX == cols - 2 && herosY == rows - 2) {
                    jeuEnCours = false;
                    JOptionPane.showMessageDialog(frame, "🎉 VICTOIRE! 🎉\nVous avez atteint la sortie!", "Victoire", JOptionPane.INFORMATION_MESSAGE);
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
                    case WALL:
                        g.setColor(Color.BLACK);
                        g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        break;
                    case PATH:
                        g.setColor(Color.WHITE);
                        g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        break;
                    case HERO:
                        g.setColor(Color.WHITE);
                        g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        g.setColor(Color.BLUE);
                        g.fillOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
                        break;
                    case ENEMY:
                        g.setColor(Color.WHITE);
                        g.fillRect(x, y, CELL_SIZE, CELL_SIZE);
                        g.setColor(Color.RED);
                        g.fillOval(x + 2, y + 2, CELL_SIZE - 4, CELL_SIZE - 4);
                        break;
                }
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(x, y, CELL_SIZE, CELL_SIZE);
            }
        }
        g.setColor(Color.BLACK);
        g.drawString("Position Héros: (" + herosX + ", " + herosY + ")  |  Position Ennemi: (" + ennemiX + ", " + ennemiY + ")", 10, rows * CELL_SIZE + 20);
        g.drawString("Utilisez les FLÈCHES pour bouger | ECHAP pour quitter", 10, rows * CELL_SIZE + 40);
    }

    private static void fillRandomMaze() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (i == 0 || j == 0 || i == rows - 1 || j == cols - 1) {
                    maze[i][j] = WALL;
                } else {
                    maze[i][j] = random.nextDouble() < initialWallChance ? WALL : PATH;
                }
            }
        }
    }

    private static int[][] doSimulationStep(int[][] oldMap) {
        int[][] newMap = new int[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int neighbors = countWallNeighbors(oldMap, i, j);

                if (oldMap[i][j] == WALL) {
                    newMap[i][j] = neighbors >= 4 ? WALL : PATH;
                } else {
                    newMap[i][j] = neighbors >= 5 ? WALL : PATH;
                }
            }
        }
        return newMap;
    }

    private static int countWallNeighbors(int[][] map, int r, int c) {
        int count = 0;
        for (int i = r - 1; i <= r + 1; i++) {
            for (int j = c - 1; j <= c + 1; j++) {
                if (i == r && j == c) continue;
                if (i < 0 || j < 0 || i >= rows || j >= cols) count++;
                else if (map[i][j] == WALL) count++;
            }
        }
        return count;
    }

    private static void connectRegions() {
        boolean[][] visited = new boolean[rows][cols];
        bfs(1, 1, visited);

        // ✅ Correction ici : typage explicite
        List<int[]> disconnectedCells = new ArrayList<int[]>();

        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < cols - 1; j++) {
                if (maze[i][j] == PATH && !visited[i][j]) {
                    disconnectedCells.add(new int[]{i, j});
                }
            }
        }

        while (!disconnectedCells.isEmpty()) {
            int[] cell = disconnectedCells.get(0);

            List<int[]> region = new ArrayList<>();
            boolean[][] regionVisited = new boolean[rows][cols];
            Queue<int[]> queue = new LinkedList<>();
            queue.add(cell);
            regionVisited[cell[0]][cell[1]] = true;

            while (!queue.isEmpty()) {
                int[] current = queue.poll();
                region.add(current);

                int r = current[0];
                int c = current[1];

                int[][] neighbors = {{r - 1, c}, {r + 1, c}, {r, c - 1}, {r, c + 1}};
                for (int[] n : neighbors) {
                    int nr = n[0], nc = n[1];
                    if (nr > 0 && nr < rows - 1 && nc > 0 && nc < cols - 1 &&
                        maze[nr][nc] == PATH && !regionVisited[nr][nc]) {
                        regionVisited[nr][nc] = true;
                        queue.add(new int[]{nr, nc});
                    }
                }
            }

            int minDist = Integer.MAX_VALUE;
            int[] bestRegionCell = null;
            int[] bestVisitedCell = null;

            for (int[] rc : region) {
                for (int i = 1; i < rows - 1; i++) {
                    for (int j = 1; j < cols - 1; j++) {
                        if (visited[i][j]) {
                            int dist = Math.abs(rc[0] - i) + Math.abs(rc[1] - j);
                            if (dist < minDist) {
                                minDist = dist;
                                bestRegionCell = rc;
                                bestVisitedCell = new int[]{i, j};
                            }
                        }
                    }
                }
            }

            carvePath(bestRegionCell, bestVisitedCell);
            bfs(1, 1, visited);

            disconnectedCells.clear();
            for (int i = 1; i < rows - 1; i++) {
                for (int j = 1; j < cols - 1; j++) {
                    if (maze[i][j] == PATH && !visited[i][j]) {
                        disconnectedCells.add(new int[]{i, j});
                    }
                }
            }
        }
    }

    private static void bfs(int startR, int startC, boolean[][] visited) {
        for (int i = 0; i < rows; i++) Arrays.fill(visited[i], false);

        Queue<int[]> queue = new LinkedList<>();
        queue.add(new int[]{startR, startC});
        visited[startR][startC] = true;

        while (!queue.isEmpty()) {
            int[] curr = queue.poll();
            int r = curr[0], c = curr[1];
            int[][] neighbors = {{r - 1, c}, {r + 1, c}, {r, c - 1}, {r, c + 1}};

            for (int[] n : neighbors) {
                int nr = n[0], nc = n[1];
                if (nr > 0 && nr < rows - 1 && nc > 0 && nc < cols - 1 &&
                    maze[nr][nc] == PATH && !visited[nr][nc]) {
                    visited[nr][nc] = true;
                    queue.add(new int[]{nr, nc});
                }
            }
        }
    }

    private static void carvePath(int[] from, int[] to) {
        int r = from[0];
        int c = from[1];

        while (c != to[1]) {
            maze[r][c] = PATH;
            c += (to[1] > c) ? 1 : -1;
        }
        while (r != to[0]) {
            maze[r][c] = PATH;
            r += (to[0] > r) ? 1 : -1;
        }
    }

    public static boolean blocage(int x, int y, int keyCode, int[][] carte) {
        int xSuiv = x;
        int ySuiv = y;

        switch (keyCode) {
            case KeyEvent.VK_UP:    ySuiv = y - 1; break;
            case KeyEvent.VK_DOWN:  ySuiv = y + 1; break;
            case KeyEvent.VK_LEFT:  xSuiv = x - 1; break;
            case KeyEvent.VK_RIGHT: xSuiv = x + 1; break;
            default: return true;
        }

        if (ySuiv < 0 || ySuiv >= carte.length || xSuiv < 0 || xSuiv >= carte[0].length)
            return true;

        return carte[ySuiv][xSuiv] == WALL;
    }

    public static int[] mouvement(int herosX, int herosY, int keyCode, int[][] carte) {
        int newX = herosX;
        int newY = herosY;
        
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

    public static int[] creerEnnemi(int x, int y, int[][] carte) {
        carte[y][x] = ENEMY;
        return new int[]{x, y};
    }

    public static int[] deplacementEnnemi(int ex, int ey, int hx, int hy, int[][] carte) {
        int newEx = ex;
        int newEy = ey;

        // ✅ Correction : initialisation de direction
        String direction = "";

        if (Math.abs(hx - ex) > Math.abs(hy - ey)) {
            if (hx < ex) direction = "gauche";
            else if (hx > ex) direction = "droite";
        } else {
            if (hy < ey) direction = "haut";
            else if (hy > ey) direction = "bas";
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
        int xSuiv = x;
        int ySuiv = y;

        switch (direction.toLowerCase()) {
            case "haut": ySuiv = y - 1; break;
            case "bas": ySuiv = y + 1; break;
            case "gauche": xSuiv = x - 1; break;
            case "droite": xSuiv = x + 1; break;
            default: return true;
        }

        if (ySuiv < 0 || ySuiv >= carte.length || xSuiv < 0 || xSuiv >= carte[0].length)
            return true;

        return carte[ySuiv][xSuiv] == WALL;
    }

    public static boolean verifierMort(int herosX, int herosY, int ennemiX, int ennemiY) {
        if (herosX == ennemiX && herosY == ennemiY) {
            System.out.println("\n==============================");
            System.out.println("💀💀💀  VOUS ÊTES MORT  💀💀💀");
            System.out.println("==============================\n");
            return true;
        }
        return false;
    }
}


