import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Se encarga unicamente de guardar y cargar la informacion en archivos de texto.
 * Cada objeto se guarda en UNA linea, con sus campos separados por punto y coma.
 *
 * Ejemplo de una linea en libros.txt:
 */
public class Persistencia {

    private static final String CARPETA = "datos";
    private static final String ARCHIVO_LIBROS = CARPETA + "/libros.txt";
    private static final String ARCHIVO_CLIENTES = CARPETA + "/clientes.txt";
    private static final String SEPARADOR = ";";

    //                        LIBROS

    /**
     * Convierte cada libro en una linea de texto y escribe todo en libros.txt.
     */
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

    /**
     * Lee libros.txt y reconstruye la lista de libros.
     * Si el archivo no existe (primera vez), devuelve una lista vacia.
     */
    public static List<Libro> cargarLibros() {
        List<Libro> libros = new ArrayList<>();

        for (String linea : leerLineas(ARCHIVO_LIBROS)) {
            if (linea.trim().isEmpty()) {
                continue; // ignora lineas en blanco
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

    //                       CLIENTES

    public static void guardarClientes(List<Cliente> clientes) {
        List<String> lineas = new ArrayList<>();

        for (Cliente c : clientes) {
            String linea = String.join(SEPARADOR,
                    limpiar(c.getDocumento()),
                    limpiar(c.getNombre()),
                    limpiar(c.getTelefono()),
                    limpiar(c.getDireccion()),
                    String.valueOf(c.tieneLibroPrestado()));
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

                // El constructor deja al cliente sin prestamo,
                // asi que si estaba guardado con prestamo, lo restauramos.
                if (Boolean.parseBoolean(p[4])) {
                    cliente.marcarConPrestamo();
                }

                clientes.add(cliente);

            } catch (RuntimeException e) {
                System.out.println("Linea ignorada en clientes.txt (formato invalido): " + linea);
            }
        }
        return clientes;
    }

    //            METODOS AUXILIARES (leer / escribir)

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
