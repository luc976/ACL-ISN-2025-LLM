import java.awt.Graphics;

public class FloatingText {
    private String text;
    private int x; // pixel coords
    private int y;
    private int life = 40; // frames

    public FloatingText(String text, int x, int y) {
        this.text = text;
        this.x = x;
        this.y = y;
    }

    /**
     * Update returns true while still alive.
     */
    public boolean update() {
        y -= 1;     // move upward
        life -= 1;
        return life > 0;
    }

    public void draw(Graphics g) {
        g.drawString(text, x, y);
    }
}
