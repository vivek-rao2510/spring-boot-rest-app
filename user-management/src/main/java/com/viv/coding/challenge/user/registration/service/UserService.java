package com.viv.coding.challenge.user.registration.service;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.viv.coding.challenge.user.registration.error.UserAlreadyExistsException;
import com.viv.coding.challenge.user.registration.error.UserNotFoundException;
import com.viv.coding.challenge.user.registration.model.User;
import com.viv.coding.challenge.user.registration.repository.UserRepository;

/**
 * This class acts as{@link Service} class that peforms the business logic of the User Management application. It in turn works with
 * {@link UserRepository}'s methods in order to save data into 'users' table
 * 
 * @Author Vivek Rao
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * This method retrieves all the {@link User}s from the 'users' table
     * 
     * @return {@link List} of {@link User}s
     */
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    /**
     * This method adds the given {@link User} in the 'users' table
     * 
     * @param newUser
     *            {@link User} to be added
     * @return the {@link User} with its 'id' field populated by the DB.
     */
    public User addUser(final User newUser) {
        return userRepository.save(newUser);
    }

    /**
     * This method updates the {@link User} in 'users' table
     * 
     * @param updatedUser
     *            {@link User} to be updated
     */
    public void updateUser(final User updatedUser) {
        final List<User> allUsers = getUsers();
        final User user = userRepository
                .findById(updatedUser.getId())
                .orElseThrow(() -> new UserNotFoundException("No User with details: " + updatedUser + " found!"));

        for (final User existingUser : allUsers) {
            if (existingUser.equals(updatedUser) && (existingUser.getId() != (updatedUser.getId()))) {
                throw new UserAlreadyExistsException("User with email: '" + existingUser.getEmail()
                        + "' and username: '" + existingUser.getUsername() + "' exists already!");
            }
        }
        final String userName = updatedUser.getUsername();
        final String email = updatedUser.getEmail();
        final String password = updatedUser.getPassword();

        if (userName != null && userName.length() > 0 && !user.getUsername().equals(userName)) {
            user.setUsername(userName);
        }
        if (email != null && email.length() > 0 && !user.getEmail().equals(email)) {
            user.setEmail(email);
        }

        if (password != null && password.length() > 0 && !user.getPassword().equals(password)) {
            user.setPassword(password);
        }

        userRepository.save(user);
    }

    /**
     * This method deletes the given {@link User} by its 'id' from the 'users' table
     * 
     * @param userId
     *            the {@link User}'s 'id' to be deleted
     */
    public void deleteUser(final Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return;
        }
        throw new UserNotFoundException("No User by id: " + userId + " found!");
    }

    /**
     * @param id
     *            the {@link User} to retrieve from the 'users' table
     * @return {@link Optional} object containing {@link User}
     */
    public Optional<User> findById(final long id) {
        return userRepository.findById(id);
    }

    /**
     * This method deletes all the {@link User}s from the 'users' table
     */
    public void deleteAll() {
        userRepository.deleteAll();
    }
}
