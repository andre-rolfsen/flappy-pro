import java.awt.*;

public class Bird {
    private int x, y;
    private double velocity;
    private static final int WIDTH = 34;
    private static final int HEIGHT = 24;
    private static final double GRAVITY = 0.5;
    private static final double JUMP_FORCE = -9.0;
    private static final int MAGNET_DURATION = 6000;

    private static final Color NORMAL_COLOR = new Color(255, 215, 0);
    private static final Color SHIELD_COLOR = new Color(50, 150, 255);
    private static final Color MAGNET_COLOR = new Color(230, 40, 40);

    private boolean hasShield;
    private boolean hasMagnet;
    private long magnetEndTime;
    private Color birdColor;
    private Hat.Type equippedHat = Hat.Type.NONE;

    public Bird(int x, int y) {
        this.x = x;
        this.y = y;
        this.birdColor = NORMAL_COLOR;
    }

    public void update() {
        velocity += GRAVITY;
        y += (int) velocity;

        if (hasMagnet && System.currentTimeMillis() > magnetEndTime) {
            hasMagnet = false;
            birdColor = hasShield ? SHIELD_COLOR : NORMAL_COLOR;
        }
    }

    public void jump() {
        velocity = JUMP_FORCE;
    }

    public void draw(Graphics2D g) {
        g.setColor(birdColor);
        g.fillRect(x, y, WIDTH, HEIGHT);

        g.setColor(Color.BLACK);
        g.fillRect(x + 23, y + 7, 5, 5);

        g.setColor(brightenColor(birdColor, 1.15f));
        g.fillRect(x + 5, y + HEIGHT / 2, 16, 10);

        g.setColor(darkenColor(birdColor, 0.75f));
        g.fillRect(x + WIDTH, y + 8, 10, 6);
        drawHat(g);
    }

    private void drawHat(Graphics2D g) {
        int hatX = x + 4;
        int hatY = y - 13;

        switch (equippedHat) {
            case TOP_HAT:
                g.setColor(new Color(30, 30, 30));
                g.fillRect(hatX + 7, hatY - 5, 16, 12);
                g.fillRect(hatX + 3, hatY + 6, 24, 4);
                break;
            case CAP:
                g.setColor(new Color(30, 100, 220));
                g.fillArc(hatX + 3, hatY, 24, 15, 0, 180);
                g.fillRect(hatX, hatY + 7, 45, 4);
                break;
            case CROWN:
                g.setColor(new Color(255, 210, 40));
                Polygon crown = new Polygon(
                        new int[]{hatX + 3, hatX + 7, hatX + 12, hatX + 17, hatX + 23, hatX + 27},
                        new int[]{hatY + 8, hatY - 5, hatY + 3, hatY - 5, hatY + 3, hatY + 8},
                        6
                );
                g.fillPolygon(crown);
                break;
            case PARTY:
                g.setColor(new Color(220, 60, 180));
                Polygon party = new Polygon(
                        new int[]{hatX + 5, hatX + 17, hatX + 25},
                        new int[]{hatY + 8, hatY - 9, hatY + 8},
                        3
                );
                g.fillPolygon(party);
                g.setColor(Color.YELLOW);
                g.fillOval(hatX + 14, hatY - 12, 6, 6);
                break;
            case NONE:
                break;
        }
    }

    public void setEquippedHat(Hat.Type hat) {
        equippedHat = hat;
    }

    public Rectangle getBounds() {
        return new Rectangle(x + 4, y + 4, WIDTH - 8, HEIGHT - 8);
    }

    public int getY() { return y; }
    public int getX() { return x; }
    public boolean hasShield() { return hasShield; }

    public void activateShield() {
        hasShield = true;
        birdColor = SHIELD_COLOR;
    }

    public void consumeShield() {
        hasShield = false;
        birdColor = hasMagnet ? MAGNET_COLOR : NORMAL_COLOR;
    }

    public boolean hasMagnet() { return hasMagnet; }

    public void activateMagnet() {
        hasMagnet = true;
        magnetEndTime = System.currentTimeMillis() + MAGNET_DURATION;
        birdColor = MAGNET_COLOR;
    }

    private Color darkenColor(Color color, float factor) {
        return new Color(clamp((int) (color.getRed() * factor)),
                clamp((int) (color.getGreen() * factor)),
                clamp((int) (color.getBlue() * factor)));
    }

    private Color brightenColor(Color color, float factor) {
        return new Color(clamp((int) (color.getRed() * factor)),
                clamp((int) (color.getGreen() * factor)),
                clamp((int) (color.getBlue() * factor)));
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
