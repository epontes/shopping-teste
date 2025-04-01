package br.com.shopping.presentation;


import br.com.shopping.application.dto.AuthenticationRequest;
import br.com.shopping.application.dto.AuthenticationResponse;
import br.com.shopping.application.dto.UserDTO;
import br.com.shopping.application.service.UserService;
import br.com.shopping.domain.entity.Role;
import br.com.shopping.domain.entity.User;
import br.com.shopping.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;


    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody UserDTO request) {
        User user = userService.createUser(request, Role.USER);

        String jwtToken = jwtService.generateToken(user);

        return ResponseEntity.ok(AuthenticationResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .userRole(user.getRole().name())
                .build());
    }

    @PostMapping("/register-admin")
    public ResponseEntity<AuthenticationResponse> registerAdmin(@RequestBody UserDTO request) {
        User user = userService.createUser(request, Role.ADMIN);

        String jwtToken = jwtService.generateToken(user);

        return ResponseEntity.ok(AuthenticationResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .userRole(user.getRole().name())
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = (User) userService.loadUserByUsername(request.getUsername());
        String jwtToken = jwtService.generateToken(user);

        return ResponseEntity.ok(AuthenticationResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .userRole(user.getRole().name())
                .build());
    }

}
