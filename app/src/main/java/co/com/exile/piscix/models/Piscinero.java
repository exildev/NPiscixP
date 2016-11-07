package co.com.exile.piscix.models;


public class Piscinero {
    private String direccion;
    private String email;
    private String fecha_nacimiento;
    private String first_name;
    private int id;
    private String imagen;
    private String last_name;
    private String telefono;

    public Piscinero(String direccion, String email, String fecha_nacimiento, String first_name, int id, String imagen, String last_name, String telefono) {
        this.direccion = direccion;
        this.email = email;
        this.fecha_nacimiento = fecha_nacimiento;
        this.first_name = first_name;
        this.id = id;
        this.imagen = imagen;
        this.last_name = last_name;
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getEmail() {
        return email;
    }

    public String getFecha_nacimiento() {
        return fecha_nacimiento;
    }

    public String getFirst_name() {
        return first_name;
    }

    public int getId() {
        return id;
    }

    public String getImagen() {
        return imagen;
    }

    public String getLast_name() {
        return last_name;
    }

    public String getTelefono() {
        return telefono;
    }
}
