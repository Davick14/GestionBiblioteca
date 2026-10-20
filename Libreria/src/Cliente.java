public class Cliente {
    private String documento;
    private String nombre;
    private String telefono;
    private String direccion;
    private boolean tieneLibroPrestado;

    // Total acumulado de multas por devoluciones tardías (en pesos)
    private int multaPendiente;

    //Constructor de la clase
    public Cliente(String documento, String nombre, String telefono, String direccion) {
        this.documento = documento;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.tieneLibroPrestado = false; // un cliente nuevo nunca tiene prestamos
        this.multaPendiente = 0;         // ni multas
    }

    //Metodos get y set
    public String getDocumento() {
        return documento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public boolean tieneLibroPrestado() {
        return tieneLibroPrestado;
    }

    //Marca que el cliente recibio un libro en prestamo.
    public void marcarConPrestamo() {
        this.tieneLibroPrestado = true;
    }

    //Marca que el cliente devolvio su libro y queda habilitado
    public void marcarSinPrestamo() {
        this.tieneLibroPrestado = false;
    }

    // ---- Multas ----

    public int getMultaPendiente() {
        return multaPendiente;
    }

    /** Suma una multa nueva al total que el cliente debe. Ignora valores no positivos. */
    public void agregarMulta(int valor) {
        if (valor > 0) {
            this.multaPendiente += valor;
        }
    }

    /** Se usa al cargar datos guardados, para restaurar la multa acumulada. */
    public void setMultaPendiente(int valor) {
        this.multaPendiente = Math.max(0, valor);
    }

    @Override
    public String toString() {
        String estado = tieneLibroPrestado ? "CON libro en prestamo" : "SIN libros pendientes";
        return String.format(
                "Documento : %s%n"
                        + "Nombre    : %s%n"
                        + "Telefono  : %s%n"
                        + "Direccion : %s%n"
                        + "Estado    : %s%n"
                        + "Multa pendiente: $%d",
                documento, nombre, telefono, direccion, estado, multaPendiente);
    }

}
