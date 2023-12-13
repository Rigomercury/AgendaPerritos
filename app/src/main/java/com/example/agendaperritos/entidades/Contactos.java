package com.example.agendaperritos.entidades;

public class Contactos {

    private int id;
    private String registro;
    private String nombre;
    private String mascota;
    private String direccion;
    private String telefono;
    private String fecha;
    private String hora;
    private String costo;


    public void setId(int id) {
        this.id = id;
    }

    public void setRegistro(String registro) {
        this.registro = registro;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public void setCosto(String costo) {
        this.costo = costo;
    }

    public int getId() {
        return id;
    }

    public String getRegistro() {
        return registro;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    public String getCosto() {
        return costo;
    }

    public String getMascota() {
        return mascota;
    }

    public void setMascota(String mascota) {
        this.mascota = mascota;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
