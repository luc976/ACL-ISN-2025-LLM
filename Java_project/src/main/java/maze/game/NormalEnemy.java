public class NormalEnemy extends Enemy {
    NormalEnemy(int x,int y){ super(x,y); }
    @Override
    public void moveToward(int hx,int hy,int[][] maze){
        if(x<hx && maze[y][x+1]!=MazeGenAccesTrial.WALL) x++;
        else if(x>hx && maze[y][x-1]!=MazeGenAccesTrial.WALL) x--;
        if(y<hy && maze[y+1][x]!=MazeGenAccesTrial.WALL) y++;
        else if(y>hy && maze[y-1][x]!=MazeGenAccesTrial.WALL) y--;
    }
}
