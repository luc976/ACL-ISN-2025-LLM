import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import javax.swing.*;




public class MazeGenAcces extends JPanel {

    // === CONSTANTES DU JEU ===
    private static final int WALL = 1;
    private static final int PATH = 0;
    private static final int HERO = 2;
    private static final int ENEMY = 3;

    // === VARIABLES GLOBALES ===
    private static int rows;
    private static int cols;
    private static final double initialWallChance = 0.45;
    private static int[][] maze;
    private static final Random random = new Random();
    static ArrayList<Projectile> projectiles = new ArrayList<>();
    static int munitions = 5;
    static Random rand = new Random();
    
    // === VARIABLES DE JEU ===
    private static int herosX = 1;
    private static int herosY = 1;
    private static final ArrayList<int[]> ennemis = new ArrayList<>();
    private static boolean jeuEnCours = true;
    
    // === VARIABLES D'AFFICHAGE ===
    private static final int CELL_SIZE = 15;
    private static JFrame frame;
    private static MazeGenAcces panel;
    
    // === VARIABLES DU TIMER (utilisation explicite de javax.swing.Timer) ===
    private static JLabel timerLabel;
    private static int secondesEcoulees = 0;
    private static javax.swing.Timer timerAffichage;
    private static javax.swing.Timer timerSpawnEnnemi;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("Choose cave map size:");
            System.out.println("1. Small (32x32)");
            System.out.println("2. Medium (64x64)");
            System.out.println("3. Large (128x128)");
            System.out.print("Enter choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1: rows = 32; cols = 32; break;
                case 2: rows = 64; cols = 64; break;
                case 3: rows = 128; cols = 128; break;
                default: rows = 21; cols = 21; break;
            }
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

        // === INITIALISATION DU HÉROS ===
        herosX = 1;
        herosY = 1;
        maze[herosY][herosX] = HERO;

