package co.com.exile.piscix.models;


public class Informativo {
    private int id;
    private String nombre;
    private String usuario;
    private String descripcion;
    private String fecha;

    public Informativo(int id, String nombre, String usuario, String descripcion, String fecha) {
        this.id = id;
        this.nombre = nombre;
        this.usuario = usuario;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getFecha() {
        return fecha;
    }
}
