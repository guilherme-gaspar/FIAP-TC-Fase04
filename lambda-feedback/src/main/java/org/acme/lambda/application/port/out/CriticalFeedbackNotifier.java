package org.acme.lambda.application.port.out;

import org.acme.lambda.domain.model.Feedback;

public interface CriticalFeedbackNotifier {

    void notify(Feedback feedback);
}
