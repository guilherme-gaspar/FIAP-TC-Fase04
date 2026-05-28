package org.acme.lambda.adapter.out.logging;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.lambda.application.port.out.CriticalFeedbackNotifier;
import org.acme.lambda.domain.model.Feedback;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Instant;

@ApplicationScoped
public class LoggingCriticalFeedbackNotifier implements CriticalFeedbackNotifier {

    private static final Logger LOG = Logger.getLogger(LoggingCriticalFeedbackNotifier.class);

    @ConfigProperty(name = "SNS_TOPIC_ARN", defaultValue = "")
    String topicArn;

    @Override
    public void notify(Feedback feedback) {
        LOG.warnf("Feedback critico recebido. nota=%d urgencia=%s, descricao=%s",
                feedback.nota(),
                feedback.urgency().name(),
                feedback.descricao());

        if (topicArn.isBlank()) {
            LOG.warn("Variavel SNS_TOPIC_ARN nao definida. Notificacao sera apenas logada.");
            return;
        }

        String payload = "Prezado time,\n\n" +
                "Um feedback crítico foi recebido:\n\n" +
                "Descrição: " + (feedback.descricao() == null ? "(sem descrição)" : feedback.descricao()) + "\n" +
                "Urgência: " + feedback.urgency().name() + "\n" +
                "Data de envio: " + Instant.now() + "\n\n" +
                "Atenciosamente,\nSistema de Feedback\n";

        try {
            LOG.infov("Publicando notificação no SNS. topicArn={0}", topicArn);
            AmazonSNS sns = AmazonSNSClientBuilder.defaultClient();
            PublishRequest publishRequest = new PublishRequest()
                    .withTopicArn(topicArn)
                    .withMessage(payload)
                    .withSubject("Feedback crítico recebido");

            sns.publish(publishRequest);
            LOG.infov("Mensagem publicada no SNS com sucesso. topic={0}", topicArn);
        } catch (Exception e) {
            LOG.errorf(e, "Falha ao publicar no SNS (best-effort). topicArn=%s", topicArn);
        }
    }
}
