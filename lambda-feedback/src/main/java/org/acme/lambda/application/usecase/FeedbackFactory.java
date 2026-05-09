package org.acme.lambda.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.lambda.application.dto.FeedbackRequest;
import org.acme.lambda.domain.model.Feedback;
import org.acme.lambda.domain.model.FeedbackUrgency;

@ApplicationScoped
public class FeedbackFactory {

    public Feedback create(FeedbackRequest request) {
        validate(request);
        int nota = request.getNota();
        return new Feedback(nota, classify(nota), request.getDescricao());
    }

    private void validate(FeedbackRequest request) {
        if (request == null || request.getNota() == null) {
            throw new IllegalArgumentException("A nota do feedback e obrigatoria");
        }

        if (request.getDescricao() == null || request.getDescricao().isBlank()) {
            throw new IllegalArgumentException("A descrição do feedback e obrigatoria");
        }

        int nota = request.getNota();
        if (nota < 0 || nota > 10) {
            throw new IllegalArgumentException("A nota do feedback deve estar entre 0 e 10");
        }
    }

    private FeedbackUrgency classify(int nota) {
        if (nota <= 4) {
            return FeedbackUrgency.CRITICO;
        }
        if (nota <= 7) {
            return FeedbackUrgency.MEDIO;
        }
        return FeedbackUrgency.POSITIVO;
    }
}
