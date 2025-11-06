package MazeGen;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;

public class MazeGenAcces {
		private int heroX = 1; // position du héros
    	private int heroY = 1;
    	private int ennemiX = 8; // position de l’ennemi
    	private int ennemiY = 8;


	    private static final int WALL = 1;
	    private static final int PATH = 0;

	    private static int rows;
	    private static int cols;
	    private static double initialWallChance = 0.45;
	    private static int[][] maze;
	    private static Random random = new Random();

	    public static void main(String[] args) {
	        Scanner scanner = new Scanner(System.in);

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

	        fillRandomMaze();

	        int iterations = 5;
	        for (int k = 0; k < iterations; k++) {
	            maze = doSimulationStep(maze);
	        }

	        maze[1][1] = PATH; // entrance
	        maze[rows - 2][cols - 2] = PATH; // exit

	        connectRegions();

	        printMaze();
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

	    // Connect all isolated path regions to the main region reachable from the entrance
	    private static void connectRegions() {
	        boolean[][] visited = new boolean[rows][cols];
	        bfs(1, 1, visited);

	        // List of unvisited path cells (disconnected areas)
	        List<int[]> disconnectedCells = new ArrayList<>();
	        for (int i = 1; i < rows - 1; i++) {
	            for (int j = 1; j < cols - 1; j++) {
	                if (maze[i][j] == PATH && !visited[i][j]) {
	                    disconnectedCells.add(new int[]{i, j});
	                }
	            }
	        }

	        // While disconnected cells remain, connect them
	        while (!disconnectedCells.isEmpty()) {
	            // Pick one disconnected cell's region
	            int[] cell = disconnectedCells.get(0);

	            // Find all cells in this disconnected region using BFS
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

	            // Find the closest pair of cells between this region and the main reachable area
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

	            // Carve a path between bestRegionCell and bestVisitedCell
	            carvePath(bestRegionCell, bestVisitedCell);

	            // Update visited with new connectivity
	            bfs(1, 1, visited);

	            // Update disconnectedCells list
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

	    // BFS to mark reachable path cells from start
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

	    // Carve a simple direct path between two points
	    private static void carvePath(int[] from, int[] to) {
	        int r = from[0];
	        int c = from[1];

	        // Carve horizontally first
	        while (c != to[1]) {
	            maze[r][c] = PATH;
	            c += (to[1] > c) ? 1 : -1;
	        }
	        // Carve vertically
	        while (r != to[0]) {
	            maze[r][c] = PATH;
	            r += (to[0] > r) ? 1 : -1;
	        }
	    }

	    private static void printMaze() {
	        for (int[] row : maze) {
	            for (int cell : row) {
	                System.out.print(cell == WALL ? "#" : " ");
	            }
	            System.out.println();
	        }
	    }
		labyrinthe[heroY][heroX] = 2; // héros
        labyrinthe[ennemiY][ennemiX] = 3; // ennemi

        // Écouteur de touches clavier
        this.setFocusable(true);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                deplacerHeros(e.getKeyCode());
            }
        });
	}
// Déplace le héros selon la touche pressée
    private void deplacerHeros(int keyCode) {
        int nouvelleX = heroX;
        int nouvelleY = heroY;

        switch (keyCode) {
            case KeyEvent.VK_LEFT:
                nouvelleX--;
                break;
            case KeyEvent.VK_RIGHT:
                nouvelleX++;
                break;
            case KeyEvent.VK_UP:
                nouvelleY--;
                break;
            case KeyEvent.VK_DOWN:
                nouvelleY++;
                break;
        }

        if (estValide(nouvelleX, nouvelleY)) {
            labyrinthe[heroY][heroX] = 0;
            heroX = nouvelleX;
            heroY = nouvelleY;
            labyrinthe[heroY][heroX] = 2;

            deplacerEnnemi(); // l'ennemi bouge à son tour
            repaint();
        }
    }

    // Déplace l’ennemi d’une case vers le héros
    private void deplacerEnnemi() {
        labyrinthe[ennemiY][ennemiX] = 0;

        int dx = Integer.compare(heroX, ennemiX); // -1, 0 ou 1
        int dy = Integer.compare(heroY, ennemiY);

        int nouvelleX = ennemiX;
        int nouvelleY = ennemiY;

        // On essaie de bouger vers le héros (priorité sur l'axe X)
        if (Math.abs(heroX - ennemiX) > Math.abs(heroY - ennemiY)) {
            if (estValide(ennemiX + dx, ennemiY)) {
                nouvelleX += dx;
            } else if (estValide(ennemiX, ennemiY + dy)) {
                nouvelleY += dy;
            }
        } else {
            if (estValide(ennemiX, ennemiY + dy)) {
                nouvelleY += dy;
            } else if (estValide(ennemiX + dx, ennemiY)) {
                nouvelleX += dx;
            }
        }

        ennemiX = nouvelleX;
        ennemiY = nouvelleY;

        // Si l’ennemi touche le héros
        if (ennemiX == heroX && ennemiY == heroY) {
            JOptionPane.showMessageDialog(this, "💀 L’ennemi vous a attrapé !");
            System.exit(0);
        }

        labyrinthe[ennemiY][ennemiX] = 3;
    }

    // Vérifie si la case est libre (pas un mur)
    private boolean estValide(int x, int y) {
        return x >= 0 && x < COLONNES && y >= 0 && y < LIGNES && labyrinthe[y][x] != 1;
    }
	    
