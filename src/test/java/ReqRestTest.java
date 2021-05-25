import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.http.Headers;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static io.restassured.path.json.JsonPath.from;

public class ReqRestTest {

    @Before
    public void setUp()
    {
        /*
        RestAssured.baseURI = "https://reqres.in";
        RestAssured.basePath = "api";
        RestAssured.filters(new RequestLoggingFilter(),new ResponseLoggingFilter());
        RestAssured.requestSpecification = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .build();
         */

        RestAssured.requestSpecification = default_RequestSpecification();

    }

    @Test
    public void loginTest()
    {
        given()
                .contentType(ContentType.JSON)
                .body("{\n" +
                        "    \"email\": \"eve.holt@reqres.in\",\n" +
                        "    \"password\": \"cityslicka\"\n" +
                        "}")
                .post("login")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("token", notNullValue());
    }

    @Test
    public void getSingleUserTest()
    {
        given()
                .contentType(ContentType.JSON)
                .get("users/2")
                .then()
                .statusCode(200)
                .body("data.id", equalTo(2));
    }

    @Test
    public void deletteUserTest()
    {
        given()
                .delete("users/2")
                .then()
                .statusCode(HttpStatus.SC_NO_CONTENT);
    }

    @Test
    public void patchUserTest()
    {
        String nameUpdated = given()
                .when()
                .body("{\n" +
                        "    \"name\": \"morpheus\",\n" +
                        "    \"job\": \"zion resident\"\n" +
                        "}")
                .patch("users/2")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("name");

        assertThat(nameUpdated,equalTo("morpheus"));
    }

    @Test
    public void putUserTest()
    {
        String nameUpdated = given()
                .when()
                .body("{\n" +
                        "    \"name\": \"morpheus\",\n" +
                        "    \"job\": \"zion resident\"\n" +
                        "}")
                .put("users/2")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath().getString("job");

        assertThat(nameUpdated,equalTo("zion resident"));
    }

    @Test
    public void getAllUsersTest()
    {
        Response response = given()
                               .get("users?page=2");


        Headers headers = response.getHeaders();
        int statusCode = response.statusCode();
        String body = response.getBody().asString();
        String contentType = response.getContentType();

        assertThat(statusCode,equalTo(HttpStatus.SC_OK));

        System.out.println("Body: "+body);
        System.out.println("Content Type: "+contentType);
        System.out.println("Headers :"+ headers.toString());
        System.out.println("**********************");
        System.out.println("**********************");
        System.out.println(headers.get("Content-Type"));
        System.out.println(headers.get("Transfer-Encoding"));

    }

    @Test
    public void getAllUsersTestJson()
    {
       String response = given()
               .when()
               .get("users?page=2")
               .then().extract().body().asString();

       int page = from(response).get("page");
       int totalPage = from(response).get("total_pages");
       int idFirstUser = from(response).get("data[0].id");

        System.out.println("Page: "+page);
        System.out.println("Total Page: "+totalPage);
        System.out.println("Id First User: "+idFirstUser);

        List<Map> userWithIdGreaterThan10 = from(response).get("data.findAll{user-> user.id > 10}");
        String mail = userWithIdGreaterThan10.get(0).get("email").toString();

        List<Map> user = from(response).get("data.findAll{user-> user.id > 10 && user.last_name == 'Howell'}");
        String id = user.get(0).get("id").toString();
        int intId = Integer.parseInt(user.get(0).get("id").toString());

    }

    @Test
    public void createUsersTest() {
        String response = given()
                .when()
                .body("{\n" +
                        "    \"name\": \"morpheus\",\n" +
                        "    \"job\": \"leader\"\n" +
                        "}")
                .post("users")
                .then()
                .extract().body().asString();
        User user = from(response).getObject("",User.class);
        System.out.println(user.getId());
        System.out.println(user.getJob());
    }

    @Test
    public void registerUsersTest() {
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setEmail("eve.holt@reqres.in");
        userRequest.setPassword("pistol");

        CreateUserResponse response = given()
                .when()
                .body(userRequest)
                .post("register")
                .then()
                .statusCode(200)
                .contentType("application/json; charset=utf-8")
                .extract()
                .body()
                .as(CreateUserResponse.class);

        assertThat(response.getId(),equalTo(4));
        assertThat(response.getToken(),equalTo("QpwL5tke4Pnpja7X4"));


        //System.out.println(user.getId());
        //System.out.println(user.getJob());
    }


    public RequestSpecification default_RequestSpecification() {


        List<Filter> filters = new ArrayList<>();
        filters.add(new RequestLoggingFilter());
        filters.add(new ResponseLoggingFilter());

        return new RequestSpecBuilder().setBaseUri("https://reqres.in")
                .setBasePath("/api")
                .addFilters(filters)
                .setContentType(ContentType.JSON).build();
    }

    public RequestSpecification default_ProdRequestSpecification() {

        return new RequestSpecBuilder().setBaseUri("https://prod.reqres.in")
                .setBasePath("/api")
                .setContentType(ContentType.JSON).build();
    }

    public ResponseSpecification default_ResponseSpecification() {

        return new ResponseSpecBuilder()
                .expectStatusCode(HttpStatus.SC_OK)
                .expectContentType(ContentType.JSON)
                .build();
    }

    @Test
    public void registerUsersTestReqSpec() {
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setEmail("eve.holt@reqres.in");
        userRequest.setPassword("pistol");

        CreateUserResponse response = given().spec(default_ProdRequestSpecification())
                .when()
                .body(userRequest)
                .post("register")
                .then()
                .statusCode(200)
                .contentType("application/json; charset=utf-8")
                .extract()
                .body()
                .as(CreateUserResponse.class);

        assertThat(response.getId(),equalTo(4));
        assertThat(response.getToken(),equalTo("QpwL5tke4Pnpja7X4"));


        //System.out.println(user.getId());
        //System.out.println(user.getJob());
    }

    @Test
    public void registerUsersTestResponseSpec() {
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setEmail("eve.holt@reqres.in");
        userRequest.setPassword("pistol");

        CreateUserResponse response = given()
                .when()
                .body(userRequest)
                .post("register")
                .then()
                .spec(default_ResponseSpecification())
                .extract()
                .body()
                .as(CreateUserResponse.class);

        assertThat(response.getId(),equalTo(4));
        assertThat(response.getToken(),equalTo("QpwL5tke4Pnpja7X4"));


        //System.out.println(user.getId());
        //System.out.println(user.getJob());
    }

}
