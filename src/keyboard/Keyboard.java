package src.keyboard;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Sets up keyboard controls with bindings to a CHIP-8 controller.
 * Uses a boolean array corresponding to keyboard presses.
 */

public class Keyboard implements KeyListener{

    public boolean[] keys = new boolean[16];
    // CHIP-8 controller:   1, 2, 3, C, 4, 5, 6, D, 7, 8, 9, E, A, 0, B, F
    // Keyboard binding:    1, 2, 3, 4, Q, W, E, R, A, S, D, F, Z, X, C, V

    @Override
    public void keyTyped(KeyEvent e) {
        // Unused
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_1) {
            keys[0x1] = true;
        }
        if (code == KeyEvent.VK_2) {
            keys[0x2] = true;
        }
        if (code == KeyEvent.VK_3) {
            keys[0x3] = true;
        }
        if (code == KeyEvent.VK_4) {
            keys[0xC] = true;
        }
        if (code == KeyEvent.VK_Q) {
            keys[0x4] = true;
        }
        if (code == KeyEvent.VK_W) {
            keys[0x5] = true;
        }
        if (code == KeyEvent.VK_E) {
            keys[0x6] = true;
        }
        if (code == KeyEvent.VK_R) {
            keys[0xD] = true;
        }
        if (code == KeyEvent.VK_A) {
            keys[0x7] = true;
        }
        if (code == KeyEvent.VK_S) {
            keys[0x8] = true;
        }
        if (code == KeyEvent.VK_D) {
            keys[0x9] = true;
        }
        if (code == KeyEvent.VK_F) {
            keys[0xE] = true;
        }
        if (code == KeyEvent.VK_Z) {
            keys[0xA] = true;
        }
        if (code == KeyEvent.VK_X) {
            keys[0x0] = true;
        }
        if (code == KeyEvent.VK_C) {
            keys[0xB] = true;
        }
        if (code == KeyEvent.VK_V) {
            keys[0xF] = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_1) {
            keys[0x1] = false;
        }
        if (code == KeyEvent.VK_2) {
            keys[0x2] = false;
        }
        if (code == KeyEvent.VK_3) {
            keys[0x3] = false;
        }
        if (code == KeyEvent.VK_4) {
            keys[0xC] = false;
        }
        if (code == KeyEvent.VK_Q) {
            keys[0x4] = false;
        }
        if (code == KeyEvent.VK_W) {
            keys[0x5] = false;
        }
        if (code == KeyEvent.VK_E) {
            keys[0x6] = false;
        }
        if (code == KeyEvent.VK_R) {
            keys[0xD] = false;
        }
        if (code == KeyEvent.VK_A) {
            keys[0x7] = false;
        }
        if (code == KeyEvent.VK_S) {
            keys[0x8] = false;
        }
        if (code == KeyEvent.VK_D) {
            keys[0x9] = false;
        }
        if (code == KeyEvent.VK_F) {
            keys[0xE] = false;
        }
        if (code == KeyEvent.VK_Z) {
            keys[0xA] = false;
        }
        if (code == KeyEvent.VK_X) {
            keys[0x0] = false;
        }
        if (code == KeyEvent.VK_C) {
            keys[0xB] = false;
        }
        if (code == KeyEvent.VK_V) {
            keys[0xF] = false;
        }
    }
}