package co.com.exile.piscix.models;

public class Mensaje {
    private String fecha;
    private String mensaje;
    private boolean tu;
    private String user;
    private boolean status;

    public Mensaje(String fecha, String mensaje, boolean tu, String user, boolean status) {
        this.fecha = fecha;
        this.mensaje = mensaje;
        this.tu = tu;
        this.user = user;
        this.status = status;
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

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
