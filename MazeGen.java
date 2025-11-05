package MazeGen;

import java.util.*;

public class MazeGenAcces {


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
}
	    /**
       * Vérifie si le déplacement du héros est bloqué.
       *
       * @param x         position actuelle du héros (colonne)
       * @param y         position actuelle du héros (ligne)
       * @param direction "haut", "bas", "gauche" ou "droite"
       * @param carte     matrice du terrain (0 = vide, 1 = mur)
       * @return true si le déplacement est bloqué, false sinon
       */
	    public static boolean blocage(int x, int y, String direction, int[][] carte) {

        int xSuiv = x;
        int ySuiv = y;

        // Calcul de la case suivante selon la direction
        switch (direction.toLowerCase()) {
            case "haut":
                ySuiv = y - 1;
                break;
            case "bas":
                ySuiv = y + 1;
                break;
            case "gauche":
                xSuiv = x - 1;
                break;
            case "droite":
                xSuiv = x + 1;
                break;
            default:
                // direction inconnue = bloqué
                return true;
        }

        // Vérifie les bords de la carte
        if (ySuiv < 0 || ySuiv >= carte.length || xSuiv < 0 || xSuiv >= carte[0].length) {
            return true; // en dehors de la carte
        }

        // Vérifie la présence d’un mur (1 = obstacle)
        if (carte[ySuiv][xSuiv] == 1) {
            return true; // bloqué par un mur
        }

        // Sinon, déplacement possible
        return false;
    }
	 // --- Fonction de création de l'ennemi ---
    public static int[] creerEnnemi(int x, int y, int[][] carte) {
        carte[y][x] = 3; // 3 = ennemi
        return new int[]{x, y};
    }

    // --- Déplacement de l’ennemi vers le héros ---
    public static int[] deplacementEnnemi(int ex, int ey, int hx, int hy, int[][] carte) {
        int newEx = ex;
        int newEy = ey;

        // Déterminer la direction la plus proche du héros
        String direction = null;

        if (Math.abs(hx - ex) > Math.abs(hy - ey)) {
            // se rapprocher horizontalement
            if (hx < ex) direction = "gauche";
            else if (hx > ex) direction = "droite";
        } else {
            // se rapprocher verticalement
            if (hy < ey) direction = "haut";
            else if (hy > ey) direction = "bas";
        }

        // Vérifie si le déplacement est possible
        if (direction != null && !blocage(ex, ey, direction, carte)) {
            carte[ey][ex] = 0; // effacer ancienne position
            switch (direction) {
                case "haut":    newEy--; break;
                case "bas":     newEy++; break;
                case "gauche":  newEx--; break;
                case "droite":  newEx++; break;
            }
            carte[newEy][newEx] = 3; // nouvelle position de l’ennemi
        }

        return new int[]{newEx, newEy};
    }
	// --- Vérifie si le héros meurt ---
    public static boolean verifierMort(int herosX, int herosY, int ennemiX, int ennemiY) {
        if (herosX == ennemiX && herosY == ennemiY) {
            System.out.println("\n==============================");
            System.out.println("💀💀💀  VOUS ÊTES MORT  💀💀💀");
            System.out.println("==============================\n");
            return true;
        }
        return false;
    }





