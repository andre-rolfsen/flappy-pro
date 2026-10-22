import java.awt.*;
import java.awt.event.KeyEvent;

public class Shop {
    private final TaskManager taskManager;
    private final Hat[] hats;
    private Hat.Type equippedHat;

    public Shop(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.hats = new Hat[Hat.Type.values().length];
        this.equippedHat = Hat.Type.NONE;

        for (Hat.Type type : Hat.Type.values()) {
            hats[type.ordinal()] = new Hat(type, type == Hat.Type.NONE);
        }
    }

    public void draw(Graphics2D g2, int width, int height) {
        g2.setColor(new Color(15, 20, 45, 245));
        g2.fillRect(0, 0, width, height);

        g2.setColor(new Color(255, 215, 70));
        g2.setFont(new Font("Arial", Font.BOLD, 28));
        g2.drawString("LOJINHA DE CHAPÉUS", 55, 55);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 15));
        g2.drawString("Saldo: " + taskManager.getCoins() + " moedas", 115, 85);
        g2.drawString("ESC para voltar", 145, height - 25);

        int y = 125;
        for (Hat hat : hats) {
            Hat.Type type = hat.getType();
            boolean equipped = equippedHat == type;

            g2.setColor(equipped
                    ? new Color(50, 180, 100, 220)
                    : new Color(40, 55, 90, 230));
            g2.fillRoundRect(35, y - 24, width - 70, 52, 14, 14);

            g2.setColor(type.getColor() == null ? Color.WHITE : type.getColor());
            g2.fillOval(55, y - 8, 24, 16);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString(type.getName(), 95, y);

            String status;
            if (equipped) {
                status = "EQUIPADO";
            } else if (hat.isOwned()) {
                status = "PRESSIONE " + keyFor(type) + " PARA EQUIPAR";
            } else {
                status = type.getPrice() + " moedas - tecla " + keyFor(type);
            }

            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.drawString(status, 95, y + 18);
            y += 65;
        }
    }

    public void handleKey(int keyCode) {
        if (keyCode == KeyEvent.VK_1) select(Hat.Type.NONE);
        if (keyCode == KeyEvent.VK_2) select(Hat.Type.TOP_HAT);
        if (keyCode == KeyEvent.VK_3) select(Hat.Type.CAP);
        if (keyCode == KeyEvent.VK_4) select(Hat.Type.CROWN);
        if (keyCode == KeyEvent.VK_5) select(Hat.Type.PARTY);
    }

    private void select(Hat.Type type) {
        Hat hat = hats[type.ordinal()];

        if (!hat.isOwned()) {
            if (!taskManager.spendCoins(type.getPrice())) {
                return;
            }
            hat.setOwned(true);
        }

        equippedHat = type;
    }

    private String keyFor(Hat.Type type) {
        return String.valueOf(type.ordinal() + 1);
    }

    public Hat.Type getEquippedHat() {
        return equippedHat;
    }
}
