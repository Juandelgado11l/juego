import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    
    // ⚠️ Reemplaza estos valores con tu configuración real de SQL Server
    private static final String URL_SERVIDOR = "localhost"; // O la IP/Nombre de tu servidor
    private static final int PUERTO = 1433; // Puerto TCP por defecto de SQL Server
    private static final String NOMBRE_DB = "GameSaveDB"; // Nombre de la base de datos
    private static final String USUARIO = "SA"; // O tu usuario
    private static final String CONTRASENA = "TuContrasenaSegura"; // Tu contraseña

    // URL de conexión completa para el driver JDBC
    private static final String JDBC_URL = 
        "jdbc:sqlserver://" + URL_SERVIDOR + ":" + PUERTO + 
        ";databaseName=" + NOMBRE_DB + 
        ";user=" + USUARIO + 
        ";password=" + CONTRASENA + 
        // Es posible que necesites agregar más propiedades como integratedSecurity=true o encrypt=true 
        ";trustServerCertificate=true;"; 

    public static Connection conectar() {
        Connection conexion = null;
        try {
            // Establece la conexión
            conexion = DriverManager.getConnection(JDBC_URL);
            System.out.println("Conexión exitosa a la base de datos.");
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos: ");
            e.printStackTrace();
        }
        return conexion;
    }

    public static void cerrar(Connection conexion) {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
                System.out.println("Conexión cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar la conexión.");
        }
    }
}