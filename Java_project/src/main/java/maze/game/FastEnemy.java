public class FastEnemy extends Enemy {
    FastEnemy(int x,int y){ super(x,y); }
    @Override
    public void moveToward(int hx,int hy,int[][] maze){
        // Move two steps per tick
        for(int i=0;i<2;i++){
            if(x<hx && maze[y][x+1]!=MazeGenAccesTrial.WALL) x++;
            else if(x>hx && maze[y][x-1]!=MazeGenAccesTrial.WALL) x--;
            if(y<hy && maze[y+1][x]!=MazeGenAccesTrial.WALL) y++;
            else if(y>hy && maze[y-1][x]!=MazeGenAccesTrial.WALL) y--;
        }
    }
}
