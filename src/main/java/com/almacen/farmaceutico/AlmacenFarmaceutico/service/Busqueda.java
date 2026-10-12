package com.almacen.farmaceutico.AlmacenFarmaceutico.service;

import java.util.Locale;

final class Busqueda {
    private Busqueda() {}
    static String contiene(String texto) {
        return "%" + texto.trim().toLowerCase(Locale.ROOT).replace("\\", "\\\\")
                .replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
