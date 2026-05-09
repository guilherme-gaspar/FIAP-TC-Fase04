package org.acme.lambda.adapter.out.logging;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.lambda.application.port.out.CriticalFeedbackNotifier;
import org.acme.lambda.domain.model.Feedback;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class LoggingCriticalFeedbackNotifier implements CriticalFeedbackNotifier {

    private static final Logger LOG = Logger.getLogger(LoggingCriticalFeedbackNotifier.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @ConfigProperty(name = "SNS_TOPIC_ARN")
    String TOPIC_ARN_ENV;

    @Override
    public void notify(Feedback feedback) {
        LOG.warnf("Feedback critico recebido. nota=%d urgencia=%s",
                feedback.nota(),
                feedback.urgency().name());

        if (TOPIC_ARN_ENV == null || TOPIC_ARN_ENV.isBlank()) {
            LOG.errorf("Variavel de ambiente %s nao definida. Nao sera possivel publicar no SNS.", TOPIC_ARN_ENV);
            return;
        }

        Map<String, Object> message = new HashMap<>();
        message.put("descricao", String.format("Feedback recebido. nota=%d", feedback.nota()));
        message.put("urgencia", feedback.urgency().name());
        message.put("dataEnvio", Instant.now().toString());

        try {
            String payload = mapper.writeValueAsString(message);

            AmazonSNS sns = AmazonSNSClientBuilder.defaultClient();
            PublishRequest publishRequest = new PublishRequest()
                    .withTopicArn(TOPIC_ARN_ENV)
                    .withMessage(payload)
                    .withSubject("Feedback crítico recebido");

            sns.publish(publishRequest);
            LOG.infov("Mensagem publicada no SNS. topic={0}", TOPIC_ARN_ENV);
        } catch (Exception e) {
            LOG.error("Erro ao publicar mensagem no SNS", e);
        }
    }
}
