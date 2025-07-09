package healthcare.healthcare_spring.service;

import healthcare.healthcare_spring.domain.User;
import healthcare.healthcare_spring.domain.UserRepository;
import healthcare.healthcare_spring.dto.request.UserRequest;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public void saveUser(UserRequest request) {
        User u = userRepository.save(new User(request.getName(),request.getHeight(),
                request.getWeight(),request.getGoalWeight(), request.getDate(), request.getGoalWalk()));
    }
    public User getUserByName(String name) {
        return userRepository.findByName(name);
    }
}
