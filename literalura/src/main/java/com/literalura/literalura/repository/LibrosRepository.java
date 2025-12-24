package com.literalura.literalura.repository;

import com.literalura.literalura.modelos.Libros;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LibrosRepository extends JpaRepository <Libros,Long>{
    // Esto sirve para no guardar el mismo libro dos veces
    Optional<Libros> findByTituloContainsIgnoreCase(String titulo);

    List<Libros> findByIdiomasIgnoreCase(String idioma);




}
