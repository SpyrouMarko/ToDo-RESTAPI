package API.Storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import API.Models.Task;

public class TaskStorage {
    private final Map<Long, Task> store = new HashMap<>();

    public void save(Task task) {
        store.put(task.GetId(), task);
    }

    public Optional<Task> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Task> findAll() {
        return new ArrayList<>(store.values());
    }

    public void delete(long id) {
        store.remove(id);
    }
}
