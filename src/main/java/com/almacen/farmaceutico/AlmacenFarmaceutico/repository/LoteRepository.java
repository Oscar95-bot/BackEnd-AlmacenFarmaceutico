package com.almacen.farmaceutico.AlmacenFarmaceutico.repository;

import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.Lote;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface LoteRepository extends JpaRepository<Lote, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Lote l where l.insumo.id = :insumoId and l.numero = :numero")
    Optional<Lote> buscarParaEntrada(@Param("insumoId") Long insumoId, @Param("numero") String numero);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Lote l where l.id = :id")
    Optional<Lote> buscarParaSalida(@Param("id") Long id);
    List<Lote> findByInsumoIdOrderByFechaVencimientoAscIdAsc(Long insumoId);
}
