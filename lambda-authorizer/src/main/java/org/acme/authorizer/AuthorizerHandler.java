package org.acme.authorizer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;

public class AuthorizerHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    @ConfigProperty(name = "API_KEY")
    private String API_KEY;

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {

        Map<String, String> headers =
                (Map<String, String>) event.get("headers");

        String token = headers.get("authorization");

        boolean autorizado =
                API_KEY.equals(token);

        return Map.of(
                "isAuthorized", autorizado
        );
    }
}

