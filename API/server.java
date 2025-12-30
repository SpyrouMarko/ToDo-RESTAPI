package API;
import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

import API.Storage.TaskStorage;

public class server {
    public static void main(String[] args) throws Exception {
        try{
            TaskStorage store = new TaskStorage();
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/tasks", new TaskHandler(store));
            server.setExecutor(null);
            server.start();
            System.out.println("Server is running on port 8080");
        }
        catch(IOException e) {
            System.out.println("Error starting the server: " + e.getMessage());
        }
        
    }
}
