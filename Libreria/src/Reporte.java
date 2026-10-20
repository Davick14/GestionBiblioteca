import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Genera el reporte general con información consolidada del uso de la biblioteca.
 */
public class Reporte {

    public static void generar(InventarioLibros inventario, List<Cliente> clientes, GestorPrestamos gestor) {

        List<Prestamo> prestamos = gestor.getPrestamos();

        int activos = 0;
        for (Prestamo p : prestamos) {
            if (p.estaActivo()) {
                activos++;
            }
        }

        System.out.println("\n========== REPORTE GENERAL ==========");
        System.out.println("Total de libros registrados      : " + inventario.getLibros().size());
        System.out.println("Total de clientes registrados    : " + clientes.size());
        System.out.println("Préstamos activos                : " + activos);
        System.out.println("Préstamos realizados (histórico) : " + prestamos.size());

        if (prestamos.isEmpty()) {
            System.out.println("Cliente con más préstamos        : (aún no hay préstamos)");
            System.out.println("Libro menos solicitado           : (aún no hay préstamos)");
            System.out.println("=====================================");
            return;
        }

        // ---- Cliente con más préstamos ----
        Map<String, Integer> porCliente = new LinkedHashMap<>();
        for (Prestamo p : prestamos) {
            String doc = p.getCliente().getDocumento();
            Integer actual = porCliente.get(doc);
            porCliente.put(doc, actual == null ? 1 : actual + 1);
        }
        int maxPrestamos = 0;
        for (int cantidad : porCliente.values()) {
            maxPrestamos = Math.max(maxPrestamos, cantidad);
        }
        List<String> topClientes = new ArrayList<>();
        for (Cliente c : clientes) {
            Integer cantidad = porCliente.get(c.getDocumento());
            if (cantidad != null && cantidad == maxPrestamos) {
                topClientes.add(c.getNombre() + " (" + c.getDocumento() + ")");
            }
        }
        System.out.println("Cliente con más préstamos        : " + String.join(", ", topClientes)
                + " - " + maxPrestamos + " préstamo(s)");

        // ---- Libro menos solicitado (se cuentan todos los libros, incluso los nunca prestados) ----
        Map<String, Integer> porLibro = new LinkedHashMap<>();
        for (Libro l : inventario.getLibros()) {
            porLibro.put(l.getId(), 0);
        }
        for (Prestamo p : prestamos) {
            String id = p.getLibro().getId();
            if (porLibro.containsKey(id)) {
                porLibro.put(id, porLibro.get(id) + 1);
            }
        }
        int minPrestamos = Integer.MAX_VALUE;
        for (int cantidad : porLibro.values()) {
            minPrestamos = Math.min(minPrestamos, cantidad);
        }
        List<String> menosSolicitados = new ArrayList<>();
        for (Libro l : inventario.getLibros()) {
            if (porLibro.get(l.getId()) == minPrestamos) {
                menosSolicitados.add(l.getTitulo() + " (" + l.getId() + ")");
            }
        }
        System.out.println("Libro(s) menos solicitado(s)     : " + String.join(", ", menosSolicitados)
                + " - " + minPrestamos + " préstamo(s)");
        System.out.println("=====================================");
    }
}
