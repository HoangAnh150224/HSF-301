package hsf.project_gr1.service;

import hsf.project_gr1.model.entity.User;

public interface AuthService {
    User register(String username, String email, String password);
    User findByUsername(String username);
}
