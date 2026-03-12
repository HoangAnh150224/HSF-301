package hsf.project_gr1.service.impl;

import hsf.project_gr1.model.entity.User;
import hsf.project_gr1.model.entity.Wallet;
import hsf.project_gr1.model.enums.UserRole;
import hsf.project_gr1.repository.UserRepository;
import hsf.project_gr1.repository.WalletRepository;
import hsf.project_gr1.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(String username, String email, String password) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        // Create user
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(UserRole.USER)
                .isActive(true)
                .build();
        
        user = userRepository.save(user);

        // Create wallet for user
        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(BigDecimal.ZERO)
                .currency("VND")
                .build();
        walletRepository.save(wallet);

        return user;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }
}
