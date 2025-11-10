import java.awt.Image;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

public class Proyectil {
    
    private int x, y;
    private int ancho, alto;
    private int velocidadX;
    private Image imagen;
    private boolean activo = true; // El estado del proyectil (si debe dibujarse y colisionar)

    // --- CONSTRUCTOR ---
    
    public Proyectil(int x, int y, String rutaImagen, int velocidadX, int ancho, int alto) {
        this.x = x;
        this.y = y;
        this.velocidadX = velocidadX;
        this.ancho = ancho;
        this.alto = alto;
        
        try {
            this.imagen = new ImageIcon(getClass().getResource(rutaImagen)).getImage();
        } catch (Exception e) {
            System.err.println("❌ Error cargando imagen de proyectil (" + rutaImagen + "): Usando Fallback.");
            this.imagen = null;
        }
    }

    // --- LÓGICA DE MOVIMIENTO ---

    public void mover(int limitePantalla) {
        // Mueve el proyectil con su velocidad definida
        this.x += velocidadX;
        
        // Desactiva si sale de la pantalla
        if (x < -ancho || x > limitePantalla) {
            this.activo = false;
        }
    }
    
    // --- DIBUJO ---

    public void dibujar(Graphics g) {
        if (activo) {
            if (imagen != null) {
                g.drawImage(imagen, x, y, ancho, alto, null);
            } else {
                // Dibujar un rectángulo simple como fallback si no carga la imagen
                g.fillRect(x, y, ancho, alto); 
            }
        }
    }
    
    // --- GETTERS Y SETTERS ---

    /**
     * ✅ CORREGIDO: Devuelve la posición X del proyectil.
     * Necesario para que Tablero haga scroll de todos los proyectiles.
     */
    public int getX() {
        return x;
    }
    
    public int getAncho() {
        return ancho;
    }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, ancho, alto);
    }

    public boolean isActivo() {
        return activo;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}