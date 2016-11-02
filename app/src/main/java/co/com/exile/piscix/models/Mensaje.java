package co.com.exile.piscix.models;

public class Mensaje {
    private String fecha;
    private String mensaje;
    private boolean tu;
    private String user;

    public Mensaje(String fecha, String mensaje, boolean tu, String user) {
        this.fecha = fecha;
        this.mensaje = mensaje;
        this.tu = tu;
        this.user = user;
    }

    public String getFecha() {
        return fecha;
    }

    public String getMensaje() {
        return mensaje;
    }

    public boolean isTu() {
        return tu;
    }

    public String getUser() {
        return user;
    }
}
