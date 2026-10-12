package com.almacen.farmaceutico.AlmacenFarmaceutico.controller;

import com.almacen.farmaceutico.AlmacenFarmaceutico.dto.ProveedorResponse;
import com.almacen.farmaceutico.AlmacenFarmaceutico.service.ProveedorService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {
    private final ProveedorService proveedores;
    public ProveedorController(ProveedorService proveedores) { this.proveedores = proveedores; }
    @GetMapping
    public List<ProveedorResponse> listar() { return proveedores.listar(); }
}
