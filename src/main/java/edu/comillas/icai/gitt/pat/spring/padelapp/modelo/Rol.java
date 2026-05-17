package edu.comillas.icai.gitt.pat.spring.padelapp.modelo;

import edu.comillas.icai.gitt.pat.spring.padelapp.clases.NombreRol;
import jakarta.persistence.*;

@Entity
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRol;

    // Como NombreRol es un Enum (ADMIN, USER...), le decimos a JPA que lo guarde como texto
    @Enumerated(EnumType.STRING)
    private NombreRol nombreRol;

    private String descripcion;

    // 1. Constructor vacío OBLIGATORIO para JPA
    public Rol() {
    }

    // 2. Getters y Setters
    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public NombreRol getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(NombreRol nombreRol) {
        this.nombreRol = nombreRol;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}