import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import jdk.jfr.Name;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.RestAssured.given;

public class eCommerce {

    //variables
    static private String url_base = "webapi.segundamano.mx";
    static private String email = "test.2609@mailinator.com";
    static private String pass = "test01010101";
    static private String access_token;
    static private String account_id;
    static private String uuid;
    static private String ad_id;
    static private String AddressId;

    @Name("ObtenerToken")
    private String ObtenerToken() {
        RestAssured.baseURI = String.format("https://%s/nga/api/v1.1/private/accounts", url_base);
        Response response = given().log().all().queryParam("lang", "es")
                .auth().preemptive().basic(email, pass).post();

        String body_response = response.getBody().prettyPrint();
        System.out.println("body response" + " " + body_response);

        //cambiar body a json
        JsonPath jsonResponse = response.jsonPath();

        String accesstoken = jsonResponse.get("access_token");
        System.out.println("Token en funcion" + " " + accesstoken);
        access_token = accesstoken;

        //Otras Variables
        String accountId = jsonResponse.get("account.account_id");
        account_id = accountId;
        System.out.println(account_id);

        String uid = jsonResponse.get("account.uuid");
        System.out.println(uid);
        uuid = uid;
        return accesstoken;
    }

    @Name("ObtenerAdrressId")

    private String ObtenerAdrressId() {

        String token = ObtenerToken();
        System.out.println("token de función: " + token);
        System.out.println("access token: " + access_token);
        System.out.println("account id funciona: " + account_id);
        System.out.println("uuid funciona: " + uuid);


        //el body es con form se usa formparam
        RestAssured.baseURI = String.format("https://%s/addresses/v1/create", url_base);
        Response response = given().log().all().formParam("contact", "Agente ventas")
                .formParam("phone", "1354654654").formParam("rfc", "CICC850612CV6")
                .formParam("zipCode", "01000").formParam("exteriorInfo", "sdfsdfsf")
                .formParam("interiorInfo", "2").formParam("region", "13")
                .formParam("area", "151876").formParam("alias", "El trabajo")
                .formParam("municipality", "348").contentType("application/x-www-form-urlencoded")
                .header("accept", "application/json, text/plain, */*").auth().preemptive().basic(uuid, access_token).post();

        JsonPath jsonResponse = response.jsonPath();
        AddressId = jsonResponse.get("addressID");
        String body_response = response.getBody().prettyPrint();
        System.out.println("Codigo de respuesta" + " " + response.getStatusCode());
        System.out.println("body response" + " " + body_response);

        return AddressId;

    }

    @Test
    @Order(2)
    @DisplayName("Test case: Obtener categorias")
    @Severity(SeverityLevel.BLOCKER)
    public void get_ObtenerCategorias_200() {

        RestAssured.baseURI = String.format("https://%s/nga/api/v1.1/public/categories/filter", url_base);

        Response response = given().log().all()
                .queryParam("lang", "es")
                .header("accept", "application/json, text/plain, */*")
                .filter(new AllureRestAssured()).get();

        String body_response = response.getBody().prettyPrint();

        //imprimir el body
        System.out.println("body response" + " " + body_response);

        //Tests
        assertEquals(200, response.getStatusCode());
        assertNotNull(body_response);
        assertTrue(body_response.contains("id"));
        assertTrue(body_response.contains("categories"));
        assertTrue(body_response.contains("all_label"));
        assertTrue(body_response.contains("icon"));
        long tiempo = response.getTime();
        assertTrue(tiempo < 4000);

    }

