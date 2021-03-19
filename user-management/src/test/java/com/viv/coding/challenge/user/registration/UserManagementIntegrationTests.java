package com.viv.coding.challenge.user.registration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viv.coding.challenge.user.registration.model.User;

/**
 * This class contains Integration Tests for the User Management Application. First, it starts the Spring Boot application and initializes a Apache
 * Tomcat server instance.
 * <p>
 * It then executes the various test cases by hitting different REST Endpoints exposed in {@link UserManagementController} and asserts the Responses.
 * <p>
 * This class uses {@link SpringBootTest} annotation which loads the actual application context.
 * <p>
 * It uses {@link SpringBootTest.WebEnvironment}.RANDOM_PORT to create run the application at some random server port.
 * <p>
 * {@link @LocalServerPort} gets the reference of port where the server has started. It helps in building the actual request URIs to mimic real client
 * interactions.
 * <p>
 * Using {@link TestRestTemplate} class helps in invoking the HTTP requests which are handled by controller class.
 * <p>
 * {@link Sql} annotation helps in populating database with some prerequisite data if test is dependent on it to test the behavior correctly. In this
 * case, a 'data.sql' file contains the schema for the 'users' table that is created in the H2 database and is a pre-requisite before starting the
 * Integration Tests
 * 
 * @Author Vivek Rao
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserManagementApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({ "classpath:data.sql" })
public class UserManagementIntegrationTests {

    @LocalServerPort
    private int port;
    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void givenUserDoesNotExists_AndWhenUserIsRegistered_then201StatusCodeIsReceived() throws JsonProcessingException {

        final User user = new User("JohnSmith", "password123", "john.smith@example.com");
        final String url = createURLWithPort("/api/v1/users/register");
        final ResponseEntity<?> responseEntity = restTemplate
                .postForEntity(url, user, String.class);
        assertEquals(201, responseEntity.getStatusCodeValue());
        final User updatedUser = objectMapper.readValue(String.valueOf(responseEntity.getBody()), User.class);
        assertEquals("/users/" + updatedUser.getId(), String.valueOf(responseEntity.getHeaders().getLocation()));
    }

    @Test
    public void givenAUserExists_AndSameUserIsRegisteredAgain_then409StatusCodeIsReceived() throws JsonProcessingException {
        final String url = createURLWithPort("/api/v1/users/register");
        final User user = new User("JohnSmith", "password123", "john.smith@example.com");
        registerAndReturnUser(user);

        final User user2 = new User("JohnSmith", "password123", "john.smith@example.com");
        final ResponseEntity<String> responseEntity = restTemplate
                .postForEntity(url, user2, String.class);
        assertEquals(409, responseEntity.getStatusCodeValue());
        assertEquals("Status: User already exists!! Please enter a different e-mail address or username.", responseEntity.getBody());
    }

    @Test
    public void givenAUserIsRegistered_whenUserLogsIn_then200StatusCodeIsReceived() throws JsonProcessingException {
        final User user = new User("JohnSmith", "password123", "john.smith@example.com");
        final User savedUser = registerAndReturnUser(user);

        final String url2 = createURLWithPort("/api/v1/users/login");
        final User user2 = new User("JohnSmith", "password123", "john.smith@example.com");
        final ResponseEntity<String> responseEntity2 = restTemplate
                .postForEntity(url2, user2, String.class);
        assertEquals(200, responseEntity2.getStatusCodeValue());
        assertEquals("Status: Login successful.", responseEntity2.getBody());
        final String url = createURLWithPort("/api/v1/users/" + savedUser.getId());
        final ResponseEntity<User> responseUser = restTemplate.getForEntity(url, User.class);
        final User retrievedUser = responseUser.getBody();
        assertEquals(savedUser, retrievedUser);
        assertNotNull(retrievedUser.getLastLoggedIn());
    }

    @Test
    public void givenAUserIsNotRegistered_whenThisUserLogsIn_thenStatusCode404IsReceived() {
        final String url = createURLWithPort("/api/v1/users/login");
        final User user = new User("JohnSmith", "password123", "john.smith@example.com");
        final ResponseEntity<String> responseEntity = restTemplate
                .postForEntity(url, user, String.class);
        assertEquals(404, responseEntity.getStatusCodeValue());
        assertEquals("Status: Login failure. Please create the user before logging in!", responseEntity.getBody());
    }

    @Test
    public void givenAUserIsRegistered_whenThisUserDetailsAreUpdated_thenStatusCode200IsReceived() throws JsonProcessingException {
        final String url = createURLWithPort("/api/v1/users/register");
        final User user = new User("JohnSmith", "password123", "john.smith@example.com");
        final ResponseEntity<?> responseEntity = createUserAndReturnResponse(url, user);
        assertEquals(201, responseEntity.getStatusCodeValue());

        final User updatedUser = objectMapper.readValue(String.valueOf(responseEntity.getBody()), User.class);
        assertEquals("/users/" + updatedUser.getId(), String.valueOf(responseEntity.getHeaders().getLocation()));

        final String url2 = createURLWithPort("/api/v1/users/" + updatedUser.getId());
        final User user2 = new User(updatedUser.getId(), "JohnSmith", "newpassword123", "john.smith1@example.com");
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<User> entity = new HttpEntity<>(user2, headers);
        final ResponseEntity<String> response = restTemplate.exchange(url2, HttpMethod.PUT, entity, String.class);
        final String responseBody = response.getBody();

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Status: Request is Successful", responseBody);
    }

    @Test
    public void givenAUserIsRegistered_whenThisUserDetailsAreUpdatedButIdIsChanged_thenStatusCode400IsReceived() throws JsonProcessingException {
        final String url = createURLWithPort("/api/v1/users/register");
        final User user = new User("JohnSmith", "password123", "john.smith@example.com");
        final ResponseEntity<?> responseEntity = createUserAndReturnResponse(url, user);
        assertEquals(201, responseEntity.getStatusCodeValue());

        final User updatedUser = objectMapper.readValue(String.valueOf(responseEntity.getBody()), User.class);
        assertEquals("/users/" + updatedUser.getId(), String.valueOf(responseEntity.getHeaders().getLocation()));

        final String url2 = createURLWithPort("/api/v1/users/" + updatedUser.getId());
        //Sending different ID in body compared to Path variable in URL
        final User user2 = new User(5555L, "JohnSmith", "newpassword123", "john.smith1@example.com");
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<User> entity = new HttpEntity<>(user2, headers);
        final ResponseEntity<String> response = restTemplate.exchange(url2, HttpMethod.PUT, entity, String.class);
        final String responseBody = response.getBody();

        assertEquals(400, response.getStatusCodeValue());
        assertEquals("User's 'id' cannot be updated using PUT operation.", responseBody);
    }

    @Test
    public void givenUser1andUser2AreRegistered_whenUser2sEmailAndUsernameAreUpdatedToBeSameAsUser1_thenStatusCode409IsReceived()
            throws JsonProcessingException {
        final User user1 = new User("JohnSmith", "password123", "john.smith@example.com");
        registerAndReturnUser(user1);
        final String url2 = createURLWithPort("/api/v1/users/register");
        final User user2 = new User("Jack", "password", "jack@example.com");
        final ResponseEntity<?> responseEntity2 = createUserAndReturnResponse(url2, user2);
        assertEquals(201, responseEntity2.getStatusCodeValue());

        final User updatedUser = objectMapper.readValue(String.valueOf(responseEntity2.getBody()), User.class);
        assertEquals("/users/" + updatedUser.getId(), String.valueOf(responseEntity2.getHeaders().getLocation()));

        //Change username Jack to existing user JohnSmith
        final User user = new User(updatedUser.getId(), "JohnSmith", "newpassword123", "john.smith@example.com");
        final String url = createURLWithPort("/api/v1/users/" + user.getId());
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<User> entity = new HttpEntity<>(user, headers);
        final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
        final String responseBody = response.getBody();

        assertEquals(409, response.getStatusCodeValue());
        assertEquals("User with email: 'john.smith@example.com' and username: 'JohnSmith' exists already!", responseBody);
    }

    @Test
    public void givenUserDoesNotExists_ifUpdateIsPerformed_thenStatusCode404IsReceived() {
        final String url = createURLWithPort("/api/v1/users/12222");
        //Change username Jack to existing user JohnSmith
        final User user = new User(12222L, "JohnSmith", "newpassword123", "john.smith@example.com");
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<User> entity = new HttpEntity<>(user, headers);
        final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
        final String responseBody = response.getBody();

        assertEquals(404, response.getStatusCodeValue());
        assertEquals(
                "No User with details: User{id=12222, username='JohnSmith', password='newpassword123', email='john.smith@example.com', lastLoggedInTime='null'} found!",
                responseBody);
    }

    @Test
    public void givenUserExists_whenGetByIdIsPerformed_thenTheExpectedUserIsReceived() throws JsonProcessingException {
        final User user = new User("JohnSmith", "password123", "john.smith@example.com");
        final User savedUser = registerAndReturnUser(user);
        final String url = createURLWithPort("/api/v1/users/" + savedUser.getId());
        final ResponseEntity<User> responseUser = restTemplate.getForEntity(url, User.class);
        final User retrievedUser = responseUser.getBody();
        assertEquals(savedUser, retrievedUser);
        assertEquals(savedUser.getId(), retrievedUser.getId());
    }

    @Test
    public void givenUserWithId10000DoesNotExists_whenGetByIdIsPerformed_then404IsReceived() throws JsonProcessingException {
        final String url = createURLWithPort("/api/v1/users/10000");
        final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Status: User does not exist!! Please check the user details entered.", response.getBody());
    }

    @Test
    public void givenUsersExist_whenGetAllUsersIsPerformed_thenAllUsersAreReceived() throws JsonProcessingException {
        final String url = createURLWithPort("/api/v1/users");
        final User user1 = new User("JohnSmith", "password123", "john.smith@example.com");
        final User user2 = new User("Jack", "password", "jack@example.com");
        final List<User> savedUsers = new ArrayList<>(2);
        Collections.addAll(savedUsers, user1, user2);
        registerAndReturnUser(user1);
        registerAndReturnUser(user2);

        final HttpEntity<User> entity = new HttpEntity<>(user1);
        final ResponseEntity<List<User>> response = restTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<User>>() {
        });
        final List<User> retrievedUsers = response.getBody();
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(retrievedUsers);
        assertTrue(retrievedUsers.size() > 0);
        //Hamcrest jar to compare 2 lists in any order
        assertThat(savedUsers, Matchers.containsInAnyOrder(retrievedUsers.toArray()));
        assertEquals(2, retrievedUsers.size());
    }

    @Test
    public void givenUsersDoNotExist_whenGetAllUsersIsPerformed_then204StatusCodeIsReceived() {
        final String url = createURLWithPort("/api/v1/users");
        final ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        final String body = response.getBody();

        assertEquals(204, response.getStatusCodeValue());
    }

    @Test
    public void givenUserExists_whenDeleteByUserIdIsPerformed_thenThatUserIsDeleted() throws JsonProcessingException {
        final User user1 = new User("JohnSmith", "password123", "john.smith@example.com");
        final User savedUser = registerAndReturnUser(user1);
        final String url = createURLWithPort("/api/v1/users/" + savedUser.getId());
        final HttpEntity<String> entity = new HttpEntity<>("");
        final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Status: Request is Successful", response.getBody());
    }

    @Test
    public void givenUserDoesNotExists_whenDeleteByUserIdIsPerformed_thenStatusCode404IsReceived() {
        final String url = createURLWithPort("/api/v1/users/10000");
        final HttpEntity<String> entity = new HttpEntity<>("");
        final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("Status: User does not exist!! Please check the user details entered.", response.getBody());
    }

    @Test
    public void givenUsersExist_whenDeleteAllUsersIsPerformed_thenStatusCode200IsReceived() throws JsonProcessingException {
        final String url = createURLWithPort("/api/v1/users");
        final User user1 = new User("JohnSmith", "password123", "john.smith@example.com");
        final User user2 = new User("Jack", "password123", "jack@example.com");
        final User savedUser1 = registerAndReturnUser(user1);
        final User savedUser2 = registerAndReturnUser(user2);
        final HttpEntity<String> entity = new HttpEntity<>("");
        final ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Status: Request is Successful", response.getBody());

        //Send GET by ID and check Users have been deleted
        final String url1 = createURLWithPort("/api/v1/users/" + savedUser1.getId());
        final ResponseEntity<String> response1 = restTemplate.getForEntity(url1, String.class);
        assertEquals(404, response1.getStatusCodeValue());
        assertEquals("Status: User does not exist!! Please check the user details entered.", response1.getBody());

        final String url2 = createURLWithPort("/api/v1/users/" + savedUser2.getId());
        final ResponseEntity<String> response2 = restTemplate.getForEntity(url2, String.class);
        assertEquals(404, response2.getStatusCodeValue());
        assertEquals("Status: User does not exist!! Please check the user details entered.", response2.getBody());
    }

    private User registerAndReturnUser(final User user) throws JsonProcessingException {
        final String url = createURLWithPort("/api/v1/users/register");
        final ResponseEntity<String> responseEntity = createUserAndReturnResponse(url, user);
        assertEquals(201, responseEntity.getStatusCodeValue());
        final User updatedUser = objectMapper.readValue(String.valueOf(responseEntity.getBody()), User.class);
        assertEquals("/users/" + updatedUser.getId(), String.valueOf(responseEntity.getHeaders().getLocation()));
        return updatedUser;
    }

    private ResponseEntity<String> createUserAndReturnResponse(final String url, final User user) {
        return restTemplate
                .postForEntity(url, user, String.class);
    }

    private String createURLWithPort(final String uri) {
        return "http://localhost:" + port + uri;
    }

}
