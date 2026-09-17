import java.time.LocalDate;

/**
 * Representa la operación de préstamo de un libro a un cliente,
 * incluyendo la fecha en que se realizó y, si aplica, la de devolución.
 */
public class Prestamo {

    private Libro libro;
    private Cliente cliente;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion; // null mientras el préstamo sigue activo

    public Prestamo(Libro libro, Cliente cliente, LocalDate fechaPrestamo) {
        this.libro = libro;
        this.cliente = cliente;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = null;
    }

    public Libro getLibro() { return libro; }
    public Cliente getCliente() { return cliente; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }

    public boolean estaActivo() {
        return fechaDevolucion == null;
    }

    public void registrarDevolucion(LocalDate fecha) {
        this.fechaDevolucion = fecha;
    }

    public String mostrarInformacion() {
        String estado = estaActivo()
                ? "ACTIVO"
                : "FINALIZADO (Devuelto: " + fechaDevolucion + ")";
        return String.format(
            "Libro: %s | Cliente: %s | Fecha préstamo: %s | Estado: %s",
            libro.getTitulo(), cliente.getNombre(), fechaPrestamo, estado
        );
    }
}