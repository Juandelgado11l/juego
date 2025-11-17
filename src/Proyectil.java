import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.ImageIcon;

public class Proyectil {
	
	private int x, y;
	private int ancho, alto;
	private int velocidadX, velocidadY;
	private Image imagen;
	private boolean activo = true; 
   

	// CONSTRUCTOR principal, ahora incluye el valor de daño
	public Proyectil(int x, int y, String rutaImagen, int velocidadX, int velocidadY, int ancho, int alto, int dano) {
		this.x = x;
		this.y = y;
		this.velocidadX = velocidadX;
		this.velocidadY = velocidadY;
		this.ancho = ancho;
		this.alto = alto;
		
		try {
			this.imagen = new ImageIcon(getClass().getResource(rutaImagen)).getImage();
		} catch (Exception e) {
			System.err.println(" Error cargando imagen de proyectil (" + rutaImagen + "): Usando fallback rojo.");
			this.imagen = null;
		}
	}
    
    // Sobrecarga del constructor para mantener compatibilidad si no se pasa el daño
    public Proyectil(int x, int y, String rutaImagen, int velocidadX, int velocidadY, int ancho, int alto) {
        this(x, y, rutaImagen, velocidadX, velocidadY, ancho, alto, 5); // Daño por defecto de 5
    }

	// LÓGICA DE MOVIMIENTO 
	public void mover(int limitePantallaX, int limitePantallaY) {
		this.x += velocidadX;
		this.y += velocidadY;
		
		// Desactiva si sale de la pantalla
		if (x < -ancho || x > limitePantallaX || y < -alto || y > limitePantallaY) {
			this.activo = false;
		}
	}
	
	// DIBUJO 
	public void dibujar(Graphics g) {
		if (activo) {
			if (imagen != null) {
				g.drawImage(imagen, x, y, ancho, alto, null);
			} else {
				// Dibujar un rectángulo rojo como fallback
				g.setColor(Color.RED);
				g.fillRect(x, y, ancho, alto);
			}
		}
	}

    // Nuevo: Método para manejar el impacto o colisión
    public void impactar() {
        this.activo = false;
        // Aquí se pueden añadir efectos de sonido o partículas si fuera necesario
    }
	
	// GETTERS Y SETTERS 
	public int getX() { return x; }
	public int getY() { return y; }
	public int getAncho() { return ancho; }
	public int getAlto() { return alto; }
	public Rectangle getHitbox() { return new Rectangle(x, y, ancho, alto); }
	public boolean isActivo() { return activo; }	
	public void setX(int x) { this.x = x; }
	public void setY(int y) { this.y = y; }
	public void setActivo(boolean activo) { this.activo = activo; }
	public void setVelocidadX(int velocidadX) { this.velocidadX = velocidadX; }
	public void setVelocidadY(int velocidadY) { this.velocidadY = velocidadY; }
}