    @Test
    @Order(3)
    public void Post_ListarCategoria_200() {

        RestAssured.baseURI = String.format("https://%s/urls/v1/public/ad-listing", url_base);

        //body
        String bodyRequest = "{\"filters\":[{\"price\":\"-60000\",\"category\":\"2021\"},\n" +
                "    {\"price\":\"60000-80000\",\"category\":\"2021\"},\n" +
                "    {\"price\":\"80000-100000\",\"category\":\"2021\"},\n" +
                "    {\"price\":\"100000-150000\",\"category\":\"2021\"},\n" +
                "    {\"price\":\"160000-\",\"category\":\"2021\"}\n" +
                "    ]}";

        Response response = given().log().all()
                .queryParam("lang", "es")
                .header("content-type", "application/json")
                .header("accept", "application/json, text/plain, */*")
                .body(bodyRequest).post();

        String body_response = response.getBody().prettyPrint();
        //imprimir el body
        System.out.println("body response" + " " + body_response);

        //Tests
        assertEquals(200, response.getStatusCode());
        assertNotNull(body_response);
        assertTrue(body_response.contains("data"));
        assertTrue(body_response.contains("urls"));
        long tiempo = response.getTime();
        assertTrue(tiempo < 4000);

    }

    @Test
    @Order(1)
    public void Post_CrearUsuario_401() {

        //Crear usuario
        String new_user = "agenteVentas" + (Math.floor(Math.random() * 900)) + "@mailinator.com";
        String body_request = "{\"account\":{\"email\":\"" + new_user + "\"}}";
        RestAssured.baseURI = String.format("https://%s/nga/api/v1.1/private/accounts?lang=es", url_base);

        Response response = given().log().all().queryParam("lang", "es")
                .contentType("application/json")
                .auth().preemptive().basic(new_user, pass).body(body_request).post();
        String body_response = response.getBody().prettyPrint();

        //Tests
        assertEquals(401, response.getStatusCode());
        assertNotNull(body_response);
        assertTrue(body_response.contains("error"));
        assertTrue(body_response.contains("code"));
        assertTrue(body_response.contains("ACCOUNT_VERIFICATION_REQUIRED"));
        long tiempo = response.getTime();
        assertTrue(tiempo < 4000);

        //imprimir info
        System.out.println("Codigo de respuesta" + " " + response.getStatusCode());
        System.out.println("body response" + " " + body_response);
    }

    @Test
    @Order(4)
    public void Post_IngresarUsuario_200() {

        String body_request = "{\"account\":{\"email\":\"" + email + "\"}}";

        //llamada
        RestAssured.baseURI = String.format("https://%s/nga/api/v1.1/private/accounts", url_base);
        Response response = given().log().all().queryParam("lang", "es")
                .contentType("application/json")
                .auth().preemptive().basic(email, pass).body(body_request).post();
        String body_response = response.getBody().prettyPrint();
        System.out.println("Codigo de respuesta" + " " + response.getStatusCode());
        System.out.println("body response" + " " + body_response);

        //Tests
        assertEquals(200, response.getStatusCode());
        assertNotNull(body_response);
        assertTrue(body_response.contains("access_token"));
        assertTrue(body_response.contains("account"));
        assertTrue(body_response.contains("email"));
        assertTrue(body_response.contains("name"));
        long tiempo = response.getTime();
        assertTrue(tiempo < 4000);
        JsonPath jsonResponse = response.jsonPath();
        boolean emailVerified = jsonResponse.get("account.email_verified");
        assertEquals(true, emailVerified);
        String contentType = response.header("Content-Type");
        assertEquals("application/json; charset=utf-8", contentType);
        //imprimir info
        System.out.println("Codigo de respuesta" + " " + response.getStatusCode());
        System.out.println("body response" + " " + body_response);
        System.out.println("email de ver" + " " + emailVerified);

    }

    @Test
    @Order(5)
    public void post_CrearDireccion_200() {
        String token = ObtenerToken();
        System.out.println("token de función: " + token);
        System.out.println("access token: " + access_token);
        System.out.println("account id funciona: " + account_id);
        System.out.println("uuid funciona: " + uuid);

        //el body es con form se usa formparam
        RestAssured.baseURI = String.format("https://%s/addresses/v1/create", url_base);
        Response response = given().log().all().formParam("contact", "Agente ventas").
                formParam("phone", "1354654654").formParam("rfc", "CICC850612CV6").
                formParam("zipCode", "01000").formParam("exteriorInfo", "sdfsdfsf").
                formParam("interiorInfo", "2").formParam("region", "13").
                formParam("area", "151876").formParam("alias", "El trabajo").
                formParam("municipality", "348").contentType("application/x-www-form-urlencoded").
                header("accept", "application/json, text/plain, */*").auth().preemptive().basic(uuid, access_token).post();

        String body_response = response.getBody().prettyPrint();
        System.out.println("Codigo de respuesta" + " " + response.getStatusCode());
        System.out.println("body response" + " " + body_response);

        //Tests
        assertEquals(201, response.getStatusCode());
        assertNotNull(body_response);
        assertTrue(body_response.contains("addressID"));
        long tiempo = response.getTime();
        assertTrue(tiempo < 4000);

    }

