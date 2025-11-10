import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;
import javax.swing.ImageIcon;

public class Personaje {

    // --- Estado de Posición y Dimensiones ---
    private int x;
    private int y;
    private final int ancho = 170;
    private final int alto = 170;
    private Image spriteActual;
    
    // --- Estado de Movimiento y Acción ---
    private boolean mirandoDerecha = true;
    private boolean isAtacando = false; 
    private boolean estaRalentizado = false; 
    private boolean estaMoviendose = false;
    
    // 🌟 --- ESTADO DE HABILIDADES DESBLOQUEABLES (NUEVO) ---
    private int saltosDisponibles = 1; // Contador actual de saltos
    private int saltosMaximos = 1;      // Límite de saltos (1 o 2)
    private int velocidadBase = 4;      // Velocidad base (4 o 6)
    
    // --- Animación y Sprites ---
    private final String SPRITE_MOVIMIENTO = "/img/caballeroC.gif";
    private final String SPRITE_REPOSO = "/img/caballero1.gif"; 
    private final String SPRITE_ATAQUE = "/img/caballero_ataque.gif";

    public Personaje(int x, int y) {
        this.x = x;
        this.y = y;
        cargarSprite("reposo"); 
    }

    // ----------------------------------------------------------------------
    // --- MÉTODOS DE HABILIDAD DESBLOQUEABLE (LLAMADOS DESDE TABLERO) ---
    // ----------------------------------------------------------------------

    /**
     * 👑 Desbloquea la habilidad de Doble Salto (otorgada por la Gárgola).
     */
    public void desbloquearDobleSalto() {
        if (this.saltosMaximos < 2) {
            this.saltosMaximos = 2;
            this.saltosDisponibles = 2;
            System.out.println("✅ Habilidad desbloqueada: ¡Doble Salto!");
        }
    }

    /**
     * 👑 Aumenta permanentemente la velocidad de movimiento base (otorgada por la Neblina).
     */
    public void aumentarVelocidad(int incremento) {
        if (this.velocidadBase < 6) { 
             this.velocidadBase += incremento; 
             System.out.println("✅ Habilidad desbloqueada: Velocidad aumentada a " + this.velocidadBase);
        }
    }

    /**
     * Resetea los saltos disponibles al tocar el suelo.
     * (Llamado desde Tablero cuando enAire es false).
     */
    public void resetearSaltos() {
        this.saltosDisponibles = this.saltosMaximos;
    }
    
    /**
     * Consume un salto.
     * (Llamado desde Tablero al ejecutar un salto).
     */
    public void usarSalto() {
        this.saltosDisponibles--;
    }
    
    // ----------------------------------------------------------------------
    // --- LÓGICA DE DIBUJO Y ANIMACIÓN ---
    // ----------------------------------------------------------------------

    public void dibujar(Graphics g, boolean esInvulnerable, long currentTime, ImageObserver observer) {
        // Lógica de parpadeo por invulnerabilidad
        if (esInvulnerable && (currentTime / 100) % 2 == 0) {
            return; 
        }
        
        // Lógica de reflejo (flip)
        if (!mirandoDerecha) {
            // Dibuja reflejado
            g.drawImage(spriteActual, x + ancho, y, -ancho, alto, observer);
        } else {
            // Dibuja normal
            g.drawImage(spriteActual, x, y, ancho, alto, observer);
        }
    }
    
    public void actualizarEstado(Controles controles) {
        // 1. Actualizar Dirección (MirandoDerecha)
        if (controles.isMoviendoDerecha()) {
            this.mirandoDerecha = true;
            this.estaMoviendose = true;
        } else if (controles.isMoviendoIzquierda()) {
            this.mirandoDerecha = false;
            this.estaMoviendose = true;
        } else {
            this.estaMoviendose = false;
        }

        // 2. Seleccionar el Sprite (Jerarquía de Animación)
        if (isAtacando) {
            cargarSprite("atacando"); 
        } else if (estaMoviendose) {
            cargarSprite("movimiento"); 
        } else {
            // Reposo
            cargarSprite("reposo"); 
        }
    }
    
    private void cargarSprite(String estado) {
        String path;
        
        if (estado.equals("atacando")) {
            path = SPRITE_ATAQUE;
        } else if (estado.equals("movimiento")) {
            path = SPRITE_MOVIMIENTO;
        } else {
            path = SPRITE_REPOSO; 
        }
        
        try {
            Image nuevaImagen = new ImageIcon(getClass().getResource(path)).getImage();
            
            if (nuevaImagen == null) {
                System.err.println("Advertencia: No se encontró el sprite para '" + estado + "'. Usando " + SPRITE_MOVIMIENTO);
                path = SPRITE_MOVIMIENTO; 
                nuevaImagen = new ImageIcon(getClass().getResource(path)).getImage();
            }
            
            if (spriteActual != nuevaImagen) { // Solo actualizar si el sprite ha cambiado
                spriteActual = nuevaImagen;
            }
        } catch (Exception e) {
            System.err.println("FATAL ERROR: No se puede cargar el recurso: " + path);
            System.err.println("Verifica que la ruta sea correcta y que los archivos existan.");
        }
    }
    
    // ----------------------------------------------------------------------
    // --- MÉTODOS GETTERS/SETTERS ---
    // ----------------------------------------------------------------------
    
    public Image getImagen() { return spriteActual; }
    public Rectangle getHitbox() { return new Rectangle(x, y, ancho, alto); }
    
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }
    
    public boolean estaAtacando() { return isAtacando; }
    public void setAtacando(boolean isAtacando) { this.isAtacando = isAtacando; }

    public boolean isMirandoDerecha() { return mirandoDerecha; }
    
    public boolean getEstaRalentizado() { return estaRalentizado; }
    public void setEstaRalentizado(boolean estaRalentizado) { this.estaRalentizado = estaRalentizado; }
    
    public boolean estaMoviendose() { return estaMoviendose; }
    
    // --- Getters para Habilidades ---
    public int getVelocidadBase() { return velocidadBase; }
    public int getSaltosDisponibles() { return saltosDisponibles; }
    public int getSaltosMaximos() { return saltosMaximos; }
}