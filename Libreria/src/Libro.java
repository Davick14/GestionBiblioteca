import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Libro {

    private String id;
    private String titulo;
    private String autor;
    private String editorial;
    private int anioPublicacion;
    private Categoria categoria;
    private EstadoLibro estado;

    // Lista de espera: el primero en reservar es el primero en ser atendido (orden de solicitud).
    private Queue<Cliente> reservas;

    public Libro(String id, String titulo, String autor, String editorial,
                 int anioPublicacion, Categoria categoria) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.editorial = editorial;
        this.anioPublicacion = anioPublicacion;
        this.categoria = categoria;
        this.estado = EstadoLibro.DISPONIBLE; // todo libro nuevo entra como disponible
        this.reservas = new LinkedList<>();
    }

    // ---- Getters (encapsulamiento) ----
    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public String getEditorial() { return editorial; }
    public int getAnioPublicacion() { return anioPublicacion; }
    public Categoria getCategoria() { return categoria; }
    public EstadoLibro getEstado() { return estado; }

    /**
     * Cambia el estado del libro (disponible, prestado, retirado).
     * La validación de si el cambio es coherente (por ejemplo, no prestar
     * un libro ya prestado) se hace en la capa que gestiona los préstamos.
     */
    public void cambiarEstado(EstadoLibro nuevoEstado) {
        this.estado = nuevoEstado;
    }

    // ---- Reservas ----

    /** Indica si hay al menos un cliente esperando este libro. */
    public boolean tieneReservas() {
        return !reservas.isEmpty();
    }

    /** Devuelve (sin sacarlo de la lista) al siguiente cliente en espera, o null si no hay. */
    public Cliente siguienteReserva() {
        return reservas.peek();
    }

    /** Indica si el cliente ya está en la lista de espera de este libro. */
    public boolean tieneReservaDe(Cliente cliente) {
        for (Cliente c : reservas) {
            if (c.getDocumento().equalsIgnoreCase(cliente.getDocumento())) {
                return true;
            }
        }
        return false;
    }

    /** Agrega al cliente al final de la lista de espera. Devuelve false si ya estaba. */
    public boolean agregarReserva(Cliente cliente) {
        if (tieneReservaDe(cliente)) {
            return false;
        }
        reservas.add(cliente);
        return true;
    }

    /** Saca al cliente de la lista de espera (por ejemplo, cuando ya retiró el libro). */
    public void quitarReserva(Cliente cliente) {
        Iterator<Cliente> it = reservas.iterator();
        while (it.hasNext()) {
            if (it.next().getDocumento().equalsIgnoreCase(cliente.getDocumento())) {
                it.remove();
                return;
            }
        }
    }

    /** Copia de la lista de espera, en orden de solicitud. */
    public List<Cliente> getReservas() {
        return new ArrayList<>(reservas);
    }

    /**
     * Devuelve la información del libro en formato legible.
     */
    public String mostrarInformacion() {
        return String.format(
            "ID: %s | Título: %s | Autor: %s | Editorial: %s | Año: %d | Categoría: %s | Estado: %s",
            id, titulo, autor, editorial, anioPublicacion, categoria, estado
        );
    }

    @Override
    public String toString() {
        return mostrarInformacion();
    }
}