    @Test
    @Order(6)
    public void post_CrearAnuncio_200() {
        String token = ObtenerToken();
        System.out.println("token de función: " + token);
        System.out.println("access token: " + access_token);
        System.out.println("account id funciona: " + account_id);
        System.out.println("uuid funciona: " + uuid);

        RestAssured.baseURI = String.format("https://%s/v2/accounts/%s/up", url_base, uuid);

        //body

        String bodyRequest = "{ \"category\": \"8142\",\n" +
                "    \"subject\": \"Te organizo tu evento\",\n" +
                "    \"body\": \"trabajamos todo tipo de eventos, desde bautizos hasta bodas y divorcio.\",\n" +
                "    \"region\": \"5\",\n" +
                "    \"municipality\": \"53\",\n" +
                "    \"area\": \"140311\",\n" +
                "    \"price\": \"733\",\n" +
                "    \"phone_hidden\": \"true\",\n" +
                "    \"show_phone\": \"false\",\n" +
                "    \"contact_phone\": \"1354654654\"}";


        Response response = given().log().all()
                .contentType("application/json").header("accept", "application/json, text/plain, */*")
                .header("x-source", "PHOENIX_DESKTOP")
                .header("accept-language", "en-US,en;q=0.9")
                .auth().preemptive().basic(uuid, access_token).body(bodyRequest).post();

        String body_response = response.getBody().prettyPrint();
        System.out.println("Codigo de respuesta" + " " + response.getStatusCode());

        //Tests
        assertEquals(200, response.getStatusCode());
        assertNotNull(body_response);
        assertTrue(body_response.contains("ad"));
        assertTrue(body_response.contains("ad_id"));
        assertTrue(body_response.contains("subject"));
        assertTrue(body_response.contains("category"));
        long tiempo = response.getTime();
        assertTrue(tiempo < 5000);

    }

    @Test
    @Order(7)
    public void Put_EditarAnuncio_200() {

        String token = ObtenerToken();
        System.out.println("token de función: " + token);
        System.out.println("access token: " + access_token);
        System.out.println("account id funciona: " + account_id);
        System.out.println("uuid funciona: " + uuid);
        System.out.println("ad id funciona : " + ad_id);

        RestAssured.baseURI = String.format("https://%s/v2/accounts/%s/up/%s", url_base, uuid, ad_id);

        //body

        String bodyRequest = "{ \"category\": \"8142\",\n" +
                "    \"subject\": \"Te organizo tu evento\",\n" +
                "    \"body\": \"Qui ut aut suscipit deserunt laudantium rerum qui dolorum.\",\n" +
                "    \"region\": \"5\",\n" +
                "    \"municipality\": \"53\",\n" +
                "    \"area\": \"140311\",\n" +
                "    \"price\": \"733\",\n" +
                "    \"phone_hidden\": \"true\",\n" +
                "    \"show_phone\": \"false\",\n" +
                "    \"contact_phone\": \"1354654654\"}";


        Response response = given().log().all()
                .contentType("application/json")
                .header("accept", "application/json, text/plain, */*")
                .header("x-source", "PHOENIX_DESKTOP")
                .auth().preemptive().basic(uuid, access_token).body(bodyRequest).put();

        String body_response = response.getBody().prettyPrint();

        //Tests
        assertEquals(200, response.getStatusCode());
        assertNotNull(body_response);
        assertTrue(body_response.contains("ad"));
        assertTrue(body_response.contains("ad_id"));
        assertTrue(body_response.contains("subject"));
        assertTrue(body_response.contains("category"));
        long tiempo = response.getTime();
        assertTrue(tiempo < 4000);

    }

