package API;
import API.Models.Task;

import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpHandler;

import API.Storage.TaskStorage;

import com.sun.net.httpserver.HttpExchange;
import java.net.URI;
import java.io.IOException;
import java.io.OutputStream;

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

        // Handle the request
        response = "Hello, this is a simple HTTP server response!";
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

    String processGetReq(HttpExchange exchange){
        String path = exchange.getRequestURI().getPath();
        String[] split = path.split("/");
        String res = "";

        //Getting all tasks
        if(split.length==2){
            res = store.findAll().toString();

        } else {
            String param = split[2];
            split = param.split("&");
            //Getting a single task by id
            if (split.length == 1){
                Long id = Long.parseLong(split[2]);
                res = store.findById(id).toString();
            } else {
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
                            validCond = true;
                            //See if its a valid predicate on the left hand side
                            boolean validPred = false;
                            for (int p = 0; p < taskAttributes.length; p++){
                                if(condSplit[0] == taskAttributes[p]){
                                    validPred = true;
                                    break;
                                }
                            }
                            if (validPred == false){
                                //Error
                            }

                            conditions.add(condSplit);
                            break;
                        }
                    }
                    if(validCond == false){
                        //Error
                    }
                    
                }
                //Getting all tasks but filtering
                List<Task> allTasks = store.findAll();
                List<Task> filtered = new ArrayList<>();
                for(int i = 0; i < allTasks.size(); i++){
                    boolean passes = true;
                    //Check against each condition
                    for (int j = 0; j < conditions.size(); j++) {
                        
                    }
                }

            }
            
        }

        return res;
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