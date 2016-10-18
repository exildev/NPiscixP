package co.com.exile.piscix.models;



public class Reporte {

    int id;
    private String nombre;
    private String descripcion;
    private String tipo_de_reporte;
    private String piscina;
    private boolean estado;
    private String fecha;
    private String cliente;
    private String cierre;
    private String numero;

    public static final String[] CIERRES = {"Automático", "Parcial", "A satisfacción"};

    public Reporte(int id, String nombre, String descripcion, String tipo_de_reporte, String piscina, boolean estado, String fecha, String cliente, String cierre, String numero) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.tipo_de_reporte = tipo_de_reporte;
        this.piscina = piscina;
        this.estado = estado;
        this.fecha = fecha;
        this.cliente = cliente;
        this.cierre = cierre;
        this.numero = numero;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getTipo_de_reporte() {
        return tipo_de_reporte;
    }

    public String getPiscina() {
        return piscina;
    }

    public boolean isEstado() {
        return estado;
    }

    public String getFecha() {
        return fecha;
    }

    public String getCliente() {
        return cliente;
    }

    public String getCierre() {
        return cierre;
    }

    public String getNumero() {
        return numero;
    }
}