    @Test
    @Order(8)
    public void Get_ObtenerCuenta_200() {
        String token = ObtenerToken();

        RestAssured.baseURI = String.format("https://%s/nga/api/v1%s", url_base, account_id);
        Response response = given().log().all()
                .header("accept", "application/json, text/plain, */*")
                .header("authorization", "tag:scmcoord.com,2013:api" + " " + access_token)
                .param("lang", "es").get();

        String body_response = response.getBody().prettyPrint();
//Tests

        assertEquals(200, response.getStatusCode());
        assertNotNull(body_response);
        assertTrue(body_response.contains("account"));
        assertTrue(body_response.contains("account_id"));
        assertTrue(body_response.contains("email"));
        assertTrue(body_response.contains("email_verified"));
        long tiempo = response.getTime();
        assertTrue(tiempo < 4000);

    }

    @Test
    @Order(9)

    public void Patch_EditarUsuario_200() {

        String token = ObtenerToken();

        RestAssured.baseURI = String.format("https://%s/nga/api/v1%s", url_base, account_id);

        //body
        String bodyRequest = "{\n" +
                "    \"account\": {\n" +
                "        \"name\": \"Lara Croft\",\n" +
                "        \"phone\": \"1354654654\",\n" +
                "        \"locations\": [\n" +
                "            {\n" +
                "                \"code\": \"5\",\n" +
                "                \"key\": \"region\",\n" +
                "                \"label\": \"Baja California Sur\",\n" +
                "                \"locations\": [\n" +
                "                    {\n" +
                "                        \"code\": \"53\",\n" +
                "                        \"key\": \"municipality\",\n" +
                "                        \"label\": \"La Paz\"\n" +
                "                    }\n" +
                "                ]\n" +
                "            }\n" +
                "        ],\n" +
                "        \"professional\": false,\n" +
                "        \"phone_hidden\": false\n" +
                "    }\n" +
                "}";


        Response response = given().log().all()
                .header("content-type", "application/json")
                .header("accept", "application/json, text/plain, */*")
                .header("authorization", "tag:scmcoord.com,2013:api" + " " + access_token)
                .param("lang", "es").body(bodyRequest).patch();
        String body_response = response.getBody().prettyPrint();


//Tests

        assertEquals(200, response.getStatusCode());
        assertNotNull(body_response);
        assertTrue(body_response.contains("account"));
        assertTrue(body_response.contains("account_id"));
        assertTrue(body_response.contains("email"));
        assertTrue(body_response.contains("email_verified"));
        long tiempo = response.getTime();
        assertTrue(tiempo < 4000);


    }

    @Test
    @Order(10)
    public void Put_EditarDireccion_200() {

        String token = ObtenerToken();
        String id = ObtenerAdrressId();
        System.out.println("address id funciona: " + AddressId);
        System.out.println("uuid funciona: " + uuid);

        //el body es con form se usa formparam
        RestAssured.baseURI = String.format("https://%s/addresses/v1/modify/%s", url_base, AddressId);

        Response response = given().log().all().formParam("contact", "Agente ventas")
                .formParam("phone", "1354653654").formParam("rfc", "CICC850612CV6")
                .formParam("zipCode", "01000").formParam("exteriorInfo", "sdf46546sf")
                .formParam("interiorInfo", "2").formParam("region", "13")
                .formParam("area", "151876").formParam("alias", "La casa")
                .formParam("municipality", "348").contentType("application/x-www-form-urlencoded")
                .header("accept", "application/json, text/plain, */*").auth().preemptive().basic(uuid, access_token).put();


        String body_response = response.getBody().prettyPrint();
        //Tests
        assertEquals(200, response.getStatusCode());
        assertNotNull(body_response);
        assertTrue(body_response.contains("message"));
        long tiempo = response.getTime();
        assertTrue(tiempo < 6000);
    }
    @Test
    @Order(11)

