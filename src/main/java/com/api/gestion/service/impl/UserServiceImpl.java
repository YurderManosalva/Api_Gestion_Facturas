package com.api.gestion.service.impl;

import com.api.gestion.constants.FacturaConstantes;
import com.api.gestion.model.PasswordResetToken;
import com.api.gestion.model.User;
import com.api.gestion.repository.PasswordResetTokenRepository;
import com.api.gestion.repository.UserRepository;
import com.api.gestion.security.CustomerDetailService;
import com.api.gestion.security.jwt.JwtUtil;
import com.api.gestion.service.UserService;
import com.api.gestion.util.FacturaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomerDetailService customerDetailService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        log.info("Registro interno de un usuario {}", requestMap);
        try {
            if (validateSignUpMap(requestMap)) {
                User user = userRepository.findByEmail((requestMap.get("email")));
                if (Objects.isNull(user)) {
                    userRepository.save(getUserFromMap(requestMap));
                    return FacturaUtils.getResponseEntity("Usuario registrado con éxito", HttpStatus.CREATED);
                } else {
                    return FacturaUtils.getResponseEntity("El ususario con ese email ya existe", HttpStatus.BAD_REQUEST);
                }
            } else {
                return FacturaUtils.getResponseEntity(FacturaConstantes.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return FacturaUtils.getResponseEntity(FacturaConstantes.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Dentro de login");
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestMap.get("email"), requestMap.get("password"))
            );

            if (authentication.isAuthenticated()) {
                if (customerDetailService.getUserDetail().getStatus().equalsIgnoreCase("true")) {
                    return new ResponseEntity<String>("{\"token\":\"" + jwtUtil.generateToken(customerDetailService.getUserDetail().getEmail(),
                            customerDetailService.getUserDetail().getRole()) + "\"}",
                            HttpStatus.OK);
                } else {
                    return new ResponseEntity<String>("{\"mensaje\":\"" + "Espera la aprobacion del administrador" + "\"}", HttpStatus.BAD_REQUEST);
                }
            }
        } catch (Exception exception) {
            log.error("{}", exception);
        }
        return new ResponseEntity<String>("{\"mensaje\":\"" + "Credenciales incorrectas" + "\"}", HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        log.info("Dentro de forgotPassword {}", requestMap);

        try {
            if (!requestMap.containsKey("email")) {
                return FacturaUtils.getResponseEntity("El email es obligatorio", HttpStatus.BAD_REQUEST);
            }

            User user = userRepository.findByEmail(requestMap.get("email"));
            if (Objects.isNull(user)) {
                return FacturaUtils.getResponseEntity("Usuario no encontrado", HttpStatus.NOT_FOUND);
            }

            String token = UUID.randomUUID().toString();
            LocalDateTime expirationDate = LocalDateTime.now().plusMinutes(15);

            PasswordResetToken existingToken = tokenRepository.findByUser(user);

            if (existingToken != null) {
                existingToken.setToken(token);
                existingToken.setExpirationDate(expirationDate);
                tokenRepository.save(existingToken);
            } else {
                PasswordResetToken resetToken = new PasswordResetToken();
                resetToken.setToken(token);
                resetToken.setUser(user);
                resetToken.setExpirationDate(expirationDate);
                tokenRepository.save(resetToken);
            }

            String resetUrl = "http://localhost:8081/user/resetPassword?token=" + token;

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(user.getEmail());
            mailMessage.setSubject("Restablecer contraseña");
            mailMessage.setText("Hola " + user.getNombre() + ",\n\n" +
                    "Para restablecer tu contraseña, haz clic en el siguiente enlace:\n" +
                    resetUrl + "\n\nEste enlace expira en 15 minutos.\n\n" +
                    "Si no solicitaste restablecer tu contraseña, ignora este correo.");
            mailSender.send(mailMessage);

            return FacturaUtils.getResponseEntity(
                    "Se ha enviado un correo con instrucciones para restablecer tu contraseña",
                    HttpStatus.OK
            );

        } catch (Exception exception) {
            log.error("Error en forgotPassword: {}", exception.getMessage());
        }

        return FacturaUtils.getResponseEntity(FacturaConstantes.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> resetPassword(Map<String, String> requestMap) {
        log.info("Dentro de resetPassword {}", requestMap);

        try {
            if (!requestMap.containsKey("token") || !requestMap.containsKey("newPassword")) {
                return FacturaUtils.getResponseEntity("El token y la nueva contraseña son obligatorios", HttpStatus.BAD_REQUEST);
            }

            PasswordResetToken resetToken = tokenRepository.findByToken(requestMap.get("token"));
            if (Objects.isNull(resetToken)) {
                return FacturaUtils.getResponseEntity("Token inválido", HttpStatus.BAD_REQUEST);
            }

            if (resetToken.isExpired()) {
                return FacturaUtils.getResponseEntity("El token ha expirado", HttpStatus.BAD_REQUEST);
            }

            User user = resetToken.getUser();
            user.setPassword(requestMap.get("newPassword"));
            userRepository.save(user);

            tokenRepository.delete(resetToken);

            return FacturaUtils.getResponseEntity("Contraseña restablecida con éxito", HttpStatus.OK);

        } catch (Exception exception) {
            log.error("Error en resetPassword: {}", exception.getMessage());
        }

        return FacturaUtils.getResponseEntity(FacturaConstantes.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateSignUpMap(Map<String, String> requestMap) {
        return requestMap.containsKey("nombre")
                && requestMap.containsKey("numeroDeContacto")
                && requestMap.containsKey("email")
                && requestMap.containsKey("password");
    }

    private User getUserFromMap(Map<String, String> requestMap) {
        User user = new User();
        user.setNombre(requestMap.get("nombre"));
        user.setNumeroDeContacto(requestMap.get("numeroDeContacto"));
        user.setEmail(requestMap.get("email"));
        user.setPassword(requestMap.get("password"));
        user.setStatus("false");
        user.setRole("user");
        return user;
    }
}
