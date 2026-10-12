package com.almacen.farmaceutico.AlmacenFarmaceutico.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "proveedores")
public class Proveedor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String nombre;
    @Column(nullable = false, unique = true, length = 11)
    private String ruc;
    @Column(nullable = false, length = 200)
    private String direccion;
    @Column(nullable = false)
    private boolean activo = true;

    protected Proveedor() {}
    public Proveedor(String nombre, String ruc, String direccion) {
        this.nombre = nombre;
        this.ruc = ruc;
        this.direccion = direccion;
    }
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getRuc() { return ruc; }
    public String getDireccion() { return direccion; }
    public boolean isActivo() { return activo; }
}
