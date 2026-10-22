import java.awt.*;

public class PowerUp {

    public enum Type {
        SHIELD,
        MAGNET
    }

    private int x;
    private int y;
    private final Type type;

    private static final int RADIUS = 12;
    private static final int SPEED = 2;
    private static final int MAGNET_RADIUS = 280;

    public PowerUp(int x, int y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void update() {
        x -= SPEED;
    }

    public void attractToBird(int birdX, int birdY) {
        int dx = birdX - x;
        int dy = birdY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < MAGNET_RADIUS && distance > 0) {
            x += (int) (dx / distance * 4);
            y += (int) (dy / distance * 4);
        }
    }

    public void draw(Graphics2D g) {
        if (type == Type.SHIELD) {
            g.setColor(new Color(100, 180, 255));
        } else {
            g.setColor(new Color(255, 100, 180));
        }

        g.fillOval(
                x - RADIUS,
                y - RADIUS,
                RADIUS * 2,
                RADIUS * 2
        );

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));

        String label = type == Type.SHIELD ? "S" : "M";
        FontMetrics fm = g.getFontMetrics();

        g.drawString(
                label,
                x - fm.stringWidth(label) / 2,
                y + 4
        );
    }

    public boolean collidesWith(Rectangle birdBounds) {
        Rectangle bounds = new Rectangle(
                x - RADIUS,
                y - RADIUS,
                RADIUS * 2,
                RADIUS * 2
        );

        return bounds.intersects(birdBounds);
    }

    public boolean isOffScreen() {
        return x + RADIUS < 0;
    }

    public Type getType() {
        return type;
    }
}