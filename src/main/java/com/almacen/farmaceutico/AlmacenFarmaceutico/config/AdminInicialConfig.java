package com.almacen.farmaceutico.AlmacenFarmaceutico.config;

import com.almacen.farmaceutico.AlmacenFarmaceutico.dto.UsuarioRequest;
import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.Rol;
import com.almacen.farmaceutico.AlmacenFarmaceutico.repository.UsuarioRepository;
import com.almacen.farmaceutico.AlmacenFarmaceutico.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;

@Configuration
public class AdminInicialConfig {
    @Bean
    CommandLineRunner administradorInicial(UsuarioService service, UsuarioRepository usuarios,
            @Value("${app.admin.usuario}") String usuario, @Value("${app.admin.contrasena}") String contrasena,
            @Value("${app.admin.nombre}") String nombre) {
        return args -> {
            if (!usuario.isBlank() && !contrasena.isBlank() && !usuarios.existsByUsuario(UsuarioService.normalizar(usuario))) {
                if (contrasena.length() < 4) throw new IllegalArgumentException("La contraseña inicial debe tener al menos 4 caracteres.");
                service.crear(new UsuarioRequest(nombre, usuario, contrasena, Rol.ADMINISTRADOR));
            }
        };
    }
}
