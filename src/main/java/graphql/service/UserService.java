package graphql.service;

import graphql.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    User createUser(User user);

    List<User> getAllUsers();

    User getUserById(Integer userId);

    Boolean deleteUserById(Integer userId);
}
