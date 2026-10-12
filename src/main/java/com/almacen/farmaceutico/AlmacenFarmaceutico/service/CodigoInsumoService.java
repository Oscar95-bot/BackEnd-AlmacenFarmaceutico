package com.almacen.farmaceutico.AlmacenFarmaceutico.service;

import com.almacen.farmaceutico.AlmacenFarmaceutico.exception.ApiException;
import com.almacen.farmaceutico.AlmacenFarmaceutico.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;
import java.util.Locale;

@Service
public class CodigoInsumoService {
    private final SecuenciaCodigoRepository secuencias;
    private final InsumoRepository insumos;
    public CodigoInsumoService(SecuenciaCodigoRepository secuencias, InsumoRepository insumos) {
        this.secuencias = secuencias;
        this.insumos = insumos;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public String siguiente() {
        var secuencia = secuencias.bloquear("INSUMO")
                .orElseThrow(() -> ApiException.conflicto("Falta la secuencia de códigos. Ejecute catalogos-mysql.sql."));
        String codigo;
        do {
            codigo = String.format(Locale.ROOT, "FAR-%06d", secuencia.siguiente());
        } while (insumos.existsByCodigo(codigo));
        return codigo;
    }
}
