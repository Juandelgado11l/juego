import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import javax.swing.ImageIcon;

public class Personaje {

    // --- Estado de Posición y Dimensiones ---
    private int x;
    private int y;
    private int ancho = 100;
    private int alto = 100;
    private Image spriteActual;
    
    // --- Estado de Movimiento y Acción ---
    private boolean mirandoDerecha = true;
    private boolean isAtacando = false; 
    private boolean estaRalentizado = false; 
    
    // --- Animación y Sprites ---
    private int frameActual = 0;
    private int numFramesCaminar = 1; // Ajustado a 1 para usar caballeroC.gif (un solo archivo)
    private long tiempoUltimoFrame = 0;
    private final int VELOCIDAD_ANIMACION = 100; 
    
    // Constantes de Sprite (basadas en tu carpeta 'img')
    private final String SPRITE_CAMINAR = "/img/caballeroC.gif";
    private final String SPRITE_ATAQUE = "/img/caballero_ataque.gif";

    public Personaje(int x, int y) {
        this.x = x;
        this.y = y;
        // La llamada inicial es segura ahora
        cargarSprite("caminando"); 
    }

    // --- Colisión y Imagen ---
    
    public Image getImagen() {
        return spriteActual;
    }

    public Rectangle getHitbox() {
        return new Rectangle(x, y, ancho, alto);
    }
    
    // --- Lógica de Dibujo y Estado ---

    public void dibujar(Graphics g, boolean esInvulnerable, long currentTime, ImageObserver observer) {
        if (esInvulnerable && (currentTime / 100) % 2 == 0) {
            return; 
        }
        
        if (!mirandoDerecha) {
            g.drawImage(spriteActual, x + ancho, y, -ancho, alto, observer);
        } else {
            g.drawImage(spriteActual, x, y, ancho, alto, observer);
        }
    }
    
    public void actualizarEstado() {
        if (isAtacando) {
            cargarSprite("atacando"); 
        } else {
            // Si no está atacando, usa el sprite de caminar/espera
            cargarSprite("caminando"); 
        }
        
        // La lógica de avance de frame queda simple o se elimina si solo usas un GIF por estado.
        // Si caballeroC.gif fuera un GIF animado, esta lógica es innecesaria.
        long currentTime = System.currentTimeMillis();
        if (currentTime - tiempoUltimoFrame > VELOCIDAD_ANIMACION) {
            // Si tuvieras más frames como caballeroC_1.gif, aquí harías la iteración.
            // frameActual = (frameActual + 1) % numFramesCaminar;
            tiempoUltimoFrame = currentTime;
        }
    }
    
    private void cargarSprite(String estado) {
        String path;
        
        if (estado.equals("atacando")) {
            path = SPRITE_ATAQUE;
        } else {
            // Este es el camino que toma el constructor y el estado normal
            path = SPRITE_CAMINAR; 
        }
        
        try {
            // 🚨 CORRECCIÓN CLAVE: Usamos la ruta fija que coincide con tu archivo.
            spriteActual = new ImageIcon(getClass().getResource(path)).getImage();
            
            if (spriteActual == null) {
                // Esto ayuda a atrapar errores de ruta incluso si ImageIcon no lanza la excepción esperada.
                throw new NullPointerException("Recurso no encontrado: " + path);
            }
            
        } catch (Exception e) {
            System.err.println("FATAL ERROR: No se puede cargar el recurso inicial: " + path);
            System.err.println("Verifica que el archivo exista en /img/ y que la carpeta 'img' esté en el Classpath.");
            // Si quieres un sprite de emergencia:
            // spriteActual = new ImageIcon().getImage(); 
        }
    }
    
    // --- Métodos Getters/Setters ---
    
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }
    
    public boolean estaAtacando() { return isAtacando; }
    public void setAtacando(boolean isAtacando) { this.isAtacando = isAtacando; }

    public boolean isMirandoDerecha() { return mirandoDerecha; }
    public void setMirandoDerecha(boolean mirandoDerecha) { this.mirandoDerecha = mirandoDerecha; }

    public boolean getEstaRalentizado() { return estaRalentizado; }
    public void setEstaRalentizado(boolean estaRalentizado) { this.estaRalentizado = estaRalentizado; }
}