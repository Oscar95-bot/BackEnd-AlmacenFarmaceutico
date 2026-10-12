package com.almacen.farmaceutico.AlmacenFarmaceutico.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String clave;
    @Column(nullable = false, length = 150)
    private String nombre;
    @Column(unique = true, length = 11)
    private String ruc;
    @Column(nullable = false, length = 200)
    private String direccion;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoCliente tipo;
    @Column(nullable = false)
    private boolean activo = true;

    protected Cliente() {}
    public Cliente(String clave, String nombre, String ruc, String direccion, TipoCliente tipo) {
        this.clave = clave;
        this.nombre = nombre;
        this.ruc = ruc;
        this.direccion = direccion;
        this.tipo = tipo;
    }
    public Long getId() { return id; }
    public String getClave() { return clave; }
    public String getNombre() { return nombre; }
    public String getRuc() { return ruc; }
    public String getDireccion() { return direccion; }
    public TipoCliente getTipo() { return tipo; }
    public boolean isActivo() { return activo; }
}
