package graphql.controller;

import graphql.entity.User;
import graphql.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping("/users")
    public User addUser(User user) {
        return userService.createUser(user);
    }

    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/{userId}")
    public User getUserById (@PathVariable Integer userId) {
        return userService.getUserById(userId);
    }

    @DeleteMapping("/users/{userId}")
    public Boolean deleteUserById (@PathVariable Integer userId) {
         return userService.deleteUserById(userId);
    }
}
