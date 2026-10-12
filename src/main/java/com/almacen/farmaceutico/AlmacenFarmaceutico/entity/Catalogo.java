package com.almacen.farmaceutico.AlmacenFarmaceutico.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "catalogos", uniqueConstraints = @UniqueConstraint(columnNames = {"tipo", "clave"}))
public class Catalogo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoCatalogo tipo;
    @Column(nullable = false, length = 80)
    private String clave;
    @Column(nullable = false, length = 200)
    private String nombre;
    @Column(nullable = false)
    private boolean activo = true;
    @Column(nullable = false)
    private boolean permiteVencidos;

    protected Catalogo() {}
    public Catalogo(TipoCatalogo tipo, String clave, String nombre, boolean permiteVencidos) {
        this.tipo = tipo;
        this.clave = clave;
        this.nombre = nombre;
        this.permiteVencidos = permiteVencidos;
    }
    public Long getId() { return id; }
    public TipoCatalogo getTipo() { return tipo; }
    public String getClave() { return clave; }
    public String getNombre() { return nombre; }
    public boolean isActivo() { return activo; }
    public boolean isPermiteVencidos() { return permiteVencidos; }
}
