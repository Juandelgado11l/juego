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
    
    // ----------------------------------------------------------------------
    // 💾 ADICIONES REQUERIDAS PARA CARGAR PARTIDA 💾
    // ----------------------------------------------------------------------

    /**
     * 💾 Establece la vida actual del personaje (utilizado al cargar una partida).
     * @param vida La cantidad de vida a restaurar.
     */
    public void setVidaActual(int vida) {
        // Asegura que la vida esté dentro del rango [0, vidaMaxima]
        if (vida >= 0) {
            this.vidaActual = Math.min(vida, this.vidaMaxima);
        } else {
            this.vidaActual = 0;
        }
    }
    
    /**
     * Establece la vida máxima (útil si la vida máxima cambia con mejoras).
     * @param vida La nueva vida máxima.
     */
    public void setVidaMaxima(int vida) {
        this.vidaMaxima = vida;
        // Si la vida actual excede la nueva máxima, la ajustamos.
        if (this.vidaActual > this.vidaMaxima) {
            this.vidaActual = this.vidaMaxima;
        }
    }
    
    // ----------------------------------------------------------------------
    // --- Métodos de Control ---
    // ----------------------------------------------------------------------
    
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