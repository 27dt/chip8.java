import java.io.IOException;
import javax.swing.*;
import java.awt.*;


public class screenPanel extends JPanel implements Runnable{
    final int rows = 32;
    final int cols = 64;
    final int scale = 10;
    
    final int WIDTH = cols * scale;
    final int HEIGHT = rows * scale;

    int FPS = 120;   // essentially speed in Hz


    chip8 current;

    Thread cpuThread;

    public screenPanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        startEmulator();
    }

    public void StartCPUThread() {
        cpuThread = new Thread(this);
        cpuThread.start();
    }

    @Override
    public void run() {
        double interval = 1000000000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (cpuThread != null) {
            //long currentTime = System.nanoTime();
            //long currentTime2 = System.currentTimeMillis();
            //System.out.println("current time: " + currentTime);
            //System.out.println("Running.");
            currentTime = System.nanoTime();
            delta += (currentTime-lastTime)/interval;
            lastTime = currentTime;

            if (delta >= 1) {
                // CONSIDER ONLY REPAINTING IF A DISPLAY INSTRUCTION IS CALLED
                // MODIFY TIMING TO RUN A SET AMOUNT OF INSTRUCTIONS PER SECOND
                // RUNS AT 60HZ; IDEAL INSTRUCTIONS PER SECOND =
                current.cycle();
                //current.cycle();
                //current.cycle();
                //current.cycle();
                current.updateTimers();

                //System.out.println(currentTime);

                repaint();
                delta--;
            }
        }
    }

    public void startEmulator() {
        try { loadRom("roms/4-flags.ch8"); }
        catch (IOException e) { System.out.println("Bad Rom"); }
    }
    
    public void paint(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;
        
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                
                if (current.getPixel(y, x) == 1) {
                    g2D.setColor(Color.WHITE);
                    g2D.fillRect(x*scale, y*scale, scale, scale);
                }
                if (current.getPixel(y, x) == 0) {
                    g2D.setColor(Color.BLACK);
                    g2D.fillRect(x*scale, y*scale, scale, scale);
                }
            }
        }
    }

    /*
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            current.cycle();
            repaint();
            System.out.println("testing");
        }
    }
    */

    public void loadRom(String rom) throws IOException{
        current = new chip8();              // creates new CHIP-8
        current.load(rom);                  // loads rom into memory
    }

    
        /*
        short opcode;
        byte[] memory = new byte[4096];   // 4096 bytes
        memory[0] = (byte) 0xAB;
        memory[1] = (byte) 0x45;
        short pc = 0;                       // 16-bit program counter

        opcode = (short) ((memory[pc] << 8) | memory[pc + 1] );
        short first = (short) (opcode & 0xF000);

        System.out.println((String.format("0x%08X", opcode)));
        System.out.println((String.format("0x%08X", first)));
        */

}
