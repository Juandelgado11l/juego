import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image; 
import javax.swing.ImageIcon; 

public class Vida {
    
    private static final int TAMAÑO_CORAZON = 69; 
    private static final int ESPACIO = 5; 
    
    private int vidaMaxima;
    private int vidaActual;
    private Image imagenCorazonLleno;
    private Image imagenCorazonVacio;
    
    // --- Constructores ---
    
    public Vida() {
        this(3); 
    }

    public Vida(int vidaMaxima) {
        this.vidaMaxima = vidaMaxima;
        this.vidaActual = vidaMaxima;
        
        try {
            // 🚨 CORRECCIÓN: Añadir la barra inicial (/) para la ruta del classpath
            imagenCorazonLleno = new ImageIcon(getClass().getResource("/img/vida.jpg")).getImage();
            imagenCorazonVacio = new ImageIcon(getClass().getResource("/img/corazonRoto.jpg")).getImage();
        } catch (Exception e) {
            System.err.println("Error cargando sprites de Vida: " + e.getMessage());
            imagenCorazonLleno = null;
            imagenCorazonVacio = null;
        }
    }
    
    // --- Métodos de Control ---
    
    /**
     * Quita una cantidad de vida al personaje.
     * @param daño Cantidad de vida a restar.
     */
    public void quitarVida(int daño) {
        this.vidaActual -= daño;
        if (this.vidaActual < 0) {
            this.vidaActual = 0;
        }
    }

    // Sobrecarga para mantener la compatibilidad con el código existente que solo llama quitarVida()
    public void quitarVida() {
        this.quitarVida(1);
    }

    public void agregarVida() {
        if (vidaActual < vidaMaxima) {
            vidaActual++;
        }
    }
    
    public boolean estaMuerto() {
        return vidaActual <= 0;
    }

    // --- Método de Dibujo ---
    
    public void dibujar(Graphics g, int x, int y) {
        
        for (int i = 0; i < vidaMaxima; i++) {
            Image imagenADibujar;
            // Calcular la posición X de cada corazón
            int xPos = x + i * (TAMAÑO_CORAZON + ESPACIO); 
            
            if (i < vidaActual) {
                imagenADibujar = imagenCorazonLleno;
            } else {
                imagenADibujar = imagenCorazonVacio;
            }
            
            if (imagenADibujar != null) {
                // Dibujar el sprite cargado
                g.drawImage(imagenADibujar, xPos, y, TAMAÑO_CORAZON, TAMAÑO_CORAZON, null);
            } else {
                // Dibujar rectángulos de marcador si los sprites fallan (fallback)
                g.setColor(i < vidaActual ? Color.RED : Color.DARK_GRAY);
                g.fillRect(xPos, y, TAMAÑO_CORAZON, TAMAÑO_CORAZON);
                g.setColor(Color.WHITE);
                g.drawRect(xPos, y, TAMAÑO_CORAZON, TAMAÑO_CORAZON);
            }
        }
    }
    
    // --- Getters ---
    
    public int getVidaActual() {
        return vidaActual;
    }
    
    public int getVidaMaxima() {
        return vidaMaxima;
    }
}