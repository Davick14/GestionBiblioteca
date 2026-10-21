import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Centraliza las operaciones de préstamo, devolución, multas y reservas.
 * Aquí viven todas las validaciones para evitar situaciones incoherentes.
 */
public class GestorPrestamos {

    private InventarioLibros inventario;
    private List<Cliente> clientes;
    private List<Prestamo> prestamos; // historial completo (activos y devueltos)

    public GestorPrestamos(InventarioLibros inventario, List<Cliente> clientes) {
        this.inventario = inventario;
        this.clientes = clientes;
        this.prestamos = new ArrayList<>();
    }

    public List<Prestamo> getPrestamos() {
        return prestamos;
    }

    // =====================================================
    //                     BÚSQUEDAS
    // =====================================================

    public Cliente buscarCliente(String documento) {
        for (Cliente c : clientes) {
            if (c.getDocumento().equalsIgnoreCase(documento)) {
                return c;
            }
        }
        return null;
    }

    /** Busca el préstamo que tiene actualmente ese libro (si está prestado). */
    public Prestamo buscarPrestamoActivoDeLibro(String idLibro) {
        for (Prestamo p : prestamos) {
            if (p.estaActivo() && p.getLibro().getId().equalsIgnoreCase(idLibro)) {
                return p;
            }
        }
        return null;
    }

    // =====================================================
    //                  PRÉSTAMOS Y DEVOLUCIONES
    // =====================================================

    /**
     * Presta un libro a un cliente. Solo lo permite si el libro está disponible
     * y el cliente no tiene otro libro prestado.
     */
    public boolean prestarLibro(String idLibro, String documento, LocalDate fecha) {

        Libro libro = inventario.buscarPorId(idLibro);
        if (libro == null) {
            System.out.println("No existe un libro con el ID: " + idLibro);
            return false;
        }

        Cliente cliente = buscarCliente(documento);
        if (cliente == null) {
            System.out.println("No existe un cliente con el documento: " + documento);
            return false;
        }

        if (libro.getEstado() == EstadoLibro.RETIRADO) {
            System.out.println("El libro fue retirado del inventario y no se puede prestar.");
            return false;
        }

        if (libro.getEstado() == EstadoLibro.PRESTADO) {
            System.out.println("El libro ya está prestado. Puede reservarlo para quedar en lista de espera.");
            return false;
        }

        if (cliente.tieneLibroPrestado()) {
            System.out.println("El cliente ya tiene un libro en préstamo. Debe devolverlo antes de pedir otro.");
            return false;
        }

        // Todo en orden: se registra el préstamo
        Prestamo prestamo = new Prestamo(libro, cliente, fecha);
        prestamos.add(prestamo);

        libro.cambiarEstado(EstadoLibro.PRESTADO);
        cliente.marcarConPrestamo();
        libro.quitarReserva(cliente); // si estaba esperando este libro, ya lo recibió

        System.out.println("Préstamo registrado correctamente.");
        System.out.println("  Libro        : " + libro.getTitulo());
        System.out.println("  Cliente      : " + cliente.getNombre());
        System.out.println("  Fecha        : " + prestamo.getFechaPrestamo());
        System.out.println("  Devolver a más tardar el: " + prestamo.getFechaLimite());
        return true;
    }

    /**
     * Registra la devolución de un libro. Si hay retraso, calcula la multa y la
     * acumula en el cliente. Si el libro tiene reservas, avisa quién sigue.
     */
    public boolean devolverLibro(String idLibro, LocalDate fechaDevolucion) {

        Libro libro = inventario.buscarPorId(idLibro);
        if (libro == null) {
            System.out.println("No existe un libro con el ID: " + idLibro);
            return false;
        }

        Prestamo prestamo = buscarPrestamoActivoDeLibro(idLibro);
        if (libro.getEstado() != EstadoLibro.PRESTADO || prestamo == null) {
            System.out.println("Este libro no figura como prestado, no se puede registrar la devolución.");
            return false;
        }

        if (fechaDevolucion.isBefore(prestamo.getFechaPrestamo())) {
            System.out.println("La fecha de devolución no puede ser anterior a la fecha del préstamo ("
                    + prestamo.getFechaPrestamo() + ").");
            return false;
        }

        Cliente cliente = prestamo.getCliente();

        prestamo.registrarDevolucion(fechaDevolucion);
        libro.cambiarEstado(EstadoLibro.DISPONIBLE);
        cliente.marcarSinPrestamo();

        System.out.println("Devolución registrada correctamente.");

        // Multa por retraso
        int diasRetraso = prestamo.calcularDiasRetraso();
        if (diasRetraso > 0) {
            int multa = prestamo.calcularMulta();
            cliente.agregarMulta(multa);
            System.out.println("  Devolución con " + diasRetraso + " día(s) de retraso.");
            System.out.println("  Multa generada: $" + multa);
            System.out.println("  Total pendiente de " + cliente.getNombre() + ": $" + cliente.getMultaPendiente());
        } else {
            System.out.println("  Devuelto a tiempo, sin multa.");
        }

        // Aviso al siguiente en la lista de espera
        if (libro.tieneReservas()) {
            Cliente siguiente = libro.siguienteReserva();
            System.out.println("  >> El libro tiene reservas. Siguiente cliente en la lista de espera: "
                    + siguiente.getNombre() + " (documento " + siguiente.getDocumento() + ")");
        }
        return true;
    }

