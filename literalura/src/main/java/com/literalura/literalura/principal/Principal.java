package com.literalura.literalura.principal;
import com.literalura.literalura.modelos.*;
import com.literalura.literalura.repository.LibrosRepository;
import com.literalura.literalura.service.ComsumoAPI;
import com.literalura.literalura.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ComsumoAPI consumoApi = new ComsumoAPI();
    private final String URL_BASE = "https://gutendex.com/books/";
    private ConvierteDatos conversor = new ConvierteDatos();
    private LibrosRepository repositorio;
    private List<DatosLibros> datosLibros = new ArrayList<>();
    private List<Libros> libros;

    public Principal(LibrosRepository repository) {
        this.repositorio = repository;
    }


    public void muestraElMenu() {
        int opcion = -1;

        while (opcion != 0) {
            System.out.println("""
                    \n===== LITERALURA =====
                    1 - Buscar Libro
                    2 - Listar libros guardados
                    3 - Listar Autores registrados
                    4 - Listar Autores vivos en un año
                    5-  Listar libros por idioma
                    0 - Salir
                    """);
            String input = teclado.nextLine();
            try {
                opcion = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                opcion = -1;
            }

            switch (opcion) {
                case 1 -> buscarYGuardarLibro();
                case 2 -> librosRegistrados();
                case 3 -> listarAutores();
                case 4 -> {
                    System.out.println("Ingrese el año:");
                    int anio = teclado.nextInt();
                    teclado.nextLine(); // limpiar buffer
                    listarAutoresVivosEn(anio);
                }
                case 5 -> listarLibrosPorIdioma();
                case 0 -> System.out.println("Saliendo...");
                default -> System.out.println("Opción inválida");
            }

        }
    }


    private ResultadosLibros buscarLibro() {
        System.out.println("Escribe el nombre del libro que deseas buscar");
        var tituloLibro = teclado.nextLine();

        var json = consumoApi.obtenerDatos(
                URL_BASE + "?search=" + tituloLibro.replace(" ", "+")
        );

        return conversor.obtenerDatos(json, ResultadosLibros.class);

    }

    private void mostrarResultados(ResultadosLibros datosBusqueda) {

        System.out.println("\n📚 RESULTADOS DE LA BÚSQUEDA");
        System.out.println("==========================");

        if (datosBusqueda.resultados() == null ||
                datosBusqueda.resultados().isEmpty()) {

            System.out.println("No se encontraron libros.");
            return;
        }

        if (datosBusqueda.resultados() != null && !datosBusqueda.resultados().isEmpty()) {

            // Obtenemos solo el primer libro (índice 0)
            DatosLibros libro = datosBusqueda.resultados().get(0);
            // Impresión de la ficha del libro
            System.out.println("\n----- LIBRO ENCONTRADO -----");
            System.out.println("Título: " + libro.titulo().toUpperCase()); // Título en mayúsculas para resaltar

            // Autores
            if (libro.autor() != null && !libro.autor().isEmpty()) {
                String autores = libro.autor()
                        .stream()
                        .map(DatosAutor::nombre)
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("Desconocido");

                System.out.println("   Autor(es): " + autores);
            }
            if (libro.numeroDeDescargas() != null) {
                System.out.println("   Descargas: " + libro.numeroDeDescargas());
            } else {
                System.out.println("   Descargas: No disponible");
            }

            // Idiomas
            if (libro.idiomas() != null && !libro.idiomas().isEmpty()) {
                System.out.println("   Idiomas: " +
                        String.join(", ", libro.idiomas()));
            }

        }
    }

    private void buscarYGuardarLibro() {

        // 1️⃣ Buscar en la API
        ResultadosLibros datos = buscarLibro();

        // 2️⃣ Mostrar en consola
        mostrarResultados(datos);

        if (datos.resultados() == null || datos.resultados().isEmpty()) {
            System.out.println("No se encontraron libros para guardar.");
            return;
        }

        // 3️⃣ Tomamos el primer libro
        DatosLibros dto = datos.resultados().get(0);

        // 4️⃣ Crear entidad Libro
        Libros libro = new Libros(dto);

        // 5️⃣ Verificar si ya existe en la base de datos
        Optional<Libros> existente = repositorio.findByTituloContainsIgnoreCase(libro.getTitulo().trim());
        if (existente.isPresent()) {
            System.out.println("⚠️ El libro ya existe en la base de datos.");
            return; // Salimos del método sin guardar
        }

        // 6️⃣ Crear y vincular autores
        if (dto.autor() != null && !dto.autor().isEmpty()) {
            List<Autor> autores = dto.autor()
                    .stream()
                    .map(Autor::new) // usa Autor(DatosAutor)
                    .toList();
            libro.setAutores(autores);
        }

        // 7️⃣ Guardar libro + autores
        repositorio.save(libro);

        System.out.println("✅ Libro y autores guardados correctamente.");
    }

    private void librosRegistrados() {
        libros = repositorio.findAll();

        libros.stream().forEach(System.out::println);
    }

    private void listarAutores() {
        // 1️⃣ Obtenemos todos los libros
        List<Libros> libros = repositorio.findAll();

        // 2️⃣ Extraemos todos los autores y los unimos en un Set para evitar duplicados
        Set<String> autores = libros.stream()
                .flatMap(libro -> libro.getAutores().stream()) // todos los objetos Autor
                .map(Autor::getNombre) // solo nombres
                .collect(Collectors.toCollection(TreeSet::new)); // TreeSet ordena y elimina duplicados

        // 3️⃣ Imprimimos
        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados.");
        } else {
            System.out.println("📚 AUTORES REGISTRADOS:");
            autores.forEach(System.out::println);
        }


    }

        private void listarAutoresVivosEn(int anio) {
            List<Libros> libros = repositorio.findAll();

            Set<String> autoresVivos = libros.stream()
                    .flatMap(libro -> libro.getAutores().stream())
                    .filter(autor -> (autor.getFechaDeNacimiento() == null || autor.getFechaDeNacimiento() <= anio)
                            && (autor.getFechaDeFallecimiento() == null || autor.getFechaDeFallecimiento() > anio))
                    .map(Autor::getNombre)
                    .collect(Collectors.toCollection(TreeSet::new));

            if (autoresVivos.isEmpty()) {
                System.out.println("No hay autores vivos en el año " + anio);
            } else {
                System.out.println("Autores vivos en " + anio + ":");
                autoresVivos.forEach(System.out::println);
            }
        }

    private void listarLibrosPorIdioma() {
        System.out.println("Ingrese el idioma a buscar:");
        String idioma = teclado.nextLine();

        List<Libros> libros = repositorio.findByIdiomasIgnoreCase(idioma);

        if (libros.isEmpty()) {
            System.out.println("No se encontraron libros en ese idioma.");
            return;
        }

        System.out.println("Libros encontrados en idioma " + idioma + ":");
        for (Libros libro : libros) {
            System.out.println("- " + libro.getTitulo());
        }
    }






















}
