package com.almacen.farmaceutico.AlmacenFarmaceutico.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "lotes", uniqueConstraints = @UniqueConstraint(columnNames = {"insumo_id", "numero"}))
public class Lote {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "insumo_id", nullable = false)
    private Insumo insumo;
    @Column(nullable = false, length = 80)
    private String numero;
    @Column(nullable = false)
    private LocalDate fechaVencimiento;
    @Column(nullable = false)
    private int stockActual;

    protected Lote() {}
    public Lote(Insumo insumo, String numero, LocalDate fechaVencimiento) {
        this.insumo = insumo;
        this.numero = numero;
        this.fechaVencimiento = fechaVencimiento;
    }
    public void sumarStock(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva.");
        stockActual = Math.addExact(stockActual, cantidad);
    }
    public void restarStock(int cantidad) {
        if (cantidad <= 0 || cantidad > stockActual) throw new IllegalArgumentException("Stock del lote insuficiente.");
        stockActual -= cantidad;
    }
    public Long getId() { return id; }
    public Insumo getInsumo() { return insumo; }
    public String getNumero() { return numero; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public int getStockActual() { return stockActual; }
}
