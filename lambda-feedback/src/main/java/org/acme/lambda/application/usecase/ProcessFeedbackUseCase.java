package org.acme.lambda.application.usecase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.lambda.application.dto.FeedbackRequest;
import org.acme.lambda.application.port.out.CriticalFeedbackNotifier;
import org.acme.lambda.application.port.out.FeedbackRepository;
import org.acme.lambda.domain.model.Feedback;

@ApplicationScoped
public class ProcessFeedbackUseCase {

    private final FeedbackFactory feedbackFactory;
    private final FeedbackRepository feedbackRepository;
    private final CriticalFeedbackNotifier criticalFeedbackNotifier;

    @Inject
    public ProcessFeedbackUseCase(
            FeedbackFactory feedbackFactory,
            FeedbackRepository feedbackRepository,
            CriticalFeedbackNotifier criticalFeedbackNotifier) {
        this.feedbackFactory = feedbackFactory;
        this.feedbackRepository = feedbackRepository;
        this.criticalFeedbackNotifier = criticalFeedbackNotifier;
    }

    public void execute(FeedbackRequest request) {
        Feedback feedback = feedbackFactory.create(request);
        feedbackRepository.save(feedback);

        if (feedback.requiresImmediateAction()) {
            criticalFeedbackNotifier.notify(feedback);
        }
    }
}
