import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class Persistencia {

    private static final String CARPETA = "datos";
    private static final String ARCHIVO_LIBROS = CARPETA + "/libros.txt";
    private static final String ARCHIVO_CLIENTES = CARPETA + "/clientes.txt";
    private static final String ARCHIVO_PRESTAMOS = CARPETA + "/prestamos.txt";
    private static final String ARCHIVO_RESERVAS = CARPETA + "/reservas.txt";
    private static final String SEPARADOR = ";";
    private static final String SIN_FECHA = "-";

    // =====================================================
    //                        LIBROS
    // =====================================================

    public static void guardarLibros(List<Libro> libros) {
        List<String> lineas = new ArrayList<>();

        for (Libro l : libros) {
            String linea = String.join(SEPARADOR,
                    limpiar(l.getId()),
                    limpiar(l.getTitulo()),
                    limpiar(l.getAutor()),
                    limpiar(l.getEditorial()),
                    String.valueOf(l.getAnioPublicacion()),
                    l.getCategoria().name(),
                    l.getEstado().name());
            lineas.add(linea);
        }

        escribirLineas(ARCHIVO_LIBROS, lineas);
    }

    public static List<Libro> cargarLibros() {
        List<Libro> libros = new ArrayList<>();

        for (String linea : leerLineas(ARCHIVO_LIBROS)) {
            if (linea.trim().isEmpty()) {
                continue;
            }
            try {
                String[] p = linea.split(SEPARADOR, -1);

                Libro libro = new Libro(
                        p[0],                       // id
                        p[1],                       // titulo
                        p[2],                       // autor
                        p[3],                       // editorial
                        Integer.parseInt(p[4]),     // anio
                        Categoria.valueOf(p[5]));   // categoria

                // El constructor siempre deja el libro DISPONIBLE,
                // asi que restauramos el estado que tenia guardado.
                libro.cambiarEstado(EstadoLibro.valueOf(p[6]));

                libros.add(libro);

            } catch (RuntimeException e) {
                System.out.println("Linea ignorada en libros.txt (formato invalido): " + linea);
            }
        }
        return libros;
    }

    // =====================================================
    //                       CLIENTES
    // =====================================================

    public static void guardarClientes(List<Cliente> clientes) {
        List<String> lineas = new ArrayList<>();

        for (Cliente c : clientes) {
            String linea = String.join(SEPARADOR,
                    limpiar(c.getDocumento()),
                    limpiar(c.getNombre()),
                    limpiar(c.getTelefono()),
                    limpiar(c.getDireccion()),
                    String.valueOf(c.tieneLibroPrestado()),
                    String.valueOf(c.getMultaPendiente()));
            lineas.add(linea);
        }

        escribirLineas(ARCHIVO_CLIENTES, lineas);
    }

    public static List<Cliente> cargarClientes() {
        List<Cliente> clientes = new ArrayList<>();

        for (String linea : leerLineas(ARCHIVO_CLIENTES)) {
            if (linea.trim().isEmpty()) {
                continue;
            }
            try {
                String[] p = linea.split(SEPARADOR, -1);

                Cliente cliente = new Cliente(p[0], p[1], p[2], p[3]);

                if (Boolean.parseBoolean(p[4])) {
                    cliente.marcarConPrestamo();
                }

                // Archivos guardados antes de existir las multas no traen este campo: queda en 0.
                if (p.length > 5 && !p[5].trim().isEmpty()) {
                    cliente.setMultaPendiente(Integer.parseInt(p[5].trim()));
                }

                clientes.add(cliente);

            } catch (RuntimeException e) {
                System.out.println("Linea ignorada en clientes.txt (formato invalido): " + linea);
            }
        }
        return clientes;
    }

    // =====================================================
    //                      PRESTAMOS
    // =====================================================

    public static void guardarPrestamos(List<Prestamo> prestamos) {
        List<String> lineas = new ArrayList<>();

        for (Prestamo p : prestamos) {
            String devolucion = (p.getFechaDevolucion() == null)
                    ? SIN_FECHA : p.getFechaDevolucion().toString();

            String linea = String.join(SEPARADOR,
                    limpiar(p.getLibro().getId()),
                    limpiar(p.getCliente().getDocumento()),
                    p.getFechaPrestamo().toString(),
                    p.getFechaLimite().toString(),
                    devolucion);
            lineas.add(linea);
        }

        escribirLineas(ARCHIVO_PRESTAMOS, lineas);
    }

    /**
     * Reconstruye los prestamos enlazandolos con los libros y clientes ya cargados.
     */
    public static List<Prestamo> cargarPrestamos(InventarioLibros inventario, List<Cliente> clientes) {
        List<Prestamo> prestamos = new ArrayList<>();

        for (String linea : leerLineas(ARCHIVO_PRESTAMOS)) {
            if (linea.trim().isEmpty()) {
                continue;
            }
            try {
                String[] p = linea.split(SEPARADOR, -1);

                Libro libro = inventario.buscarPorId(p[0]);
                Cliente cliente = buscarCliente(clientes, p[1]);

                if (libro == null || cliente == null) {
                    System.out.println("Prestamo ignorado en prestamos.txt (libro o cliente no existe): " + linea);
                    continue;
                }

                LocalDate fechaPrestamo = LocalDate.parse(p[2]);
                LocalDate fechaLimite = LocalDate.parse(p[3]);
                LocalDate fechaDevolucion = p[4].equals(SIN_FECHA) ? null : LocalDate.parse(p[4]);

                prestamos.add(new Prestamo(libro, cliente, fechaPrestamo, fechaLimite, fechaDevolucion));

            } catch (RuntimeException e) {
                System.out.println("Linea ignorada en prestamos.txt (formato invalido): " + linea);
            }
        }
        return prestamos;
    }

    // =====================================================
    //                       RESERVAS
    // =====================================================

    public static void guardarReservas(List<Libro> libros) {
        List<String> lineas = new ArrayList<>();

        for (Libro libro : libros) {
            if (!libro.tieneReservas()) {
                continue;
            }
            StringBuilder documentos = new StringBuilder();
            for (Cliente c : libro.getReservas()) {
                if (documentos.length() > 0) {
                    documentos.append(",");
                }
                documentos.append(limpiar(c.getDocumento()).replace(",", ""));
            }
            lineas.add(limpiar(libro.getId()) + SEPARADOR + documentos);
        }

        escribirLineas(ARCHIVO_RESERVAS, lineas);
    }

    /**
     * Restaura las listas de espera de cada libro, respetando el orden guardado.
     */
    public static void cargarReservas(InventarioLibros inventario, List<Cliente> clientes) {
        for (String linea : leerLineas(ARCHIVO_RESERVAS)) {
            if (linea.trim().isEmpty()) {
                continue;
            }
            try {
                String[] p = linea.split(SEPARADOR, -1);

                Libro libro = inventario.buscarPorId(p[0]);
                if (libro == null) {
                    System.out.println("Reserva ignorada en reservas.txt (libro no existe): " + linea);
                    continue;
                }

                for (String documento : p[1].split(",")) {
                    if (documento.trim().isEmpty()) {
                        continue;
                    }
                    Cliente cliente = buscarCliente(clientes, documento.trim());
                    if (cliente != null) {
                        libro.agregarReserva(cliente);
                    }
                }

            } catch (RuntimeException e) {
                System.out.println("Linea ignorada en reservas.txt (formato invalido): " + linea);
            }
        }
    }

    // =====================================================
    //            METODOS AUXILIARES (leer / escribir)
    // =====================================================

    private static Cliente buscarCliente(List<Cliente> clientes, String documento) {
        for (Cliente c : clientes) {
            if (c.getDocumento().equalsIgnoreCase(documento)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Escribe las lineas en el archivo, creando la carpeta "datos" si no existe.
     * Reemplaza el contenido anterior del archivo.
     */
    private static void escribirLineas(String ruta, List<String> lineas) {
        try {
            Path path = Paths.get(ruta);
            Files.createDirectories(path.getParent());
            Files.write(path, lineas, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Error al guardar en " + ruta + ": " + e.getMessage());
        }
    }

    /**
     * Lee todas las lineas de un archivo. Si no existe, devuelve lista vacia.
     */
    private static List<String> leerLineas(String ruta) {
        Path path = Paths.get(ruta);

        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try {
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Error al leer " + ruta + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Evita que un texto escrito por el usuario dane el formato del archivo:
     * cambia los ";" por "," y quita saltos de linea.
     */
    private static String limpiar(String texto) {
        return texto.replace(SEPARADOR, ",").replace("\r", " ").replace("\n", " ");
    }
}
