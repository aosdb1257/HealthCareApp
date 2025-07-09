package healthcare.healthcare_spring.controller;

import healthcare.healthcare_spring.domain.User;
import healthcare.healthcare_spring.dto.request.UserRequest;
import healthcare.healthcare_spring.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/user")
    public void saveUser(@RequestBody UserRequest request) {
        userService.saveUser(request);
    }
    @GetMapping("/user/{name}")
    public User getUserByName(@PathVariable String name) {
        return userService.getUserByName(name);
    }
}
