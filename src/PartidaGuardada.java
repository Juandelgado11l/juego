public class PartidaGuardada {
    private int id;
    private String nombre;
    private String fecha; 

    // Constructor
    public PartidaGuardada(int id, String nombre, String fecha) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
    }
    
    // Getters para acceder a los datos
    public int getId() { 
        return id; 
    }
    
    public String getNombre() { 
        return nombre; 
    }
    
    public String getFecha() { 
        return fecha; 
    }

    /**
     * Sobrescribe el método toString() para devolver una representación legible 
     * del objeto PartidaGuardada.
     * * El JComboBox usa este método para saber qué texto mostrar al usuario.
     */
    @Override
    public String toString() {
        // Formatea la salida para que el usuario vea el nombre y la fecha.
        // Ejemplo de salida: "Mi Partida Épica (Guardada: 10/11/2025)"
        return nombre + " (Guardada: " + fecha + ")";
    }
}