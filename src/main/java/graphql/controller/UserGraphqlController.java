package graphql.controller;

import graphql.entity.User;
import graphql.service.UserService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.util.List;

@Controller
public class UserGraphqlController {

    private final UserService userService;

    public UserGraphqlController(UserService userService) {
        this.userService = userService;
    }

    @QueryMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @QueryMapping
    public User getUserById(@Argument Integer userId) {
        return userService.getUserById(userId);
    }

    @MutationMapping
    public User createUser(@Argument String userName, @Argument String userEmail, @Argument String userMobile) {

        User user = new User();
        user.setUserName(userName);
        user.setUserEmail(userEmail);
        user.setUserMobile(userMobile);

        return userService.createUser(user);
    }

    @MutationMapping
    public Boolean deleteUserById(@Argument Integer userId) {
        return userService.deleteUserById(userId);
    }
}