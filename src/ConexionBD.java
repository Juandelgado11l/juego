import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String URL_SERVIDOR = "JUANDG"; 
    private static final int PUERTO = 1433; // Puerto TCP por defecto de SQL Server
    private static final String NOMBRE_BD = "GameSaveBD"; // Nombre de la base de datos

    // Nombre de la clase del driver
    private static final String DRIVER_CLASS = "com.microsoft.sqlserver.jdbc.SQLServerDriver"; 

    // URL de conexión completa para el driver JDBC
    private static final String JBDC_URL = 
        "jdbc:sqlserver://" + URL_SERVIDOR + ":" + PUERTO + 
        ";databaseName=" + NOMBRE_BD + 
        ";integratedSecurity=true" + 
        ";trustServerCertificate=true;"; 

    public static Connection conectar() {
        Connection conexion = null;
        try {
            // forzar a carga del driver JDBC 🌟
            // Esto resuelve el error "No suitable driver found"
            Class.forName(DRIVER_CLASS); 
            
            // Establece la conexión
            conexion = DriverManager.getConnection(JBDC_URL);
            System.out.println("Conexión exitosa a la base de datos.");
            
        } catch (ClassNotFoundException e) {
            // Se lanza si el driver no se encuentra en el ClassPath (aunque ya lo pusiste, es buena práctica)
            System.err.println("Error: Driver JDBC no encontrado. Asegúrate de que el archivo JAR esté en el Classpath.");
            e.printStackTrace();
        } catch (SQLException e) {
            // Se lanza si hay un error de conexión (e.g., servidor no levantado, TCP/IP deshabilitado)
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