package edu.comillas.icai.gitt.pat.spring.padelapp.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class ConfiguracionSeguridad {

    @Bean
    public SecurityFilterChain configuracion(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/pistaPadel/auth/**").permitAll()
                        // Solo ADMIN puede modificar datos (POST, PUT, PATCH, DELETE)
                        .requestMatchers(HttpMethod.POST, "/pistaPadel/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/pistaPadel/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/pistaPadel/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/pistaPadel/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService usuarios() {
        // Definimos un ADMIN y un USER en memoria para probar
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("admin123")
                .roles("ADMIN")
                .build();

        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("user123")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(admin, user);
    }
}