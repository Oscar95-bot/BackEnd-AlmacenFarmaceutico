package com.almacen.farmaceutico.AlmacenFarmaceutico.repository;

import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.Movimiento;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.Repository;

// No se heredan delete/update para el historial.
public interface MovimientoRepository extends Repository<Movimiento, Long>, JpaSpecificationExecutor<Movimiento> {
    Movimiento save(Movimiento movimiento);
}
