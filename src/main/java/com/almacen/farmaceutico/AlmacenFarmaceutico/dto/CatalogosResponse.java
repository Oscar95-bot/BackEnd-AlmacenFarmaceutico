package com.almacen.farmaceutico.AlmacenFarmaceutico.dto;

import java.util.List;

public record CatalogosResponse(List<CatalogoResponse> categorias, List<CatalogoResponse> unidadesMedida,
        List<CatalogoResponse> presentaciones, List<CatalogoResponse> motivosEntrada,
        List<CatalogoResponse> motivosSalida) {}
