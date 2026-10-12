package com.almacen.farmaceutico.AlmacenFarmaceutico.controller;

import com.almacen.farmaceutico.AlmacenFarmaceutico.dto.CatalogosResponse;
import com.almacen.farmaceutico.AlmacenFarmaceutico.service.CatalogoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {
    private final CatalogoService catalogos;
    public CatalogoController(CatalogoService catalogos) { this.catalogos = catalogos; }
    @GetMapping
    public CatalogosResponse listar() { return catalogos.listar(); }
}
