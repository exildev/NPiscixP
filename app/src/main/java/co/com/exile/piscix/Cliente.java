package co.com.exile.piscix;



public class Cliente {

    public static final boolean ES_PROPIETARIO = true;

    private String first_name;
    private int id;
    private String imagen;
    private String last_name;
    private boolean tipo;

    public Cliente(String first_name, int id, String imagen, String last_name, boolean tipo) {
        this.first_name = first_name;
        this.id = id;
        this.imagen = imagen;
        this.last_name = last_name;
        this.tipo = tipo;
    }

    public Cliente() {
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public boolean isTipo() {
        return tipo;
    }

    public void setTipo(boolean tipo) {
        this.tipo = tipo;
    }
}
