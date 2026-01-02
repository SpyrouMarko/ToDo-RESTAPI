package API;
import API.Models.Task;
import API.Exception.ExceptionHandler;
import API.Models.Response;
import API.Models.Status;
import API.Models.Priority;
import API.Storage.TaskStorage;

import org.xml.sax.ErrorHandler;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.Headers;


import com.sun.net.httpserver.HttpExchange;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class TaskHandler implements HttpHandler {

    TaskStorage store;
    String[] relationalOperators = new String[]{"!=","="};
    String[] taskAttributes = new String[]{"id", "name", "description", "priority", "status"};
    
    public TaskHandler(TaskStorage store){
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException 
    {
        String method = exchange.getRequestMethod();
        Response response = null;
        switch (method) {
            case "GET":
                response = processGetReq(exchange);
                break;
            case "POST":
                response = processPostReq(exchange);
                break;
            case "PUT":
                response = processPutReq(exchange);
                break;
            case "DELETE":
                response = processDelReq(exchange);
                break;
            default:
                // Method is not one of the afformentioned
                break;
        }
        //Already been handled by an exception
        if(response == null) return;

        // Handle the request
        exchange.getResponseHeaders().set("Content-Type", response.contentType);
        exchange.sendResponseHeaders(response.statusCode, response.body.getBytes().length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.body.getBytes());
        }
        /*
        200 OK

        201 Created

        204 No Content

        400 Bad Request

        404 Not Found

        405 Method Not Allowed
        */
    }

    Response processGetReq(HttpExchange exchange) throws IOException{
        String path = exchange.getRequestURI().getPath();
        //Split based on the / char to see if there is a specific id or name to be searched
        String[] split = path.split("/");
        Response res = null;
        //No specific task
        if(split.length==2){
            //See if there is a query
            String query = exchange.getRequestURI().getQuery();
            System.out.println("Query");
            System.out.println(query);
            //No conditions, return all
            if (query == null) {
                return new Response(200, toJson(store.findAll()), "application/json");
            } 

            //If there is a query, split into each query part and parse it
            String[] params = query.split("&");
            List<String[]> conditions = new ArrayList<>();
            //Loop through all conditions
            for (int j = 0; j < params.length; j++) {
                System.out.println(params[j]);
                //Parse the conditions into lists of LHS, operator, RHS
                //Check if its a valid condition on the left
                if(params[j]=="" || params[j]==null) continue;

                boolean validCond = false;
                
                //find which relational operator is in the middle
                for (int relationOperator = 0; relationOperator < relationalOperators.length; relationOperator++){
                    
                    String[] condSplit = params[j].split(relationalOperators[relationOperator]);
                    if(condSplit.length == 1){
                        continue;
                    } else {
                        System.out.println("Found relational operator");
                        //if we find a valid one, we know the condition is valid(partially) and continue by checking
                        //if the LHS is valid, the validPred check
                        validCond = true;
                        boolean validPred = false;
                        //check by checking the name of the predicate LHS against all the names
                        //Of attributes that a task can have
                        for (int p = 0; p < taskAttributes.length; p++){
                            if(condSplit[0].equals(taskAttributes[p])){
                                validPred = true;
                                break;
                            }
                        }
                        //Handle exception if left hand side is not valid
                        if (validPred == false){
                            System.out.println("Pred");
                            ExceptionHandler.RestException(exchange, 400);
                            return null;
                        }
                        //Otherwise add condition and break since we found the operator and the LHS is valid

                        List<String> temp = new ArrayList<>(Arrays.asList(condSplit));

                        temp.add(1, relationalOperators[relationOperator]);
                        
                        String[] result = temp.toArray(new String[0]);

                        conditions.add(result);
                        
                        break;
                    }
                }
                //If condition is invalid, aka did not find a valid operator in the parameter, handle exception
                if(validCond == false){
                    System.out.println("Cond");
                    ExceptionHandler.RestException(exchange, 400);
                    return null;
                }
                
            }
            //Getting all tasks but filtering
            List<Task> allTasks = store.findAll();
            List<Task> filteredTasks = new ArrayList<>();
            for(int i = 0; i < allTasks.size(); i++){
                Task task = allTasks.get(i);
                boolean passes = true;
                //Check against each condition
                for (int j = 0; j < conditions.size(); j++) {
                    System.out.println(conditions.get(j)[0]);
                    String[] condition = conditions.get(j);
                    String val = getTaskFieldValue(task, condition[0]);
                    //No need to recheck null since prechecked
                    
                    boolean equal = val.equals(condition[2]);
                    boolean oper = (condition[1]=="=");
                    //XOR if they are equal and if the condition is =
                    //Returns true if equal is true and oper is false
                    //Or if equal is false and oper is true
                    
                    if ((equal ^ oper)){
                        passes = false;
                        break;
                    }
                }
                
                if(passes) filteredTasks.add(task);
            }
            res = new Response(200, toJson(filteredTasks), "application/json");
            
        } else if (split.length == 3){            
            System.out.println("Specific");
            //Getting a single task by id
            Long id = Long.parseLong(split[2]);
            Optional<Task> task = store.findById(id);
            if(!task.isPresent()){
                ExceptionHandler.RestException(exchange, 204);
                return null;
            }
            res = new Response(200, toJson(store.findById(id).get()), "application/json");
            
        } else {
            ExceptionHandler.RestException(exchange, 400);
        }

        return res;
    }

    private String getTaskFieldValue(Task task, String field) {
        return switch (field) {
            case "id" -> String.valueOf(task.getId());
            case "name" -> task.getName();
            case "desc" -> task.getDesc();
            case "status" -> task.getStatus().name();
            case "priority" -> task.getPriority().name();
            default -> null;
        };
    }

    Response processPostReq(HttpExchange exchange) throws IOException{
        System.out.println("Post request");
        Map<String,String> body = extractBodyJSONMap(exchange);
        
        if (body == null) return null;

        if(body.size() != 4) {
            System.out.println("Incorrect body size");
            ExceptionHandler.RestException(exchange, 400);
            return null;
        }
        //Have to -1 to ignore ID
        for(int i = 0; i < taskAttributes.length-1;i++) {
            if(!body.containsKey(taskAttributes[i+1])) {
                System.out.println("Post request body has invalid field");
                ExceptionHandler.RestException(exchange, 400);
                return null;
            }
        }
        String name = body.get("name");
        String desc = body.get("description");
        Status status = null;
        Priority priority = null;
        try {
            status = Status.valueOf(body.get("status"));
            priority = Priority.valueOf(body.get("priority"));
        } catch(Exception e) {
            System.out.println("Priority Or Status section have invalid values");
            ExceptionHandler.RestException(exchange, 400);
            return null;
        }
        
        Task newTask = new Task(store.nextID(),name,desc,priority,status);
        store.save(newTask);
        Response res = new Response(201, toJson(newTask), "application/json");
        return res;
    }
    
    Response processPutReq(HttpExchange exchange){
        Response res = null;
        return res;
    }
    Response processDelReq(HttpExchange exchange){
        Response res = null;
        return res;
    }

    Map<String,String> extractBodyJSONMap(HttpExchange exchange) throws IOException{
        Headers headers = exchange.getRequestHeaders();
        if(!headers.containsKey("content-type")){
            ExceptionHandler.RestException(exchange,400);
            return null;
        }

        if(!headers.getFirst("content-type").startsWith("application/json")){
            ExceptionHandler.RestException(exchange,415);
            return null;
        }

        String bodyS;
        try (InputStream is = exchange.getRequestBody()) {
            bodyS = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }   

        return parseJsonObject(bodyS);

    }

    Map<String, String> parseJsonObject(String json) {
        Map<String, String> map = new HashMap<>();

        json = json.trim();
        if (!json.startsWith("{") || !json.endsWith("}")) {
            throw new IllegalArgumentException("Invalid JSON");
        }

        json = json.substring(1, json.length() - 1).trim();
        if (json.isEmpty()) return map;

        String[] pairs = json.split(",");

        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length != 2) continue;

            String key = kv[0].trim().replaceAll("^\"|\"$", "");
            String value = kv[1].trim().replaceAll("^\"|\"$", "");

            map.put(key, value);
        }

        return map;
    }

    public String toJson(Task t) {
        return "{"
            + "\"id\":" + t.getId() + ","
            + "\"name\":\"" + t.getName() + "\","
            + "\"desc\":\"" + t.getDesc() + "\","
            + "\"status\":\"" + t.getStatus() + "\","
            + "\"priority\":\"" + t.getPriority() + "\""
            + "}";
    }
    
    public String toJson(List<Task> tasks) {
        return tasks.stream()
            .map(this::toJson)
            .collect(Collectors.joining(",", "[", "]"));
    }
}
