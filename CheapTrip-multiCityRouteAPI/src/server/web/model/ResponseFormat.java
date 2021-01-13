package server.web.model;

import server.web.util.ResponseUtil;

public enum ResponseFormat {
    HTML, JSON;

    private static final String HTML_FORMAT_CODE = "html";
    private static final String JSON_FORMAT_CODE = "json";

    public static ResponseFormat getFormat(String formatCode){
        if(formatCode != null) {
            switch (formatCode) {
                case HTML_FORMAT_CODE:
                    return HTML;
                case JSON_FORMAT_CODE:
                    return JSON;
            }
        }
        return ResponseUtil.getDefaultResponseFormat();
    }
}
