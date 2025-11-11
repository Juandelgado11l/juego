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

public class Cinematica extends JFrame {

    private JFXPanel jfxPanel;
    private MediaPlayer mediaPlayer;
    private MediaView mediaView;

    private static final String RUTA_VIDEO = "src/video/intro_game.mp4";
    
    // 🌟 ADICIÓN: Referencia a la ventana de juego, para iniciarla al cargar partida.
    private int idPartidaACargar = -1; 
    

    public Cinematica() {
        // Llama al constructor que permite pasar un ID de partida.
        this(-1); 
    }
    
    // 🌟 NUEVO CONSTRUCTOR para permitir cargar una partida (ID > -1)
    public Cinematica(int idCarga) {
        super("Cinemática");
        this.idPartidaACargar = idCarga;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ✅ COMO TU JUEGO ORIGINAL:
        // Barra de titulo arriba, maximizado, sin barra de tareas abajo
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);

        setLayout(new BorderLayout());

        jfxPanel = new JFXPanel();
        add(jfxPanel, BorderLayout.CENTER);

        Platform.runLater(() -> {
            try {
                File mediaFile = new File(RUTA_VIDEO);

                Media media = new Media(mediaFile.toURI().toString());
                mediaPlayer = new MediaPlayer(media);
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
                        dispose();
                        // 🌟 IMPORTANTE: Pasamos el ID de la partida a la clase Juego
                        new Juego(this.idPartidaACargar); 
                    });
                });

            } catch (Exception e) {
                System.out.println("Error al cargar video: " + e.getMessage());
                // Si falla el video, saltamos directamente al juego.
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    new Juego(this.idPartidaACargar);
                });
            }
        });
    }

    /**
     * 🏆 MÉTODO REQUERIDO: Inicia la cinemática.
     * Esto resuelve el error "The method iniciar() is undefined".
     */
    public void iniciar() {
        // En tu código, el constructor ya inicia casi todo,
        // pero setVisible(true) es lo que hace visible la ventana.
        // Lo movemos aquí para responder a la llamada `new Cinematica().iniciar();`
        setVisible(true);
    }
    
    // -----------------------------------------------------------

    public static void main(String[] args) {
        // Si se ejecuta directamente, inicia una nueva cinemática (nueva partida)
        SwingUtilities.invokeLater(() -> new Cinematica().iniciar()); 
    }
}