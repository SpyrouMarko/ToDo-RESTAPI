package API.Models;

public class Response {
    public int statusCode;
    public String body;
    public String contentType;
    public Response(int statusCode, String body, String contentType) {
        this.statusCode = statusCode;
        this.body = body;
        this.contentType = contentType;
    }
}
