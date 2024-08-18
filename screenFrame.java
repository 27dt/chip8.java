import java.awt.*;
import javax.swing.*;

public class screenFrame extends JFrame {
    screenPanel panel;

    public screenFrame() {
        panel = new screenPanel();
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.add(panel);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setResizable(false);
        this.setTitle("JavaCHIP-8");

        panel.StartCPUThread();
    }
}
