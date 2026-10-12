package com.almacen.farmaceutico.AlmacenFarmaceutico.repository;

import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    List<Proveedor> findByActivoTrueOrderByNombreAsc();
}
