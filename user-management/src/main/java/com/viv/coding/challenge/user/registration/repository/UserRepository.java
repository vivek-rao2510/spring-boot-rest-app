package com.viv.coding.challenge.user.registration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viv.coding.challenge.user.registration.model.User;

/**
 * This interface extends {@link JpaRepository} which contains API for basic CRUD operations and also API for pagination and sorting
 * 
 * @Author Vivek Rao
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
