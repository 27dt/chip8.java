import javax.swing.JFrame;

public class screenFrame extends JFrame {

    public screenFrame() {
        
        screenPanel panel = new screenPanel();
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
