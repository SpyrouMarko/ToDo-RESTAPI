package API.Storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import API.Models.Priority;
import API.Models.Status;
import API.Models.Task;

public class TaskStorage {
    private final Map<Long, Task> store =  new HashMap<>();

    public void save(Task task) {
        store.put(task.getId(), task);
    }

    public Optional<Task> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Task> findAll() {
        store.put(1L, new Task(1L,"H","H", Priority.LOW));
        return new ArrayList<>(store.values());
    }

    public void delete(long id) {
        store.remove(id);
    }
}
