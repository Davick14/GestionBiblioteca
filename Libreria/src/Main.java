import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private static Scanner sc = new Scanner(System.in);

    private static InventarioLibros inventario = new InventarioLibros();

    // Lista de clientes almacenados en memoria
    private static ArrayList<Cliente> clientes = new ArrayList<>();

    // Gestiona préstamos, devoluciones, multas y reservas
    private static GestorPrestamos gestor = new GestorPrestamos(inventario, clientes);

    // Formato de fecha que se le pide al usuario (estricto: rechaza fechas imposibles como 31/02)
    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    public static void main(String[] args) {

        cargarDatos();

        int opcion;

        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opción: ");

            switch (opcion) {

                case 1:
                    registrarLibro();
                    break;

                case 2:
                    inventario.listarLibros();
                    break;

                case 3:
                    registrarCliente();
                    break;

                case 4:
                    consultarClientes();
                    break;

                case 5:
                    prestarLibro();
                    break;

                case 6:
                    devolverLibro();
                    break;

                case 7:
                    gestor.listarPrestamosActivos();
                    break;

                case 8:
                    consultarMultas();
                    break;

                case 9:
                    reservarLibro();
                    break;

                case 10:
                    consultarReservas();
                    break;

                case 11:
                    Reporte.generar(inventario, clientes, gestor);
                    break;

                case 0:
                    System.out.println("Saliendo del sistema...");
                    break;

                default:
                    System.out.println("Opción no válida. Intente de nuevo.");
            }

        } while (opcion != 0);

        sc.close();
    }

    /**
     * Lee los archivos de la carpeta "datos" y llena inventario, clientes,
     * préstamos y reservas. El orden importa: primero libros y clientes.
     */
    private static void cargarDatos() {
        for (Libro libro : Persistencia.cargarLibros()) {
            inventario.registrarLibro(libro);
        }
        clientes.addAll(Persistencia.cargarClientes());
        gestor.getPrestamos().addAll(Persistencia.cargarPrestamos(inventario, clientes));
        Persistencia.cargarReservas(inventario, clientes);

        System.out.println("Datos cargados: " + inventario.getLibros().size()
                + " libros, " + clientes.size() + " clientes y "
                + gestor.getPrestamos().size() + " préstamos.");
    }

    /**
     * Escribe en los archivos el estado actual de todo el sistema.
     * Se llama despues de cada operacion que modifica datos.
     */
    private static void guardarDatos() {
        Persistencia.guardarLibros(inventario.getLibros());
        Persistencia.guardarClientes(clientes);
        Persistencia.guardarPrestamos(gestor.getPrestamos());
        Persistencia.guardarReservas(inventario.getLibros());
    }

    /**
     * Imprime las opciones del menú principal.
     */
    private static void mostrarMenu() {
        System.out.println("\n===== MENÚ PRINCIPAL - Biblioteca LibrosYMas =====");
        System.out.println("1. Registrar nuevo libro");
        System.out.println("2. Mostrar inventario de libros");
        System.out.println("3. Registrar cliente");
        System.out.println("4. Consultar clientes");
        System.out.println("5. Prestar libro");
        System.out.println("6. Devolver libro");
        System.out.println("7. Ver préstamos activos");
        System.out.println("8. Ver multas de un cliente");
        System.out.println("9. Reservar libro");
        System.out.println("10. Consultar reservas de un libro");
        System.out.println("11. Reporte general");
        System.out.println("0. Salir");
    }

    /**
     * Pide los datos de un libro por consola y lo registra en el inventario.
     */
    private static void registrarLibro() {

        System.out.println("\n--- Registrar nuevo libro ---");

        System.out.print("ID único del libro: ");
        String id = sc.nextLine().trim();

        if (id.isEmpty()) {
            System.out.println("El ID del libro no puede estar vacío.");
            return;
        }

        System.out.print("Título: ");
        String titulo = sc.nextLine().trim();

        System.out.print("Autor: ");
        String autor = sc.nextLine().trim();

        System.out.print("Editorial: ");
        String editorial = sc.nextLine().trim();

        int anio = leerEntero("Año de publicación: ");

        Categoria categoria = seleccionarCategoria();

        Libro libro = new Libro(
            id,
            titulo,
            autor,
            editorial,
            anio,
            categoria
        );

        boolean registrado = inventario.registrarLibro(libro);

        if (registrado) {
            System.out.println("Libro registrado correctamente.");
            guardarDatos();
        }
    }

    /**
     * Registra un nuevo cliente.
     */
    private static void registrarCliente() {

        System.out.println("\n--- Registrar nuevo cliente ---");

        System.out.print("Documento: ");
        String documento = sc.nextLine().trim();

        if (documento.isEmpty()) {
            System.out.println("El documento no puede estar vacío.");
            return;
        }

        if (gestor.buscarCliente(documento) != null) {
            System.out.println("Ya existe un cliente registrado con el documento: " + documento);
            return;
        }

        System.out.print("Nombre: ");
        String nombre = sc.nextLine().trim();

        System.out.print("Teléfono: ");
        String telefono = sc.nextLine().trim();

        System.out.print("Dirección: ");
        String direccion = sc.nextLine().trim();

        Cliente cliente = new Cliente(
            documento,
            nombre,
            telefono,
            direccion
        );

        clientes.add(cliente);
        guardarDatos();

        System.out.println("Cliente registrado correctamente.");
    }

    /**
     * Consulta y muestra todos los clientes registrados.
     */
    private static void consultarClientes() {

        System.out.println("\n--- Clientes registrados ---");

        if (clientes.isEmpty()) {
            System.out.println("No hay clientes registrados.");
            return;
        }

        for (Cliente cliente : clientes) {
            System.out.println("----------------------------");
            System.out.println(cliente);
        }
    }

    /**
     * Presta un libro a un cliente con la fecha de hoy.
     */
    private static void prestarLibro() {

        System.out.println("\n--- Prestar libro ---");

        System.out.print("ID del libro: ");
        String idLibro = sc.nextLine().trim();

        System.out.print("Documento del cliente: ");
        String documento = sc.nextLine().trim();

        if (gestor.prestarLibro(idLibro, documento, LocalDate.now())) {
            guardarDatos();
        }
    }

    /**
     * Registra la devolución de un libro. La fecha es opcional (Enter = hoy),
     * lo que permite simular devoluciones tardías para probar las multas.
     */
    private static void devolverLibro() {

        System.out.println("\n--- Devolver libro ---");

        System.out.print("ID del libro: ");
        String idLibro = sc.nextLine().trim();

        LocalDate fecha = leerFechaOpcional("Fecha de devolución (dd/MM/aaaa) o Enter para hoy: ");

        if (gestor.devolverLibro(idLibro, fecha)) {
            guardarDatos();
        }
    }

    /**
     * Muestra las multas de un cliente específico.
     */
    private static void consultarMultas() {

        System.out.println("\n--- Consultar multas ---");

        System.out.print("Documento del cliente: ");
        String documento = sc.nextLine().trim();

        gestor.consultarMultas(documento);
    }

    /**
     * Registra una reserva para un libro que está prestado.
     */
    private static void reservarLibro() {

        System.out.println("\n--- Reservar libro ---");

        System.out.print("ID del libro: ");
        String idLibro = sc.nextLine().trim();

        System.out.print("Documento del cliente: ");
        String documento = sc.nextLine().trim();

        if (gestor.reservarLibro(idLibro, documento)) {
            guardarDatos();
        }
    }

    /**
     * Muestra la lista de espera de un libro.
     */
    private static void consultarReservas() {

        System.out.println("\n--- Consultar reservas ---");

        System.out.print("ID del libro: ");
        String idLibro = sc.nextLine().trim();

        gestor.consultarReservas(idLibro);
    }

    /**
     * Muestra las categorías disponibles y devuelve la que el usuario elija.
     */
    private static Categoria seleccionarCategoria() {

        Categoria[] categorias = Categoria.values();
        int opcion;

        do {
            System.out.println("Seleccione la categoría:");

            for (int i = 0; i < categorias.length; i++) {
                System.out.println((i + 1) + ". " + categorias[i]);
            }

            opcion = leerEntero("Opción: ");

            if (opcion < 1 || opcion > categorias.length) {
                System.out.println("Categoría inválida, intente de nuevo.");
            }

        } while (opcion < 1 || opcion > categorias.length);

        return categorias[opcion - 1];
    }

    /**
     * Lee un entero desde consola de forma segura.
     */
    private static int leerEntero(String mensaje) {

        while (true) {

            System.out.print(mensaje);

            String entrada = sc.nextLine().trim();

            try {
                return Integer.parseInt(entrada);

            } catch (NumberFormatException e) {
                System.out.println("Por favor ingrese un número válido.");
            }
        }
    }

    /**
     * Lee una fecha con formato dd/MM/aaaa. Si el usuario solo presiona Enter,
     * devuelve la fecha de hoy. Repite la pregunta hasta recibir algo válido.
     */
    private static LocalDate leerFechaOpcional(String mensaje) {

        while (true) {

            System.out.print(mensaje);

            String entrada = sc.nextLine().trim();

            if (entrada.isEmpty()) {
                return LocalDate.now();
            }

            try {
                return LocalDate.parse(entrada, FORMATO_FECHA);

            } catch (DateTimeParseException e) {
                System.out.println("Fecha inválida. Use el formato dd/MM/aaaa, por ejemplo 25/12/2026.");
            }
        }
    }
}