        // === CRÉATION DU PREMIER ENNEMI ===
        creerEnnemi(5, 5, maze);

        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Maze Game - Utilisez les fleches pour vous deplacer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        panel = new MazeGenAcces();
        panel.setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE + 70));
        panel.setFocusable(true);
        
        // === CRÉATION DU LABEL TIMER ===
        timerLabel = new JLabel("Temps: 0s | Ennemis: 1", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        timerLabel.setForeground(Color.DARK_GRAY);
        frame.add(timerLabel, BorderLayout.NORTH);
        
        // === TIMER D'AFFICHAGE (toutes les secondes) ===
        secondesEcoulees = 0;
        timerAffichage = new javax.swing.Timer(1000, e -> {
            secondesEcoulees++;
            timerLabel.setText("Temps: " + secondesEcoulees + "s | Ennemis: " + ennemis.size());
        });
        timerAffichage.start();
        
        // === TIMER DE SPAWN D'ENNEMI (toutes les 10 secondes) ===
        timerSpawnEnnemi = new javax.swing.Timer(10000, e -> {
            if (jeuEnCours) {
                spawnNouvelEnnemi();
                timerLabel.setText("Temps: " + secondesEcoulees + "s | Ennemis: " + ennemis.size());
                panel.repaint();
            }
        });
        timerSpawnEnnemi.start();
        
        // === LISTENER CLAVIER ===
        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!jeuEnCours) return;
                
                int keyCode = e.getKeyCode();
                int[] newPos;
                
                switch (keyCode) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_RIGHT:
                        newPos = mouvement(herosX, herosY, keyCode, maze);
                        herosX = newPos[0];
                        herosY = newPos[1];
                        break;
                    case KeyEvent.VK_ESCAPE:
                        System.exit(0);
                        return;
                    default:
                        return;
                }
                
                // === DÉPLACEMENT DE TOUS LES ENNEMIS ===
                for (int[] ennemi : ennemis) {
                    int[] nouvellePos = deplacementEnnemi(ennemi[0], ennemi[1], herosX, herosY, maze);
                    ennemi[0] = nouvellePos[0];
                    ennemi[1] = nouvellePos[1];
                }
                
                // === VÉRIFICATION MORT ===
                if (verifierMort(herosX, herosY)) {
                    jeuEnCours = false;
                    timerAffichage.stop();
                    timerSpawnEnnemi.stop();
                    JOptionPane.showMessageDialog(frame, 
                        "GAME OVER!\nTemps survecu: " + secondesEcoulees + "s\nEnnemis: " + ennemis.size(), 
                        "Defaite", JOptionPane.ERROR_MESSAGE);
                }
                
                // === VÉRIFICATION VICTOIRE ===
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
    
    // === MÉTHODE POUR FAIRE APPARAÎTRE UN NOUVEL ENNEMI ===
    private static void spawnNouvelEnnemi() {
        int tentatives = 0;
        int maxTentatives = 100;
        
        while (tentatives < maxTentatives) {
            int x = random.nextInt(cols - 2) + 1;
            int y = random.nextInt(rows - 2) + 1;
            
            int distanceHeros = Math.abs(x - herosX) + Math.abs(y - herosY);
            if (maze[y][x] == PATH && distanceHeros > 5) {
                creerEnnemi(x, y, maze);
                System.out.println("Nouvel ennemi apparu en (" + x + ", " + y + ") ! Total: " + ennemis.size());
                return;
            }
            tentatives++;
        }
        
        int centreX = cols / 2;
        int centreY = rows / 2;
        if (maze[centreY][centreX] == PATH) {
            creerEnnemi(centreX, centreY, maze);
            System.out.println("Nouvel ennemi apparu au centre ! Total: " + ennemis.size());
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
        g.drawString("Heros: (" + herosX + ", " + herosY + ")  |  Ennemis: " + ennemis.size(), 10, rows * CELL_SIZE + 20);
        g.drawString("FLECHES: bouger | ECHAP: quitter | Sortie: coin bas-droite", 10, rows * CELL_SIZE + 40);
        
        int distanceMin = Integer.MAX_VALUE;
        for (int[] ennemi : ennemis) {
            int dist = Math.abs(ennemi[0] - herosX) + Math.abs(ennemi[1] - herosY);
            if (dist < distanceMin) distanceMin = dist;
        }
        if (distanceMin <= 3) {
            g.setColor(Color.RED);
            g.drawString("DANGER! Ennemi proche!", 10, rows * CELL_SIZE + 60);
        }
    }

    // === MÉTHODES DE GÉNÉRATION DU LABYRINTHE ===
    
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

        ArrayList<int[]> disconnectedCells = new ArrayList<>();

        for (int i = 1; i < rows - 1; i++) {
            for (int j = 1; j < cols - 1; j++) {
                if (maze[i][j] == PATH && !visited[i][j]) {
                    disconnectedCells.add(new int[]{i, j});
                }
            }
        }

        while (!disconnectedCells.isEmpty()) {
            int[] cell = disconnectedCells.get(0);

            ArrayList<int[]> region = new ArrayList<>();
            boolean[][] regionVisited = new boolean[rows][cols];
            Queue<int[]> queue = new LinkedList<>();
            queue.add(cell);
            regionVisited[cell[0]][cell[1]] = true;

            while (!queue.isEmpty()) {
                int[] current = queue.poll();
                region.add(current);
                int r = current[0], c = current[1];
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
        int r = from[0], c = from[1];
        while (c != to[1]) {
            maze[r][c] = PATH;
            c += (to[1] > c) ? 1 : -1;
        }
        while (r != to[0]) {
            maze[r][c] = PATH;
            r += (to[0] > r) ? 1 : -1;
        }
    }

    // === MÉTHODES DE JEU ===

    public static boolean blocage(int x, int y, int keyCode, int[][] carte) {
        int xSuiv = x, ySuiv = y;
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
        int newX = herosX, newY = herosY;
        if (!blocage(herosX, herosY, keyCode, carte)) {
            carte[herosY][herosX] = PATH;
            switch (keyCode) {
                case KeyEvent.VK_UP:    newY = herosY - 1; break;
                case KeyEvent.VK_DOWN:  newY = herosY + 1; break;
                case KeyEvent.VK_LEFT:  newX = herosX - 1; break;
                case KeyEvent.VK_RIGHT: newX = herosX + 1; break;
            }
            carte[newY][newX] = HERO;
        }
        return new int[]{newX, newY};
    }

    public static void creerEnnemi(int x, int y, int[][] carte) {
        carte[y][x] = ENEMY;
        ennemis.add(new int[]{x, y});
    }

    public static int[] deplacementEnnemi(int ex, int ey, int hx, int hy, int[][] carte) {
        int newEx = ex, newEy = ey;
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
                case "haut":    newEy--; break;
                case "bas":     newEy++; break;
                case "gauche":  newEx--; break;
                case "droite":  newEx++; break;
            }
            carte[newEy][newEx] = ENEMY;
        }
        return new int[]{newEx, newEy};
    }

    private static boolean blocageString(int x, int y, String direction, int[][] carte) {
        int xSuiv = x, ySuiv = y;
        switch (direction.toLowerCase()) {
            case "haut":    ySuiv = y - 1; break;
            case "bas":     ySuiv = y + 1; break;
            case "gauche":  xSuiv = x - 1; break;
            case "droite":  xSuiv = x + 1; break;
            default: return true;
        }
        if (ySuiv < 0 || ySuiv >= carte.length || xSuiv < 0 || xSuiv >= carte[0].length)
            return true;
        return carte[ySuiv][xSuiv] == WALL;
    }

    public static boolean verifierMort(int herosX, int herosY) {
        for (int[] ennemi : ennemis) {
            if (herosX == ennemi[0] && herosY == ennemi[1]) {
                System.out.println("\n==============================");
                System.out.println("  VOUS ETES MORT  ");
                System.out.println("==============================\n");
                return true;
            }
        }
        return false;
    }
    // ======================
    // === CLASSE PROJECTILE
    // ======================
    static class Projectile {
        int x, y;
        int dx, dy;

        Projectile(int x, int y, int dx, int dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }

        void move() {
            x += dx;
            y += dy;
        }
    }


    // ======================
    // === TIR PROJECTILE
    // ======================
    public static void tirerProjectile(int[][] lab, int[] heroPos, char touche) {

        if (munitions <= 0) {
            System.out.println("Plus de munitions !");
            return;
        }

        int x = heroPos[0];
        int y = heroPos[1];

        switch (Character.toLowerCase(touche)) {
            case 'z': projectiles.add(new Projectile(x - 1, y, -1, 0)); break;
            case 's': projectiles.add(new Projectile(x + 1, y, 1, 0)); break;
            case 'q': projectiles.add(new Projectile(x, y - 1, 0, -1)); break;
            case 'd': projectiles.add(new Projectile(x, y + 1, 0, 1)); break;
            default: return;
        }

        munitions--;
        System.out.println("Tir ! Munitions restantes : " + munitions);

        if (munitions == 0) {
            popMunitions(lab);
        }
    }


    // ======================
    // === MISE À JOUR PROJOS
    // ======================
    public static void updateProjectiles(int[][] lab) {

        Iterator<Projectile> it = projectiles.iterator();

        while (it.hasNext()) {
            Projectile p = it.next();
            p.move();

            if (p.x < 0 || p.y < 0 || p.x >= lab.length || p.y >= lab[0].length) {
                it.remove();
                continue;
            }

            if (lab[p.x][p.y] == 1) {
                it.remove();
                continue;
            }

            if (lab[p.x][p.y] == 3) {
                lab[p.x][p.y] = 0;
                System.out.println("Ennemi éliminé !");
                it.remove();
                continue;
            }
        }
    }


    // ======================
    // === POP MUNITIONS
    // ======================
    public static void popMunitions(int[][] lab) {

        int x, y;

        do {
            x = rand.nextInt(lab.length);
            y = rand.nextInt(lab[0].length);
        } while (lab[x][y] != 0);

        lab[x][y] = 4;
        System.out.println("Boîte de munitions apparue en : " + x + "," + y);
    }


    // ======================
    // === RAMASSAGE MUNITIONS
    // ======================
    public static void checkPickupMunitions(int[][] lab, int[] heroPos) {

        int x = heroPos[0];
        int y = heroPos[1];

        if (lab[x][y] == 4) {
            munitions += 5;
            lab[x][y] = 2;
            System.out.println("Munitions ramassées ! Total = " + munitions);
        }
}
}

