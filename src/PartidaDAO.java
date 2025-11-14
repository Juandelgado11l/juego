import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PartidaDAO {

    // Guarda o actualiza una partida
    public boolean guardarPartida(String nombrePartida, int contadorRosas, int vida,
                                  int saltos, int velocidad, int posX, int posY,
                                  int cinematicaTerminada) {

        String SQL_UPDATE =
            "UPDATE PARTIDAS_GUARDADAS SET ROSA_CONTADOR = ?, VIDA_ACTUAL = ?, SALTO_MAXIMO = ?, " +
            "VELOCIDAD_BASE = ?, POS_X = ?, POS_Y = ?, CINEMATICA_TERMINADA = ?, FECHA_GUARDADO = GETDATE() " +
            "WHERE NOMBRE_PARTIDA = ?";

        String SQL_INSERT =
            "INSERT INTO PARTIDAS_GUARDADAS (NOMBRE_PARTIDA, ROSA_CONTADOR, VIDA_ACTUAL, SALTO_MAXIMO, VELOCIDAD_BASE, POS_X, POS_Y, CINEMATICA_TERMINADA, FECHA_GUARDADO) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";

        try (Connection conn = ConexionBD.conectar()) {

            // Intentar actualizar
            try (PreparedStatement stmtUpdate = conn.prepareStatement(SQL_UPDATE)) {
                stmtUpdate.setInt(1, contadorRosas);
                stmtUpdate.setInt(2, vida);
                stmtUpdate.setInt(3, saltos);
                stmtUpdate.setInt(4, velocidad);
                stmtUpdate.setInt(5, posX);
                stmtUpdate.setInt(6, posY);
                stmtUpdate.setInt(7, cinematicaTerminada);
                stmtUpdate.setString(8, nombrePartida);

                int filasAfectadas = stmtUpdate.executeUpdate();
                if (filasAfectadas > 0) return true;
            }

            // Insertar si no existe
            try (PreparedStatement stmtInsert = conn.prepareStatement(SQL_INSERT)) {
                stmtInsert.setString(1, nombrePartida);
                stmtInsert.setInt(2, contadorRosas);
                stmtInsert.setInt(3, vida);
                stmtInsert.setInt(4, saltos);
                stmtInsert.setInt(5, velocidad);
                stmtInsert.setInt(6, posX);
                stmtInsert.setInt(7, posY);
                stmtInsert.setInt(8, cinematicaTerminada);

                return stmtInsert.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error al guardar partida: " + e.getMessage());
            return false;
        }
    }

    // Carga los datos de una partida
    public int[] cargarPartida(int idPartida) {
        String SQL_SELECT =
            "SELECT ROSA_CONTADOR, VIDA_ACTUAL, SALTO_MAXIMO, VELOCIDAD_BASE, POS_X, POS_Y, CINEMATICA_TERMINADA " +
            "FROM PARTIDAS_GUARDADAS WHERE ID = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT)) {

            stmt.setInt(1, idPartida);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
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
        } catch (SQLException e) {
            System.err.println("Error al cargar partida (ID: " + idPartida + "): " + e.getMessage());
        }
        return null;
    }

    // Lista todas las partidas guardadas
    public List<PartidaGuardada> listarPartidas() {
        List<PartidaGuardada> partidas = new ArrayList<>();
        String SQL_SELECT_ALL =
            "SELECT ID, NOMBRE_PARTIDA, FECHA_GUARDADO " +
            "FROM PARTIDAS_GUARDADAS ORDER BY FECHA_GUARDADO DESC";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("ID");
                String nombre = rs.getString("NOMBRE_PARTIDA");
                String fecha = rs.getString("FECHA_GUARDADO");
                partidas.add(new PartidaGuardada(id, nombre, fecha));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar partidas: " + e.getMessage());
        }
        return partidas;
    }

    // Verifica si un nombre de partida ya existe
    public boolean existeNombre(String nombrePartida) {
        String SQL_CHECK = "SELECT COUNT(*) FROM PARTIDAS_GUARDADAS WHERE NOMBRE_PARTIDA = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement stmt = conn.prepareStatement(SQL_CHECK)) {

            stmt.setString(1, nombrePartida);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar nombre: " + e.getMessage());
        }
        return false;
    }
}
