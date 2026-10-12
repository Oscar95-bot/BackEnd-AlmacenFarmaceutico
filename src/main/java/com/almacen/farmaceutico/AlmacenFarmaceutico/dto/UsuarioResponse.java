package com.almacen.farmaceutico.AlmacenFarmaceutico.dto;

import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.Rol;
import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.Usuario;

public record UsuarioResponse(Long id, String nombreCompleto, String usuario, Rol rol, boolean activo) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombreCompleto(), u.getUsuario(), u.getRol(), u.isActivo());
    }
}
