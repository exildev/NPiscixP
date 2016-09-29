package co.com.exile.piscix;



class Field {

    int id;
    private String name;

    Field(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String toString() { return name; }
}
