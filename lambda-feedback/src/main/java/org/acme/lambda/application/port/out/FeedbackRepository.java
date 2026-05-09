package org.acme.lambda.application.port.out;

import org.acme.lambda.domain.model.Feedback;

public interface FeedbackRepository {

    void save(Feedback feedback);
}
