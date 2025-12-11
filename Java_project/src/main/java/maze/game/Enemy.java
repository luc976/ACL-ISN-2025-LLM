package maze.game;

public class Enemy {
    public int x, y;          // current grid position
    public double animX, animY; // for smooth animation
    public boolean isKnockback = false;
    public int kbFrames = 0;

    public Enemy(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.animX = startX;
        this.animY = startY;
    }

    // Moves toward the hero using simple step logic
    public void moveToward(int heroX, int heroY, int[][] maze) {
        if (isKnockback) return; // <-- skip moving if being knocked back

        int dx = 0, dy = 0;
        if (heroX > x) dx = 1;
        else if (heroX < x) dx = -1;
        if (heroY > y) dy = 1;
        else if (heroY < y) dy = -1;

        // Try horizontal move first
        if (dx != 0 && canMoveTo(x + dx, y, maze)) x += dx;
        else if (dy != 0 && canMoveTo(x, y + dy, maze)) y += dy;
    }

    private boolean canMoveTo(int nx, int ny, int[][] maze) {
        if (nx < 0 || nx >= maze[0].length || ny < 0 || ny >= maze.length) return false;
        return maze[ny][nx] != MazeGenAccesTrial.WALL;
    }
}

