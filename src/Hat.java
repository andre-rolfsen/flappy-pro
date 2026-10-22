import java.awt.*;

public class Hat {
    public enum Type {
        NONE("SEM CHAPÉU", 0, null),
        TOP_HAT("CARTOLA", 25, new Color(30, 30, 30)),
        CAP("BONÉ", 40, new Color(30, 100, 220)),
        CROWN("COROA", 75, new Color(255, 210, 40)),
        PARTY("FESTA", 100, new Color(220, 60, 180));

        private final String name;
        private final int price;
        private final Color color;

        Type(String name, int price, Color color) {
            this.name = name;
            this.price = price;
            this.color = color;
        }

        public String getName() {
            return name;
        }

        public int getPrice() {
            return price;
        }

        public Color getColor() {
            return color;
        }
    }

    private final Type type;
    private boolean owned;

    public Hat(Type type, boolean owned) {
        this.type = type;
        this.owned = owned;
    }

    public Type getType() {
        return type;
    }

    public boolean isOwned() {
        return owned;
    }

    public void setOwned(boolean owned) {
        this.owned = owned;
    }
}

