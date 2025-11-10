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
    
    public Vida() {
        this(3); 
    }

    public Vida(int vidaMaxima) {
        this.vidaMaxima = vidaMaxima;
        this.vidaActual = vidaMaxima;
        
        try {
            imagenCorazonLleno = new ImageIcon(getClass().getResource("img/vida.jpg")).getImage();
            imagenCorazonVacio = new ImageIcon(getClass().getResource("img/corazonRoto.jpg")).getImage();
        } catch (Exception e) {
            imagenCorazonLleno = null;
            imagenCorazonVacio = null;
        }
    }
    
    public void quitarVida() {
        if (vidaActual > 0) {
            vidaActual--;
        }
    }

    public void agregarVida() {
        if (vidaActual < vidaMaxima) {
            vidaActual++;
        }
    }
    
    public boolean estaMuerto() {
        return vidaActual <= 0;
    }

    public void dibujar(Graphics g, int x, int y) {
        
        for (int i = 0; i < vidaMaxima; i++) {
            Image imagenADibujar;
            int xPos = x + i * (TAMAÑO_CORAZON + ESPACIO); 
            
            if (i < vidaActual) {
                imagenADibujar = imagenCorazonLleno;
            } else {
                imagenADibujar = imagenCorazonVacio;
            }
            
            if (imagenADibujar != null) {
                g.drawImage(imagenADibujar, xPos, y, TAMAÑO_CORAZON, TAMAÑO_CORAZON, null);
            } else {
                g.setColor(i < vidaActual ? Color.RED : Color.DARK_GRAY);
                g.fillRect(xPos, y, TAMAÑO_CORAZON, TAMAÑO_CORAZON);
                g.setColor(Color.WHITE);
                g.drawRect(xPos, y, TAMAÑO_CORAZON, TAMAÑO_CORAZON);
            }
        }
    }
    
    public int getVidaActual() {
        return vidaActual;
    }
    
    public int getVidaMaxima() {
        return vidaMaxima;
    }
}