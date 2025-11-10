import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Color;
import javax.swing.ImageIcon;

public class Obstaculos {
    
    // --- Propiedades de Posición y Visuales ---
    private int x;
    private int y;
    private int ancho = 180;  
    private int alto = 180;  
    private Image sprite; 
    private String nombreImagen; 

    // --- Propiedades de Juego ---
    private int vidaMaxima;
    private int vidaActual;
    private boolean esMovil = false;
    private int velocidadMovil = 2; 
    
    // Cooldown de Ataque
    private long tiempoUltimoAtaque = 0;
    private final long COOLDOWN_ATAQUE = 1200; 
    private boolean estaAtacandoAnimacion = false;
    private long finAnimacionAtaque = 0;

    // ==========================================================
    // CONSTRUCTOR 1 (3 ARGUMENTOS) - Para NO DESTRUCTIBLES (Fantasma, Planta Carnívora)
    // ==========================================================
    public Obstaculos(int x, int y, String rutaSprite) {
        this.x = x;
        this.y = y;
        this.nombreImagen = rutaSprite;
        cargarSprite(rutaSprite);
        
        // El fantasma es el único móvil aquí
        this.esMovil = rutaSprite.contains("fantasma");
        
        // ✅ Fantasma y Planta Carnívora no son destructibles
        this.vidaMaxima = 0; 
        this.vidaActual = 0;
    }
    
    // ==========================================================
    // CONSTRUCTOR 2 (4 ARGUMENTOS) - Para DESTRUCTIBLES (Árbol, Enredadera)
    // ==========================================================
    public Obstaculos(int x, int y, String rutaSprite, int vidaInicial) {
        this.x = x;
        this.y = y;
        this.nombreImagen = rutaSprite;
        cargarSprite(rutaSprite);
        
        this.esMovil = rutaSprite.contains("fantasma"); // Aunque estos no deberían ser fantasmas
        
        // ✅ Destructibles con vida asignada
        this.vidaMaxima = vidaInicial;
        this.vidaActual = vidaInicial;
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
    
    // DENTRO DE LA CLASE Obstaculos.java

        public void dibujarBarraVida(Graphics g) {
     // ✅ CORRECCIÓN: Ahora se dibuja si vidaMaxima > 0, independientemente de la vidaActual.
      if (vidaMaxima > 0) { 
            int barraAncho = ancho;
         int barraAlto = 5;
            int barraY = y - 10;
        
         // Dibuja el fondo rojo (la vida máxima)
         g.setColor(Color.RED);
         g.fillRect(x, barraY, barraAncho, barraAlto);
        
          // Dibuja la parte verde (la vida actual)
          g.setColor(Color.GREEN);
          int vidaAncho = (int) ((double) vidaActual / vidaMaxima * barraAncho);
          g.fillRect(x, barraY, vidaAncho, barraAlto);

          // Dibuja el borde
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
        if (vidaMaxima > 0) {
            this.vidaActual -= dano;
            if (this.vidaActual < 0) this.vidaActual = 0;
        }
    }
    
    public boolean estaDestruido() {
        return vidaActual <= 0 && vidaMaxima > 0;
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

    public Image getImagen() {
        return sprite;
    }
    
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