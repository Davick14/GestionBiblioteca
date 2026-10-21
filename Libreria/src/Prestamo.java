import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Representa un préstamo: qué libro, a qué cliente, cuándo se prestó,
 * hasta cuándo debía devolverse y (si ya ocurrió) cuándo se devolvió.
 */
public class Prestamo {

    // Reglas de la biblioteca (se pueden ajustar aquí en un solo lugar)
    public static final int PLAZO_DIAS = 7;        // días permitidos para devolver
    public static final int MULTA_POR_DIA = 1000;  // pesos por cada día de retraso

    private Libro libro;
    private Cliente cliente;
    private LocalDate fechaPrestamo;
    private LocalDate fechaLimite;
    private LocalDate fechaDevolucion; // null mientras el préstamo siga activo

    /** Crea un préstamo nuevo: la fecha límite se calcula sola. */
    public Prestamo(Libro libro, Cliente cliente, LocalDate fechaPrestamo) {
        this.libro = libro;
        this.cliente = cliente;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaLimite = fechaPrestamo.plusDays(PLAZO_DIAS);
        this.fechaDevolucion = null;
    }

    /** Reconstruye un préstamo guardado en archivo (con todas sus fechas). */
    public Prestamo(Libro libro, Cliente cliente, LocalDate fechaPrestamo,
                    LocalDate fechaLimite, LocalDate fechaDevolucion) {
        this.libro = libro;
        this.cliente = cliente;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaLimite = fechaLimite;
        this.fechaDevolucion = fechaDevolucion;
    }

    public Libro getLibro() { return libro; }
    public Cliente getCliente() { return cliente; }
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public LocalDate getFechaLimite() { return fechaLimite; }
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }

    public boolean estaActivo() {
        return fechaDevolucion == null;
    }

    public void registrarDevolucion(LocalDate fecha) {
        this.fechaDevolucion = fecha;
    }

    /** Días de retraso al devolver. Si no se ha devuelto o se devolvió a tiempo, es 0. */
    public int calcularDiasRetraso() {
        if (fechaDevolucion == null) {
            return 0;
        }
        long dias = ChronoUnit.DAYS.between(fechaLimite, fechaDevolucion);
        return dias > 0 ? (int) dias : 0;
    }

    /** Multa de este préstamo: días de retraso por el valor diario. */
    public int calcularMulta() {
        return calcularDiasRetraso() * MULTA_POR_DIA;
    }

    @Override
    public String toString() {
        String devuelto = (fechaDevolucion == null) ? "pendiente" : fechaDevolucion.toString();
        return String.format(
            "Libro: %s (%s) | Cliente: %s (%s) | Préstamo: %s | Límite: %s | Devolución: %s",
            libro.getTitulo(), libro.getId(),
            cliente.getNombre(), cliente.getDocumento(),
            fechaPrestamo, fechaLimite, devuelto);
    }
}
