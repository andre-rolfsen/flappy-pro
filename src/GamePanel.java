import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 600;
    private static final int GROUND_HEIGHT = 40;
    private static final int INITIAL_PIPE_INTERVAL = 1800;
    private static final int MIN_PIPE_INTERVAL = 1050;
    private static final int POWERUP_SPAWN_CHANCE = 25;
    private static final int COIN_SPAWN_CHANCE = 75;
    private static final int BOMB_SPAWN_CHANCE = 45;
    private static final int CLOUD_COUNT = 6;
    private static final int TRANSITION_DURATION = 1200;
    private static final int PHASE_NAME_DURATION = 1800;
    private static final int MIN_BOMB_COIN_DISTANCE = 35;

    private final Random random = new Random();
    private Bird bird;
    private List<Pipe> pipes;
    private List<PowerUp> powerUps;
    private List<Coin> coins;
    private List<Bomb> bombs;
    private List<Cloud> clouds;
    private TaskManager taskManager;
    private Shop shop;
    private Timer gameTimer;
    private Timer pipeTimer;
    private int score;
    private int phase;
    private boolean gameOver;
    private boolean started;
    private boolean shopOpen;
    private Color skyColor;
    private Color targetSkyColor;
    private Color groundColor;
    private Color targetGroundColor;
    private Color pipeColor;
    private Color targetPipeColor;
    private long phaseTransitionStart;
    private long phaseNameStartTime;
    private long gameStartTime;
    private int lastSurvivalSecond;

    private static final String[] PHASE_NAMES = {
            "", "EXCELENTE!", "RARO!!", "ÉPICO!!", "LEGENDÁRIO"
    };

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        taskManager = new TaskManager();
        shop = new Shop(taskManager);
        initGame();
    }

    private void initGame() {
        bird = new Bird(80, HEIGHT / 2);
        bird.setEquippedHat(shop.getEquippedHat());
        pipes = new ArrayList<>();
        powerUps = new ArrayList<>();
        coins = new ArrayList<>();
        bombs = new ArrayList<>();
        clouds = new ArrayList<>();
        createClouds();

        score = 0;
        phase = 1;
        gameOver = false;
        started = false;
        shopOpen = false;
        gameStartTime = 0;
        lastSurvivalSecond = 0;

        skyColor = new Color(135, 206, 235);
        targetSkyColor = skyColor;
        groundColor = new Color(210, 180, 100);
        targetGroundColor = groundColor;
        pipeColor = new Color(50, 180, 50);
        targetPipeColor = pipeColor;
        phaseTransitionStart = 0;
        phaseNameStartTime = 0;

        gameTimer = new Timer(16, this);
        pipeTimer = new Timer(INITIAL_PIPE_INTERVAL, e -> spawnPipe());
    }

    private void createClouds() {
        for (int i = 0; i < CLOUD_COUNT; i++) {
            int x = random.nextInt(WIDTH + 200) - 100;
            int y = 50 + random.nextInt(190);
            int width = 55 + random.nextInt(55);
            int height = 25 + random.nextInt(20);
            int speed = 1 + random.nextInt(2);
            clouds.add(new Cloud(x, y, width, height, speed));
        }
    }

    private void spawnPipe() {
        if (!started || gameOver || shopOpen) return;

        int pipeSpeed = getPipeSpeed();
        int pipeGap = getPipeGap();
        Pipe pipe = new Pipe(WIDTH, pipeSpeed, pipeGap, pipeColor);
        pipes.add(pipe);

        int centerY = pipe.getTopHeight() + pipeGap / 2;
        Integer coinY = null;

        if (random.nextInt(100) < COIN_SPAWN_CHANCE) {
            coinY = centerY;
            coins.add(new Coin(WIDTH, coinY));
        }

        if (random.nextInt(100) < COIN_SPAWN_CHANCE / 2) {
            int secondCoinY = centerY - 45;
            if (coinY == null || Math.abs(secondCoinY - coinY) >= MIN_BOMB_COIN_DISTANCE) {
                coins.add(new Coin(WIDTH + 35, secondCoinY));
            }
        }

        if (random.nextInt(100) < POWERUP_SPAWN_CHANCE) {
            PowerUp.Type type = random.nextBoolean()
                    ? PowerUp.Type.SHIELD
                    : PowerUp.Type.MAGNET;
            powerUps.add(new PowerUp(WIDTH, centerY, type));
        }

        if (random.nextInt(100) < BOMB_SPAWN_CHANCE) {
            int bombY = generateBombY(pipeGap, centerY, coinY);
            bombs.add(new Bomb(WIDTH, bombY));
        }
    }

    private int generateBombY(int pipeGap, int centerY, Integer coinY) {
        int minY = centerY - pipeGap / 2 + 15;
        int maxY = centerY + pipeGap / 2 - 15;
        if (maxY <= minY) return centerY;

        for (int attempt = 0; attempt < 20; attempt++) {
            int bombY = minY + random.nextInt(maxY - minY + 1);
            if (coinY == null || Math.abs(bombY - coinY) >= MIN_BOMB_COIN_DISTANCE) {
                return bombY;
            }
        }

        return coinY == null ? centerY : (coinY <= centerY ? maxY : minY);
    }

    private void startGame() {
        started = true;
        gameStartTime = System.currentTimeMillis();
        spawnPipe();
        gameTimer.start();
        pipeTimer.start();
        requestFocusInWindow();
    }

    private void restartGame() {
        gameTimer.stop();
        pipeTimer.stop();
        initGame();
        startGame();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (shopOpen || gameOver || !started) return;

        bird.update();
        for (Cloud cloud : clouds) cloud.update();
        updateSurvivalTask();
        updatePipes();
        updateCoins();
        updatePowerUps();
        updateBombs();
        checkBoundaries();

        if (gameOver) {
            gameTimer.stop();
            pipeTimer.stop();
        }

        updateSkyTransition();
        repaint();
    }

    private void updateSurvivalTask() {
        int seconds = (int) ((System.currentTimeMillis() - gameStartTime) / 1000);
        if (seconds > lastSurvivalSecond) {
            taskManager.addProgress(Task.Type.SURVIVE_TIME,
                    seconds - lastSurvivalSecond);
            lastSurvivalSecond = seconds;
        }
    }

    private void updatePipes() {
        List<Pipe> toRemove = new ArrayList<>();
        for (Pipe pipe : pipes) {
            pipe.update();

            if (!pipe.isScored() && pipe.getX() + pipe.getWidth() < bird.getX()) {
                score++;
                pipe.setScored(true);
                taskManager.addProgress(Task.Type.PASS_PIPES, 1);
                updatePhase();
            }

            if (pipe.collidesWith(bird.getBounds())) {
                if (bird.hasShield()) {
                    bird.consumeShield();
                    pipe.setScored(true);
                    toRemove.add(pipe);
                } else {
                    gameOver = true;
                }
            }

            if (pipe.isOffScreen()) toRemove.add(pipe);
        }
        pipes.removeAll(toRemove);
    }

    private void updateCoins() {
        List<Coin> toRemove = new ArrayList<>();
        for (Coin coin : coins) {
            coin.update();
            if (bird.hasMagnet()) coin.attractToBird(bird.getX(), bird.getY());

            if (coin.collidesWith(bird.getBounds())) {
                score++;
                taskManager.addCoins(1);
                taskManager.addProgress(Task.Type.COLLECT_COINS, 1);
                updatePhase();
                toRemove.add(coin);
            } else if (coin.isOffScreen()) {
                toRemove.add(coin);
            }
        }
        coins.removeAll(toRemove);
    }

    private void updatePowerUps() {
        List<PowerUp> toRemove = new ArrayList<>();
        for (PowerUp powerUp : powerUps) {
            powerUp.update();
            if (bird.hasMagnet()) powerUp.attractToBird(bird.getX(), bird.getY());

            if (powerUp.collidesWith(bird.getBounds())) {
                if (canCollectPowerUp(powerUp)) {
                    applyPowerUp(powerUp);
                    toRemove.add(powerUp);
                }
            } else if (powerUp.isOffScreen()) {
                toRemove.add(powerUp);
            }
        }
        powerUps.removeAll(toRemove);
    }

    private void updateBombs() {
        List<Bomb> toRemove = new ArrayList<>();
        for (Bomb bomb : bombs) {
            bomb.update();

            if (bomb.collidesWith(bird.getBounds())) {
                if (bird.hasShield()) {
                    bird.consumeShield();
                    taskManager.addProgress(Task.Type.DESTROY_BOMBS, 1);
                    toRemove.add(bomb);
                } else {
                    gameOver = true;
                }
            } else if (bomb.isOffScreen()) {
                toRemove.add(bomb);
            }
        }
        bombs.removeAll(toRemove);
    }

    private void checkBoundaries() {
        if (bird.getY() >= HEIGHT - GROUND_HEIGHT || bird.getY() < 0) {
            if (bird.hasShield()) bird.consumeShield();
            else gameOver = true;
        }
    }

    private boolean canCollectPowerUp(PowerUp powerUp) {
        return !bird.hasShield() && !bird.hasMagnet();
    }

    private void applyPowerUp(PowerUp powerUp) {
        if (powerUp.getType() == PowerUp.Type.SHIELD) {
            if (!bird.hasShield() && !bird.hasMagnet()) bird.activateShield();
        } else if (powerUp.getType() == PowerUp.Type.MAGNET) {
            if (!bird.hasShield() && !bird.hasMagnet()) bird.activateMagnet();
        }
    }

    private void updatePhase() {
        int newPhase = score / 5 + 1;
        if (newPhase != phase) {
            phase = newPhase;
            targetSkyColor = getSkyColorForPhase(phase);
            targetGroundColor = getGroundColorForPhase(phase);
            targetPipeColor = getPipeColorForPhase(phase);
            phaseTransitionStart = System.currentTimeMillis();
            phaseNameStartTime = System.currentTimeMillis();
            updatePipeTimer();
        }
    }

    private void updatePipeTimer() {
        int interval = getPipeInterval();
        pipeTimer.setDelay(interval);
        pipeTimer.setInitialDelay(interval);
    }

    private int getPipeSpeed() { return Math.min(9, 4 + (phase - 1) / 2); }
    private int getPipeGap() { return Math.max(105, 160 - (phase - 1) * 8); }
    private int getPipeInterval() {
        return Math.max(MIN_PIPE_INTERVAL,
                INITIAL_PIPE_INTERVAL - (phase - 1) * 110);
    }

    private Color getSkyColorForPhase(int currentPhase) {
        Color[] colors = {
                new Color(135, 206, 235), new Color(122, 109, 201),
                new Color(213, 123, 231), new Color(220, 102, 114),
                new Color(255, 217, 115), new Color(255, 254, 193)
        };
        return colors[Math.min(currentPhase - 1, colors.length - 1)];
    }

    private Color getGroundColorForPhase(int currentPhase) {
        Color[] colors = {
                new Color(210, 180, 100), new Color(180, 140, 80),
                new Color(160, 100, 140), new Color(140, 80, 80),
                new Color(180, 140, 60), new Color(200, 180, 120)
        };
        return colors[Math.min(currentPhase - 1, colors.length - 1)];
    }

    private Color getPipeColorForPhase(int currentPhase) {
        Color[] colors = {
                new Color(40, 120, 60), new Color(220, 180, 60),
                new Color(60, 220, 120), new Color(60, 180, 200),
                new Color(80, 80, 180), new Color(140, 100, 60)
        };
        return colors[Math.min(currentPhase - 1, colors.length - 1)];
    }

    private void updateSkyTransition() {
        if (phaseTransitionStart == 0) return;
        long elapsed = System.currentTimeMillis() - phaseTransitionStart;
        float progress = Math.min(1.0f, elapsed / (float) TRANSITION_DURATION);
        skyColor = interpolateColor(skyColor, targetSkyColor, progress);
        groundColor = interpolateColor(groundColor, targetGroundColor, progress);
        pipeColor = interpolateColor(pipeColor, targetPipeColor, progress);

        if (progress >= 1.0f) {
            skyColor = targetSkyColor;
            groundColor = targetGroundColor;
            pipeColor = targetPipeColor;
            phaseTransitionStart = 0;
        }
    }

    private Color interpolateColor(Color start, Color end, float progress) {
        return new Color(
                interpolate(start.getRed(), end.getRed(), progress),
                interpolate(start.getGreen(), end.getGreen(), progress),
                interpolate(start.getBlue(), end.getBlue(), progress)
        );
    }

    private int interpolate(int start, int end, float progress) {
        return (int) (start + (end - start) * progress);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        if (shopOpen) {
            shop.draw(g2, WIDTH, HEIGHT);
            return;
        }

        drawSky(g2);
        drawClouds(g2);
        drawGround(g2);
        for (Pipe pipe : pipes) pipe.draw(g2);
        for (Bomb bomb : bombs) bomb.draw(g2);
        for (Coin coin : coins) coin.draw(g2);
        for (PowerUp powerUp : powerUps) powerUp.draw(g2);
        bird.draw(g2);
        drawScore(g2);
        drawTasks(g2);
        drawPhase(g2);
        drawPowerUpStatus(g2);

        if (!started) drawOverlay(g2, "FLAPPY PRO", "PRESSIONE ESPAÇO");
        else if (gameOver) drawOverlay(g2, "GAME OVER", "L: LOJA   ESPAÇO: REINICIAR");
    }

    private void drawSky(Graphics2D g2) {
        g2.setColor(skyColor);
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        if (phaseTransitionStart != 0) {
            long elapsed = System.currentTimeMillis() - phaseTransitionStart;
            float progress = Math.min(1.0f, elapsed / (float) TRANSITION_DURATION);
            g2.setColor(new Color(255, 255, 255, (int) (90 * (1.0f - progress))));
            g2.fillRect(0, 0, WIDTH, HEIGHT);
        }
    }

    private void drawClouds(Graphics2D g2) {
        for (Cloud cloud : clouds) cloud.draw(g2);
    }

    private void drawGround(Graphics2D g2) {
        g2.setColor(groundColor);
        g2.fillRect(0, HEIGHT - GROUND_HEIGHT, WIDTH, GROUND_HEIGHT);
        g2.setColor(darkenColor(groundColor, 0.8f));
        g2.fillRect(0, HEIGHT - GROUND_HEIGHT - 5, WIDTH, 10);
    }

    private void drawScore(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 36));
        String text = String.valueOf(score);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (WIDTH - fm.stringWidth(text)) / 2, 60);
    }

    private void drawTasks(Graphics2D g2) {
        int panelX = 8;
        int panelY = HEIGHT - 142;
        int panelWidth = WIDTH - 16;
        int panelHeight = 104;

        g2.setColor(new Color(20, 35, 65, 215));
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 18, 18);
        g2.setColor(new Color(80, 180, 255, 180));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 18, 18);

        g2.setColor(new Color(255, 215, 80));
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        g2.drawString("TAREFAS", panelX + 12, panelY + 19);
        g2.drawString("SALDO: " + taskManager.getCoins(), panelX + 285, panelY + 19);

        int y = panelY + 38;
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        for (Task task : taskManager.getActiveTasks()) {
            g2.setColor(task.isCompleted()
                    ? new Color(100, 255, 150)
                    : new Color(235, 240, 255));
            String text = "• " + task.getDescription() + " (" +
                    task.getProgress() + "/" + task.getTarget() + ") +" +
                    task.getReward() + " MOEDAS";
            g2.drawString(text, panelX + 12, y);
            y += 19;
        }
    }

    private void drawPhase(Graphics2D g2) {
        if (phaseNameStartTime == 0) return;
        long elapsed = System.currentTimeMillis() - phaseNameStartTime;
        if (elapsed >= PHASE_NAME_DURATION) return;
        String[] names = {"", "EXCELENTE!", "RARO!!", "ÉPICO!!", "LEGENDÁRIO"};
        String text = names[Math.min(phase - 1, names.length - 1)];
        if (text.isEmpty()) return;
        int alpha = (int) (255 * (1.0f - elapsed / (float) PHASE_NAME_DURATION));
        g2.setColor(new Color(255, 255, 255, alpha));
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (WIDTH - fm.stringWidth(text)) / 2, 25);
    }

    private void drawPowerUpStatus(Graphics2D g2) {
        int y = 45;
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        if (bird.hasShield()) {
            g2.setColor(new Color(50, 150, 255));
            g2.drawString("BLINDADO!", 15, y);
            y += 15;
        }
        if (bird.hasMagnet()) {
            g2.setColor(new Color(230, 40, 40));
            g2.drawString("MAGNÉTICO!", 15, y);
        }
    }

    private void drawOverlay(Graphics2D g2, String title, String subtitle) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 42));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, HEIGHT / 2 - 30);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        fm = g2.getFontMetrics();
        g2.drawString(subtitle, (WIDTH - fm.stringWidth(subtitle)) / 2, HEIGHT / 2 + 20);
    }

    private Color darkenColor(Color color, float factor) {
        return new Color(
                Math.max(0, (int) (color.getRed() * factor)),
                Math.max(0, (int) (color.getGreen() * factor)),
                Math.max(0, (int) (color.getBlue() * factor))
        );
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (shopOpen) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                shopOpen = false;
                repaint();
            } else {
                shop.handleKey(e.getKeyCode());
                bird.setEquippedHat(shop.getEquippedHat());
                repaint();
            }
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_L && gameOver) {
            shopOpen = true;
            repaint();
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE) handleInput();
    }

    @Override public void mouseClicked(MouseEvent e) { handleInput(); }

    private void handleInput() {
        if (!started) {
            startGame();
            bird.jump();
        } else if (gameOver) {
            restartGame();
        } else {
            bird.jump();
        }
    }

    @Override public void keyReleased(KeyEvent e) { }
    @Override public void keyTyped(KeyEvent e) { }
    @Override public void mousePressed(MouseEvent e) { }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }
}
