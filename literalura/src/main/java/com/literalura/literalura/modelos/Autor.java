package com.literalura.literalura.modelos;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
    @Table(name = "autores")
    public class Autor {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
       private String nombre;
       private Integer  fechaDeNacimiento;
       private Integer fechaDeFallecimiento;

       @ManyToOne
       private Libros libro; // Este es el "libro" que usa mappedBy en la otra clase
       public Autor() {
           // 🔴 OBLIGATORIO para JPA
       }


       public Long getId() {
           return id;
       }

       public void setId(Long id) {
           this.id = id;
       }

       public String getNombre() {
           return nombre;
       }

       public void setNombre(String nombre) {
           this.nombre = nombre;
       }

       public Integer getFechaDeNacimiento() {
           return fechaDeNacimiento;
       }

       public void setFechaDeNacimiento(Integer fechaDeNacimiento) {
           this.fechaDeNacimiento = fechaDeNacimiento;
       }

       public Integer getFechaDeFallecimiento() {
           return fechaDeFallecimiento;
       }

       public void setFechaDeFallecimiento(Integer fechaDeFallecimiento) {
           this.fechaDeFallecimiento = fechaDeFallecimiento;
       }

       public Libros getLibro() {
           return libro;
       }

       public void setLibro(Libros libro) {
           this.libro = libro;
       }

       public Autor(DatosAutor datosAutor) {
           this.nombre = datosAutor.nombre();
           this.fechaDeNacimiento = datosAutor.fechaDeNacimiento();
           this.fechaDeFallecimiento = datosAutor.fechaDeFallecimiento();
       }

   }









