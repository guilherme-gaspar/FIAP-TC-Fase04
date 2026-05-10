package org.acme.lambda.adapter.out.logging;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.lambda.application.port.out.CriticalFeedbackNotifier;
import org.acme.lambda.domain.model.Feedback;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;

import java.time.Instant;

@ApplicationScoped
public class LoggingCriticalFeedbackNotifier implements CriticalFeedbackNotifier {

    private static final Logger LOG = Logger.getLogger(LoggingCriticalFeedbackNotifier.class);

    @ConfigProperty(name = "SNS_TOPIC_ARN")
    String TOPIC_ARN_ENV;

    @ConfigProperty(name = "TARGET_FUNCTION_NAME")
    String TARGET_FUNCTION_NAME;

    @Override
    public void notify(Feedback feedback) {
        LOG.warnf("Feedback critico recebido. nota=%d urgencia=%s, descricao:=%s",
                feedback.nota(),
                feedback.urgency().name(),
                feedback.descricao());

        if (TOPIC_ARN_ENV == null || TOPIC_ARN_ENV.isBlank()) {
            LOG.errorf("Variavel de ambiente %s nao definida. Nao sera possivel publicar no SNS.", TOPIC_ARN_ENV);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Prezado time,\n\n");
        sb.append("Um feedback crítico foi recebido:\n\n");
        sb.append("Descrição: ").append(feedback.descricao() == null ? "(sem descrição)" : feedback.descricao()).append("\n");
        sb.append("Urgência: ").append(feedback.urgency().name()).append("\n");
        sb.append("Data de envio: ").append(Instant.now().toString()).append("\n\n");
        sb.append("Atenciosamente,\n");
        sb.append("Sistema de Feedback\n");

        try {
            String payload = sb.toString();

            AmazonSNS sns = AmazonSNSClientBuilder.defaultClient();
            PublishRequest publishRequest = new PublishRequest()
                    .withTopicArn(TOPIC_ARN_ENV)
                    .withMessage(payload)
                    .withSubject("Feedback crítico recebido");

            sns.publish(publishRequest);
            LOG.infov("Mensagem publicada no SNS. topic={0}", TOPIC_ARN_ENV);

            // Also invoke the target lambda to store feedback in the database
            if (TARGET_FUNCTION_NAME != null && !TARGET_FUNCTION_NAME.isBlank()) {
                try {
                    AWSLambda lambda = AWSLambdaClientBuilder.defaultClient();
                    InvokeRequest invokeRequest = new InvokeRequest()
                            .withFunctionName(TARGET_FUNCTION_NAME)
                            .withInvocationType("Event")
                            .withPayload('{'+"\"descricao\":\"" + escapeForJson(feedback.descricao()) + "\"," +
                                    "\"urgency\":\"" + feedback.urgency().name() + "\"," +
                                    "\"nota\": " + feedback.nota() + "}" );

                    InvokeResult invokeResult = lambda.invoke(invokeRequest);
                    LOG.infov("Invocação (async) da função alvo concluída. status={0}", invokeResult.getStatusCode());
                } catch (Exception e) {
                    LOG.error("Erro ao invocar função alvo", e);
                }
            } else {
                LOG.warn("Variavel de ambiente TARGET_FUNCTION_NAME nao definida. Nao sera possivel invocar a lambda de armazenamento.");
            }
        } catch (Exception e) {
            LOG.error("Erro ao publicar mensagem no SNS", e);
        }
    }

    // Simple JSON string escaper for small payloads
    private String escapeForJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
