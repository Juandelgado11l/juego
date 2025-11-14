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
    
    // 🌟 NUEVAS VARIABLES NECESARIAS PARA EL CONTROL EN TABLERO
    private boolean estaSaltando = false; // <-- AÑADIDO
    private int velocidadX = 0;         // <-- AÑADIDO

    // 🌟 --- Estado de Habilidades Desbloqueables ---
    private int saltosDisponibles = 1; 
    private int saltosMaximos = 1; 
    private int velocidadBase = 5; 

    // --- Estado de Vida ---
    private int vida = 3; // Vida actual en corazones
    private final int vidaMaxima = 3; // Máximo de corazones

    // --- Rosas recogidas ---
    private int rosasRecogidas = 0;

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
    // --- MÉTODOS DE HABILIDAD DESBLOQUEABLE ---
    // ----------------------------------------------------------------------
    public void desbloquearDobleSalto() {
        if (this.saltosMaximos < 2) {
            this.saltosMaximos = 2;
            this.saltosDisponibles = 2;
            System.out.println("✅ Habilidad desbloqueada: ¡Doble Salto!");
        }
    }

    public void aumentarVelocidad(int incremento) {
        if (this.velocidadBase < 6) {
            this.velocidadBase += incremento;
            System.out.println("✅ Habilidad desbloqueada: Velocidad aumentada a " + this.velocidadBase);
        }
    }

    public void resetearSaltos() {
        this.saltosDisponibles = this.saltosMaximos;
    }

    public void usarSalto() {
        this.saltosDisponibles--;
    }

    // ----------------------------------------------------------------------
    // --- LÓGICA DE DIBUJO Y ANIMACIÓN ---
    // ----------------------------------------------------------------------
    public void dibujar(Graphics g, boolean esInvulnerable, long currentTime, ImageObserver observer) {
        if (esInvulnerable && (currentTime / 100) % 2 == 0) return;

        if (!mirandoDerecha) {
            g.drawImage(spriteActual, x + ancho, y, -ancho, alto, observer);
        } else {
            g.drawImage(spriteActual, x, y, ancho, alto, observer);
        }
    }

    public void actualizarEstado(Controles controles) {
        if (controles.isMoviendoDerecha()) {
            this.mirandoDerecha = true;
            this.estaMoviendose = true;
        } else if (controles.isMoviendoIzquierda()) {
            this.mirandoDerecha = false;
            this.estaMoviendose = true;
        } else {
            this.estaMoviendose = false;
        }

        if (isAtacando) {
            cargarSprite("atacando");
        } else if (estaMoviendose) {
            cargarSprite("movimiento");
        } else {
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

            if (spriteActual != nuevaImagen) {
                spriteActual = nuevaImagen;
            }
        } catch (Exception e) {
            System.err.println("FATAL ERROR: No se puede cargar el recurso: " + path);
            System.err.println("Verifica que la ruta sea correcta y que los archivos existan.");
        }
    }

    // ----------------------------------------------------------------------
    // --- MÉTODOS GETTERS/SETTERS Y CORRECCIONES PARA EL CASTILLO ---
    // ----------------------------------------------------------------------
    public Image getImagen() { return spriteActual; }
    
    // CORRECCIÓN 1: Método getRect() requerido por Tablero para colisiones
    public Rectangle getRect() { return new Rectangle(x, y, ancho, alto); } 
    
    // Método getHitbox() para mantener el código que usa este nombre (opcional)
    public Rectangle getHitbox() { return new Rectangle(x, y, ancho, alto); }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getAncho() { return ancho; }
    public int getAlto() { return alto; }
    
    // CORRECCIÓN 2: Setter setVelocidadX() requerido por Tablero
    public int getVelocidadX() { return velocidadX; }
    public void setVelocidadX(int velocidadX) { this.velocidadX = velocidadX; }
    

    public boolean estaAtacando() { return isAtacando; }
    public void setAtacando(boolean isAtacando) { this.isAtacando = isAtacando; }

    public boolean isMirandoDerecha() { return mirandoDerecha; }

    // CORRECCIÓN 3: Métodos setSaltando() y estaSaltando() requerido por Tablero
    public boolean estaSaltando() { return estaSaltando; }
    public void setSaltando(boolean estaSaltando) { this.estaSaltando = estaSaltando; } // <-- CORREGIDO
    
    public boolean getEstaRalentizado() { return estaRalentizado; }
    public void setEstaRalentizado(boolean estaRalentizado) { this.estaRalentizado = estaRalentizado; }

    public boolean estaMoviendose() { return estaMoviendose; }

    // --- Vida ---
    public int getVida() { return vida; }
    public void setVida(int vida) {
        if (vida < 0) this.vida = 0;
        else if (vida > vidaMaxima) this.vida = vidaMaxima;
        else this.vida = vida;
    }
    public void perderCorazon() { setVida(vida - 1); }
    public void ganarCorazon() { setVida(vida + 1); }
    public int getVidaMaxima() { return vidaMaxima; }

    // --- Rosas recogidas ---
    public int getRosasRecogidas() { return rosasRecogidas; }
    public void setRosasRecogidas(int rosasRecogidas) {
        if (rosasRecogidas < 0) this.rosasRecogidas = 0;
        else this.rosasRecogidas = rosasRecogidas;
    }
    public void recogerRosa() { rosasRecogidas++; }
    public void perderRosas(int cantidad) { setRosasRecogidas(rosasRecogidas - cantidad); }

    // --- Getters para habilidades ---
    public int getVelocidadBase() { return velocidadBase; }
    public int getSaltosDisponibles() { return saltosDisponibles; }
    public int getSaltosMaximos() { return saltosMaximos; }

    // --- Setters para carga de partida ---
    public void setVelocidadBase(int nuevaVelocidad) { this.velocidadBase = nuevaVelocidad; }
    public void setSaltosMaximos(int maxSaltos) { 
        this.saltosMaximos = maxSaltos; 
        this.saltosDisponibles = maxSaltos; 
    }
}