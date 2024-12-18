import java.io.IOException;
import java.util.Arrays;
import javax.swing.*;
import java.awt.*;

public class ScreenPanel extends JPanel implements Runnable{
    final int ROWS = 32;                // chip8 display rows
    final int COLS = 64;                // chip8 display columns
    final int SCALE = 10;               // Pixel size
    final int WIDTH = COLS * SCALE;
    final int HEIGHT = ROWS * SCALE;

    int clockFrequency = 500;            
    chip8 current = new chip8();

    // Initialize thread for cpu clock.
    Thread cpuThread;                 
    Keyboard keyboard = new Keyboard();

    // ScreenPanel constructor.
    public ScreenPanel() {
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

    // Updates CPU and graphics at a rate of clockFreq Hz.
    @Override
    public void run() {
        // clockFreq intervals per second (in nanoseconds).
        double interval = 1000000000/clockFrequency;             

        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (cpuThread != null) {
            // Takes current time in Nanoseconds, determines if delta < / > interval.
            currentTime = System.nanoTime();                
            delta += (currentTime - lastTime) / interval;   
            lastTime = currentTime;                         

            // If Delta > 1, drawInterval is passed and update/repaint is called.
            // Else (Delta < 1), loop continues as current interval is not over.
            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }
        
        
            
        
        }
    }

    // Cycles CPU, updates timers and sound.
    public void update() {
        //if (keyboard.up == true) {
            //System.out.println("up");
        //}
        //else if (keyboard.down == true) {
            //System.out.println("down");
        //}
        //else if (keyboard.left == true) {
            //System.out.println("left");
        //}
        //else if (keyboard.right == true) {
            //System.out.println("right");
        //}
        current.setKeys(keyboard.keys);
        current.cycleTimers();              // MOVE THIS TO ITS OWN 60HZ DECREMENT
        current.cycle();

        System.out.println(Arrays.toString(current.getKeys()));
        //current.updateTimers();
        //current.updateSound();
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
            loadRom("roms/5-Breakout (Brix hack) [David Winter, 1997].ch8");
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