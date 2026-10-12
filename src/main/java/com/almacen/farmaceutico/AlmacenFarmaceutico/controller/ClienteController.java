package com.almacen.farmaceutico.AlmacenFarmaceutico.controller;

import com.almacen.farmaceutico.AlmacenFarmaceutico.dto.ClienteResponse;
import com.almacen.farmaceutico.AlmacenFarmaceutico.service.ClienteService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService clientes;
    public ClienteController(ClienteService clientes) { this.clientes = clientes; }
    @GetMapping
    public List<ClienteResponse> listar() { return clientes.listar(); }
}
