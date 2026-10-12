package com.almacen.farmaceutico.AlmacenFarmaceutico.service;

import com.almacen.farmaceutico.AlmacenFarmaceutico.dto.ProveedorResponse;
import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.Proveedor;
import com.almacen.farmaceutico.AlmacenFarmaceutico.exception.ApiException;
import com.almacen.farmaceutico.AlmacenFarmaceutico.repository.ProveedorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProveedorService {
    private final ProveedorRepository proveedores;
    public ProveedorService(ProveedorRepository proveedores) { this.proveedores = proveedores; }
    public List<ProveedorResponse> listar() {
        return proveedores.findByActivoTrueOrderByNombreAsc().stream().map(ProveedorResponse::from).toList();
    }
    public Proveedor obtener(Long id) {
        if (id == null) throw ApiException.invalido("Seleccione un proveedor.");
        return proveedores.findById(id).filter(Proveedor::isActivo)
                .orElseThrow(() -> ApiException.invalido("El proveedor seleccionado no existe o está inactivo."));
    }
}
