import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

@SuppressWarnings("serial")
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
    private static JLabel ammoLabel;
    private static int secondesEcoulees = 0;
    private static Timer timer;

    private static ArrayList<Projectile> projectiles = new ArrayList<Projectile>();
    private static int ammo = 5; // starting ammo
    private static int shotDirX = 0, shotDirY = -1; // initial shooting direction (up)

    static final int PORTAL = 9;

    static boolean speedBoost = false;
    static boolean freezeEnemies = false;
    static boolean shield = false;
    private static Timer powerTimer;
    private static Timer enemySpawnTimer;
    private static Timer projectileTimer;

    private static int lastHeroDX = 0;
    private static int lastHeroDY = 0;

    private static ArrayList<Enemy> enemies = new ArrayList<Enemy>();
    private static Random rand = new Random();

    private static String[][] mapsChoisies;

    private JButton retryButton;

    // Portal teleport safety
    private static boolean justTeleported = false;
    private static int teleDestX = -1, teleDestY = -1;

    // Floating texts for pickups
    private static ArrayList<FloatingText> floatingTexts = new ArrayList<FloatingText>();

    // ammo tile pulse ticker (for simple animation)
    private static int ammoPulseTick = 0;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                afficherDifficulte();
            }
        });
    }

    static class Portal {
        int x1, y1;
        int x2, y2;

        Portal(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    }

    static ArrayList<Portal> portals = new ArrayList<>();

    private static void afficherDifficulte() {
        final JFrame difficulteFrame = new JFrame("Choisissez la difficulté");
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

    private static void afficherMenu() {
        final JFrame menuFrame = new JFrame("🎮 Menu du jeu - MazeGen");
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setSize(300, 250);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.setLayout(new GridLayout(6, 1, 10, 10));

        JLabel lblNiveau = new JLabel("Choisissez un niveau :", SwingConstants.CENTER);
        final JComboBox<Integer> niveauBox = new JComboBox<Integer>();
        for (int i = 1; i <= mapsChoisies.length; i++)
            niveauBox.addItem(i);

        JButton jouerBtn = new JButton("🚀 Jouer !");
        jouerBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int niveauChoisi = (Integer) niveauBox.getSelectedItem();
                maze = new int[rows][cols];
                enemies.clear();
                String[] mapSelectionnee = mapsChoisies[niveauChoisi - 1];
                for (int i = 0; i < rows; i++)
                    for (int j = 0; j < cols; j++)
                        maze[i][j] = mapSelectionnee[i].charAt(j) == '0' ? WALL : PATH;

                herosX = 1;
                herosY = 1;
                placeRandomExit();
                placePortals();

                // reset gameplay state
                ammo = 5;
                projectiles.clear();
                justTeleported = false;
                teleDestX = teleDestY = -1;
                floatingTexts.clear();

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

    private static void createAndShowGUI() {
        jeuEnCours = true;

        frame = new JFrame("Maze Game 🧩");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new MazeGenAccesTrial();
        panel.setPreferredSize(new Dimension(cols * CELL_SIZE, rows * CELL_SIZE + 50));
        panel.setFocusable(true);
        panel.setupRetryButton();

        timerLabel = new JLabel("Temps: 0s", SwingConstants.LEFT);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 16));

        ammoLabel = new JLabel("Ammo: " + ammo, SwingConstants.RIGHT);
        ammoLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // top panel to hold timer and ammo counter
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(timerLabel, BorderLayout.WEST);
        topPanel.add(ammoLabel, BorderLayout.EAST);
        frame.add(topPanel, BorderLayout.NORTH);

        secondesEcoulees = 0;
        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                secondesEcoulees++;
                timerLabel.setText("Temps: " + secondesEcoulees + "s");
            }
        });
        timer.start();

        // projectile timer (moves projectiles independently) - also drive floating texts and small animations
        projectileTimer = new Timer(60, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ammoPulseTick = (ammoPulseTick + 1) % 60;
                updateProjectiles();
                updateFloatingTexts();
                panel.repaint();
            }
        });
        projectileTimer.start();

        // power-up spawn timer
        Timer powerSpawn = new Timer(12000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                spawnPowerUp();
            }
        });
        powerSpawn.start();

        // enemy spawn timer: spawn first enemy immediately and then periodic
        spawnEnemy();
        enemySpawnTimer = new Timer(5000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                spawnEnemy();
            }
        });
        enemySpawnTimer.start();

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!jeuEnCours)
                    return;

                int code = e.getKeyCode();

                // Direction keys for shooting (Z Q S D layout)
                if (code == KeyEvent.VK_Z) {
                    shotDirX = 0;
                    shotDirY = -1;
                } else if (code == KeyEvent.VK_S) {
                    shotDirX = 0;
                    shotDirY = 1;
                } else if (code == KeyEvent.VK_Q) {
                    shotDirX = -1;
                    shotDirY = 0;
                } else if (code == KeyEvent.VK_D) {
                    shotDirX = 1;
                    shotDirY = 0;
                } else if (code == KeyEvent.VK_SPACE) {
                    if (ammo > 0) {
                        projectiles.add(new Projectile(herosX, herosY, shotDirX, shotDirY));
                        ammo--;
                        if (ammoLabel != null)
                            ammoLabel.setText("Ammo: " + ammo);
                    }
                }

                // Hero movement
                int[] pos = mouvement(herosX, herosY, code, maze);
                herosX = pos[0];
                herosY = pos[1];

                // Portal teleportation (after movement)
                // Use justTeleported to avoid immediate bounce-back issues
                if (justTeleported) {
                    // if player moved off the teleport destination portal, clear the flag
                    if (herosX != teleDestX || herosY != teleDestY) {
                        justTeleported = false;
                    } else {
                        // still on the destination portal this keypress: do not re-teleport
                    }
                }

                if (!justTeleported) {
                    for (Portal p : portals) {
                        if (herosX == p.x1 && herosY == p.y1) {
                            herosX = p.x2;
                            herosY = p.y2;
                            justTeleported = true;
                            teleDestX = herosX;
                            teleDestY = herosY;
                            break;
                        } else if (herosX == p.x2 && herosY == p.y2) {
                            herosX = p.x1;
                            herosY = p.y1;
                            justTeleported = true;
                            teleDestX = herosX;
                            teleDestY = herosY;
                            break;
                        }
                    }
                }

                // Track last hero movement (for knockback)
                if (code == KeyEvent.VK_UP) {
                    lastHeroDX = 0;
                    lastHeroDY = -1;
                } else if (code == KeyEvent.VK_DOWN) {
                    lastHeroDX = 0;
                    lastHeroDY = 1;
                } else if (code == KeyEvent.VK_LEFT) {
                    lastHeroDX = -1;
                    lastHeroDY = 0;
                } else if (code == KeyEvent.VK_RIGHT) {
                    lastHeroDX = 1;
                    lastHeroDY = 0;
                }

                // Enemy movement
                for (Enemy en : new ArrayList<Enemy>(enemies)) {
                    if (!freezeEnemies)
                        en.moveToward(herosX, herosY, maze);
                }

                // Hero death / shield
                if (verifierMort(herosX, herosY)) {
                    if (shield) {
                        shield = false;
                        pushEnemyBack(herosX, herosY);
                    } else {
                        gameOverWithPopup();
                    }
                }

                // Power-ups pickup
                int tile = maze[herosY][herosX];
                if (tile == POWER_SPEED) {
                    activateSpeedBoost();
                    maze[herosY][herosX] = PATH;
                } else if (tile == POWER_FREEZE) {
                    activateFreeze();
                    maze[herosY][herosX] = PATH;
                } else if (tile == POWER_SHIELD) {
                    activateShield();
                    maze[herosY][herosX] = PATH;
                } else if (tile == POWER_AMMO) {
                    ammo += 3;
                    maze[herosY][herosX] = PATH;
                    if (ammoLabel != null)
                        ammoLabel.setText("Ammo: " + ammo);

                    // floating text centered on the hero tile (pixel coords)
                    int px = herosX * CELL_SIZE + CELL_SIZE / 4;
                    int py = herosY * CELL_SIZE;
                    floatingTexts.add(new FloatingText("+3 Ammo", px, py));
                }

                if (tile == EXIT) {
                    gameOverWithPopup(true);
                }

                panel.repaint();
            }
        });

        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        panel.requestFocusInWindow();
    }

    // ---------------- Power-ups ----------------
    private static void activateSpeedBoost() {
        speedBoost = true;
        if (powerTimer != null)
            powerTimer.stop();
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
        if (powerTimer != null)
            powerTimer.stop();
        powerTimer = new Timer(5000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                freezeEnemies = false;
            }
        });
        powerTimer.setRepeats(false);
        powerTimer.start();
    }

    private static void activateShield() {
        shield = true;
        if (powerTimer != null)
            powerTimer.stop();
        powerTimer = new Timer(10000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                shield = false;
            }
        });
        powerTimer.setRepeats(false);
        powerTimer.start();
    }

    private static void spawnPowerUp() {
        int x = 0, y = 0, tries = 0;
        int[] types = { POWER_SPEED, POWER_FREEZE, POWER_SHIELD, POWER_AMMO };
        do {
            x = rand.nextInt(cols);
            y = rand.nextInt(rows);
            tries++;
        } while (tries < 200 && maze[y][x] != PATH);
        if (tries < 200)
            maze[y][x] = types[rand.nextInt(types.length)];
    }

    // ---------------- Exit ----------------
    private static void placeRandomExit() {
        ArrayList<int[]> possibleExits = new ArrayList<int[]>();

        // Top and bottom borders
        for (int x = 1; x < cols - 1; x++) {
            if (maze[1][x] == PATH)
                possibleExits.add(new int[] { x, 0 });
            if (maze[rows - 2][x] == PATH)
                possibleExits.add(new int[] { x, rows - 1 });
        }

        // Left and right borders
        for (int y = 1; y < rows - 1; y++) {
            if (maze[y][1] == PATH)
                possibleExits.add(new int[] { 0, y });
            if (maze[y][cols - 2] == PATH)
                possibleExits.add(new int[] { cols - 1, y });
        }

        if (!possibleExits.isEmpty()) {
            int[] exitPos = possibleExits.get(rand.nextInt(possibleExits.size()));
            maze[exitPos[1]][exitPos[0]] = EXIT;
        }
    }

    // ---------------- Enemy ----------------
    private static void spawnEnemy() {
        int x = 0, y = 0, tries = 0;
        do {
            x = rand.nextInt(cols);
            y = rand.nextInt(rows);
            tries++;
        } while (tries < 200 && (maze[y][x] != PATH || (Math.abs(x - herosX) < 3 && Math.abs(y - herosY) < 3)));
        if (tries < 200)
            enemies.add(new Enemy(x, y));
    }

    private static boolean verifierMort(int hx, int hy) {
        for (Enemy e : enemies)
            if (e.x == hx && e.y == hy)
                return true;
        return false;
    }

    private static void pushEnemyBack(int hx, int hy) {
        for (Enemy e : enemies) {
            if (e.x == hx && e.y == hy) {
                int dx = -lastHeroDX;
                int dy = -lastHeroDY;
                if (dx == 0 && dy == 0)
                    dy = 1;
                int push = 3;
                int newX = e.x;
                int newY = e.y;
                for (int i = 0; i < push; i++) {
                    int nextX = newX + dx;
                    int nextY = newY + dy;
                    if (nextX < 0 || nextX >= cols || nextY < 0 || nextY >= rows)
                        break;
                    if (maze[nextY][nextX] == WALL)
                        break;
                    newX = nextX;
                    newY = nextY;
                }
                animateKnockback(e, newX, newY);
            }
        }
    }

    private static void animateKnockback(final Enemy e, final int targetX, final int targetY) {
        e.isKnockback = true;
        e.kbFrames = 8;
        final double dx = (targetX - e.animX) / e.kbFrames;
        final double dy = (targetY - e.animY) / e.kbFrames;

        final Timer anim = new Timer(20, null);
        anim.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                if (e.kbFrames <= 0) {
                    e.isKnockback = false;
                    e.x = (int) (e.animX = targetX);
                    e.y = (int) (e.animY = targetY);
                    anim.stop();
                    panel.repaint();
                    return;
                }
                e.animX += dx;
                e.animY += dy;
                e.kbFrames--;
                panel.repaint();
            }
        });
        anim.start();
    }

    public static int[] mouvement(int hx, int hy, int keyCode, int[][] carte) {
        int step = speedBoost ? 2 : 1;
        int newX = hx, newY = hy;
        if (keyCode == KeyEvent.VK_UP)
            newY = hy - step;
        else if (keyCode == KeyEvent.VK_DOWN)
            newY = hy + step;
        else if (keyCode == KeyEvent.VK_LEFT)
            newX = hx - step;
        else if (keyCode == KeyEvent.VK_RIGHT)
            newX = hx + step;
        else
            return new int[] { hx, hy };

        if (newY < 0 || newY >= carte.length || newX < 0 || newX >= carte[0].length)
            return new int[] { hx, hy };
        if (carte[newY][newX] == WALL)
            return new int[] { hx, hy };
        return new int[] { newX, newY };
    }

    // ---------------- Retry/GameOver ----------------
    private void setupRetryButton() {
        retryButton = new JButton("Retry");
        retryButton.setVisible(false);
        retryButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showRetryPopup();
            }
        });
        this.add(retryButton);
    }

    private static void showRetryPopup() {
        int result = JOptionPane.showConfirmDialog(frame, "Voulez-vous rejouer?", "Retry", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION)
            fadeOutAndRestart();
        else
            frame.dispose();
    }

    private static void fadeOutAndRestart() {
        new Thread(new Runnable() {
            public void run() {

                if (timer != null)
                    timer.stop();
                if (enemySpawnTimer != null)
                    enemySpawnTimer.stop();
                if (projectileTimer != null)
                    projectileTimer.stop();

                // ⭐ FIX: reset projectile system
                if (projectiles != null) {
                    projectiles.clear();
                }
                ammo = 5; // reset starting ammo

                enemies.clear();
                panel = null;

                if (frame != null)
                    frame.dispose();
                frame = null;

                jeuEnCours = true;
                secondesEcoulees = 0;

                MazeGenAccesTrial.main(new String[] {});
            }

        }).start();
    }

    private static void gameOverWithPopup() {
        gameOverWithPopup(false);
    }

    private static void gameOverWithPopup(boolean victory) {
        jeuEnCours = false;
        if (timer != null)
            timer.stop();
        if (enemySpawnTimer != null)
            enemySpawnTimer.stop();
        if (projectileTimer != null)
            projectileTimer.stop();
        if (victory)
            JOptionPane.showMessageDialog(frame, "🎉 VICTOIRE 🎉", "Bravo!", JOptionPane.INFORMATION_MESSAGE);
        else
            JOptionPane.showMessageDialog(frame, "💀 GAME OVER 💀", "Défaite", JOptionPane.ERROR_MESSAGE);
        showRetryPopup();
    }

    // ---------------- Paint ----------------
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (maze == null)
            return;

        // Draw portals first (pink)
        g.setColor(Color.PINK);
        for (Portal p : portals) {
            g.fillRect(p.x1 * CELL_SIZE, p.y1 * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            g.fillRect(p.x2 * CELL_SIZE, p.y2 * CELL_SIZE, CELL_SIZE, CELL_SIZE);
        }

        // Dessiner les cases
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int x = j * CELL_SIZE, y = i * CELL_SIZE;
                int tile = maze[i][j];
                if (tile == WALL)
                    g.setColor(Color.BLACK);
                else if (tile == EXIT)
                    g.setColor(Color.GREEN);
                else if (tile == POWER_SPEED)
                    g.setColor(Color.CYAN);
                else if (tile == POWER_FREEZE)
                    g.setColor(Color.MAGENTA);
                else if (tile == POWER_SHIELD)
                    g.setColor(Color.ORANGE);
                else if (tile == PORTAL)
                    g.setColor(Color.PINK); // 🟪 Portail
                else
                    g.setColor(Color.WHITE);
                g.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                // Draw ammo pickup with pulsing icon if tile is POWER_AMMO
                if (tile == POWER_AMMO) {
                    // pulsing radius
                    double pulse = Math.sin(ammoPulseTick * 0.15) * 2.0; // -2 .. 2
                    int s = (int)(CELL_SIZE * 0.6 + pulse);
                    int cx = x + (CELL_SIZE - s) / 2;
                    int cy = y + (CELL_SIZE - s) / 2;

                    // yellow circle + small black ammo rectangle
                    g.setColor(Color.YELLOW);
                    g.fillOval(cx, cy, s, s);

                    g.setColor(Color.BLACK);
                    int rectW = Math.max(4, s/3);
                    int rectH = Math.max(3, s/4);
                    g.fillRect(x + CELL_SIZE/2 - rectW/2, y + CELL_SIZE/2 - rectH/2, rectW, rectH);
                }
            }
        }


        // projectiles
        g.setColor(Color.MAGENTA);
        for (Projectile p : projectiles) {
            g.fillOval(p.x * CELL_SIZE + 5, p.y * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
        }

        // hero
        g.setColor(Color.BLUE);
        g.fillOval(herosX * CELL_SIZE + 2, herosY * CELL_SIZE + 2, CELL_SIZE - 4, CELL_SIZE - 4);

        // enemies
        g.setColor(Color.RED);
        for (Enemy e : enemies) {
            double drawX = e.isKnockback ? e.animX : e.x;
            double drawY = e.isKnockback ? e.animY : e.y;
            g.fillOval((int) (drawX * CELL_SIZE + 2), (int) (drawY * CELL_SIZE + 2), CELL_SIZE - 4, CELL_SIZE - 4);
        }

        // floating texts
        g.setColor(Color.BLACK);
        for (FloatingText ft : floatingTexts) {
            ft.draw(g);
        }
    }

    // ---------------- Projectiles ----------------
    private static void updateProjectiles() {
        ArrayList<Projectile> toRemove = new ArrayList<Projectile>();
        for (Projectile p : new ArrayList<Projectile>(projectiles)) {
            p.move(maze);

            // check collision with enemies
            for (Enemy en : new ArrayList<Enemy>(enemies)) {
                if (p.x == en.x && p.y == en.y) {
                    enemies.remove(en);
                    p.active = false;
                    break;
                }
            }

            if (!p.active)
                toRemove.add(p);
        }
        projectiles.removeAll(toRemove);
    }

    // ---------------- Floating texts ----------------
    private static void updateFloatingTexts() {
        for (int i = 0; i < floatingTexts.size(); i++) {
            boolean alive = floatingTexts.get(i).update();
            if (!alive) {
                floatingTexts.remove(i);
                i--;
            }
        }
    }

    // ---------------- Portails ----------------
    private static void placePortals() {
        portals.clear();
        int numPortals = 1;
        if (rows >= 64 && rows < 128)
            numPortals = 2;
        else if (rows >= 128)
            numPortals = 4;

        for (int i = 0; i < numPortals; i++) {
            int x1 = 0, y1 = 0, x2 = 0, y2 = 0;
            int tries = 0;
            do {
                x1 = rand.nextInt(cols);
                y1 = rand.nextInt(rows);
                x2 = rand.nextInt(cols);
                y2 = rand.nextInt(rows);
                tries++;
            } while (tries < 200 && (maze[y1][x1] != PATH || maze[y2][x2] != PATH));

            if (tries < 200) {
                maze[y1][x1] = PORTAL;
                maze[y2][x2] = PORTAL;
                portals.add(new Portal(x1, y1, x2, y2));
            }
        }
    }

}
