package com.almacen.farmaceutico.AlmacenFarmaceutico.service;

import com.almacen.farmaceutico.AlmacenFarmaceutico.dto.ClienteResponse;
import com.almacen.farmaceutico.AlmacenFarmaceutico.entity.Cliente;
import com.almacen.farmaceutico.AlmacenFarmaceutico.exception.ApiException;
import com.almacen.farmaceutico.AlmacenFarmaceutico.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ClienteService {
    private final ClienteRepository clientes;
    public ClienteService(ClienteRepository clientes) { this.clientes = clientes; }
    public List<ClienteResponse> listar() {
        return clientes.findByActivoTrueOrderByNombreAsc().stream().map(ClienteResponse::from).toList();
    }
    public Cliente obtener(Long id) {
        if (id == null) throw ApiException.invalido("Seleccione el cliente o destino.");
        return clientes.findById(id).filter(Cliente::isActivo)
                .orElseThrow(() -> ApiException.invalido("El cliente o destino no existe o está inactivo."));
    }
}
