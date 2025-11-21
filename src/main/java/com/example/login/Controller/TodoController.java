package com.example.login.Controller;

import com.example.login.Entity.Todo;
import com.example.login.Service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;

    // Tüm Todo'ları Getir
    @GetMapping
    public List<Todo> getTodos(Principal principal) {
        // Token'dan gelen kullanıcı adını alıyoruz
        return todoService.getUserTodos(principal.getName());
    }

    // Yeni Todo Ekle
    @PostMapping
    public Todo addTodo(@RequestBody Todo todo, Principal principal) {
        return todoService.addTodo(principal.getName(), todo.getContent());
    }

    // Todo Güncelle
    @PutMapping("/{id}")
    public Todo updateTodo(@PathVariable Long id, @RequestBody Todo todo) {
        return todoService.updateTodo(id, todo);
    }

    // Todo Sil
    @DeleteMapping("/{id}")
    public String deleteTodo(@PathVariable Long id) {
        todoService.deleteTodo(id);
        return "Todo silindi.";
    }
}