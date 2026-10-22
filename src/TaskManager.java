import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TaskManager {
    private static final int MAX_ACTIVE_TASKS = 3;

    private final Random random = new Random();
    private final List<Task> activeTasks = new ArrayList<>();
    private int coins;

    public TaskManager() {
        generateInitialTasks();
    }

    private void generateInitialTasks() {
        while (activeTasks.size() < MAX_ACTIVE_TASKS) {
            activeTasks.add(generateTask());
        }
    }

    private Task generateTask() {
        Task.Type[] types = Task.Type.values();
        Task.Type type = types[random.nextInt(types.length)];

        switch (type) {
            case COLLECT_COINS:
                return new Task(type, 5 + random.nextInt(6), 10 + random.nextInt(11));
            case PASS_PIPES:
                return new Task(type, 3 + random.nextInt(5), 12 + random.nextInt(14));
            case SURVIVE_TIME:
                return new Task(type, 10 + random.nextInt(11), 15 + random.nextInt(16));
            case DESTROY_BOMBS:
                return new Task(type, 1 + random.nextInt(3), 20 + random.nextInt(21));
            default:
                return new Task(type, 1, 10);
        }
    }

    public void addProgress(Task.Type type, int amount) {
        for (int i = 0; i < activeTasks.size(); i++) {
            Task task = activeTasks.get(i);

            if (task.getType() == type && !task.isCompleted()) {
                task.addProgress(amount);

                if (task.isCompleted()) {
                    coins += task.getReward();
                    activeTasks.set(i, generateTask());
                }
            }
        }
    }

    public List<Task> getActiveTasks() {
        return activeTasks;
    }

    public int getCoins() {
        return coins;
    }

    public void addCoins(int amount) {
        coins += amount;
    }

    public boolean spendCoins(int amount) {
        if (amount < 0 || coins < amount) return false;
        coins -= amount;
        return true;
    }

    public void resetTasks() {
        activeTasks.clear();
        generateInitialTasks();
    }
}
