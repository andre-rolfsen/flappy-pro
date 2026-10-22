import java.awt.*;

public class Cloud {
    private int x;
    private final int y;
    private final int width;
    private final int height;
    private final int speed;

    public Cloud(int x, int y, int width, int height, int speed) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }

    public void update() {
        x -= speed;
        if (x + width < 0) {
            x = 400 + width;
        }
    }

    public void draw(Graphics2D g) {
        Color cloudColor = new Color(255, 255, 255, 190);
        g.setColor(cloudColor);

        int baseY = y + height / 3;
        g.fillOval(x, baseY, width / 2, height / 2);
        g.fillOval(x + width / 4, y, width / 2, height * 2 / 3);
        g.fillOval(x + width / 2, baseY, width / 2, height / 2);
        g.fillRoundRect(x + width / 8, baseY, width * 3 / 4, height / 2, 20, 20);
    }
}
