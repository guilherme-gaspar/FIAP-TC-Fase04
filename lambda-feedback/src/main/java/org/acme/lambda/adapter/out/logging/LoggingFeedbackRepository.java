package org.acme.lambda.adapter.out.logging;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.lambda.application.port.out.FeedbackRepository;
import org.acme.lambda.domain.model.Feedback;
import org.jboss.logging.Logger;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class LoggingFeedbackRepository implements FeedbackRepository {

    private static final Logger LOG = Logger.getLogger(LoggingFeedbackRepository.class);
    private static final String TARGET_FUNCTION_NAME = "lambda-armazena-feedback";

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void save(Feedback feedback) {
        Map<String, Object> payload = buildRequest(feedback);

        try {
            String json = mapper.writeValueAsString(payload);

            AWSLambda lambda = AWSLambdaClientBuilder.defaultClient();
            InvokeRequest request = new InvokeRequest()
                    .withFunctionName(TARGET_FUNCTION_NAME)
                    .withPayload(json);

            InvokeResult result = lambda.invoke(request);

            int statusCode = result.getStatusCode();
            String response = result.getPayload() != null ?
                    new String(result.getPayload().array(), StandardCharsets.UTF_8) : null;

            LOG.infov("Invocação de {0} finalizada. status={1} response={2}",
                    TARGET_FUNCTION_NAME, statusCode, response);
        } catch (Exception e) {
            LOG.error("Erro ao invocar lambda de armazenamento de feedback", e);
        }
    }

    private static Map<String, Object> buildRequest(Feedback feedback) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("nota", feedback.nota());
        payload.put("urgency", feedback.urgency().name());
        return payload;
    }
}
