package com.viv.coding.challenge.user.registration;

import java.net.URI;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viv.coding.challenge.user.registration.error.UpdateIdOperationNotAllowedException;
import com.viv.coding.challenge.user.registration.error.UserAlreadyExistsException;
import com.viv.coding.challenge.user.registration.error.UserNotFoundException;
import com.viv.coding.challenge.user.registration.model.Status;
import com.viv.coding.challenge.user.registration.model.User;
import com.viv.coding.challenge.user.registration.service.UserService;

/**
 * This class acts as the entry point for all REST requests related to User Management application.
 * <p>
 * HTTP requests are handled here since it has the {@link RestController} annotation.
 * <p>
 * Swagger related documentation can be accessed using the following links:
 * <ol>
 * <li>API Docs: http://localhost:8080/v3/api-docs/</li>
 * <li>Swagger UI: http://localhost:8080/swagger-ui.html</li>
 * </ol>
 * A table 'users' is created in the in-memory H2 Database which is used to store the user details and query them.
 * 
 * @Author Vivek Rao
 */
@RestController
@RequestMapping(value = "api/v1/users", produces = { MediaType.APPLICATION_JSON_VALUE })
public class UserManagementController {

    @Autowired
    UserService userService;

    /**
     * This method handles the POST request to register a new {@link User}. It first gets all the saved users by calling
     * {@link UserService#getUsers()} and returns a {@link HttpStatus#CONFLICT} if the given User's details are already in use by another
     * {@link User}.
     * <p>
     * If the {@link User} doesn't exist, then it is added to the 'users' table and a {@link HttpStatus#CREATED} status code is sent back along with
     * the newly created {@link User} updated with it's 'id' value updated.
     * 
     * @param user
     *            the {@link User} details to be added
     * @return {@link ResponseEntity} containing appropriate Response
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody final User user) {
        final List<User> users = userService.getUsers();

        for (final User storedUser : users) {
            if (storedUser.equals(user)) {
                return new ResponseEntity<>(String.valueOf(Status.USER_ALREADY_EXISTS), HttpStatus.CONFLICT);
            }
        }
        final User updatedUserWithId = userService.addUser(user);

        return ResponseEntity
                .created(URI
                        .create(String.format("/users/%s", updatedUserWithId.getId())))
                .body(updatedUserWithId);
    }

    /**
     * The method logs in a {@link User}.
     * <p>
     * If the {@link User} hasn't been registered yet using '/user/register' endpoint previously, then it returns a {@link HttpStatus#NOT_FOUND}
     * status code along with an appropriate message.
     * <p>
     * If the {@link User} has been registered already then the 'lastLoggedIn' attribute for that {@link User} is updated in 'users' table and a
     * {@link HttpStatus#OK} is sent back.
     * 
     * @param loggedInUser
     *            the {@link User} to be logged in
     * @return {@link ResponseEntity} containing appropriate Response
     */
    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody final User loggedInUser) {
        final List<User> users = userService.getUsers();

        for (final User user : users) {
            if (loggedInUser.getEmail().equals(user.getEmail())
                    && loggedInUser.getPassword().equals(user.getPassword())) {
                user.setLastLoggedIn(new Timestamp(System.currentTimeMillis()));
                userService.updateUser(user);
                return ResponseEntity.ok().body(String.valueOf(Status.LOGIN_SUCCESSFUL));
            }
        }
        return new ResponseEntity<>(String.valueOf(Status.LOGIN_FAILED), HttpStatus.NOT_FOUND);
    }

    /**
     * This method updates the {@link User} details.
     * <p>
     * If the 'id' from {@link User} doesn't match the path variable sent in the Request, then {@link UpdateIdOperationNotAllowedException} is thrown
     * and a {@link HttpStatus#BAD_REQUEST} is returned.
     * <p>
     * If the {@link User} doesn't exist in the 'users' table, then a {@link UserNotFoundException} is thrown and a {@link HttpStatus#NOT_FOUND} is
     * returned.
     * <p>
     * If the {@link User} doesn't exist in the 'users' table, but the 'username' or 'email' being updated is already in use, then
     * {@link UserAlreadyExistsException} is thrown and {@link HttpStatus#CONFLICT} is returned.
     * <p>
     * Else, the {@link User} details are updated and {@link HttpStatus#OK} is sent back along with an appropriate message.
     * 
     * @param user
     *            the {@link User} to be updated
     * @return {@link ResponseEntity} containing appropriate Response
     */
    @PutMapping(path = "{id}")
    public ResponseEntity<String> updateUser(@RequestBody final User user, @PathVariable(value = "id") final long id) {
        try {
            if (id != user.getId()) {
                throw new UpdateIdOperationNotAllowedException("User's 'id' cannot be updated using PUT operation.");
            }
            userService.updateUser(user);
            return new ResponseEntity<>(String.valueOf(Status.SUCCESS), HttpStatus.OK);
        } catch (final UserNotFoundException exception) {
            final String message = exception.getMessage();
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        } catch (final UserAlreadyExistsException exception) {
            final String message = exception.getMessage();
            return new ResponseEntity<>(message, HttpStatus.CONFLICT);
        } catch (final UpdateIdOperationNotAllowedException exception) {
            final String message = exception.getMessage();
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * This method retrieves a {@link User} by its 'id'. If the user is found, it is returned. If the user doesn't exist then,
     * {@link HttpStatus#NOT_FOUND} is sent back along with a message.
     * 
     * @param id
     *            the 'id' of the {@link User} to be retrieved
     * @return {@link ResponseEntity} containing appropriate Response
     */
    @GetMapping(path = "{id}")
    public ResponseEntity<?> findUserById(@PathVariable(value = "id") final long id) {
        final Optional<User> user = userService.findById(id);

        if (user.isPresent()) {
            return ResponseEntity.ok().body(user.get());
        } else {
            return new ResponseEntity<>(String.valueOf(Status.USER_NOT_FOUND), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * This method retrieves all the {@link User}s present in the 'users' table. If the table's empty a {@link HttpStatus#NO_CONTENT} is sent back.
     * 
     * @return {@link ResponseEntity} containing appropriate Response
     */
    @GetMapping()
    public ResponseEntity<?> getAllUsers() {
        final List<User> users = userService.getUsers();
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    /**
     * This method deletes a {@link User} by its 'id'.
     * <p>
     * If the User doesn't exist, a {@link UserNotFoundException} is thrown and a {@link HttpStatus#NOT_FOUND} is sent back.
     * <p>
     * If the User exists, then the {@link User} is deleted from the 'users' table and {@link HttpStatus#OK} is sent back.
     * 
     * @param id
     *            The 'id' of the {@link User} to be deleted
     * @return {@link ResponseEntity} containing appropriate Response
     */
    @DeleteMapping(path = "{id}")
    public ResponseEntity<String> deleteUser(@PathVariable(value = "id") final long id) {
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(String.valueOf(Status.SUCCESS), HttpStatus.OK);
        } catch (final UserNotFoundException exception) {
            return new ResponseEntity<>(String.valueOf(Status.USER_NOT_FOUND), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * This method deletes all the {@link User}s in the 'users' table and sends back a {@link HttpStatus#OK} status code.
     * 
     * @return {@link ResponseEntity} containing appropriate Response
     */
    @DeleteMapping()
    public ResponseEntity<String> deleteUsers() {
        userService.deleteAll();
        return new ResponseEntity<>(String.valueOf(Status.SUCCESS), HttpStatus.OK);
    }

}
