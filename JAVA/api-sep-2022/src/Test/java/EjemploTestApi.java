import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class EjemploTestApi {
    @Test
    public void api_test(){
        RestAssured.baseURI = String.format("https://reqres.in/api/users?page=2");
        Response response = given().log().all().headers("Accept","*/*").get();
        String body_response = response.getBody().prettyPrint();

//para poder ver info de parametros headers etc es que se aplica log y all

    //Creo variable para mostrar el status code

    String responseCode  = String.format(String.valueOf(response.getStatusCode()));
    System.out.println("Status Code"+ " "+ responseCode);

    //Assertions
    assertEquals(200,response.getStatusCode());
    assertNotNull(body_response);
    assertTrue(body_response.contains("id"));

    //validar tiempo de respuesta
        long tiempo = response.getTime();
        assertTrue(tiempo < 800);
    //validar header

        String headers_response = response.getHeaders().toString();
        System.out.println("Las cabeceras" + headers_response);
        assertTrue(headers_response.contains("application/json"));
}
        @Test
    public void AddPet(){

        RestAssured.baseURI = String.format("https://petstore.swagger.io/v2/pet");

        String requestBody = "{\n" +
                "  \n" +
                "  \"category\": {\n" +
                "    \n" +
                "    \"name\": \"Perrosrazagrande\"\n" +
                "  },\n" +
                "  \"name\": \"Ignacio\",\n" +
                "  \"photoUrls\": [\n" +
                "    \"string\"\n" +
                "  ],\n" +
                "  \"tags\": [\n" +
                "    {\n" +
                "      \n" +
                "      \"name\": \"string\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"status\": \"available\"\n" +
                "}";

        Response response = given().log().all().header("Accept","*/*").
                header("Content-Type","application/json").body(requestBody).post();
        String body_response = response.getBody().prettyPrint();
        System.out.println("Cuerpo de respuesta"+" "+body_response);
        System.out.println("Codigo de respuesta"+" "+response.getStatusCode());

    }
    }