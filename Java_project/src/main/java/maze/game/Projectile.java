package maze.game;
public class Projectile {
    int x, y;
    int dirX, dirY;
    boolean active = true;
    private static final int SPEED = 1;

    public Projectile(int startX, int startY, int dirX, int dirY){
        this.x = startX;
        this.y = startY;
        this.dirX = dirX;
        this.dirY = dirY;
    }

    public void move(int[][] maze){
        int newX = x + dirX * SPEED;
        int newY = y + dirY * SPEED;

        // check bounds
        if(newX < 0 || newX >= maze[0].length || newY < 0 || newY >= maze.length) {
            active = false;
            return;
        }

        // stop if hits wall
        if(maze[newY][newX] == MazeGenAccesTrial.WALL){
            active = false;
            return;
        }

        x = newX;
        y = newY;
    }
}

