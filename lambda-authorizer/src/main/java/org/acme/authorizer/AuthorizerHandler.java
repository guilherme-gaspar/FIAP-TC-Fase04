package org.acme.authorizer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Map;

public class AuthorizerHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final Logger LOG = Logger.getLogger(AuthorizerHandler.class);

    @ConfigProperty(name = "API_KEY")
    String API_KEY;

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> event, Context context) {
        LOG.info("=== Iniciando AuthorizerHandler ===");
        LOG.info("API_KEY value: " + (API_KEY != null ? "***" : "NULL"));
        LOG.info("Event: " + event);

        try {
            Map<String, String> headers =
                    (Map<String, String>) event.get("headers");

            LOG.info("Headers: " + headers);

            if (headers == null) {
                LOG.warn("Headers é null!");
                return Map.of("isAuthorized", false);
            }

            String token = headers.get("authorization");
            LOG.info("Token recebido: " + (token != null ? "***" : "NULL"));

            if (API_KEY == null) {
                LOG.error("API_KEY não foi injetado! Verifique a variável de ambiente.");
                return Map.of("isAuthorized", false);
            }

            boolean autorizado = API_KEY.equals(token);
            LOG.info("Autorização resultado: " + autorizado);

            return Map.of(
                    "isAuthorized", autorizado
            );
        } catch (Exception e) {
            LOG.error("Erro ao processar autorização: " + e.getMessage(), e);
            return Map.of(
                    "isAuthorized", false
            );
        }
    }
}

