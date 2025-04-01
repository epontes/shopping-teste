package br.com.shopping.application.service;

import br.com.shopping.application.dto.UserDTO;
import br.com.shopping.domain.entity.Role;
import br.com.shopping.domain.entity.User;
import br.com.shopping.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("O usuário não foi encontrado com e-mail "  + username));
    }

    public User createUser(UserDTO userDTO, Role role) {
        if(userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Esse e-mail já está em uso");
        }

        User user =  User.builder()
                .email(userDTO.getEmail())
                .username(userDTO.getUsername())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .fullName(userDTO.getFullName())
                .role(role)
                .build();

        return userRepository.save(user);

    }

    private List<Object[]> getTop5UsersBySpent() {
        return userRepository.findTop5UsersByTotalSpent();
    }

    public List<Object[]> getAverageTicketByUser() {
        return userRepository.findAverageTicketByUser();
    }
}
