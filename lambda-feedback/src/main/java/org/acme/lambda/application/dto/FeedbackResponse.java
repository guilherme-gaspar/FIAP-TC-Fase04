package org.acme.lambda.application.dto;

public class FeedbackResponse {

    private String descricao;

    public FeedbackResponse(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

}
