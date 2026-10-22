public class Task {
    public enum Type {
        COLLECT_COINS,
        PASS_PIPES,
        SURVIVE_TIME,
        DESTROY_BOMBS
    }

    private final Type type;
    private final int target;
    private final int reward;
    private int progress;
    private boolean completed;

    public Task(Type type, int target, int reward) {
        this.type = type;
        this.target = target;
        this.reward = reward;
        this.progress = 0;
        this.completed = false;
    }

    public void addProgress(int amount) {
        if (completed) return;

        progress = Math.min(target, progress + amount);
        if (progress >= target) completed = true;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Type getType() {
        return type;
    }

    public int getTarget() {
        return target;
    }

    public int getReward() {
        return reward;
    }

    public int getProgress() {
        return progress;
    }

    public String getDescription() {
        switch (type) {
            case COLLECT_COINS:
                return "COLETE " + target + " MOEDAS";
            case PASS_PIPES:
                return "PASSE " + target + " CANOS";
            case SURVIVE_TIME:
                return "SOBREVIVA " + target + " SEGUNDOS";
            case DESTROY_BOMBS:
                return "DESTRUA " + target + " BOMBAS";
            default:
                return "Task";
        }
    }
}
