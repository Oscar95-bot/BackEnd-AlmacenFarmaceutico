package com.almacen.farmaceutico.AlmacenFarmaceutico.controller;

import com.almacen.farmaceutico.AlmacenFarmaceutico.dto.*;
import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.TipoMovimiento;
import com.almacen.farmaceutico.AlmacenFarmaceutico.exception.ApiException;
import com.almacen.farmaceutico.AlmacenFarmaceutico.service.MovimientoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {
    private final MovimientoService movimientos;
    public MovimientoController(MovimientoService movimientos) { this.movimientos = movimientos; }

    @GetMapping
    public List<MovimientoResponse> listar(@RequestParam(required = false) TipoMovimiento tipo,
            @RequestParam(required = false) LocalDate desde, @RequestParam(required = false) LocalDate hasta,
            @RequestParam(required = false) Long insumoId, @RequestParam(required = false) String texto) {
        return movimientos.listar(tipo, desde, hasta, insumoId, texto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponse registrar(@Valid @RequestBody MovimientoRequest request, Authentication auth) {
        return movimientos.registrar(request, auth.getName());
    }

    @PostMapping("/entrada")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponse entrada(@Valid @RequestBody MovimientoRequest request, Authentication auth) {
        if (request.tipo() != TipoMovimiento.ENTRADA) throw ApiException.invalido("El tipo debe ser ENTRADA.");
        return movimientos.registrar(request, auth.getName());
    }

    @PostMapping("/salida")
    @ResponseStatus(HttpStatus.CREATED)
    public MovimientoResponse salida(@Valid @RequestBody MovimientoRequest request, Authentication auth) {
        if (request.tipo() != TipoMovimiento.SALIDA) throw ApiException.invalido("El tipo debe ser SALIDA.");
        return movimientos.registrar(request, auth.getName());
    }
}