    public void Get_ListadoDirecciones_200() {

        String token = ObtenerToken();
        System.out.println("uuid funciona: " + uuid);

        RestAssured.baseURI = String.format("https://%s/addresses/v1/get", url_base);

        Response response = given().log().all().contentType("application/x-www-form-urlencoded")
                .header("accept", "application/json, text/plain, */*")
                .auth().preemptive().basic(uuid, access_token).get();

        String body_response = response.getBody().prettyPrint();

        //Tests
        assertEquals(200, response.getStatusCode());
        assertNotNull(body_response);
        assertTrue(body_response.contains("addresses"));
        assertTrue(body_response.contains("exteriorInfo"));
        assertTrue(body_response.contains("interiorInfo"));
        assertTrue(body_response.contains("phone"));
        long tiempo = response.getTime();
        assertTrue(tiempo < 6000);

    }

    @Test
    @Order(12)
    public void Get_ListadoFavoritos_200() {

        String token = ObtenerToken();
        System.out.println("uuid funciona: " + uuid);


        RestAssured.baseURI = String.format("https://%s/favorites/v1/private/accounts/%s", url_base, uuid);

        Response response = given().log().all()
                .header("accept", "application/json, text/plain, */*")
                .auth().preemptive().basic(uuid, access_token).get();

        String body_response = response.getBody().prettyPrint();

        //Tests
        assertEquals(200, response.getStatusCode());
        assertNotNull(body_response);
        assertTrue(body_response.contains("name"));
        assertTrue(body_response.contains("list_ids"));
        long tiempo = response.getTime();
        assertTrue(tiempo < 6000);


    }

    @Test
    @Order(13)
    public void Get_ListShops_200() {

        String token = ObtenerToken();
        System.out.println("uuid funciona: " + uuid);

        RestAssured.baseURI = String.format("https://%s/shops/api/v1/public/shops", url_base);

        Response response = given().log().all()
                .header("accept", "application/json, text/plain, */*")
                .header("x-requested-with", "XMLHttpRequest")
                .param("limit", "15").get();

        String body_response = response.getBody().prettyPrint();

        //Tests
        assertEquals(200, response.getStatusCode());
        assertNotNull(body_response);
        assertTrue(body_response.contains("id"));
        assertTrue(body_response.contains("cover"));
        long tiempo = response.getTime();
        assertTrue(tiempo < 6000);

    }

    @Test
    @Order(14)

    public void Post_CrearAlerta_200(){

        String token = ObtenerToken();
        System.out.println("uuid funciona: " + uuid);


        RestAssured.baseURI = String.format("https://%s/alerts/v1/private/account/%s/alert", url_base,uuid);

        String bodyRequest = "{\n" +
                "    \"ad_listing_service_filters\": {\n" +
                "        \"region\": \"17\",\n" +
                "        \"category_lv0\": \"1000\",\n" +
                "        \"category_lv1\": \"1020\"\n" +
                "    }\n" +
                "}";

        Response response = given().log().all()
                .header("accept", "application/json, text/plain, */*")
                .header("content-type", "application/json").body(bodyRequest)
                .auth().preemptive().basic(uuid, access_token).post();

        String body_response = response.getBody().prettyPrint();

        //Tests
        assertEquals(200, response.getStatusCode());
        assertNotNull(body_response);
        assertTrue(body_response.contains("alert"));
        assertTrue(body_response.contains("title"));
        assertTrue(body_response.contains("description"));
        long tiempo = response.getTime();
        assertTrue(tiempo < 6000);

    }
    @Test
    @Order(15)
    public void Delete_Address_200(){

        String token = ObtenerToken();
        String id = ObtenerAdrressId();

        System.out.println("uuid funciona: " + uuid);


        RestAssured.baseURI = String.format("https://%s/addresses/v1/delete/%s", url_base,AddressId);
        Response response = given().log().all()
                .header("accept", "application/json, text/plain, */*")
                .auth().preemptive().basic(uuid, access_token).delete();

        String body_response = response.getBody().prettyPrint();

        //Tests
        assertEquals(200, response.getStatusCode());
        assertNotNull(body_response);
        assertTrue(body_response.contains("message"));
        long tiempo = response.getTime();
        assertTrue(tiempo < 6000);

    }


}


