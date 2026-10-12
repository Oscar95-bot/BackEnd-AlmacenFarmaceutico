package com.almacen.farmaceutico.AlmacenFarmaceutico.service;

import com.almacen.farmaceutico.AlmacenFarmaceutico.dto.*;
import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.Usuario;
import com.almacen.farmaceutico.AlmacenFarmaceutico.exception.ApiException;
import com.almacen.farmaceutico.AlmacenFarmaceutico.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    public List<UsuarioResponse> listar() { return usuarios.findAll().stream().map(UsuarioResponse::from).toList(); }

    public Usuario actual(String nombreUsuario) {
        return usuarios.findByUsuario(normalizar(nombreUsuario)).filter(Usuario::isActivo)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "La sesión ya no corresponde a un usuario activo."));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        String nombreUsuario = normalizar(request.usuario());
        if (usuarios.existsByUsuario(nombreUsuario)) throw ApiException.conflicto("El usuario ya existe.");
        return UsuarioResponse.from(usuarios.save(new Usuario(request.nombreCompleto().trim(), nombreUsuario,
                hash(request.contrasena()), request.rol())));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest request, String actor) {
        Usuario usuario = buscar(id);
        if (usuario.getUsuario().equals(normalizar(actor)) && (!request.activo() || request.rol() != usuario.getRol())) {
            throw ApiException.invalido("No puede desactivar su propia cuenta ni cambiar su propio rol.");
        }
        usuario.actualizar(request.nombreCompleto().trim(), request.rol(), request.activo());
        if (request.contrasena() != null) usuario.cambiarContrasena(hash(request.contrasena()));
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public void inactivar(Long id, String actor) {
        Usuario usuario = buscar(id);
        if (usuario.getUsuario().equals(normalizar(actor))) throw ApiException.invalido("No puede desactivar su propia cuenta.");
        usuario.actualizar(usuario.getNombreCompleto(), usuario.getRol(), false);
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado."));
    }

    private String hash(String contrasena) {
        if (contrasena.isBlank() || contrasena.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw ApiException.invalido("La contraseña no puede estar vacía ni superar 72 bytes.");
        }
        return encoder.encode(contrasena);
    }

    public static String normalizar(String usuario) { return usuario.trim().toLowerCase(Locale.ROOT); }
}
