import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


 
public class GestionPrestamos {

    private InventarioLibros inventarioLibros;
    private List<Cliente> clientes;
    private List<Prestamo> prestamos;

    public GestionPrestamos(InventarioLibros inventarioLibros, List<Cliente> clientes) {
        this.inventarioLibros = inventarioLibros;
        this.clientes = clientes;
        this.prestamos = new ArrayList<>();
    }

    /**
     * Intenta registrar el préstamo de un libro a un cliente.
     * Devuelve un mensaje indicando el resultado (éxito o el motivo del rechazo).
     */
    public String prestarLibro(String idLibro, String documentoCliente) {
        Libro libro = inventarioLibros.buscarPorId(idLibro);
        if (libro == null) {
            return "No existe un libro con el ID: " + idLibro;
        }

        Cliente cliente = buscarClientePorDocumento(documentoCliente);
        if (cliente == null) {
            return "No existe un cliente con el documento: " + documentoCliente;
        }

        if (libro.getEstado() != EstadoLibro.DISPONIBLE) {
            return "El libro '" + libro.getTitulo() + "' no está disponible (estado actual: " + libro.getEstado() + ").";
        }

        if (cliente.tieneLibroPrestado()) {
            return "El cliente " + cliente.getNombre() + " ya tiene un libro en préstamo.";
        }

        libro.cambiarEstado(EstadoLibro.PRESTADO);
        cliente.marcarConPrestamo();

        Prestamo prestamo = new Prestamo(libro, cliente, LocalDate.now());
        prestamos.add(prestamo);

        return "Préstamo registrado: '" + libro.getTitulo() + "' a " + cliente.getNombre()
                + " el " + prestamo.getFechaPrestamo() + ".";
    }

    /**
     * Intenta registrar la devolución de un libro.
     * Devuelve un mensaje indicando el resultado (éxito o el motivo del rechazo).
     */
    public String devolverLibro(String idLibro) {
        Libro libro = inventarioLibros.buscarPorId(idLibro);
        if (libro == null) {
            return "No existe un libro con el ID: " + idLibro;
        }

        if (libro.getEstado() != EstadoLibro.PRESTADO) {
            return "El libro '" + libro.getTitulo() + "' no figura como prestado.";
        }

        Prestamo prestamoActivo = buscarPrestamoActivoPorLibro(idLibro);
        if (prestamoActivo == null) {
            return "No se encontró un préstamo activo registrado para este libro.";
        }

        libro.cambiarEstado(EstadoLibro.DISPONIBLE);
        prestamoActivo.getCliente().marcarSinPrestamo();
        prestamoActivo.registrarDevolucion(LocalDate.now());

        return "Devolución registrada: '" + libro.getTitulo() + "' devuelto por "
                + prestamoActivo.getCliente().getNombre() + ".";
    }

    /**
     * Muestra por consola los préstamos que siguen activos.
     */
    public void listarPrestamosActivos() {
        boolean hayActivos = false;
        System.out.println("----- Préstamos Activos -----");
        for (Prestamo prestamo : prestamos) {
            if (prestamo.estaActivo()) {
                System.out.println(prestamo.mostrarInformacion());
                hayActivos = true;
            }
        }
        if (!hayActivos) {
            System.out.println("No hay préstamos activos actualmente.");
        }
    }

    private Cliente buscarClientePorDocumento(String documento) {
        for (Cliente cliente : clientes) {
            if (cliente.getDocumento().equalsIgnoreCase(documento)) {
                return cliente;
            }
        }
        return null;
    }

    private Prestamo buscarPrestamoActivoPorLibro(String idLibro) {
        for (Prestamo prestamo : prestamos) {
            if (prestamo.getLibro().getId().equalsIgnoreCase(idLibro) && prestamo.estaActivo()) {
                return prestamo;
            }
        }
        return null;
    }
}