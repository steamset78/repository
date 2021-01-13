package server.web.model;

public class RequestError
{

    private final String errorMessage;
    private final int statusCode;

    public RequestError(int statusCode) {
        this(statusCode, "");
    }

    public RequestError(int statusCode, String errorMessage) {
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }
}