package com.almacen.farmaceutico.AlmacenFarmaceutico.config;

import com.almacen.farmaceutico.AlmacenFarmaceutico.repository.UsuarioRepository;
import com.almacen.farmaceutico.AlmacenFarmaceutico.service.JwtService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

/** Valida el JWT y consulta el estado y rol actuales antes de autorizar cada petición. */
final class UsuarioActivoFilter extends OncePerRequestFilter {
    private final UsuarioRepository usuarios;
    private final JwtService jwt;
    private final DefaultBearerTokenResolver bearer = new DefaultBearerTokenResolver();
    UsuarioActivoFilter(UsuarioRepository usuarios, JwtService jwt) {
        this.usuarios = usuarios;
        this.jwt = jwt;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getRequestURI().equals(request.getContextPath() + "/api/auth/login")) {
            chain.doFilter(request, response);
            return;
        }
        try {
            String token = bearer.resolve(request);
            if (token != null) {
                var claims = jwt.validar(token);
                var usuario = usuarios.findByUsuario(claims.getSubject()).filter(u -> u.isActivo()
                        && u.getId().equals(claims.getClaim("usuarioId") instanceof Number id ? id.longValue() : null));
                if (usuario.isEmpty()) {
                    rechazar(response);
                    return;
                }
                var auth = new JwtAuthenticationToken(claims,
                        List.of(new SimpleGrantedAuthority("ROLE_" + usuario.get().getRol().name())));
                var context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(auth);
                SecurityContextHolder.setContext(context);
            }
        } catch (JwtException | OAuth2AuthenticationException ex) {
            rechazar(response);
            return;
        }
        chain.doFilter(request, response);
    }

    private void rechazar(HttpServletResponse response) throws IOException {
        SecurityContextHolder.clearContext();
        response.setStatus(401);
        response.setHeader("WWW-Authenticate", "Bearer");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"mensaje\":\"Token inválido, vencido o usuario inactivo.\"}");
    }
}
