package com.assignment;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;

/**
 * Test class for User API testing.
 * @author sbadekar
 */
public class UserTest {

    /**
     * Test basic User API get using Hamcrest's matchers.
     */
    @Test
    public void testUserGet() {
        RestAssured
                .given().header("Content-Type","application/json")
                .when().get("https://reqres.in/api/users")
                .then().statusCode(200)
                .and()
                .contentType(ContentType.JSON)
                .and().assertThat().body("data", hasItem(hasProperty("first_name", is("George"))));
    }

    @Test
    public void testUserCreation() {
        Map<String,String> user = new HashMap<String, String>();
        user.put("name", "sujata");
        user.put("job", "automation");

        RestAssured
                .given().contentType("application/json")
                .body(user)
                .when().post("https://reqres.in/api/users")
                .then()
                .contentType(ContentType.JSON)
                .and().statusCode(201)
                .and().assertThat().body("name", is("sujata"));
    }

    @Test
    public void testUserUpdate() {
        Map<String,String> user = new HashMap<String, String>();
        user.put("name", "badekar");

        RestAssured
                .given().contentType("application/json")
                .body(user)
                .when().put("https://reqres.in/api/users/2")
                .then()
                .contentType(ContentType.JSON)
                .and().statusCode(200)
                .and().assertThat().body("name", is("badekar"));
    }

    @Test
    public void testUserDeletion() {

        RestAssured
                .given()
                .when().delete("https://reqres.in/api/users/2")
                .then().statusCode(204);
    }
}
