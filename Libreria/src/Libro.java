public class Libro {
 
    private String id;
    private String titulo;
    private String autor;
    private String editorial;
    private int anioPublicacion;
    private Categoria categoria;
    private EstadoLibro estado;
 
    public Libro(String id, String titulo, String autor, String editorial,
                 int anioPublicacion, Categoria categoria) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.editorial = editorial;
        this.anioPublicacion = anioPublicacion;
        this.categoria = categoria;
        this.estado = EstadoLibro.DISPONIBLE; // todo libro nuevo entra como disponible
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
 