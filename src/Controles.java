import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Controles extends KeyAdapter {

    // Banderas de estado para el movimiento y acciones
    private boolean isSaltando = false;
    private boolean isAtacando = false;
    private boolean isMoviendoDerecha = false;
    private boolean isMoviendoIzquierda = false;

    // --- Métodos Getters ---
    
    public boolean isSaltando() {
        return isSaltando;
    }

    public boolean isAtacando() {
        return isAtacando;
    }

    public boolean isMoviendoDerecha() {
        return isMoviendoDerecha;
    }

    public boolean isMoviendoIzquierda() {
        return isMoviendoIzquierda;
    }

    // --- Métodos Setters (CRUCIAL para corregir el error) ---

    /**
     * Establece el estado de salto. 
     * Este método es llamado por Tablero.java para resetear el flag después de iniciar un salto.
     * @param isSaltando Nuevo estado de salto.
     */
    public void setSaltando(boolean isSaltando) {
        this.isSaltando = isSaltando;
    }

    public void setAtacando(boolean isAtacando) {
        this.isAtacando = isAtacando;
    }


    // --- Implementación de KeyListener ---
    
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        
        // 🏃‍♂️ Movimiento
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            isMoviendoDerecha = true;
        }
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            isMoviendoIzquierda = true;
        }

        // ⬆️ Salto
        if (key == KeyEvent.VK_SPACE) {
            isSaltando = true; // Se activa la bandera para que Tablero lo detecte
        }
        
        // 🗡️ Ataque (Asumimos la tecla J para atacar)
        if (key == KeyEvent.VK_J) {
            isAtacando = true;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        
        // 🏃‍♂️ Movimiento
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            isMoviendoDerecha = false;
        }
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            isMoviendoIzquierda = false;
        }

        // 🗡️ Ataque 
        if (key == KeyEvent.VK_J) {
            isAtacando = false; // El ataque termina cuando se suelta la tecla
        }
        
        // Nota: La bandera 'isSaltando' se resetea en la clase Tablero, no aquí.
    }
}