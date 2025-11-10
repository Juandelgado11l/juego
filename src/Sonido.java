import javax.sound.sampled.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class Sonido {

    private Map<String, Clip> sonidos;
    private String[][] rutas;

    public Sonido() {
        sonidos = new HashMap<>();

        rutas = new String[][] {
            {"correr", "/sonido/correr.wav"},
            {"juego", "/sonido/juego.wav"},
            {"saltar", "/sonido/saltar.wav"},
            {"golpe", "/sonido/golpe.wav"},
            {"risa", "/sonido/risa.wav"}
        };

        cargarSonidos();
    }

    private void cargarSonidos() {
        for (String[] r : rutas) {
            try {
                URL url = getClass().getResource(r[1]);

                if (url == null) {
                    System.out.println("❌ No se encontró: " + r[1]);
                    continue;
                }

                AudioInputStream audio = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(audio);

                sonidos.put(r[0], clip);

            } catch (Exception e) {
                System.out.println("⚠ Error cargando sonido " + r[1] + ": " + e.getMessage());
            }
        }
    }

    public void reproducirSonido(String pista) {
        Clip clip = sonidos.get(pista);
        if (clip != null) {
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public void loopSonido(String pista) {
        Clip clip = sonidos.get(pista);
        if (clip != null) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }
}
