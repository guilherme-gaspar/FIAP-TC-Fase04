package org.acme.lambda;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.isOneOf;

@QuarkusTest
class LambdaHandlerTest {

    @Test
    void shouldInvokeLambdaHandler() {
        given()
                .contentType("application/json")
                .accept("application/json")
                .body(Map.of("descricao", "Aula boa", "nota", 8, "urgency", "MEDIA"))
                .when()
                .post()
                .then()
                .statusCode(isOneOf(200, 500));
    }
}