    public void listarPrestamosActivos() {
        System.out.println("\n--- Préstamos activos ---");
        boolean hay = false;
        for (Prestamo p : prestamos) {
            if (p.estaActivo()) {
                System.out.println(p);
                hay = true;
            }
        }
        if (!hay) {
            System.out.println("No hay préstamos activos.");
        }
    }

    // =====================================================
    //                         MULTAS
    // =====================================================

    /** Muestra las multas de un cliente: detalle por préstamo y total pendiente. */
    public void consultarMultas(String documento) {
        Cliente cliente = buscarCliente(documento);
        if (cliente == null) {
            System.out.println("No existe un cliente con el documento: " + documento);
            return;
        }

        System.out.println("\n--- Multas de " + cliente.getNombre() + " (" + cliente.getDocumento() + ") ---");

        boolean hayRetrasos = false;
        for (Prestamo p : prestamos) {
            if (p.getCliente().getDocumento().equalsIgnoreCase(documento) && p.calcularMulta() > 0) {
                System.out.println("- " + p.getLibro().getTitulo() + ": "
                        + p.calcularDiasRetraso() + " día(s) de retraso -> $" + p.calcularMulta());
                hayRetrasos = true;
            }
        }
        if (!hayRetrasos) {
            System.out.println("No registra devoluciones con retraso.");
        }
        System.out.println("Total pendiente: $" + cliente.getMultaPendiente());
    }

    // =====================================================
    //                        RESERVAS
    // =====================================================

    /** Pone al cliente en la lista de espera de un libro que está prestado. */
    public boolean reservarLibro(String idLibro, String documento) {

        Libro libro = inventario.buscarPorId(idLibro);
        if (libro == null) {
            System.out.println("No existe un libro con el ID: " + idLibro);
            return false;
        }

        Cliente cliente = buscarCliente(documento);
        if (cliente == null) {
            System.out.println("No existe un cliente con el documento: " + documento);
            return false;
        }

        if (libro.getEstado() == EstadoLibro.DISPONIBLE) {
            System.out.println("El libro está disponible, no necesita reserva. Puede prestarlo directamente.");
            return false;
        }

        if (libro.getEstado() == EstadoLibro.RETIRADO) {
            System.out.println("El libro fue retirado del inventario y no se puede reservar.");
            return false;
        }

        Prestamo actual = buscarPrestamoActivoDeLibro(idLibro);
        if (actual != null && actual.getCliente().getDocumento().equalsIgnoreCase(documento)) {
            System.out.println("Este cliente ya tiene el libro en préstamo, no puede reservarlo.");
            return false;
        }

        if (!libro.agregarReserva(cliente)) {
            System.out.println("Este cliente ya tiene una reserva para este libro.");
            return false;
        }

        System.out.println("Reserva registrada. " + cliente.getNombre() + " quedó en la posición "
                + libro.getReservas().size() + " de la lista de espera.");
        return true;
    }

    /** Muestra la lista de espera de un libro, en orden de solicitud. */
    public void consultarReservas(String idLibro) {
        Libro libro = inventario.buscarPorId(idLibro);
        if (libro == null) {
            System.out.println("No existe un libro con el ID: " + idLibro);
            return;
        }

        System.out.println("\n--- Reservas de \"" + libro.getTitulo() + "\" ---");
        List<Cliente> espera = libro.getReservas();
        if (espera.isEmpty()) {
            System.out.println("Este libro no tiene reservas.");
            return;
        }
        for (int i = 0; i < espera.size(); i++) {
            Cliente c = espera.get(i);
            System.out.println((i + 1) + ". " + c.getNombre() + " (documento " + c.getDocumento() + ")");
        }
    }
}
