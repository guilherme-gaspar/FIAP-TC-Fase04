package org.acme.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jboss.logging.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@ApplicationScoped
@Named("feedback-storage")
public class FeedbackStorageLambda implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final Logger LOG = Logger.getLogger(FeedbackStorageLambda.class);

    @Inject
    DataSource dataSource;

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        String descricao = asString(input.get("descricao"));
        String urgency = asString(input.get("urgency"));
        Integer nota = asInteger(input.get("nota"));

        if (descricao == null || descricao.isBlank() || nota == null) {
            return response(400, "Payload inválido: descricao e nota são obrigatórios.");
        }

        String sql = "INSERT INTO feedback (descricao, urgency, nota, data_envio) VALUES (?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, descricao);
            statement.setString(2, urgency == null || urgency.isBlank() ? "BAIXA" : urgency);
            statement.setInt(3, nota);
            statement.setTimestamp(4, Timestamp.from(Instant.now()));
            statement.executeUpdate();
            return response(200, "Feedback salvo com sucesso.");
        } catch (Exception e) {
            LOG.error("Erro ao salvar feedback no PostgreSQL", e);
            return response(500, "Erro ao salvar feedback.");
        }
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private static Integer asInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number number) return number.intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Map<String, Object> response(int statusCode, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("statusCode", statusCode);
        response.put("message", message);
        return response;
    }
}
