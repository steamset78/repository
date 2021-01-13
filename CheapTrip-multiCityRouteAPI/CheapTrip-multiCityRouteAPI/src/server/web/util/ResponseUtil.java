package server.web.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import server.web.model.RequestError;
import server.web.model.ResponseFormat;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ResponseUtil {

    private static final String JSON_ERROR_MESSAGE_KEY = "message";

    public static Gson getGson(){
        return new GsonBuilder()
                .create();
    }

    public static ResponseFormat getDefaultResponseFormat(){
        return ResponseFormat.HTML;
    }

    public static void setResponseContentType(HttpServletResponse response, ResponseFormat responseFormat){
        response.setCharacterEncoding("UTF-8");
        switch (responseFormat){
            case HTML:
                response.setContentType("text/html;charset=UTF-8");
                break;
            case JSON:
                response.setContentType("application/json;charset=UTF-8");
                break;
        }
    }


    public static void respondWithError(HttpServletResponse response, RequestError error, ResponseFormat responseFormat){
        PrintWriter writer;

        setResponseContentType(response, responseFormat);
        response.setStatus(error.getStatusCode());

        switch (responseFormat){
            case HTML:
                if(error.getErrorMessage() != null) {
                    try {
                        writer = response.getWriter();
                        writer.write(error.getErrorMessage());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case JSON:
                JsonObject responseJSON = new JsonObject();
                responseJSON.addProperty(JSON_ERROR_MESSAGE_KEY, error.getErrorMessage());

                try {
                    writer = response.getWriter();
                    writer.write(responseJSON.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                break;
        }
    }

    public static PrintWriter getSuccessfulResponseWriter(HttpServletResponse response, ResponseFormat responseFormat){
        ResponseUtil.setResponseContentType(response, responseFormat);
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = null;
        try {
            out = response.getWriter();
        } catch (IOException e) {
            e.printStackTrace();
            ResponseUtil.respondWithError(
                    response,
                    new RequestError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR),
                    responseFormat
            );
        }
        return out;
    }
}
