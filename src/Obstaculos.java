import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Color;
import javax.swing.ImageIcon;

public class Obstaculos {
    
    // --- Propiedades de Posición y Visuales ---
    private int x;
    private int y;
    private int ancho = 80;  
    private int alto = 80;   
    private Image sprite; // <-- Este es el objeto Image que necesitamos devolver
    private String nombreImagen; 

    // --- Propiedades de Juego ---
    private int vidaMaxima;
    private int vidaActual;
    private boolean esMovil = false;
    private int velocidadMovil = 2; 
    
    // Cooldown de Ataque
    private long tiempoUltimoAtaque = 0;
    private final long COOLDOWN_ATAQUE = 1000; 
    private boolean estaAtacandoAnimacion = false;
    private long finAnimacionAtaque = 0;

    public Obstaculos(int x, int y, String rutaSprite) {
        this.x = x;
        this.y = y;
        this.nombreImagen = rutaSprite;
        cargarSprite(rutaSprite);
        
        if (rutaSprite.contains("fantasma")) {
            this.esMovil = true;
            this.vidaMaxima = 1;
        } else if (rutaSprite.contains("plantas_carnivoras") || rutaSprite.contains("enredaderas")) {
            this.vidaMaxima = 3;
        } else {
            this.vidaMaxima = 1;
        }
        
        this.vidaActual = this.vidaMaxima;
    }

    // --- Métodos de Dibujo y Actualización ---
    private void cargarSprite(String rutaSprite) {
        try {
            this.sprite = new ImageIcon(getClass().getResource(rutaSprite)).getImage();
        } catch (Exception e) {
            System.err.println("Error cargando sprite para Obstáculo: " + e.getMessage());
            this.sprite = new ImageIcon().getImage(); 
        }
    }
    
    public void dibujar(Graphics g) {
        g.drawImage(sprite, x, y, ancho, alto, null);
        
        if (estaAtacandoAnimacion && System.currentTimeMillis() < finAnimacionAtaque) {
            g.setColor(new Color(255, 0, 0, 100));
            g.fillOval(x - 10, y - 10, ancho + 20, alto + 20);
        } else {
            estaAtacandoAnimacion = false;
        }
    }
    
    public void dibujarBarraVida(Graphics g) {
        if (vidaActual < vidaMaxima) {
            int barraAncho = ancho;
            int barraAlto = 5;
            int barraY = y - 10;
            
            g.setColor(Color.RED);
            g.fillRect(x, barraY, barraAncho, barraAlto);
            
            g.setColor(Color.GREEN);
            int vidaAncho = (int) ((double) vidaActual / vidaMaxima * barraAncho);
            g.fillRect(x, barraY, vidaAncho, barraAlto);
            
            g.setColor(Color.BLACK);
            g.drawRect(x, barraY, barraAncho, barraAlto);
        }
    }
    
    public void actualizarEstado() {
        // Lógica de animación o estado específico del obstáculo
    }
    
    public void mover() {
        if (esMovil) {
            x += velocidadMovil;
        }
    }

    // --- Lógica de Combate y Colisión ---

    public Rectangle getRect() {
        return new Rectangle(x, y, ancho, alto);
    }
    
    public void recibirDano(int dano) {
        this.vidaActual -= dano;
        if (this.vidaActual < 0) this.vidaActual = 0;
    }
    
    public boolean estaDestruido() {
        return vidaActual <= 0;
    }
    
    public boolean tieneVida() {
        return vidaMaxima > 0;
    }
    
    // --- Lógica de Ataque ---

    public boolean puedeAtacar() {
        return System.currentTimeMillis() > tiempoUltimoAtaque + COOLDOWN_ATAQUE;
    }
    
    public void iniciarCooldown() {
        this.tiempoUltimoAtaque = System.currentTimeMillis();
    }
    
    public void iniciarAtaqueAnimacion() {
        this.estaAtacandoAnimacion = true;
        this.finAnimacionAtaque = System.currentTimeMillis() + 300; 
    }

    // --- Getters y Setters ---

    // ✅ MÉTODO CORREGIDO: getImagen() para la colisión pixel-perfect
    public Image getImagen() {
        return sprite;
    }
    
    // ✅ MÉTODO CORREGIDO: getHitbox() solicitado
    public Rectangle getHitbox() {
        return getRect();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public int getAncho() {
        return ancho;
    }
    
    public int getAlto() {
        return alto;
    }

    public String getNombreImagen() {
        return nombreImagen;
    }
}