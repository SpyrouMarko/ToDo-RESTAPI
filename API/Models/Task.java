package API.Models;


public class Task {
    Long id;
    String name;
    String desc;
    Status status;
    Priority priority;

    public Task(Long id, String name, String desc, Priority priority, Status status){
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.status = status;
        this.priority = priority;
    }

    public void UpdateVals(Long id, String name, String desc, Status status, Priority priority) {
        if (id != null) this.id = id;
        if (name != null) this.name = name;
        if (desc != null) this.desc = desc;
        if (priority != null) this.priority = priority;
        if (status != null) this.status = status;

    }

    public Long getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getDesc(){
        return desc;
    }

    public Status getStatus(){
        return status;
    }

    public Priority getPriority(){
        return priority;
    }

    
}
