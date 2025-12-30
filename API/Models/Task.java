package API.Models;


public class Task {
    Long id;
    String name;
    String desc;
    Status status;
    Priority priority;

    public Task(Long id, String name, String desc, Priority priority){
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.status = Status.TODO;
        this.priority = priority;
    }

    public void UpdateVals(Long id, String name, String desc, Status status, Priority priority) {
        if (id != null) this.id = id;
        if (name != null) this.name = name;
        if (desc != null) this.desc = desc;
        if (priority != null) this.priority = priority;
        if (status != null) this.status = status;

    }

    public Long GetId(){
        return id;
    }
}
