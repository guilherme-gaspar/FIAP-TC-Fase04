package org.acme.lambda.domain.model;

public record Feedback(int nota,
                       FeedbackUrgency urgency,
                       String descricao) {

    public boolean requiresImmediateAction() {
        return urgency == FeedbackUrgency.CRITICO;
    }
}
