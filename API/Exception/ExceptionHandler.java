package API.Exception;

import java.io.IOException;
import java.io.OutputStream;

import com.sun.net.httpserver.HttpExchange;

public class ExceptionHandler {
    private ExceptionHandler() {}

    public static void RestException(HttpExchange exchange, int code) throws IOException{
        /*
        200 OK

        201 Created

        204 No Content

        400 Bad Request

        404 Not Found

        405 Method Not Allowed
        */
        String res = "";
        switch (code) {
            case 204:
                res = "No content";
                break;
            case 400:
                res = "Bad Request";
                break;
            case 404:
                res = "Not Found";
                break;
            case 405:
                res = "Method Not Allowed";
                break;
            case 415:
                res = "Unsupported Media Type";
            default:
                break;
        }
        exchange.sendResponseHeaders(code, res.length());
        OutputStream os = exchange.getResponseBody();
        os.write(res.getBytes());
        os.close();
    }
}

