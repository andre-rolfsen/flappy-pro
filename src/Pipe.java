import java.awt.*;
import java.util.Random;

public class Pipe {

    private int x;
    private int topHeight;

    private final int width = 60;
    private final int screenHeight = 600;

    private final int speed;
    private final int gap;
    private final Color pipeColor;

    private boolean scored = false;

    public Pipe(int x, int speed, int gap, Color pipeColor) {
        this.x = x;
        this.speed = speed;
        this.gap = gap;
        this.pipeColor = pipeColor;

        Random random = new Random();

        int minimumTopHeight = 70;
        int maximumTopHeight = screenHeight - gap - 120;

        this.topHeight = random.nextInt(
                maximumTopHeight - minimumTopHeight
        ) + minimumTopHeight;
    }

    public void update() {
        x -= speed;
    }

    public void draw(Graphics2D g) {
        g.setColor(pipeColor);

        g.fillRect(x, 0, width, topHeight);

        g.setColor(darkenColor(pipeColor, 0.7f));
        g.fillRect(x - 4, topHeight - 20, width + 8, 20);

        int bottomY = topHeight + gap;

        g.setColor(pipeColor);
        g.fillRect(
                x,
                bottomY,
                width,
                screenHeight - bottomY
        );

        g.setColor(darkenColor(pipeColor, 0.7f));
        g.fillRect(x - 4, bottomY, width + 8, 20);
    }

    private Color darkenColor(Color color, float factor) {
        int red = (int) (color.getRed() * factor);
        int green = (int) (color.getGreen() * factor);
        int blue = (int) (color.getBlue() * factor);

        return new Color(
                Math.max(0, red),
                Math.max(0, green),
                Math.max(0, blue)
        );
    }

    public boolean collidesWith(Rectangle birdBounds) {
        Rectangle topPipe = new Rectangle(
                x,
                0,
                width,
                topHeight
        );

        Rectangle bottomPipe = new Rectangle(
                x,
                topHeight + gap,
                width,
                screenHeight - topHeight - gap
        );

        return topPipe.intersects(birdBounds)
                || bottomPipe.intersects(birdBounds);
    }

    public boolean isOffScreen() {
        return x + width < 0;
    }

    public int getX() {
        return x;
    }

    public int getWidth() {
        return width;
    }

    public int getTopHeight(){
        return topHeight;
    }

    public boolean isScored() {
        return scored;
    }

    public void setScored(boolean scored) {
        this.scored = scored;
    }
}