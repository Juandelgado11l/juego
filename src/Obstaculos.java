import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Color;
import javax.swing.ImageIcon;

public class Obstaculos {

    // Propiedades de Posición y Visuales
    private int x;
    private int y;
    private int ancho;    
    private int alto;    
    private Image sprite; 
    private String nombreImagen; 

    // Propiedades de Juego
    private int vidaMaxima;
    private int vidaActual;
    private boolean esMovil = false;
    private int velocidadMovil = 2; 

    // PROPIEDADES AÑADIDAS PARA JEFES
    private boolean esJefe = false;
    private boolean mirandoDerecha = true; // Dirección por defecto para proyectiles/animaciones

    // Cooldown de Ataque
    private long tiempoUltimoAtaque = 0;
    // COOLDOWN DEFAULT (Enredadera)
    private long COOLDOWN_ATAQUE = 1200; 
    
    
    private boolean estaAtacandoAnimacion = false;
    private long finAnimacionAtaque = 0;

    private boolean activo = true;

    // CONSTRUCTOR 1 - Obstáculo fijo o móvil sin vida
    public Obstaculos(int x, int y, String rutaSprite, boolean esMovil) {
        this.x = x;
        this.y = y;
        this.nombreImagen = rutaSprite;
        this.esMovil = esMovil;
        cargarSprite(rutaSprite);

        if (esMovil) {
            this.ancho = 100;
            this.alto = 100;
        } else {
            this.ancho = 200;
            this.alto = 200;
        }

        this.vidaMaxima = 0; 
        this.vidaActual = 0;
    }

    // CONSTRUCTOR 2 - Obstáculos destructibles (árbol, enredadera) 
    public Obstaculos(int x, int y, String rutaSprite, int vidaInicial, boolean esMovil) {
        this.x = x;
        this.y = y;
        this.nombreImagen = rutaSprite;
        this.esMovil = esMovil;
        cargarSprite(rutaSprite);

        if (esMovil) {
            this.ancho = 100;
            this.alto = 100;
        } else {
            this.ancho = 200;
            this.alto = 200;
        }

        this.vidaMaxima = vidaInicial;
        this.vidaActual = vidaInicial;
    }
    
    // CONSTRUCTOR 3 - Obstáculo con vida (Jefes, Objetos destructibles)
    // AÑADIDO: esJefe para marcar explícitamente a los Jefes
    public Obstaculos(int x, int y, String rutaSprite, int vidaInicial, boolean esMovil, int ancho, int alto, boolean esJefe) {
        this.x = x;
        this.y = y;
        this.nombreImagen = rutaSprite;
        this.esMovil = esMovil;
        cargarSprite(rutaSprite);
        this.ancho = ancho;
        this.alto = alto;

        this.vidaMaxima = vidaInicial;
        this.vidaActual = vidaInicial;
        this.esJefe = esJefe; // Establecer la bandera de Jefe

       
    }

    public boolean esJefe(int numero) {
    return numero == 3 || numero == 6 || numero == 9;
    }

    public void setY(int y) {
        this.y = y;
    }

    private void cargarSprite(String rutaSprite) {
        try {
            this.sprite = new ImageIcon(getClass().getResource(rutaSprite)).getImage();
        } catch (Exception e) {
            System.err.println("Error cargando sprite para Obstáculo: " + rutaSprite + " - " + e.getMessage());
            this.sprite = new ImageIcon().getImage(); 
        }
    }

    public void dibujar(Graphics g) {
        g.drawImage(sprite, x, y, ancho, alto, null);

        // Animación de ataque (un simple círculo rojo parpadeante)
        if (estaAtacandoAnimacion && System.currentTimeMillis() < finAnimacionAtaque) {
            g.setColor(new Color(255, 0, 0, 100));
            g.fillOval(x - 10, y - 10, ancho + 20, alto + 20);
        } else {
            estaAtacandoAnimacion = false;
        }
    }

    public void dibujarBarraVida(Graphics g) {
        if (vidaMaxima > 0) {
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
    

    // Movimiento de scroll propio del obstáculo (SOLO MÓVILES)
    public void mover() {
        if (esMovil) {
            x -= velocidadMovil;
        }
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, ancho, alto);
    }

    public void recibirDano(int dano) {
        if (vidaMaxima > 0) {
            vidaActual -= dano;
            if (vidaActual < 0) vidaActual = 0;
        }
    }

    public boolean estaDestruido() {
        return vidaActual <= 0 && vidaMaxima > 0;
    }

    public boolean tieneVida() {
        return vidaMaxima > 0;
    }

    public boolean puedeAtacar() {
        return System.currentTimeMillis() > tiempoUltimoAtaque + COOLDOWN_ATAQUE;
    }

    public void iniciarCooldown() {
        tiempoUltimoAtaque = System.currentTimeMillis();
    }

    public void iniciarAtaqueAnimacion() {
        estaAtacandoAnimacion = true;
        finAnimacionAtaque = System.currentTimeMillis() + 300; 
    }

    // Getters y setters
    public boolean esMovil() {
        return esMovil;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

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
    
    // MÉTODOS AÑADIDOS
    public boolean isJefe() {
        return esJefe;
    }
    
    public boolean isMirandoDerecha() {
        return mirandoDerecha;
    }

    public void setMirandoDerecha(boolean mirandoDerecha) {
        this.mirandoDerecha = mirandoDerecha;
    }
}