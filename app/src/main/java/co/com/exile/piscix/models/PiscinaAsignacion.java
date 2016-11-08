package co.com.exile.piscix.models;

/**
 * Created by pico on 07/11/2016.
 */

public class PiscinaAsignacion {
    private int id;
    private String nombre;
    private String tipo;
    private double ancho;
    private double largo;
    private double profundidad;
    private boolean estado;
    private String cliente;
    private boolean asignacion;

    public PiscinaAsignacion(int id, String nombre, String tipo, double ancho, double largo, double profundidad, boolean estado, String cliente, boolean asignacion) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.ancho = ancho;
        this.largo = largo;
        this.profundidad = profundidad;
        this.estado = estado;
        this.cliente = cliente;
        this.asignacion = asignacion;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public double getAncho() {
        return ancho;
    }

    public double getLargo() {
        return largo;
    }

    public double getProfundidad() {
        return profundidad;
    }

    public boolean isEstado() {
        return estado;
    }

    public String getCliente() {
        return cliente;
    }

    public boolean isAsignacion() {
        return asignacion;
    }
}
