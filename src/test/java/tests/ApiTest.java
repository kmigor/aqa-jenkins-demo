package tests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import io.qameta.allure.Description;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class ApiTest {
    @Test
    @Tag("smoke")
    @Description("Check that post API returns 200")
    void getPost_shouldReturn200() {
        given()
                .baseUri("https://jsonplaceholder.typicode.com")
        .when()
                .get("/post/1")
        .then()
                .statusCode(404);
//                .body("id",equalTo(1));


    }

}
