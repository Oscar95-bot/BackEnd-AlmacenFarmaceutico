package com.almacen.farmaceutico.AlmacenFarmaceutico.service;

import com.almacen.farmaceutico.AlmacenFarmaceutico.dto.*;
import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.*;
import com.almacen.farmaceutico.AlmacenFarmaceutico.exception.ApiException;
import com.almacen.farmaceutico.AlmacenFarmaceutico.repository.CatalogoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CatalogoService {
    private final CatalogoRepository catalogos;
    public CatalogoService(CatalogoRepository catalogos) { this.catalogos = catalogos; }

    public CatalogosResponse listar() {
        List<Catalogo> activos = catalogos.findByActivoTrueOrderByNombreAsc();
        return new CatalogosResponse(filtrar(activos, TipoCatalogo.CATEGORIA), filtrar(activos, TipoCatalogo.UNIDAD_MEDIDA),
                filtrar(activos, TipoCatalogo.PRESENTACION), filtrar(activos, TipoCatalogo.MOTIVO_ENTRADA),
                filtrar(activos, TipoCatalogo.MOTIVO_SALIDA));
    }

    public Catalogo obtener(Long id, TipoCatalogo tipo) {
        if (id == null) throw ApiException.invalido("Seleccione una opción de " + tipo + ".");
        Catalogo catalogo = catalogos.findById(id).orElseThrow(() -> ApiException.invalido("La opción seleccionada no existe."));
        if (catalogo.getTipo() != tipo || !catalogo.isActivo()) {
            throw ApiException.invalido("La opción no pertenece al catálogo requerido o está inactiva.");
        }
        return catalogo;
    }

    public Catalogo motivoInicial() {
        return catalogos.findByTipoAndClave(TipoCatalogo.MOTIVO_ENTRADA, "CARGA_INICIAL").filter(Catalogo::isActivo)
                .orElseThrow(() -> ApiException.conflicto("Falta el motivo de carga inicial. Ejecute catalogos-mysql.sql."));
    }

    private List<CatalogoResponse> filtrar(List<Catalogo> lista, TipoCatalogo tipo) {
        return lista.stream().filter(c -> c.getTipo() == tipo).map(CatalogoResponse::from).toList();
    }
}
