package org.acme.lambda.adapter.in.lambda;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.acme.lambda.application.dto.FeedbackRequest;
import org.acme.lambda.application.usecase.ProcessFeedbackUseCase;

@Path("/avaliacao")
@Consumes(MediaType.APPLICATION_JSON)
public class FeedbackLambdaHandler {

    @Inject
    ProcessFeedbackUseCase processFeedbackUseCase;

    @POST
    public String receber(FeedbackRequest input) {
        processFeedbackUseCase.execute(input);
        return "Feedback recebido com sucesso";
    }
}
