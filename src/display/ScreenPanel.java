package src.display;
import java.io.IOException;
import javax.swing.*;
import java.awt.*;

import src.cpu.chip8;
import src.keyboard.Keyboard;

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
        int cycleCount = 0; // Counts cycles within 500hz for 60hz timer decreasing.

        while (cpuThread != null) {
            // Takes current time in Nanoseconds, determines if delta < / > interval.
            currentTime = System.nanoTime();                
            delta += (currentTime - lastTime) / interval;   
            lastTime = currentTime;                         

            // Cycles timers roughly at 60hz, or after 8 cycles running at 500hz (~60hz)
            if (cycleCount == 8) {
                current.cycleTimers();
                cycleCount = 0;
            }
            
            // If Delta > 1, drawInterval is passed and update/repaint is called.
            // Else (Delta < 1), loop continues as current interval is not over.
            if (delta >= 1) {
                update();
                repaint();
                cycleCount++; // increments cycle counter
                delta--;
            }
        }
    }

    // Cycles CPU, updates timers and sound.
    public void update() {
        current.setKeys(keyboard.keys);
        current.cycle();
    }
    
    // Gets each pixel on chip8 to determine if on/off, and paints accordingly.
    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                
                if (current.getPixel(y, x) == 1) {
                    g2D.setColor(Color.GREEN);
                    g2D.fillRect(x*SCALE, y*SCALE, SCALE, SCALE);
                }
                if (current.getPixel(y, x) == 0) {
                    g2D.setColor(Color.BLACK);
                    g2D.fillRect(x*SCALE, y*SCALE, SCALE, SCALE);
                }
                // g2D.setColor(Color.BLACK);
                // g2D.drawRect(x*SCALE, y*SCALE, SCALE, SCALE);
            }
        }
    }

    // Starts emulator by loading rom.
    public void startEmulator() {
        try {
            loadRom("roms/3-corax+.ch8");
            //loadRom("roms/4-flags.ch8");
            //loadRom("roms/2-ibm-logo.ch8");
            //loadRom("roms/5-Breakout (Brix hack) [David Winter, 1997].ch8");
            //loadRom("roms/6-Keypad Test [Hap, 2006].ch8");
        }
        catch (IOException e) {
            System.out.println("Error: Bad Rom");
            System.exit(1);
        }
    }    

    // Loads a rom file form the roms folder into memory, using chip8's load function.
     public void loadRom(String rom) throws IOException{
        current.load(rom);       
    }
}