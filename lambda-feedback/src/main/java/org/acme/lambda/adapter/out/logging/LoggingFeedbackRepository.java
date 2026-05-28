package org.acme.lambda.adapter.out.logging;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.lambda.application.port.out.FeedbackRepository;
import org.acme.lambda.domain.model.Feedback;
import org.jboss.logging.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;

@ApplicationScoped
public class LoggingFeedbackRepository implements FeedbackRepository {

    private static final Logger LOG = Logger.getLogger(LoggingFeedbackRepository.class);

    @Inject
    DataSource dataSource;

    @Override
    public void save(Feedback feedback) {
        String sql = "INSERT INTO feedback (descricao, urgency, nota, data_envio) VALUES (?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, feedback.descricao());
            statement.setString(2, feedback.urgency().name());
            statement.setInt(3, feedback.nota());
            statement.setTimestamp(4, java.sql.Timestamp.from(Instant.now()));

            statement.executeUpdate();
            LOG.infov("Feedback salvo com sucesso. nota={0}, urgency={1}", feedback.nota(), feedback.urgency().name());
        } catch (Exception e) {
            LOG.error("Erro ao salvar feedback no PostgreSQL", e);
            throw new RuntimeException("Erro ao salvar feedback", e);
        }
    }
}
