import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PartidaDAO {

    // ----------------------------------------------------------------------
    // --- MÉTODO 1: GUARDAR PARTIDA (UPDATE O INSERT) ---
    // ----------------------------------------------------------------------

    /**
     * Guarda el estado actual del juego. Intenta actualizar (UPDATE) por nombre,
     * y si no existe (0 filas afectadas), realiza una inserción (INSERT).
     * @param nombrePartida Nombre del slot de guardado.
     * @param contadorRosas Rosas recogidas.
     * @param vida Vida actual.
     * @param saltos Saltos máximos (1 o 2).
     * @param velocidad Velocidad base (4 o 6).
     * @param posX Posición X del personaje.
     * @param posY Posición Y del personaje.
     * @param cinematicaTerminada 0 si no terminó, 1 si sí.
     * @return true si la operación fue exitosa (UPDATE o INSERT).
     */
    public boolean guardarPartida(String nombrePartida, int contadorRosas, int vida, 
                                  int saltos, int velocidad, int posX, int posY, 
                                  int cinematicaTerminada) {
        
        // 🚩 1. SQL para actualizar una partida existente por NOMBRE_PARTIDA
        String SQL_UPDATE = 
            "UPDATE PARTIDAS_GUARDADAS SET ROSA_CONTADOR = ?, VIDA_ACTUAL = ?, SALTO_MAXIMO = ?, " +
            "VELOCIDAD_BASE = ?, POS_X = ?, POS_Y = ?, CINEMATICA_TERMINADA = ?, FECHA_GUARDADO = GETDATE() " +
            "WHERE NOMBRE_PARTIDA = ?";
            
        // 🚩 2. SQL para insertar una nueva partida si el UPDATE no afecta filas
        String SQL_INSERT = 
            "INSERT INTO PARTIDAS_GUARDADAS (NOMBRE_PARTIDA, ROSA_CONTADOR, VIDA_ACTUAL, SALTO_MAXIMO, VELOCIDAD_BASE, POS_X, POS_Y, CINEMATICA_TERMINADA, FECHA_GUARDADO) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";

        try (Connection conn = ConexionBD.conectar()) {
            
            // --- PASO A: INTENTAR ACTUALIZAR (UPDATE) ---
            try (PreparedStatement stmtUpdate = conn.prepareStatement(SQL_UPDATE)) {
                stmtUpdate.setInt(1, contadorRosas);
                stmtUpdate.setInt(2, vida);
                stmtUpdate.setInt(3, saltos);
                stmtUpdate.setInt(4, velocidad);
                stmtUpdate.setInt(5, posX);
                stmtUpdate.setInt(6, posY);
                stmtUpdate.setInt(7, cinematicaTerminada);
                stmtUpdate.setString(8, nombrePartida); // Condición WHERE
                
                int filasAfectadas = stmtUpdate.executeUpdate();
                
                if (filasAfectadas > 0) {
                    // Carga sobrescrita: el slot ya existía.
                    return true;
                }
            } // El PreparedStatement stmtUpdate se cierra automáticamente

            // --- PASO B: SI NO SE ACTUALIZÓ NADA, INSERTAR (INSERT) ---
            try (PreparedStatement stmtInsert = conn.prepareStatement(SQL_INSERT)) {
                stmtInsert.setString(1, nombrePartida);
                stmtInsert.setInt(2, contadorRosas);
                stmtInsert.setInt(3, vida);
                stmtInsert.setInt(4, saltos);
                stmtInsert.setInt(5, velocidad);
                stmtInsert.setInt(6, posX);
                stmtInsert.setInt(7, posY);
                stmtInsert.setInt(8, cinematicaTerminada);
                
                int filasAfectadas = stmtInsert.executeUpdate();
                return filasAfectadas > 0;
            } // El PreparedStatement stmtInsert se cierra automáticamente

        } catch (SQLException e) {
            System.err.println("Error al guardar/actualizar partida: " + e.getMessage());
            return false;
        }
    }

    // ----------------------------------------------------------------------
    // --- MÉTODO 2: CARGAR DATOS (SELECT por ID) ---
    // ----------------------------------------------------------------------

    /**
     * Carga todos los datos de estado de una partida por su ID.
     * @param idPartida El ID único de la partida a cargar.
     * @return Un array de enteros con los 7 datos, o null si falla.
     */
    public int[] cargarPartida(int idPartida) {
        
        // 🚩 SQL para seleccionar los 7 datos de estado del juego
        String SQL_SELECT = 
            "SELECT ROSA_CONTADOR, VIDA_ACTUAL, SALTO_MAXIMO, VELOCIDAD_BASE, POS_X, POS_Y, CINEMATICA_TERMINADA " +
            "FROM PARTIDAS_GUARDADAS WHERE ID = ?"; 
        
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT)) {

            stmt.setInt(1, idPartida);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // 🚩 El array contiene 7 elementos, en el orden en que se van a aplicar:
                    return new int[]{
                        rs.getInt("ROSA_CONTADOR"),
                        rs.getInt("VIDA_ACTUAL"),
                        rs.getInt("SALTO_MAXIMO"),
                        rs.getInt("VELOCIDAD_BASE"),
                        rs.getInt("POS_X"),
                        rs.getInt("POS_Y"),
                        rs.getInt("CINEMATICA_TERMINADA")
                    };
                }
            }
            return null;
        } catch (SQLException e) {
            System.err.println("Error al cargar partida (ID: " + idPartida + "): " + e.getMessage());
            return null;
        }
    }
    
    // ----------------------------------------------------------------------
    // --- MÉTODO 3: LISTAR PARTIDAS (SELECT para Menú) ---
    // ----------------------------------------------------------------------
    
    /**
     * Obtiene una lista de partidas guardadas para mostrar en el menú.
     * @return Una lista de objetos PartidaGuardada (ID, Nombre, Fecha).
     */
    public List<PartidaGuardada> listarPartidas() {
        
        List<PartidaGuardada> partidas = new ArrayList<>();
        String SQL_SELECT_ALL = 
            "SELECT ID, NOMBRE_PARTIDA, FECHA_GUARDADO " +
            "FROM PARTIDAS_GUARDADAS ORDER BY FECHA_GUARDADO DESC";
        
        // Conexión, Statement y ResultSet se cierran automáticamente
        try (Connection conn = ConexionBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                int id = rs.getInt("ID");
                String nombre = rs.getString("NOMBRE_PARTIDA");
                String fecha = rs.getString("FECHA_GUARDADO"); 
                
                // Asegúrate de que tu clase PartidaGuardada tenga este constructor.
                partidas.add(new PartidaGuardada(id, nombre, fecha));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar partidas: " + e.getMessage());
        }
        return partidas;
    }
}