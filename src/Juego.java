import javax.swing.JFrame;

public class Juego extends JFrame implements Runnable {

    private Tablero tablero;
    private Thread hilo;

    public Juego() {
        setTitle("Mi Juego");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ✅ EXACTAMENTE COMO TU JUEGO ORIGINAL:
        // barra arriba, maximizado, sin barra de tareas
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        // NO usar setUndecorated(true)

        tablero = new Tablero();
        add(tablero);

        setVisible(true);

        iniciarHilo();
    }

    private void iniciarHilo() {
        hilo = new Thread(this);
        hilo.start();
    }

    @Override
    public void run() {
        while (true) {
            try { Thread.sleep(17); } catch (Exception e) {}

            tablero.actualizar();
            tablero.repaint();
        }
    }
}
