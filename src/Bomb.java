import java.awt.*;

public class Bomb {
    private int x;
    private int y;

    private static final int RADIUS = 13;
    private static final int SPEED = 2;

    public Bomb(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        x -= SPEED;
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(35, 35, 35));
        g.fillOval(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
        g.setColor(Color.DARK_GRAY);
        g.drawOval(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
        g.setColor(new Color(255, 120, 0));
        g.fillOval(x - 4, y - 4, 8, 8);
        g.setColor(new Color(120, 70, 30));
        g.setStroke(new BasicStroke(2));
        g.drawLine(x + 5, y - 9, x + 10, y - 16);
        g.setColor(Color.YELLOW);
        g.fillOval(x + 8, y - 18, 5, 5);
    }

    public boolean collidesWith(Rectangle bounds) {
        return new Rectangle(
                x - RADIUS, y - RADIUS,
                RADIUS * 2, RADIUS * 2
        ).intersects(bounds);
    }

    public boolean isOffScreen() {
        return x + RADIUS < 0;
    }
}
