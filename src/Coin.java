import java.awt.*;

public class Coin {
    private int x;
    private int y;

    private static final int RADIUS = 10;
    private static final int SPEED = 2;
    private static final int MAGNET_RADIUS = 280;
    private static final int ATTRACT_SPEED = 6;

    public Coin(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() {
        x -= SPEED;
    }

    public void attractToBird(int birdX, int birdY) {
        int dx = birdX - x;
        int dy = birdY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < MAGNET_RADIUS && distance > 0) {
            x += (int) (dx / distance * ATTRACT_SPEED);
            y += (int) (dy / distance * ATTRACT_SPEED);
        }
    }

    public void draw(Graphics2D g) {
        g.setColor(new Color(255, 215, 0));
        g.fillOval(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
        g.setColor(new Color(255, 240, 120));
        g.fillOval(x - 6, y - 8, 5, 5);
        g.setColor(new Color(180, 130, 0));
        g.drawOval(x - RADIUS, y - RADIUS, RADIUS * 2, RADIUS * 2);
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
