package com.almacen.farmaceutico.AlmacenFarmaceutico.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 120)
    private String nombreCompleto;
    @Column(nullable = false, unique = true, length = 50)
    private String usuario;
    @Column(nullable = false, length = 100)
    private String contrasena;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Rol rol;
    @Column(nullable = false)
    private boolean activo = true;

    protected Usuario() {}
    public Usuario(String nombreCompleto, String usuario, String contrasena, Rol rol) {
        this.nombreCompleto = nombreCompleto;
        this.usuario = usuario;
        this.contrasena = contrasena;
        this.rol = rol;
    }
    public void actualizar(String nombreCompleto, Rol rol, boolean activo) {
        this.nombreCompleto = nombreCompleto;
        this.rol = rol;
        this.activo = activo;
    }
    public void cambiarContrasena(String hash) { contrasena = hash; }
    public Long getId() { return id; }
    public String getNombreCompleto() { return nombreCompleto; }
    public String getUsuario() { return usuario; }
    public String getContrasena() { return contrasena; }
    public Rol getRol() { return rol; }
    public boolean isActivo() { return activo; }
}
