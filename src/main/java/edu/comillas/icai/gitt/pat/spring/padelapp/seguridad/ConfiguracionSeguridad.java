package edu.comillas.icai.gitt.pat.spring.padelapp.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {


    @Bean
    public SecurityFilterChain configuracion(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) -> authorize
                        // 1. RUTAS PÚBLICAS
                        .requestMatchers("/pistaPadel/auth/**").permitAll()
                        .requestMatchers("/pistaPadel/health").permitAll()

                        // 2. RUTAS DE ADMIN (POST, PUT, DELETE)
                        .requestMatchers(HttpMethod.POST, "/pistaPadel/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/pistaPadel/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/pistaPadel/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/pistaPadel/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults()); // Basic Auth para Postman

        return http.build();
    }

    @Bean
    public UserDetailsService usuarios(PasswordEncoder encoder) {

        UserDetails admin = User.builder()
                .username("admin")
                .password(encoder.encode("admin123"))
                .roles("ADMIN")
                .build();

        UserDetails user = User.builder()
                .username("user")
                .password(encoder.encode("user123"))
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }
}