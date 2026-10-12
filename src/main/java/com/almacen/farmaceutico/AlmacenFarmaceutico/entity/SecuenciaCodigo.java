package com.almacen.farmaceutico.AlmacenFarmaceutico.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "secuencias_codigo")
public class SecuenciaCodigo {
    @Id @Column(length = 50)
    private String nombre;
    @Column(nullable = false)
    private long valor;

    protected SecuenciaCodigo() {}
    public SecuenciaCodigo(String nombre, long valor) { this.nombre = nombre; this.valor = valor; }
    public long siguiente() { valor = Math.incrementExact(valor); return valor; }
}
