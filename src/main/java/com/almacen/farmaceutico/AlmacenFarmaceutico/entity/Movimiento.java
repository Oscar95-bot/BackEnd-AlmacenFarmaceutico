package com.almacen.farmaceutico.AlmacenFarmaceutico.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Auditoría de solo inserción: no se exponen operaciones de edición o eliminación. */
@Entity
@Immutable
@Table(name = "movimientos")
public class Movimiento {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Insumo insumo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Lote lote;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private Usuario usuario;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimiento tipo;
    @Column(nullable = false)
    private int cantidad;
    @Column(nullable = false)
    private LocalDateTime fechaHora;
    @Column(nullable = false)
    private LocalDate fechaOperacion;
    @Column(nullable = false, length = 200)
    private String motivo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "motivo_id", nullable = false)
    private Catalogo motivoCatalogo;
    @Column(nullable = false, length = 150)
    private String origenDestino;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;
    @Column(length = 11)
    private String proveedorRuc;
    @Column(length = 200)
    private String proveedorDireccion;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
    @Column(length = 11)
    private String clienteRuc;
    @Column(length = 200)
    private String clienteDireccion;
    @Column(length = 80)
    private String numeroGuia;
    @Column(length = 500)
    private String observaciones;
    // Instantáneas: editar el catálogo o el usuario no altera la auditoría histórica.
    @Column(nullable = false, length = 50)
    private String codigoInsumo;
    @Column(nullable = false, length = 150)
    private String nombreInsumo;
    @Column(nullable = false, length = 120)
    private String responsable;

    protected Movimiento() {}
    public Movimiento(Insumo insumo, Lote lote, Usuario usuario, TipoMovimiento tipo, int cantidad,
                      LocalDate fechaOperacion, Catalogo motivo, String origenDestino,
                        String numeroGuia, String observaciones, Proveedor proveedor, Cliente cliente) {
        this.insumo = insumo;
        this.lote = lote;
        this.usuario = usuario;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.fechaHora = LocalDateTime.now();
        this.fechaOperacion = fechaOperacion;
        this.motivoCatalogo = motivo;
        this.motivo = motivo.getNombre();
        this.origenDestino = origenDestino;
        this.proveedor = proveedor;
        this.proveedorRuc = proveedor == null ? null : proveedor.getRuc();
        this.proveedorDireccion = proveedor == null ? null : proveedor.getDireccion();
        this.cliente = cliente;
        this.clienteRuc = cliente == null ? null : cliente.getRuc();
        this.clienteDireccion = cliente == null ? null : cliente.getDireccion();
        this.numeroGuia = numeroGuia;
        this.observaciones = observaciones;
        this.codigoInsumo = insumo.getCodigo();
        this.nombreInsumo = insumo.getNombre();
        this.responsable = usuario.getNombreCompleto();
    }
    public Long getId() { return id; }
    public Insumo getInsumo() { return insumo; }
    public Lote getLote() { return lote; }
    public TipoMovimiento getTipo() { return tipo; }
    public int getCantidad() { return cantidad; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public LocalDate getFechaOperacion() { return fechaOperacion; }
    public String getMotivo() { return motivo; }
    public Long getMotivoId() { return motivoCatalogo.getId(); }
    public String getOrigenDestino() { return origenDestino; }
    public Long getProveedorId() { return proveedor == null ? null : proveedor.getId(); }
    public String getProveedorRuc() { return proveedorRuc; }
    public String getProveedorDireccion() { return proveedorDireccion; }
    public Long getClienteId() { return cliente == null ? null : cliente.getId(); }
    public String getClienteRuc() { return clienteRuc; }
    public String getClienteDireccion() { return clienteDireccion; }
    public String getNumeroGuia() { return numeroGuia; }
    public String getObservaciones() { return observaciones; }
    public String getCodigoInsumo() { return codigoInsumo; }
    public String getNombreInsumo() { return nombreInsumo; }
    public String getResponsable() { return responsable; }
}
