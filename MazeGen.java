package MazeGen;

import java.util.Random;

import java.util.Scanner;

public class MazeGen {
	
	private static final int WALL = 1;
	private static final int PATH = 0;
	
	private static int rows;
	private static int cols;
	private static double initialWallChance = 0.45; // default density
	private static int[][] maze;
	private static Random random = new Random();
	
	public static void main(String[] args) {
	Scanner scanner = new Scanner(System.in);
	
	// Step 1: User chooses size
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

	        // Step 2: Randomly fill the map with walls/paths
	        fillRandomMaze();

	        // Step 3: Smooth map using Cellular Automata
	        int iterations = 5;
	        for (int k = 0; k < iterations; k++) {
	            maze = doSimulationStep(maze);
	        }

	        // Step 4: Ensure entrance/exit
	        maze[1][1] = PATH;                 // entrance
	        maze[rows - 2][cols - 2] = PATH;   // exit

	        // Step 5: Print cave
	        printMaze();
}

	    private static void fillRandomMaze() {
	        for (int i = 0; i < rows; i++) {
	            for (int j = 0; j < cols; j++) {
	                // Borders are always walls
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

	    private static void printMaze() {
	        for (int[] row : maze) {
	            for (int cell : row) {
	                System.out.print(cell == WALL ? "#" : " ");
	            }
	            System.out.println();
	        }
	    }
	}


