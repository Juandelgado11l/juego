// Archivo: Juego.java (CORREGIDO)

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Juego extends JFrame {

    private Tablero tablero;
    
    // --- Constructor 1: NUEVA PARTIDA ---
    public Juego() {
        this(-1);
    }
    
    // --- Constructor 2: CARGAR PARTIDA ---
    public Juego(int idPartidaACargar) {
        setTitle("Caballero de rosas");
        
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        
        // Crear el tablero
        tablero = new Tablero(idPartidaACargar);
        add(tablero);

        // Listener para guardar al cerrar
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                manejarCierreYGuardado();
            }
        });

        setVisible(true);
        tablero.iniciarJuego();
    }

    /**
     * Pregunta si desea guardar antes de cerrar y DELEGA el guardado al Tablero.
     * Valida que el nombre de la partida no esté vacío.
     */
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
                        JOptionPane.showMessageDialog(
                            this, 
                            "Partida guardada con éxito.", 
                            "Guardado", 
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        dispose(); 
                    } else {
                        JOptionPane.showMessageDialog(
                            this, 
                            "Error al guardar la partida.", 
                            "Error de Guardado", 
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "El nombre de la partida no puede estar vacío.",
                        "Nombre inválido",
                        JOptionPane.WARNING_MESSAGE
                    );
                }
            }

        } else if (opcion == JOptionPane.NO_OPTION) {
            dispose();
        }
    }
}
