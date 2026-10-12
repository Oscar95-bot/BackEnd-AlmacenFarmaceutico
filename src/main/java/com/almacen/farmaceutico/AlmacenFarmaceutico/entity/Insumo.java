package com.almacen.farmaceutico.AlmacenFarmaceutico.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "insumos")
public class Insumo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String codigo;
    @Column(nullable = false, length = 150)
    private String nombre;
    @Column(nullable = false, length = 80)
    private String categoria;
    @Column(nullable = false, length = 50)
    private String unidadMedida;
    @Column(length = 100)
    private String presentacion;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Catalogo categoriaCatalogo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidad_medida_id", nullable = false)
    private Catalogo unidadMedidaCatalogo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "presentacion_id", nullable = false)
    private Catalogo presentacionCatalogo;
    @Column(length = 100)
    private String concentracion;
    @Column(nullable = false)
    private int stockActual;
    @Column(nullable = false)
    private int stockMinimo;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;
    @Column(length = 100)
    private String ubicacion;
    @Column(length = 150)
    private String proveedor;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedorEntidad;
    @Column(nullable = false)
    private boolean activo = true;

    protected Insumo() {}

    public Insumo(String codigo, String nombre, Catalogo categoria, Catalogo unidadMedida,
                  Catalogo presentacion, String concentracion, int stockMinimo,
                   BigDecimal precioUnitario, String ubicacion, Proveedor proveedor) {
        this.codigo = codigo;
        actualizar(nombre, categoria, unidadMedida, presentacion, concentracion,
                stockMinimo, precioUnitario, ubicacion, proveedor);
    }

    public void actualizar(String nombre, Catalogo categoria, Catalogo unidadMedida,
                           Catalogo presentacion, String concentracion, int stockMinimo,
                            BigDecimal precioUnitario, String ubicacion, Proveedor proveedor) {
        this.nombre = nombre;
        this.categoriaCatalogo = categoria;
        this.unidadMedidaCatalogo = unidadMedida;
        this.presentacionCatalogo = presentacion;
        // Se conservan las columnas anteriores para migrar sin eliminar información.
        this.categoria = categoria.getNombre();
        this.unidadMedida = unidadMedida.getNombre();
        this.presentacion = presentacion.getNombre();
        this.concentracion = concentracion;
        this.stockMinimo = stockMinimo;
        this.precioUnitario = precioUnitario;
        this.ubicacion = ubicacion;
        this.proveedorEntidad = proveedor;
        this.proveedor = proveedor == null ? null : proveedor.getNombre();
    }

    public void sumarStock(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva.");
        stockActual = Math.addExact(stockActual, cantidad);
    }

    public void restarStock(int cantidad) {
        if (cantidad <= 0 || cantidad > stockActual) {
            throw new IllegalArgumentException("La cantidad supera el stock disponible o no es válida.");
        }
        stockActual -= cantidad;
    }

    public void inactivar() { activo = false; }
    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getCategoria() { return categoriaCatalogo.getNombre(); }
    public String getUnidadMedida() { return unidadMedidaCatalogo.getNombre(); }
    public String getPresentacion() { return presentacionCatalogo.getNombre(); }
    public Long getCategoriaId() { return categoriaCatalogo.getId(); }
    public Long getUnidadMedidaId() { return unidadMedidaCatalogo.getId(); }
    public Long getPresentacionId() { return presentacionCatalogo.getId(); }
    public String getConcentracion() { return concentracion; }
    public int getStockActual() { return stockActual; }
    public int getStockMinimo() { return stockMinimo; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public String getUbicacion() { return ubicacion; }
    public String getProveedor() { return proveedorEntidad == null ? proveedor : proveedorEntidad.getNombre(); }
    public Long getProveedorId() { return proveedorEntidad == null ? null : proveedorEntidad.getId(); }
    public String getProveedorRuc() { return proveedorEntidad == null ? null : proveedorEntidad.getRuc(); }
    public String getProveedorDireccion() { return proveedorEntidad == null ? null : proveedorEntidad.getDireccion(); }
    public boolean isActivo() { return activo; }
    public String getEstado() {
        return stockActual == 0 ? "Agotado" : stockActual <= stockMinimo ? "Por Agotar" : "Disponible";
    }
}
