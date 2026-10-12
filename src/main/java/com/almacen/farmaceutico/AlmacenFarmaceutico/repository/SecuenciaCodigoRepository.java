package com.almacen.farmaceutico.AlmacenFarmaceutico.repository;

import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.SecuenciaCodigo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface SecuenciaCodigoRepository extends JpaRepository<SecuenciaCodigo, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SecuenciaCodigo s where s.nombre = :nombre")
    Optional<SecuenciaCodigo> bloquear(@Param("nombre") String nombre);
}
