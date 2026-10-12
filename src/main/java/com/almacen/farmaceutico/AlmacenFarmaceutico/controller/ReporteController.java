package com.almacen.farmaceutico.AlmacenFarmaceutico.controller;

import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.TipoMovimiento;
import com.almacen.farmaceutico.AlmacenFarmaceutico.service.ReporteService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {
    private final ReporteService reportes;
    public ReporteController(ReporteService reportes) { this.reportes = reportes; }

    @GetMapping("/inventario.csv")
    public ResponseEntity<byte[]> inventario(@RequestParam(required = false) String nombre,
                                            @RequestParam(required = false) String categoria) {
        return descargar("inventario.csv", reportes.inventario(nombre, categoria));
    }

    @GetMapping("/movimientos.csv")
    public ResponseEntity<byte[]> movimientos(@RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta,
            @RequestParam(required = false) Long insumoId, @RequestParam(required = false) String texto) {
        return descargar("movimientos.csv", reportes.historial(tipo, desde, hasta, insumoId, texto));
    }

    private ResponseEntity<byte[]> descargar(String nombre, byte[] contenido) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
                .body(contenido);
    }
}
