package com.almacen.farmaceutico.AlmacenFarmaceutico.controller;

import com.almacen.farmaceutico.AlmacenFarmaceutico.dto.*;
import com.almacen.farmaceutico.AlmacenFarmaceutico.service.InsumoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/insumos")
public class InsumoController {
    private final InsumoService insumos;
    public InsumoController(InsumoService insumos) { this.insumos = insumos; }

    @GetMapping
    public List<InsumoResponse> listar(@RequestParam(required = false) String nombre,
            @RequestParam(required = false) String categoria, @RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return insumos.listar(nombre, categoria, incluirInactivos, false);
    }

    @GetMapping("/{id}")
    public InsumoResponse obtener(@PathVariable Long id) {
        return insumos.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InsumoResponse crear(@Valid @RequestBody InsumoRequest request, Authentication auth) {
        return insumos.crear(request, auth.getName());
    }

    @PutMapping("/{id}")
    public InsumoResponse actualizar(@PathVariable Long id, @Valid @RequestBody InsumoRequest request) {
        return insumos.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inactivar(@PathVariable Long id) {
        insumos.inactivar(id);
    }

    @GetMapping("/alertas/stock")
    public List<InsumoResponse> stockBajo() {
        return insumos.listar(null, null, false, true);
    }

    @GetMapping("/{id}/lotes")
    public List<LoteResponse> lotes(@PathVariable Long id) {
        return insumos.listarLotes(id);
    }

    @GetMapping("/alertas/vencimientos")
    public List<LoteResponse> vencimientos(@RequestParam(defaultValue = "30") int dias) {
        return insumos.alertasVencimiento(dias);
    }
}
