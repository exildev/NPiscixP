package co.com.exile.piscix.models;

/**
 * Created by pico on 26/10/2016.
 */

public class Planilla {
    private double ancho;
    private Integer espera;
    private double largo;
    private String nombreCF;
    private String nombreCL;
    private String nombreP;
    private int piscina;
    private int piscinero_id;
    private Integer planilla;
    private double profundidad;
    private Boolean salida;
    private String tipo;
    private Integer orden;
    private Integer id;
    private Double latitud;
    private Double longitud;

    public Planilla(double ancho, Integer espera, double largo, String nombreCF, String nombreCL, String nombreP, int piscina, int piscinero_id, Integer planilla, double profundidad, Boolean salida, String tipo, Integer orden, Integer id, Double latitud, Double longitud) {
        this.ancho = ancho;
        this.espera = espera;
        this.largo = largo;
        this.nombreCF = nombreCF;
        this.nombreCL = nombreCL;
        this.nombreP = nombreP;
        this.piscina = piscina;
        this.piscinero_id = piscinero_id;
        this.planilla = planilla;
        this.profundidad = profundidad;
        this.salida = salida;
        this.tipo = tipo;
        this.orden = orden;
        this.id = id;
        this.latitud = latitud;
        this.longitud = longitud;
    }

    public double getAncho() {
        return ancho;
    }

    public Integer getEspera() {
        return espera;
    }

    public double getLargo() {
        return largo;
    }

    public String getNombreCF() {
        return nombreCF;
    }

    public String getNombreCL() {
        return nombreCL;
    }

    public String getNombreP() {
        return nombreP;
    }

    public int getPiscina() {
        return piscina;
    }

    public int getPiscinero_id() {
        return piscinero_id;
    }

    public Integer getPlanilla() {
        return planilla;
    }

    public double getProfundidad() {
        return profundidad;
    }

    public Boolean getSalida() {
        return salida;
    }

    public String getTipo() {
        return tipo;
    }

    public Integer getOrden() {
        return orden;
    }

    public Integer getId() {
        return id;
    }

    public Double getLatitud() {
        return latitud;
    }

    public Double getLongitud() {
        return longitud;
    }
}
