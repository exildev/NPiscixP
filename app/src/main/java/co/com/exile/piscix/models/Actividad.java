package co.com.exile.piscix.models;


public class Actividad {
    private String title;
    private String color;
    private String date;

    public Actividad(String title, String color, String date) {
        this.title = title;
        this.color = color;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getColor() {
        return color;
    }

    public String getDate() {
        return date;
    }
}
