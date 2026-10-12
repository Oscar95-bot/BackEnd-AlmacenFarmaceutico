package com.almacen.farmaceutico.AlmacenFarmaceutico.service;

import com.almacen.farmaceutico.AlmacenFarmaceutico.dto.*;
import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.*;
import com.almacen.farmaceutico.AlmacenFarmaceutico.exception.ApiException;
import com.almacen.farmaceutico.AlmacenFarmaceutico.repository.*;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InsumoService {
    private final InsumoRepository insumos;
    private final LoteRepository lotes;
    private final MovimientoService movimientos;
    private final CatalogoService catalogos;
    private final CodigoInsumoService codigos;
    private final ProveedorService proveedores;

    public InsumoService(InsumoRepository insumos, LoteRepository lotes, MovimientoService movimientos,
                         CatalogoService catalogos, CodigoInsumoService codigos, ProveedorService proveedores) {
        this.insumos = insumos;
        this.lotes = lotes;
        this.movimientos = movimientos;
        this.catalogos = catalogos;
        this.codigos = codigos;
        this.proveedores = proveedores;
    }

    public List<InsumoResponse> listar(String texto, String categoria, boolean incluirInactivos, boolean stockBajo) {
        Specification<Insumo> filtro = (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (!incluirInactivos) condiciones.add(cb.isTrue(root.get("activo")));
            if (stockBajo) condiciones.add(cb.lessThanOrEqualTo(root.get("stockActual"), root.get("stockMinimo")));
            if (categoria != null && !categoria.isBlank()) condiciones.add(cb.equal(cb.lower(root.get("categoriaCatalogo").get("nombre")), categoria.trim().toLowerCase(java.util.Locale.ROOT)));
            if (texto != null && !texto.isBlank()) {
                String patron = Busqueda.contiene(texto);
                condiciones.add(cb.or(cb.like(cb.lower(root.get("codigo")), patron, '\\'),
                        cb.like(cb.lower(root.get("nombre")), patron, '\\')));
            }
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
        return insumos.findAll(filtro, Sort.by("nombre", "id")).stream().map(InsumoResponse::from).toList();
    }

    public InsumoResponse obtener(Long id) { return InsumoResponse.from(buscar(id)); }

    @Transactional
    public InsumoResponse crear(InsumoRequest request, String actor) {
        Catalogo categoria = catalogos.obtener(request.categoriaId(), TipoCatalogo.CATEGORIA);
        Catalogo unidad = catalogos.obtener(request.unidadMedidaId(), TipoCatalogo.UNIDAD_MEDIDA);
        Catalogo presentacion = catalogos.obtener(request.presentacionId(), TipoCatalogo.PRESENTACION);
        Proveedor proveedor = request.proveedorId() == null ? null : proveedores.obtener(request.proveedorId());
        Insumo insumo = insumos.save(new Insumo(codigos.siguiente(), request.nombre().trim(), categoria,
                unidad, presentacion, request.concentracion(), minimo(request),
                request.precioUnitario(), request.ubicacion(), proveedor));
        if (request.stockActual() != null && request.stockActual() > 0) {
            if (proveedor == null) throw ApiException.invalido("Seleccione el proveedor del stock inicial.");
            movimientos.registrar(new MovimientoRequest(insumo.getId(), TipoMovimiento.ENTRADA,
                    request.stockActual(), catalogos.motivoInicial().getId(), null, "CARGA-INICIAL",
                    LocalDate.now(), null, request.numeroLote(), request.fechaVencimiento(), null, proveedor.getId(), null), actor);
        }
        return InsumoResponse.from(insumo);
    }

    @Transactional
    public InsumoResponse actualizar(Long id, InsumoRequest request) {
        Insumo insumo = bloquear(id);
        if (request.stockActual() != null && request.stockActual() != insumo.getStockActual()) {
            throw ApiException.invalido("El stock solo se modifica mediante entradas o salidas.");
        }
        insumo.actualizar(request.nombre().trim(), catalogos.obtener(request.categoriaId(), TipoCatalogo.CATEGORIA),
                catalogos.obtener(request.unidadMedidaId(), TipoCatalogo.UNIDAD_MEDIDA),
                catalogos.obtener(request.presentacionId(), TipoCatalogo.PRESENTACION), request.concentracion(), minimo(request),
                request.precioUnitario(), request.ubicacion(), request.proveedorId() == null ? null : proveedores.obtener(request.proveedorId()));
        return InsumoResponse.from(insumo);
    }

    @Transactional
    public void inactivar(Long id) { bloquear(id).inactivar(); }

    public List<LoteResponse> listarLotes(Long insumoId) {
        buscar(insumoId);
        return lotes.findByInsumoIdOrderByFechaVencimientoAscIdAsc(insumoId).stream().map(LoteResponse::from).toList();
    }

    public List<LoteResponse> alertasVencimiento(int dias) {
        if (dias < 0 || dias > 365) throw ApiException.invalido("Los días deben estar entre 0 y 365.");
        LocalDate limite = LocalDate.now().plusDays(dias);
        return lotes.findAll(Sort.by("fechaVencimiento", "id")).stream()
                .filter(l -> l.getInsumo().isActivo() && l.getStockActual() > 0 && !l.getFechaVencimiento().isAfter(limite))
                .map(LoteResponse::from).toList();
    }

    private Insumo buscar(Long id) { return insumos.findById(id).orElseThrow(() -> ApiException.noEncontrado("Insumo no encontrado.")); }
    private Insumo bloquear(Long id) { return insumos.buscarParaActualizar(id).orElseThrow(() -> ApiException.noEncontrado("Insumo no encontrado.")); }
    private int minimo(InsumoRequest request) { return request.stockMinimo() == null ? 10 : request.stockMinimo(); }
}
