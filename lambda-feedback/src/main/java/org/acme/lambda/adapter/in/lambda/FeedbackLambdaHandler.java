package org.acme.lambda.adapter.in.lambda;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.lambda.application.dto.FeedbackRequest;
import org.acme.lambda.application.dto.FeedbackResponse;
import org.acme.lambda.application.usecase.ProcessFeedbackUseCase;

@Path("/avaliacao")
@Consumes(MediaType.APPLICATION_JSON)
public class FeedbackLambdaHandler {

    @Inject
    ProcessFeedbackUseCase processFeedbackUseCase;

    @POST
    public Response receber(FeedbackRequest input) {
        try {
            processFeedbackUseCase.execute(input);
            return Response.ok()
                    .entity(new FeedbackResponse("Feedback recebido com sucesso"))
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new FeedbackResponse(e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new FeedbackResponse("Erro interno ao processar feedback"))
                    .build();
        }
    }
}
