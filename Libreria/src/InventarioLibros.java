import java.util.ArrayList;
import java.util.List;
 
/**
 * Centraliza el almacenamiento en memoria de los libros y las operaciones
 * de registro y consulta del inventario.
 */
public class InventarioLibros {
 
    private List<Libro> libros;
 
    public InventarioLibros() {
        this.libros = new ArrayList<>();
    }
 
    /**
     * Registra un nuevo libro, evitando IDs duplicados.
     */
    public boolean registrarLibro(Libro libro) {
        if (buscarPorId(libro.getId()) != null) {
            System.out.println("Ya existe un libro registrado con el ID: " + libro.getId());
            return false;
        }
        libros.add(libro);
        return true;
    }
 
    /**
     * Busca un libro por su identificador único.
     */
    public Libro buscarPorId(String id) {
        for (Libro libro : libros) {
            if (libro.getId().equalsIgnoreCase(id)) {
                return libro;
            }
        }
        return null;
    }
 
    /**
     * Muestra por consola el inventario completo.
     */
    public void listarLibros() {
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados en el inventario.");
            return;
        }
        System.out.println("----- Inventario de Libros -----");
        for (Libro libro : libros) {
            System.out.println(libro.mostrarInformacion());
        }
    }
 
    public List<Libro> getLibros() {
        return libros;
    }
}
