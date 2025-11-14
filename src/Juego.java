
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Juego extends JFrame {

    private Tablero tablero;

    // Nueva partida
    public Juego() {
        this(-1);
    }

    // Cargar partida
    public Juego(int idPartidaACargar) {
        setTitle("Caballero de rosas");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);

        tablero = new Tablero(idPartidaACargar);
        add(tablero);

        // Guardar al cerrar ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                manejarCierreYGuardado();
            }
        });

        setVisible(true);
        tablero.iniciarJuego();
    }

    // Guardar o salir del juego
    private void manejarCierreYGuardado() {
        int opcion = JOptionPane.showConfirmDialog(
            this, 
            "¿Deseas guardar la partida antes de salir?", 
            "Confirmar Salida", 
            JOptionPane.YES_NO_CANCEL_OPTION
        );

        if (opcion == JOptionPane.YES_OPTION) {
            String nombrePartida = JOptionPane.showInputDialog(
                this,
                "Introduce el nombre para la partida:",
                "Guardar Partida",
                JOptionPane.PLAIN_MESSAGE
            );

            if (nombrePartida != null) {
                if (!nombrePartida.trim().isEmpty()) {
                    boolean guardado = tablero.guardarEstadoDelJuego(nombrePartida);

                    if (guardado) {
                        JOptionPane.showMessageDialog(this, "Partida guardada con éxito.", "Guardado", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error al guardar la partida.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "El nombre de la partida no puede estar vacío.", "Nombre inválido", JOptionPane.WARNING_MESSAGE);
                }
            }

        } else if (opcion == JOptionPane.NO_OPTION) {
            dispose();
        }
    }
}
