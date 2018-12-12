package com.assignment;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.config.EncoderConfig.encoderConfig;
import static org.hamcrest.Matchers.*;
import static com.assignment.constants.RxConstants.*;
import static org.testng.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Test class for User API testing.
 * @author sbadekar
 */
public class UserTest {

    @BeforeClass
    public void setUp() {
        RestAssured.config = new RestAssuredConfig().encoderConfig(encoderConfig().defaultContentCharset("UTF-8"));
        RestAssured.baseURI = baseURI;
    }

    @Test(description = "verify Get API Call ")
    public void testUserGet() {
        given()
                .when().get("/users")
                .then().statusCode(HTTP_STATUS_CODE_OK)
                .and()
                .contentType(ContentType.JSON)
                .and().assertThat().body("data.first_name", hasItem("George")).log().all();
    }

    @Test(description = "verify Post API Call ")
    public void testUserCreation() {
        Map<String,String> user = new HashMap<>();
        user.put("name", "John");
        user.put("job", "Software Engineer");

        given().contentType(ContentType.JSON)
                .body(user)
                .when().post("/users")
                .then()
                .contentType(ContentType.JSON)
                .and().statusCode(HTTP_STATUS_CODE_CREATED)
                .and().assertThat().body("name", is("John")).log().all();
    }

    @Test(description = "verify Put API Call ")
    public void testUserUpdate() {
        Map<String,String> user = new HashMap<>();
        user.put("name", "Merry");

        given().contentType(ContentType.JSON)
                .body(user)
                .when().put("/users/2")
                .then()
                .contentType(ContentType.JSON)
                .and().statusCode(HTTP_STATUS_CODE_OK)
                .and().assertThat().body("name", is("Merry")).log().all();;
    }

    @Test(description = "verify Delete API Call ")
    public void testUserDeletion() {
        given()
                .when().delete("/users/2")
                .then().statusCode(HTTP_STATUS_CODE_NO_CONTENT).log().all();;
    }


    //Additional Test Cases
    @Test(description = "verify Get API Call for user which does not exist")
    public void testUserGetInvalidUser() {
        given().contentType(ContentType.JSON)
                .when().get("/users/100")
                .then().statusCode(HTTP_STATUS_CODE_NOT_FOUND).log().all();
    }

    @Test(description = "verify Get API Call for Per page records & total_pages calculation based on per page count ")
    public void testUserGetperPageRecord() {
        Response response =  RestAssured.given().contentType(ContentType.JSON)
                .when().get("/users?per_page=1");
        assertEquals(HTTP_STATUS_CODE_OK , response.statusCode());
        User user = response.as(User.class);
        assertEquals(user.getPage(), new Integer(1));
        assertEquals(user.getTotal(), new Integer(12));
        assertEquals(user.getTotal_pages(), new Integer(12));
        assertEquals( user.getData().size(),1);
    }

    @Test(description = "verify Get API Call for all given query parameters for setting pagination")
    public void testUserGetWithAllQueryParameters() {
        Response response =  RestAssured.given().contentType(ContentType.JSON)
                .when().get("/users?per_page=6&page=2&total=12&total_pages=2");
        assertEquals(HTTP_STATUS_CODE_OK , response.statusCode());
        User user = response.as(User.class);
        List<Integer> idList= user.getData().stream().map(Employee :: getId).collect(Collectors.toList());
        assertThat(idList, hasSize(6));
        assertThat(idList, contains(7, 8, 9, 10, 11, 12));

    }

    @Test(description = "verify Get API Call for  maximum records limit crossed : it shows 12 only as total is 12 ")
    public void testUserGetWithMaxLimit() {
        RestAssured
                .given().contentType(ContentType.JSON)
                .when().get("/users?per_page=15")
                .then().statusCode(HTTP_STATUS_CODE_OK)
                .and()
                .contentType(ContentType.JSON)
                .and().assertThat().body("total", is(12))
                .and().assertThat().body("total_pages", is(1))
                .and().assertThat().body("data",hasSize(12)).log().all();
    }

    @Test(description = "verify Get API Call by giving page number which does not have record: Empty set returns ")
    public void testUserGetWithNorecordPage() {
        RestAssured
                .given().contentType(ContentType.JSON)
                .when().get("/users?page=13")
                .then().statusCode(HTTP_STATUS_CODE_OK)
                .and()
                .contentType(ContentType.JSON)
                .and().assertThat().body("data",hasSize(0)).log().all();
    }

    @Test(description = "verify Get API Call with invalid query parameters: Empty set returns ")
    public void testUserGetWithInvalidQueryParameters() {
        RestAssured
                .given().contentType(ContentType.JSON)
                .when().get("/users?per_page=-1&page=50&total=20&total_pages=100")
                .then().statusCode(HTTP_STATUS_CODE_OK)
                .and()
                .contentType(ContentType.JSON)
                .and().assertThat().body("data",hasSize(0)).log().all();
    }

     /*
     Below scenarios tried for invalid datatype for other methods but seems no
     validaton added so test cases are getting passed.
    */
     @Test(description = "verify Post API Call with invalid data: Users still getting created no validations ")
    public void testUserCreationWithInvalidData() {
        Map<Integer,Integer> user = new HashMap<>();
        user.put(1,2);
        user.put(3,4);
        user.put(5,6);
        user.put(7,8);

        RestAssured
                .given().contentType(ContentType.JSON)
                .body(user)
                .when().post("/users")
                .then()
                .contentType(ContentType.JSON)
                .and().statusCode(HTTP_STATUS_CODE_CREATED).log().all();
    }

    @Test(description = "verify Put API Call with invalid data: Still getting updated as no validation ")
    public void testUserUpdateWithInvalidData() {
        Map<Integer,Boolean> user = new HashMap<Integer, Boolean>();
        user.put(5, false);

        RestAssured
                .given().contentType(ContentType.JSON)
                .body(user)
                .when().put("/users/-1")
                .then()
                .contentType(ContentType.JSON)
                .and().statusCode(HTTP_STATUS_CODE_OK).log().all();
    }

}
