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

    public Cinematica() {
        super("Cinemática");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ✅ COMO TU JUEGO ORIGINAL:
        // Barra de titulo arriba, maximizado, sin barra de tareas abajo
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        // NO usar setUndecorated(true)
        // porque eso quita la barra de título

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
                        new Juego(); 
                    });
                });

            } catch (Exception e) {
                System.out.println("Error al cargar video: " + e.getMessage());
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    new Juego();
                });
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Cinematica());
    }
}
