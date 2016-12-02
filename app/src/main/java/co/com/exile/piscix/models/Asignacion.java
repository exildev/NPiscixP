package co.com.exile.piscix.models;

/**
 * Created by pico on 07/11/2016.
 */

public class Asignacion {
    private int id;
    private int piscina_id;
    private String nombre;
    private double ancho;
    private double largo;
    private double profundidad;
    private String tipo;
    private String cliente;
    private int orden;
    private boolean haveGPS;

    public Asignacion(int id, int piscina_id, String nombre, double ancho, double largo, double profundidad, String tipo, String cliente, int orden, boolean haveGPS) {
        this.id = id;
        this.piscina_id = piscina_id;
        this.nombre = nombre;
        this.ancho = ancho;
        this.largo = largo;
        this.profundidad = profundidad;
        this.tipo = tipo;
        this.cliente = cliente;
        this.orden = orden;
        this.haveGPS = haveGPS;
    }

    public int getId() {
        return id;
    }

    public int getPiscina_id() {
        return piscina_id;
    }

    public String getNombre() {
        return nombre;
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

    public String getTipo() {
        return tipo;
    }

    public String getCliente() {
        return cliente;
    }

    public int getOrden() {
        return orden;
    }

    public boolean isHaveGPS() {
        return haveGPS;
    }

    @Override
    public String toString() {
        return "Asignacion{" +
                "id=" + id +
                ", piscina_id=" + piscina_id +
                ", nombre='" + nombre + '\'' +
                ", ancho=" + ancho +
                ", largo=" + largo +
                ", profundidad=" + profundidad +
                ", tipo='" + tipo + '\'' +
                ", cliente='" + cliente + '\'' +
                ", orden=" + orden +
                ", haveGPS=" + haveGPS +
                '}';
    }
}
