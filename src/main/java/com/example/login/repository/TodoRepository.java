package com.example.login.repository;

import com.example.login.Entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    // Kullanıcı adına göre todoları getir
    List<Todo> findByUser_Username(String username);
}