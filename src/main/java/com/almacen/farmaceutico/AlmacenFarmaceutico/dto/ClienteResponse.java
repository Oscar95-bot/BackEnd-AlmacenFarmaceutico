package com.almacen.farmaceutico.AlmacenFarmaceutico.dto;

import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.*;

public record ClienteResponse(Long id, String clave, String nombre, String ruc, String direccion, TipoCliente tipo) {
    public static ClienteResponse from(Cliente c) {
        return new ClienteResponse(c.getId(), c.getClave(), c.getNombre(), c.getRuc(), c.getDireccion(), c.getTipo());
    }
}
