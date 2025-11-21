package com.example.login.Service;

import com.example.login.Entity.Todo;
import com.example.login.Entity.User;
import com.example.login.repository.TodoRepository;
import com.example.login.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    // O anki kullanıcının Todo'larını getir
    public List<Todo> getUserTodos(String username) {
        return todoRepository.findByUser_Username(username);
    }

    // Yeni Todo Ekle
    public Todo addTodo(String username, String content) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Todo todo = new Todo();
        todo.setContent(content);
        todo.setUser(user); // Todo'yu kullanıcıya bağla

        return todoRepository.save(todo);
    }

    // Todo Sil (Sadece kendi todo'sunu silebilir)
    public void deleteTodo(Long id) {
        todoRepository.deleteById(id);
    }

    // Todo Güncelle (Tamamlandı işaretle veya metni değiştir)
    public Todo updateTodo(Long id, Todo gelenTodo) {
        Todo mevcutTodo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo bulunamadı"));

        mevcutTodo.setContent(gelenTodo.getContent());
        mevcutTodo.setCompleted(gelenTodo.isCompleted());

        return todoRepository.save(mevcutTodo);
    }
}