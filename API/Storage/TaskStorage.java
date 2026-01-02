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
    private Long largestID = 0L;

    public void save(Task task) {
        store.put(task.getId(), task);
    }

    public Optional<Task> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<Task> findAll() {
        store.put(1L, new Task(1L,"First","First", Priority.LOW, Status.TODO));
        store.put(2L, new Task(2L,"Second","Second", Priority.MEDIUM, Status.TODO));
        Task t3 = new Task(3L,"Third","Third", Priority.MEDIUM , Status.IN_PROGRESS);
        store.put(3L, t3);
        largestID = 3L;
        return new ArrayList<>(store.values());
    }

    public void delete(long id) {
        store.remove(id);
        if(id == largestID) {
            for(long i = id-1; i > 0; i--) {
                if(store.containsKey(i)) {
                    largestID = i;
                    break;
                }
            }
            if (largestID == id) {
                largestID = 0L;
            }
        }
    }

    public Long nextID(){
        largestID += 1;
        return largestID;
    }
}
