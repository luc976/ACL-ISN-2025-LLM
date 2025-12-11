package maze.game;
import java.awt.*;

public class PowerUp {
    static final int SPEED=0, FREEZE=1, SHIELD=2;
    int x,y,type;

    public PowerUp(int x,int y,int type){
        this.x=x; this.y=y; this.type=type;
    }

    public void draw(Graphics g,int cell){
        switch(type){
            case SPEED -> g.setColor(Color.CYAN);
            case FREEZE -> g.setColor(Color.MAGENTA);
            case SHIELD -> g.setColor(Color.ORANGE);
        }
        g.fillRect(x*cell+2,y*cell+2,cell-4,cell-4);
    }

    public void activate(){
        switch(type){
            case SPEED -> MazeGenAccesTrial.speedBoost=true;
            case FREEZE -> MazeGenAccesTrial.freezeEnemies=true;
            case SHIELD -> MazeGenAccesTrial.shield=true;
        }
    }
}

