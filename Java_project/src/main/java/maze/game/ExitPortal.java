package maze.game;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;


public class ExitPortal {
    int x,y;
    double pulse=0;
    boolean growing=true;

    public ExitPortal(int[][] maze, Random rand){
        // pick a random border tile that is PATH
        ArrayList<int[]> candidates = new ArrayList<>();
        int rows=maze.length, cols=maze[0].length;
        for(int i=1;i<cols-1;i++){
            if(maze[1][i]==MazeGenAccesTrial.PATH) candidates.add(new int[]{i,0});
            if(maze[rows-2][i]==MazeGenAccesTrial.PATH) candidates.add(new int[]{i,rows-1});
        }
        for(int i=1;i<rows-1;i++){
            if(maze[i][1]==MazeGenAccesTrial.PATH) candidates.add(new int[]{0,i});
            if(maze[i][cols-2]==MazeGenAccesTrial.PATH) candidates.add(new int[]{cols-1,i});
        }
        int[] pos = candidates.get(rand.nextInt(candidates.size()));
        x=pos[0]; y=pos[1];
    }

    public boolean isAt(int hx,int hy){ return hx==x && hy==y; }

    public void draw(Graphics g,int cell){
        pulse += (growing?0.1:-0.1);
        if(pulse>5) growing=false;
        if(pulse<0) growing=true;

        g.setColor(Color.GREEN);
        int s = cell + (int)pulse;
        g.fillOval(x*cell - (s-cell)/2, y*cell - (s-cell)/2, s, s);
    }
}


