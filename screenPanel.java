import java.io.IOException;
import javax.swing.*;
import java.awt.*;

public class screenPanel extends JPanel implements Runnable{
    final int ROWS = 32;                // chip8 display rows
    final int COLS = 64;                // chip8 display columns
    final int SCALE = 10;               // Pixel size
    final int WIDTH = COLS * SCALE;
    final int HEIGHT = ROWS * SCALE;

    int clockFreq = 120;                // Clock Frequency (Hz)
    chip8 current = new chip8();        // Initialize chip8
    Thread cpuThread;                   // Initialize thread for timer
    Keyboard keyboard = new Keyboard();  // Keyboard Handling

    // screenPanel constructor.
    public screenPanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.addKeyListener(keyboard);
        this.setFocusable(true);
        startEmulator();
    }

    // Initializes cpu thread for timers.
    public void StartCPUThread() {
        cpuThread = new Thread(this);
        cpuThread.start();
    }

    @Override
    public void run() { // Thank you RyiSnow
        double drawInterval = 1000000000/clockFreq;             // ~clockFreq times per second (Nanosecond conversion)

        double delta = 0;
        long lastTime = System.nanoTime();                      // Last system time in nanoseconds
        long currentTime;

        while (cpuThread != null) {
            currentTime = System.nanoTime();                    // Current time in nanoseconds
            delta += (currentTime - lastTime) / drawInterval;   // Determine if delta < or > drawInterval
            lastTime = currentTime;                             // Updates lastTime

            // If Delta > 1, drawInterval is passed and update/repaint is called
            // Else (Delta < 1), loop continues as current interval is not over
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        }
    }

    // Cycles CPU, updates timers and sound.
    public void update() {
        if (keyboard.up == true) {
            System.out.println("up");
        }
        else if (keyboard.down == true) {
            System.out.println("down");
        }
        else if (keyboard.left == true) {
            System.out.println("left");
        }
        else if (keyboard.right == true) {
            System.out.println("right");
        }
        
        current.cycle();
        current.updateTimers();
        current.updateSound();
    }
    
    // Gets each pixel on chip8 to determine if on/off, and paints accordingly.
    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                
                if (current.getPixel(y, x) == 1) {
                    g2D.setColor(Color.WHITE);
                    g2D.fillRect(x*SCALE, y*SCALE, SCALE, SCALE);
                }
                if (current.getPixel(y, x) == 0) {
                    g2D.setColor(Color.BLACK);
                    g2D.fillRect(x*SCALE, y*SCALE, SCALE, SCALE);
                }
            }
        }
    }

    // Starts emulator by loading rom.
    public void startEmulator() {
        try {
            loadRom("roms/4-flags.ch8");
        }
        catch (IOException e) {
            System.out.println("Bad Rom");
        }
    }    

    // Loads a rom file form the roms folder into memory, using chip8's load function.
     public void loadRom(String rom) throws IOException{
        current.load(rom);       
    }
}