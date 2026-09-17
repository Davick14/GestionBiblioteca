 public class Main { public static void main(String[] args) { } }

 
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
     * Imprime las opciones del menú principal.
     */
    private static void mostrarMenu() {
        System.out.println("\n===== MENÚ PRINCIPAL - Biblioteca LibrosYMas =====");
        System.out.println("1. Registrar nuevo libro");
        System.out.println("2. Mostrar inventario de libros");
        System.out.println("0. Salir");
    }
 
    /**
     * Pide los datos de un libro por consola y lo registra en el inventario.
     */
    private static void registrarLibro() {
        System.out.println("\n--- Registrar nuevo libro ---");
 
        System.out.print("ID único del libro: ");
        String id = sc.nextLine().trim();
 
        System.out.print("Título: ");
        String titulo = sc.nextLine().trim();
 
        System.out.print("Autor: ");
        String autor = sc.nextLine().trim();
 
        System.out.print("Editorial: ");
        String editorial = sc.nextLine().trim();
 
        int anio = leerEntero("Año de publicación: ");
 
        Categoria categoria = seleccionarCategoria();
 
        Libro libro = new Libro(id, titulo, autor, editorial, anio, categoria);
        boolean registrado = inventario.registrarLibro(libro);
 
        if (registrado) {
            System.out.println("Libro registrado correctamente.");
        }
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
     * Lee un entero desde consola de forma segura, repitiendo la pregunta
     * si el usuario ingresa un valor no numérico.
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
}
      