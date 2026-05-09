package org.acme.lambda.adapter.in.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.acme.lambda.application.dto.FeedbackRequest;
import org.acme.lambda.application.usecase.ProcessFeedbackUseCase;

@Named("feedback")
public class FeedbackLambdaHandler implements RequestHandler<FeedbackRequest, String> {

    @Inject
    ProcessFeedbackUseCase processFeedbackUseCase;

    @Override
    public String handleRequest(FeedbackRequest input, Context context) {
        processFeedbackUseCase.execute(input);
        return "Feedback recebido com sucesso";
    }
}
