import javax.swing.JFrame;

public class ScreenFrame extends JFrame {

    public ScreenFrame() {
        
        ScreenPanel panel = new ScreenPanel();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(panel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setResizable(false);
        this.setTitle("chip8.java");

        panel.StartCPUThread();
    }
}
