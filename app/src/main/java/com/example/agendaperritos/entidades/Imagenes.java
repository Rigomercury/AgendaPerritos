package com.example.agendaperritos.entidades;

public class Imagenes {
    private int id;
    private String registro;
    private byte[] imagen;

    public void setId(int id) {
        this.id = id;
    }
    public void setRegistro(String registro) {
        this.registro = registro;
    }
    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public int getId() {
        return id;
    }
    public String getRegistro() {
        return registro;
    }
    public byte[] getImagen() {
        return imagen;
    }
}
