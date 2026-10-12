package com.almacen.farmaceutico.AlmacenFarmaceutico.repository;

import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsuario(String usuario);
    boolean existsByUsuario(String usuario);
}
