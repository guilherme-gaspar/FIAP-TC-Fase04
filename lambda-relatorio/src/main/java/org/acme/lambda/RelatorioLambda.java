package org.acme.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Named("relatorio")
public class RelatorioLambda implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final Logger LOG = Logger.getLogger(RelatorioLambda.class);

    @Inject
    DataSource dataSource;

    @ConfigProperty(name = "SNS_TOPIC_ARN", defaultValue = "")
    String topicArn;

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        LOG.info("Iniciando geração do relatório semanal de feedbacks.");
        try (Connection connection = dataSource.getConnection()) {
            String report = buildReport(connection);
            publish(report);
            LOG.info("Relatório semanal processado com sucesso.");
            return response(200, "Relatório enviado com sucesso.");
        } catch (Exception e) {
            LOG.error("Erro ao gerar/enviar relatório", e);
            return response(500, "Erro ao gerar relatório.");
        }
    }

    private String buildReport(Connection connection) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Prezado time,\n\nRelatório semanal de feedbacks\n\n");

        double average = queryAvg(connection);
        List<String> byDay = queryByDay(connection);
        List<String> byUrgency = queryByUrgency(connection);

        LOG.infov("Resumo do relatório: media={0}, itensPorDia={1}, itensPorUrgencia={2}", average, byDay.size(), byUrgency.size());

        sb.append("Média das avaliações: ").append(average).append("\n\n");
        sb.append("Quantidade de avaliações por dia:\n");
        for (String item : byDay) {
            sb.append("- ").append(item).append("\n");
        }

        sb.append("\nQuantidade de avaliações por urgência:\n");
        for (String item : byUrgency) {
            sb.append("- ").append(item).append("\n");
        }
        sb.append("\nAtenciosamente,\nSistema de Feedback\n");
        return sb.toString();
    }

    private double queryAvg(Connection connection) throws Exception {
        String sql = "SELECT COALESCE(AVG(nota),0) FROM feedback WHERE data_envio >= ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, LocalDate.now().minusDays(7));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return Math.round(rs.getDouble(1) * 100.0) / 100.0;
            }
        }
    }

    private List<String> queryByDay(Connection connection) throws Exception {
        String sql = "SELECT DATE(data_envio), COUNT(*) FROM feedback WHERE data_envio >= ? GROUP BY DATE(data_envio) ORDER BY DATE(data_envio)";
        List<String> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, LocalDate.now().minusDays(7));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getDate(1) + ": " + rs.getInt(2));
                }
            }
        }
        return result;
    }

    private List<String> queryByUrgency(Connection connection) throws Exception {
        String sql = "SELECT urgency, COUNT(*) FROM feedback WHERE data_envio >= ? GROUP BY urgency ORDER BY urgency";
        List<String> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, LocalDate.now().minusDays(7));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString(1) + ": " + rs.getInt(2));
                }
            }
        }
        return result;
    }

    private void publish(String message) {
        if (topicArn.isBlank()) {
            LOG.warn("SNS_TOPIC_ARN não definido. Relatório será apenas logado.");
            LOG.info(message);
            return;
        }

        LOG.infov("Publicando relatório no SNS. topicArn={0}", topicArn);
        AmazonSNS sns = AmazonSNSClientBuilder.defaultClient();
        sns.publish(new PublishRequest().withTopicArn(topicArn).withSubject("Relatório semanal de feedbacks").withMessage(message));
        LOG.info("Relatório publicado no SNS com sucesso.");
    }

    private static Map<String, Object> response(int statusCode, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("statusCode", statusCode);
        response.put("message", message);
        return response;
    }
}
