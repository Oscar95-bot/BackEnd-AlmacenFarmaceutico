package com.almacen.farmaceutico.AlmacenFarmaceutico.repository;

import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CatalogoRepository extends JpaRepository<Catalogo, Long> {
    List<Catalogo> findByActivoTrueOrderByNombreAsc();
    Optional<Catalogo> findByTipoAndClave(TipoCatalogo tipo, String clave);
}
