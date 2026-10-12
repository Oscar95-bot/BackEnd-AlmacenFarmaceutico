package com.almacen.farmaceutico.AlmacenFarmaceutico.config;

import com.almacen.farmaceutico.AlmacenFarmaceutico.repository.UsuarioRepository;
import com.almacen.farmaceutico.AlmacenFarmaceutico.service.UsuarioService;
import com.almacen.farmaceutico.AlmacenFarmaceutico.service.JwtService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.web.cors.*;
import java.util.Arrays;
import java.util.List;

@Configuration
@OpenAPIDefinition(security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UserDetailsService userDetailsService(UsuarioRepository usuarios) {
        return nombre -> usuarios.findByUsuario(UsuarioService.normalizar(nombre))
                .map(u -> User.withUsername(u.getUsuario()).password(u.getContrasena())
                        .roles(u.getRol().name()).disabled(!u.isActivo()).build())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));
    }

    @Bean
    AuthenticationManager authenticationManager(UserDetailsService users, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(users);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.cors.origins}") String origins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization"));
        config.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwt,
                                          UsuarioRepository usuarios) throws Exception {
        http.cors(cors -> {})
                .csrf(config -> config.disable())
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .requestCache(config -> config.disable())
                .logout(config -> config.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()
                         .requestMatchers("/api/auth/login").permitAll()
                         .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
                        .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.POST, "/api/insumos", "/api/movimientos", "/api/movimientos/**")
                            .hasAnyRole("ADMINISTRADOR", "OPERATIVO")
                        .requestMatchers(HttpMethod.PUT, "/api/insumos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/insumos/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/reportes/**").hasAnyRole("ADMINISTRADOR", "LOGISTICA")
                        .requestMatchers(HttpMethod.GET, "/api/auth/perfil", "/api/insumos", "/api/insumos/**", "/api/catalogos").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMINISTRADOR", "OPERATIVO", "LOGISTICA")
                        .anyRequest().denyAll())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(401);
                            res.setHeader("WWW-Authenticate", "Bearer");
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"mensaje\":\"Debe iniciar sesión.\"}");
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(403);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"mensaje\":\"No tiene permiso para realizar esta operación.\"}");
                        }))
                .addFilterAfter(new UsuarioActivoFilter(usuarios, jwt), SecurityContextHolderFilter.class);
        return http.build();
    }
}
