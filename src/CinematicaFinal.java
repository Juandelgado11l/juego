import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.io.File;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.media.MediaPlayer.Status; // Necesaria para detectar el estado STALLED

public class CinematicaFinal extends JFrame {

    private JFXPanel jfxPanel;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;
    
    // RUTA FINAL: Usamos la carga de archivo (File) que funciona en tu entorno
    private static final String RUTA_VIDEO = "src/video2/video_final.mp4"; 

    public CinematicaFinal() {
        super("Fin del Juego");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        setLayout(new BorderLayout());
        
        // Cierre y limpieza robusta de recursos al cerrar la ventana
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                detenerYLimpiar();
                System.exit(0);
            }
        });

        jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);

        Platform.runLater(() -> {
            try {
                // 🛑 CORRECCIÓN: Usamos la carga de archivo del sistema (File)
                File mediaFile = new File(RUTA_VIDEO); 
                
                if (!mediaFile.exists()) {
                    throw new RuntimeException("❌ ERROR: El archivo de video no se encontró en la ruta: " + mediaFile.getAbsolutePath());
                }
                
                Media media = new Media(mediaFile.toURI().toString());
                
                // DIAGNÓSTICO: Comprueba errores de carga de archivo/códec
                media.errorProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        System.err.println("🚨 ERROR DE CARGA DE MEDIA (ARCHIVO/CÓDEC): " + newValue.getMessage());
                        detenerYLimpiar();
                    }
                });
                
                mediaPlayer = new MediaPlayer(media);
                
                // DIAGNÓSTICO: Comprueba errores de reproducción
                mediaPlayer.setOnError(() -> {
                    System.err.println("🚨 ERROR DE REPRODUCCIÓN (PLAYER): " + mediaPlayer.getError().getMessage());
                    detenerYLimpiar();
                });
                
                // 🛑 NUEVO: Detector de Bloqueo (STALLED)
                mediaPlayer.statusProperty().addListener((obs, oldStatus, newStatus) -> {
                    if (newStatus == Status.STALLED) {
                        System.err.println("🚨 REPRODUCTOR BLOQUEADO (STALLED). Forzando cierre para evitar congelamiento.");
                        SwingUtilities.invokeLater(() -> {
                            detenerYLimpiar();
                            dispose();
                            System.exit(0);
                        });
                    }
                });
                
                mediaView = new MediaView(mediaPlayer);

                int w = Toolkit.getDefaultToolkit().getScreenSize().width;
                int h = Toolkit.getDefaultToolkit().getScreenSize().height;

                mediaView.setFitWidth(w);
                mediaView.setFitHeight(h);
                mediaView.setPreserveRatio(false);

                Group root = new Group(mediaView);
                Scene scene = new Scene(root, w, h);
                jfxPanel.setScene(scene);

                mediaPlayer.play();

                mediaPlayer.setOnEndOfMedia(() -> {
                    SwingUtilities.invokeLater(() -> {
                        System.out.println("Fin del Juego. Cerrando...");
                        detenerYLimpiar();
                        dispose();
                        System.exit(0); // Cierre final de todo el programa
                    });
                });

            } catch (Exception e) {
                System.err.println("❌❌❌ FALLO TOTAL AL INICIALIZAR VIDEO ❌❌❌");
                System.err.println("Mensaje: " + e.getMessage());
                e.printStackTrace(); 
                detenerYLimpiar();
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    System.exit(0);
                });
            }
        });
    }

    // Método para detener y liberar recursos de JavaFX
    private void detenerYLimpiar() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    public void iniciar() {
        setVisible(true);
    }
}