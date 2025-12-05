import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

public class MazeGenAccesTrial extends JPanel {

    static final int WALL = 0, PATH = 1, EXIT = 4;
    static final int POWER_SPEED = 5, POWER_FREEZE = 6, POWER_SHIELD = 7;
    static final int POWER_AMMO = 8;

    private static int rows, cols;
    private static int[][] maze;

    private static int herosX, herosY;

    private static boolean jeuEnCours = true;

    private static final int CELL_SIZE = 15;
    private static JFrame frame;
    private static MazeGenAccesTrial panel;

    private static JLabel timerLabel;
    private static int secondesEcoulees = 0;
    private static Timer timer;

    private static ArrayList<Projectile> projectiles = new ArrayList<>();
    private static int ammo = 5;
    private static int shotDirX = 0, shotDirY = -1;

    static boolean speedBoost = false;
    static boolean freezeEnemies = false;
    static boolean shield = false;

    private static Timer powerTimer;
    private static Timer enemySpawnTimer;
    private static Timer projectileTimer;

    private static int lastHeroDX = 0;
    private static int lastHeroDY = 0;

    private static ArrayList<Enemy> enemies = new ArrayList<>();
    private static Random rand = new Random();

    private static String[][] mapsChoisies;

    private JButton retryButton;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                afficherDifficulte();
            }
        });
    }

    // ------------------------ Choix difficulté ------------------------

    private static void afficherDifficulte() {
        JFrame difficulteFrame = new JFrame("Difficulté");
        difficulteFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        difficulteFrame.setSize(300, 200);
        difficulteFrame.setLocationRelativeTo(null);
        difficulteFrame.setLayout(new GridLayout(4, 1, 10, 10));

        JLabel lbl = new JLabel("Sélectionnez la difficulté :", SwingConstants.CENTER);
        JButton facileBtn = new JButton("Facile");
        JButton moyenBtn = new JButton("Moyen");
        JButton difficileBtn = new JButton("Difficile");

        facileBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mapsChoisies = maps_facile.maps_facile;
                rows = 32;
                cols = 32;
                difficulteFrame.dispose();
                afficherMenu();
            }
        });

        moyenBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mapsChoisies = maps_moyen.maps_moyen;
                rows = 64;
                cols = 64;
                difficulteFrame.dispose();
                afficherMenu();
            }
        });

        difficileBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mapsChoisies = maps_difficile.maps_difficile;
                rows = 128;
                cols = 128;
                difficulteFrame.dispose();
                afficherMenu();
            }
        });

        difficulteFrame.add(lbl);
        difficulteFrame.add(facileBtn);
        difficulteFrame.add(moyenBtn);
        difficulteFrame.add(difficileBtn);
        difficulteFrame.setVisible(true);
    }

    // ------------------------ Menu du choix de niveau ------------------------

    private static void afficherMenu() {
        JFrame menuFrame = new JFrame("Menu du jeu - MazeGen");
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setSize(300, 250);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setLayout(new GridLayout(6, 1, 10, 10));

        JLabel lblNiveau = new JLabel("Choisissez un niveau :", SwingConstants.CENTER);
        JComboBox<Integer> niveauBox = new JComboBox<Integer>();

        for (int i = 1; i <= mapsChoisies.length; i++)
            niveauBox.addItem(i);

        JButton jouerBtn = new JButton("Jouer !");
        jouerBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int niveauChoisi = (Integer) niveauBox.getSelectedItem();
                chargerMap(niveauChoisi - 1);
                menuFrame.dispose();
                createAndShowGUI();
            }
        });

        menuFrame.add(new JLabel(""));
        menuFrame.add(lblNiveau);
        menuFrame.add(niveauBox);
        menuFrame.add(new JLabel(""));
        menuFrame.add(jouerBtn);
        menuFrame.add(new JLabel(""));
        menuFrame.setVisible(true);
    }

    // ------------------------ Chargement d'une map ------------------------

    private static void chargerMap(int index) {
        maze = new int[rows][cols];
        enemies.clear();

        String[] mapSelectionnee = mapsChoisies[index];

        for (int i = 0; i < rows; i++) {
            String ligne = mapSelectionnee[i];
            for (int j = 0; j < cols; j++) {
                char c = ligne.charAt(j);
                if (c == '0')
                    maze[i][j] = WALL;
                else
                    maze[i][j] = PATH;
            }
        }

        herosX = 1;
        herosY = 1;

        placeRandomExit();
    }

    // ------------------------ Création fenêtre + timers ------------------------

    private static void createAndShowGUI() {

        jeuEnCours = true;

        projectileTimer = new Timer(60, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateProjectiles();
                panel.repaint();
            }
        });
        projectileTimer.start();

        Timer powerSpawn = new Timer(12000, new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                spawnPowerUp();
            }
        });
        powerSpawn.start();

        enemySpawnTimer = new Timer(4000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                spawnEnemy();
            }
        });
        enemySpawnTimer.start();

        frame = new JFrame("Maze Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new MazeGenAccesTrial();
        panel.setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE + 50));
        panel.setFocusable(true);
        panel.setupRetryButton();

        timerLabel = new JLabel("Temps: 0s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(timerLabel, BorderLayout.NORTH);

        secondesEcoulees = 0;
        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                secondesEcoulees++;
                timerLabel.setText("Temps: " + secondesEcoulees + "s");
            }
        });
        timer.start();

        // ---------------------- Gestion des touches ----------------------

        panel.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (!jeuEnCours)
                    return;

                int key = e.getKeyCode();

                if (key == KeyEvent.VK_Z) { shotDirX = 0; shotDirY = -1; }
                else if (key == KeyEvent.VK_S) { shotDirX = 0; shotDirY = 1; }
                else if (key == KeyEvent.VK_Q) { shotDirX = -1; shotDirY = 0; }
                else if (key == KeyEvent.VK_D) { shotDirX = 1; shotDirY = 0; }
                else if (key == KeyEvent.VK_SPACE) {
                    if (ammo > 0) {
                        projectiles.add(new Projectile(herosX, herosY, shotDirX, shotDirY));
                        ammo--;
                    }
                }

                int[] pos = mouvement(herosX, herosY, key, maze);
                herosX = pos[0];
                herosY = pos[1];

                if (key == KeyEvent.VK_UP) { lastHeroDX = 0; lastHeroDY = -1; }
                else if (key == KeyEvent.VK_DOWN) { lastHeroDX = 0; lastHeroDY = 1; }
                else if (key == KeyEvent.VK_LEFT) { lastHeroDX = -1; lastHeroDY = 0; }
                else if (key == KeyEvent.VK_RIGHT) { lastHeroDX = 1; lastHeroDY = 0; }

                for (Enemy en : enemies)
                    if (!freezeEnemies)
                        en.moveToward(herosX, herosY, maze);

                if (verifierMort(herosX, herosY)) {
                    if (shield) {
                        shield = false;
                        pushEnemyBack(herosX, herosY);
                    } else {
                        gameOverWithPopup();
                    }
                }

                int tile = maze[herosY][herosX];

                if (tile == POWER_SPEED) { activateSpeedBoost(); maze[herosY][herosX] = PATH; }
                else if (tile == POWER_FREEZE) { activateFreeze(); maze[herosY][herosX] = PATH; }
                else if (tile == POWER_SHIELD) { activateShield(); maze[herosY][herosX] = PATH; }
                else if (tile == POWER_AMMO) { ammo += 3; maze[herosY][herosX] = PATH; }

                if (tile == EXIT)
                    gameOverWithPopup(true);

                panel.repaint();
            }
        });

        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        panel.requestFocusInWindow();
    }
    // ------------------------ Mouvements du joueur ------------------------

    private static int[] mouvement(int hx, int hy, int keyCode, int[][] maze) {
        int newX = hx, newY = hy;
        int step = speedBoost ? 2 : 1;

        if (keyCode == KeyEvent.VK_UP) newY = hy - step;
        else if (keyCode == KeyEvent.VK_DOWN) newY = hy + step;
        else if (keyCode == KeyEvent.VK_LEFT) newX = hx - step;
        else if (keyCode == KeyEvent.VK_RIGHT) newX = hx + step;
        else return new int[]{hx, hy};

        if (newX < 0 || newY < 0 || newX >= cols || newY >= rows)
            return new int[]{hx, hy};

        if (maze[newY][newX] == WALL)
            return new int[]{hx, hy};

        return new int[]{newX, newY};
    }

    // ------------------------ Spawn ENNEMIS ------------------------

    private static void spawnEnemy() {
        int x, y;

        do {
            y = rand.nextInt(rows - 2) + 1;
            x = rand.nextInt(cols - 2) + 1;
        } while (maze[y][x] != PATH || distance(x, y, herosX, herosY) < 10);

        enemies.add(new Enemy(x, y));
    }

    private static double distance(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2));
    }

    // ------------------------ Vérifier collision avec ENNEMI ------------------------

    private static boolean verifierMort(int hx, int hy) {
        for (Enemy e : enemies) {
            if (e.x == hx && e.y == hy)
                return true;
        }
        return false;
    }

    // ------------------------ Push-back des ennemis ------------------------

    private static void pushEnemyBack(int hx, int hy) {
        ArrayList<Enemy> toRemove = new ArrayList<Enemy>();

        for (Enemy e : enemies) {
            if (Math.abs(e.x - hx) + Math.abs(e.y - hy) <= 1) {
                int dx = e.x - hx;
                int dy = e.y - hy;

                int newX = e.x + dx * 2;
                int newY = e.y + dy * 2;

                if (newX >= 0 && newY >= 0 && newX < cols && newY < rows && maze[newY][newX] == PATH) {
                    e.x = newX;
                    e.y = newY;
                } else {
                    toRemove.add(e);
                }
            }
        }

        enemies.removeAll(toRemove);
    }

    // ------------------------ EXIT (sortie du labyrinthe) ------------------------

    private static void placeRandomExit() {
        ArrayList<int[]> possible = new ArrayList<int[]>();

        for (int x = 1; x < cols - 1; x++) {
            if (maze[1][x] == PATH) possible.add(new int[]{x, 0});
            if (maze[rows - 2][x] == PATH) possible.add(new int[]{x, rows - 1});
        }
        for (int y = 1; y < rows - 1; y++) {
            if (maze[y][1] == PATH) possible.add(new int[]{0, y});
            if (maze[y][cols - 2] == PATH) possible.add(new int[]{cols - 1, y});
        }

        if (possible.size() == 0)
            return;

        int[] exitPos = possible.get(rand.nextInt(possible.size()));
        maze[exitPos[1]][exitPos[0]] = EXIT;
    }

    // ------------------------ Système de POWER-UPS ------------------------

    private static void spawnPowerUp() {
        int x, y;
        int type;

        do {
            x = rand.nextInt(cols - 2) + 1;
            y = rand.nextInt(rows - 2) + 1;
        } while (maze[y][x] != PATH);

        int r = rand.nextInt(4);

        if (r == 0) type = POWER_SPEED;
        else if (r == 1) type = POWER_FREEZE;
        else if (r == 2) type = POWER_SHIELD;
        else type = POWER_AMMO;

        maze[y][x] = type;
    }

    private static void activateSpeedBoost() {
        speedBoost = true;

        if (powerTimer != null) powerTimer.stop();

        powerTimer = new Timer(6000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                speedBoost = false;
            }
        });
        powerTimer.setRepeats(false);
        powerTimer.start();
    }

    private static void activateFreeze() {
        freezeEnemies = true;

        if (powerTimer != null) powerTimer.stop();

        powerTimer = new Timer(6000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                freezeEnemies = false;
            }
        });
        powerTimer.setRepeats(false);
        powerTimer.start();
    }

    private static void activateShield() {
        shield = true;

        if (powerTimer != null) powerTimer.stop();

        powerTimer = new Timer(8000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                shield = false;
            }
        });
        powerTimer.setRepeats(false);
        powerTimer.start();
    }

    // ------------------------ PROJECTILES ------------------------

    private static void updateProjectiles() {
        ArrayList<Projectile> toRemove = new ArrayList<Projectile>();
        ArrayList<Enemy> enemyDead = new ArrayList<Enemy>();

        for (Projectile p : projectiles) {
            if (!p.active) {
                toRemove.add(p);
                continue;
            }

            p.move();

            if (p.x < 0 || p.y < 0 || p.x >= cols || p.y >= rows) {
                p.active = false;
                toRemove.add(p);
                continue;
            }

            if (maze[p.y][p.x] == WALL) {
                p.active = false;
                toRemove.add(p);
                continue;
            }

            for (Enemy e : enemies) {
                if (p.x == e.x && p.y == e.y) {
                    enemyDead.add(e);
                    p.active = false;
                    toRemove.add(p);
                    break;
                }
            }
        }

        enemies.removeAll(enemyDead);
        projectiles.removeAll(toRemove);
    }
    // ------------------------ Rendu graphique ------------------------

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {

                int v = maze[y][x];

                if (v == WALL) g.setColor(Color.DARK_GRAY);
                else if (v == PATH) g.setColor(Color.BLACK);
                else if (v == EXIT) g.setColor(Color.GREEN);

                else if (v == POWER_SPEED) g.setColor(Color.CYAN);
                else if (v == POWER_FREEZE) g.setColor(Color.MAGENTA);
                else if (v == POWER_SHIELD) g.setColor(Color.ORANGE);
                else if (v == POWER_AMMO) g.setColor(Color.YELLOW);

                g.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }

        // Joueur
        g.setColor(shield ? Color.CYAN : Color.WHITE);
        g.fillOval(herosX * cellSize + 4, herosY * cellSize + 4, cellSize - 8, cellSize - 8);

        // Ennemis
        g.setColor(Color.RED);
        for (Enemy e : enemies) {
            g.fillRect(e.x * cellSize + 4, e.y * cellSize + 4, cellSize - 8, cellSize - 8);
        }

        // Projectiles
        g.setColor(Color.YELLOW);
        for (Projectile p : projectiles) {
            g.fillRect(p.x * cellSize + 6, p.y * cellSize + 6, cellSize - 12, cellSize - 12);
        }

        // Game Over
        if (gameOver) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("GAME OVER", getWidth()/2 - 120, getHeight()/2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Appuie sur R pour recommencer", getWidth()/2 - 150, getHeight()/2 + 40);
        }

        // Win
        if (win) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.drawString("VICTOIRE !", getWidth()/2 - 100, getHeight()/2);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Appuie sur R pour rejouer", getWidth()/2 - 110, getHeight()/2 + 40);
        }
    }

    // ------------------------ Enemy class ------------------------

    public static class Enemy {
        int x, y;

        public Enemy(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void update(int hx, int hy, int[][] maze) {
            if (freezeEnemies) return;

            int dx = 0, dy = 0;

            if (Math.random() < 0.5) {
                if (hx < x) dx = -1;
                else if (hx > x) dx = 1;
            } else {
                if (hy < y) dy = -1;
                else if (hy > y) dy = 1;
            }

            int nx = x + dx;
            int ny = y + dy;

            if (nx >= 0 && ny >= 0 && nx < cols && ny < rows && maze[ny][nx] != WALL) {
                x = nx;
                y = ny;
            }
        }
    }

    // ------------------------ Projectile class ------------------------

    public static class Projectile {
        int x, y;
        int dx, dy;
        boolean active = true;

        public Projectile(int x, int y, int dx, int dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }

        public void move() {
            x += dx;
            y += dy;
        }
    }

    // ------------------------ Launcher ------------------------

    public static void main(String[] args) {
        JFrame f = new JFrame("Maze Game Java 1.8");
        MazeGenAccesTrial panel = new MazeGenAccesTrial();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(panel);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
    }
}
