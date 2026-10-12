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
public class MovimientoService {
    private final InsumoRepository insumos;
    private final LoteRepository lotes;
    private final MovimientoRepository movimientos;
    private final UsuarioService usuarios;
    private final CatalogoService catalogos;
    private final ProveedorService proveedores;
    private final ClienteService clientes;

    public MovimientoService(InsumoRepository insumos, LoteRepository lotes,
                              MovimientoRepository movimientos, UsuarioService usuarios, CatalogoService catalogos,
                              ProveedorService proveedores, ClienteService clientes) {
        this.insumos = insumos;
        this.lotes = lotes;
        this.movimientos = movimientos;
        this.usuarios = usuarios;
        this.catalogos = catalogos;
        this.proveedores = proveedores;
        this.clientes = clientes;
    }

    @Transactional
    public MovimientoResponse registrar(MovimientoRequest request, String actor) {
        Usuario usuario = usuarios.actual(actor);
        if (request.cantidad() == null || request.cantidad() <= 0) throw ApiException.invalido("La cantidad debe ser positiva.");
        if (request.tipo() == null) throw ApiException.invalido("Indique el tipo de movimiento.");
        Catalogo motivo = catalogos.obtener(request.motivoId(), request.tipo() == TipoMovimiento.ENTRADA
                ? TipoCatalogo.MOTIVO_ENTRADA : TipoCatalogo.MOTIVO_SALIDA);
        Proveedor proveedor = null;
        Cliente cliente = null;
        String origenDestino;
        if (request.tipo() == TipoMovimiento.ENTRADA) {
            if (request.clienteId() != null) throw ApiException.invalido("Una entrada debe indicar proveedor, no cliente de salida.");
            proveedor = proveedores.obtener(request.proveedorId());
            origenDestino = proveedor.getNombre();
        } else if (motivo.getClave().equals("DEVOLUCION_PROVEEDOR")) {
            if (request.clienteId() != null) throw ApiException.invalido("La devolución requiere seleccionar un proveedor.");
            proveedor = proveedores.obtener(request.proveedorId());
            origenDestino = proveedor.getNombre();
        } else {
            if (request.proveedorId() != null) throw ApiException.invalido("El proveedor solo corresponde a entradas o devoluciones a proveedor.");
            cliente = clientes.obtener(request.clienteId());
            if (!motivo.getClave().equals("TRANSFERENCIA")) {
                TipoCliente tipoDestino = motivo.isPermiteVencidos() ? TipoCliente.INTERNO : TipoCliente.CLIENTE;
                if (cliente.getTipo() != tipoDestino) throw ApiException.invalido("El destino seleccionado no corresponde al motivo de salida.");
            }
            origenDestino = cliente.getNombre();
        }
        // Todos los movimientos de un insumo toman el mismo bloqueo antes de modificar sus lotes.
        Insumo insumo = insumos.buscarParaActualizar(request.insumoId())
                .orElseThrow(() -> ApiException.noEncontrado("Insumo no encontrado."));
        if (!insumo.isActivo()) throw ApiException.invalido("El insumo está inactivo.");
        LocalDate fecha = request.fechaOperacion() == null ? LocalDate.now() : request.fechaOperacion();
        if (fecha.isAfter(LocalDate.now())) throw ApiException.invalido("La fecha de operación no puede ser futura.");

        Lote lote;
        if (request.tipo() == TipoMovimiento.ENTRADA) {
            lote = registrarEntrada(insumo, request);
        } else {
            lote = registrarSalida(insumo, request, motivo);
        }
        Movimiento movimiento = new Movimiento(insumo, lote, usuario, request.tipo(), request.cantidad(),
                fecha, motivo, origenDestino,
                request.numeroGuia(), request.observaciones(), proveedor, cliente);
        return MovimientoResponse.from(movimientos.save(movimiento));
    }

    private Lote registrarEntrada(Insumo insumo, MovimientoRequest request) {
        if (request.numeroLote() == null || request.numeroLote().isBlank() || request.fechaVencimiento() == null) {
            throw ApiException.invalido("La entrada requiere número de lote y fecha de vencimiento.");
        }
        if (request.fechaVencimiento().isBefore(LocalDate.now())) throw ApiException.invalido("No se pueden ingresar lotes vencidos.");
        if (request.numeroGuia() == null || request.numeroGuia().isBlank()) throw ApiException.invalido("Indique el número de guía o factura.");
        String numero = request.numeroLote().trim();
        Lote lote = lotes.buscarParaEntrada(insumo.getId(), numero)
                .orElseGet(() -> new Lote(insumo, numero, request.fechaVencimiento()));
        if (!lote.getFechaVencimiento().equals(request.fechaVencimiento())) {
            throw ApiException.conflicto("El lote ya existe con otra fecha de vencimiento.");
        }
        lote.sumarStock(request.cantidad());
        insumo.sumarStock(request.cantidad());
        return lotes.save(lote);
    }

    private Lote registrarSalida(Insumo insumo, MovimientoRequest request, Catalogo motivo) {
        if (request.loteId() == null) throw ApiException.invalido("Seleccione el lote a despachar.");
        Lote lote = lotes.buscarParaSalida(request.loteId()).orElseThrow(() -> ApiException.noEncontrado("Lote no encontrado."));
        if (!lote.getInsumo().getId().equals(insumo.getId())) throw ApiException.invalido("El lote no pertenece al insumo seleccionado.");
        if (lote.getFechaVencimiento().isBefore(LocalDate.now()) && !motivo.isPermiteVencidos()) {
            throw ApiException.invalido("Un lote vencido solo puede salir por un motivo de baja autorizado, no por venta o despacho.");
        }
        if (motivo.getClave().equals("VENCIMIENTO") && !lote.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw ApiException.invalido("La baja por vencimiento requiere un lote vencido.");
        }
        if (request.cantidad() > insumo.getStockActual() || request.cantidad() > lote.getStockActual()) {
            throw ApiException.invalido("Stock insuficiente para la cantidad solicitada.");
        }
        lote.restarStock(request.cantidad());
        insumo.restarStock(request.cantidad());
        return lote;
    }

    public List<MovimientoResponse> listar(TipoMovimiento tipo, LocalDate desde, LocalDate hasta,
                                          Long insumoId, String texto) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) throw ApiException.invalido("El rango de fechas es inválido.");
        Specification<Movimiento> filtro = (root, query, cb) -> {
            List<Predicate> condiciones = new ArrayList<>();
            if (tipo != null) condiciones.add(cb.equal(root.get("tipo"), tipo));
            if (desde != null) condiciones.add(cb.greaterThanOrEqualTo(root.get("fechaHora"), desde.atStartOfDay()));
            if (hasta != null) condiciones.add(cb.lessThan(root.get("fechaHora"), hasta.plusDays(1).atStartOfDay()));
            if (insumoId != null) condiciones.add(cb.equal(root.get("insumo").get("id"), insumoId));
            if (texto != null && !texto.isBlank()) {
                String patron = Busqueda.contiene(texto);
                condiciones.add(cb.or(cb.like(cb.lower(root.get("codigoInsumo")), patron, '\\'),
                        cb.like(cb.lower(root.get("nombreInsumo")), patron, '\\')));
            }
            return cb.and(condiciones.toArray(Predicate[]::new));
        };
        return movimientos.findAll(filtro, Sort.by(Sort.Direction.DESC, "fechaHora", "id"))
                .stream().map(MovimientoResponse::from).toList();
    }
}
