import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

public class Rosa {
    private int x, y;
    private Image imagen;
    public static final int ANCHO = 74;
    public static final int ALTO = 74;

    public Rosa(int x, int y) {
        this.x = x;
        this.y = y;
        try {
            this.imagen = new ImageIcon(getClass().getResource("/img/flor.gif")).getImage();
        } catch (Exception e) {
             System.err.println("Error al cargar imagen de Rosa.");
             this.imagen = null;
        }
    }

    public void dibujar(Graphics g) {
        if (imagen != null) {
            g.drawImage(imagen, x, y, ANCHO, ALTO, null);
        }
    }

    // ✅ Método de movimiento para que la rosa siga el scroll
    public void mover(int scrollSpeed) { 
        this.x += scrollSpeed; 
    }

    // --- SETTERS ---
    public void setX(int x) { 
        this.x = x; 
    }
    public void setY(int y) { 
        this.y = y; 
    }
            // 📄 En Rosa.java (dentro de la clase)

// Asumiendo que ya tienes getHitbox() definido:
public Rectangle getRect() {
    // Simplemente llama al método existente, o devuelve el nuevo rectángulo.
    return getHitbox(); 
}

    // --- GETTERS ---
    
    // ✅ CORREGIDO: Renombrado de getRect() a getHitbox() para compatibilidad
    public Rectangle getHitbox() {
        return new Rectangle(x, y, ANCHO, ALTO);
    }
    
    public Image getImagen() { return imagen; } // Necesario para Pixel-Perfect
    public int getX() { return x; }
    public int getY() { return y; }
    public int getAncho() { return ANCHO; }
    public int getAlto() { return ALTO; }
}