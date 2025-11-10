import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Controles extends KeyAdapter {

    // Banderas de estado para el movimiento y acciones
    private boolean isSaltando = false;
    private boolean isAtacando = false;
    private boolean isMoviendoDerecha = false;
    private boolean isMoviendoIzquierda = false; // RESTAURADA

    // --- Métodos Getters y Setters (Se mantienen sin cambios) ---
    
    public boolean isSaltando() {
        return isSaltando;
    }

    public boolean isAtacando() {
        return isAtacando;
    }

    public boolean isMoviendoDerecha() {
        return isMoviendoDerecha;
    }

    public boolean isMoviendoIzquierda() { // Se usará para mover la posición X del personaje
        return isMoviendoIzquierda;
    }

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
        
        // 🏃‍♂️ Movimiento DERECHA
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            isMoviendoDerecha = true;
        }
        // 🏃‍♂️ Movimiento IZQUIERDA (RESTAURADA)
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            isMoviendoIzquierda = true;
        }

        // ⬆️ Salto
        if (key == KeyEvent.VK_W) {
            isSaltando = true;
        }
        
        // 🗡️ Ataque
        if (key == KeyEvent.VK_P) {
            isAtacando = true;
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        
        // 🏃‍♂️ Movimiento DERECHA
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            isMoviendoDerecha = false;
        }
        // 🏃‍♂️ Movimiento IZQUIERDA (RESTAURADA)
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
            isMoviendoIzquierda = false;
        }
          // 🏃‍♂️ Movimiento saltar (RESTAURADA)
        if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_W) {
            isSaltando = false;
        }

        // 🗡️ Ataque 
        if (key == KeyEvent.VK_P) {
            isAtacando = false; 
        }
    }
}