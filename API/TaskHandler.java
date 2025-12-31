package API;
import API.Models.Task;
import API.Exception.ExceptionHandler;


import org.xml.sax.ErrorHandler;

import com.sun.net.httpserver.HttpHandler;

import API.Storage.TaskStorage;

import com.sun.net.httpserver.HttpExchange;
import java.net.URI;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TaskHandler implements HttpHandler {

    TaskStorage store;
    String[] relationalOperators = new String[]{"!=","="};
    String[] taskAttributes = new String[]{"id", "name", "desc", "status", "priority"};
    
    public TaskHandler(TaskStorage store){
        this.store = store;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException 
    {
        String method = exchange.getRequestMethod();
        String response = "";
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
        exchange.sendResponseHeaders(200, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
        /*
        200 OK

        201 Created

        204 No Content

        400 Bad Request

        404 Not Found

        405 Method Not Allowed
        */
    }

    String processGetReq(HttpExchange exchange) throws IOException{
        String path = exchange.getRequestURI().getPath();
        //Split based on the / char to see if there is a specific id or name to be searched
        String[] split = path.split("/");
        String res = "";
        System.out.println(split.length);
        for(int i = 0; i < split.length;i++){
            System.out.println(split[i]);
        }
        //No specific task
        if(split.length==2){
            //Split based on ? to see if there are conditions
            String[] params = split[1].split("\\?");
            System.out.println(params.length);
            //No conditions, return all
            if (params.length == 1) {
                //Need to return as like a json, currently returning the object ID
                System.out.println(store.findAll().toString());
                return store.findAll().toString();
            } 

            //If there are, get them
            String param = params[1];
            split = param.split("&");
            List<String[]> conditions = new ArrayList<>();

            //Loop through all conditions
            for (int j = 0; j < split.length; j++) {
                //Split them into parsed conditions
                boolean validCond = false;
                //find which condition it splits on
                for (int relationOperator = 0; relationOperator < relationalOperators.length; relationOperator++){
                    
                    String[] condSplit = split[j].split(relationalOperators[relationOperator]);
                    if(condSplit.length == 1){
                        continue;
                    } else {
                        validCond = false;
                        //See if its a valid predicate on the left hand side
                        boolean validPred = false;
                        for (int p = 0; p < taskAttributes.length; p++){
                            if(condSplit[0].equals(taskAttributes[p])){
                                validPred = true;
                                break;
                            }
                        }
                        //Handle exception if left hand side is not valid
                        if (validPred == false){
                            ExceptionHandler.RestException(exchange, 400);
                            return null;
                        }
                        //Otherwise add condition and break
                        conditions.add(condSplit);
                        break;
                    }
                }
                //If condition is invalid, aka did not find a valid operator in the valid, handle exception
                if(validCond == false){
                    ExceptionHandler.RestException(exchange, 400);
                    return null;
                }
                
            }
            //Getting all tasks but filtering
            List<Task> allTasks = store.findAll();
            List<Task> filtered = new ArrayList<>();
            for(int i = 0; i < allTasks.size(); i++){
                Task task = allTasks.get(i);
                boolean passes = true;
                //Check against each condition
                for (int j = 0; j < conditions.size(); j++) {
                    String[] condition = conditions.get(j);
                    String val = getTaskFieldValue(task, condition[0]);
                    //No need to recheck null since prechecked
                    boolean equal = val.equals(condition[2]);
                    boolean oper = (condition[1]=="=");
                    //XOR if they are equal and if the condition is =
                    //Returns false if equal is true and oper is false
                    //Or if equal is false and oper is true
                    if (!(equal ^ oper)){
                        passes = false;
                        break;
                    }
                    

                }
                if(passes) filtered.add(task);
            }
            
        } else if (split.length == 3){            
            //Getting a single task by id
            Long id = Long.parseLong(split[2]);
            Optional<Task> task = store.findById(id);
            if(!task.isPresent()){
                ExceptionHandler.RestException(exchange, 204);

            }
            res = store.findById(id).toString();
            
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

    String processPostReq(HttpExchange exchange){
        String res = "";
        return res;
    }
    
    String processPutReq(HttpExchange exchange){
        String res = "";
        return res;
    }
    String processDelReq(HttpExchange exchange){
        String res = "";
        return res;
    }
}