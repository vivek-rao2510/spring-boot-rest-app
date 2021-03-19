package com.viv.coding.challenge.user.registration.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

/**
 * Entity class that describes a 'User'.
 * 
 * @Author Vivek Rao
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @NotEmpty(message = "Username must not be empty")
    private String username;
    //Improvement store encrypted password
    @NotEmpty(message = "Password must not be empty")
    private String password;
    @NotEmpty(message = "Email must not be empty")
    private String email;
    private java.sql.Timestamp lastLoggedIn;

    public User() {

    }

    public User(final Long id,
            @NotEmpty(message = "Username must not be empty") final String username,
            @NotEmpty(message = "Password must not be empty") final String password,
            @NotEmpty(message = "Email must not be empty") final String email) {
        super();
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public User(@NotEmpty(message = "Username must not be empty") final String username,
            @NotEmpty(message = "Password must not be empty") final String password,
            @NotEmpty(message = "Email must not be empty") final String email) {
        super();
        this.username = username;
        this.password = password;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public java.sql.Timestamp getLastLoggedIn() {
        return lastLoggedIn;
    }

    public void setLastLoggedIn(final java.sql.Timestamp lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", lastLoggedInTime='" + lastLoggedIn + "'}";
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        final User user = (User) object;
        return getUsername().equals(user.getUsername()) || getEmail().equals(user.getEmail());
    }

}
