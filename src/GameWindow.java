import javax.swing.*;

public class GameWindow extends JFrame {

    public GameWindow() {
        setTitle("FLAPPY PRO");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        GamePanel gamePanel = new GamePanel();
        add(gamePanel);
        pack();

        setLocationRelativeTo(null);
        setVisible(true);
    }
}