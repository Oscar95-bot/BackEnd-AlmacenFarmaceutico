package com.almacen.farmaceutico.AlmacenFarmaceutico.service;

import com.almacen.farmaceutico.AlmacenFarmaceutico.dto.*;
import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.TipoMovimiento;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class ReporteService {
    private final InsumoService insumos;
    private final MovimientoService movimientos;
    public ReporteService(InsumoService insumos, MovimientoService movimientos) {
        this.insumos = insumos;
        this.movimientos = movimientos;
    }
    public byte[] inventario(String texto, String categoria) {
        StringBuilder csv = new StringBuilder("\uFEFF").append(fila("Código", "Nombre", "Categoría", "Stock", "Stock mínimo", "Precio", "Estado"));
        for (InsumoResponse i : insumos.listar(texto, categoria, false, false)) {
            csv.append(fila(i.codigo(), i.nombre(), i.categoria(), i.stockActual(), i.stockMinimo(), i.precioUnitario(), i.estado()));
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
    public byte[] historial(TipoMovimiento tipo, LocalDate desde, LocalDate hasta, Long insumoId, String texto) {
        StringBuilder csv = new StringBuilder("\uFEFF").append(fila("ID", "Fecha y hora", "Tipo", "Código", "Insumo", "Lote", "Cantidad", "Origen/Destino", "Guía", "Responsable", "Motivo"));
        for (MovimientoResponse m : movimientos.listar(tipo, desde, hasta, insumoId, texto)) {
            csv.append(fila(m.id(), m.fechaHora(), m.tipo(), m.codigoInsumo(), m.nombreInsumo(), m.numeroLote(),
                    m.cantidad(), m.origenDestino(), m.numeroGuia(), m.responsable(), m.motivo()));
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
    private String fila(Object... valores) {
        return Arrays.stream(valores).map(this::celda).collect(Collectors.joining(",")) + "\r\n";
    }
    private String celda(Object valor) {
        String texto = valor == null ? "" : valor.toString();
        // Evita que los textos ingresados se interpreten como fórmulas al abrir el CSV.
        if (!texto.isEmpty() && ("=+-@".indexOf(texto.stripLeading().isEmpty() ? ' ' : texto.stripLeading().charAt(0)) >= 0
                || texto.charAt(0) == '\t' || texto.charAt(0) == '\r' || texto.charAt(0) == '\n')) texto = "'" + texto;
        return "\"" + texto.replace("\"", "\"\"") + "\"";
    }
}
