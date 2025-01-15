package src.cpu;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.io.File;

public class chip8 {
    private byte[] memory;                      // 4096 bytes
    private short[] V;                          // 16 8-bit Vx registers   =---- 05/16/24 - maybe its a signed issue, but switching this to SHORT fixes V overflows
    private short I;                            // 16-bit register to store mem address
    private short pc;                           // 16-bit program counter
    private byte sp;                            // 8-bit stack pointer
    private short[] stack;                      // 16 16-bit values   
    private byte deltimer;                      // 8 bit delay timer
    private byte sndtimer;                      // 8 bit sound timer
    private byte[][] display;                   // display
    private boolean[] keys;                     // keyboard keys

    // NOTE: DISPLAY AND SOUND TIMERS MIGHT BE MESSED UP BY USING SIGNED 8BIT
    // NOTE: MIGHT HAVE TO SWITCH TO INT USING VALUE & 0XFF 
    // NOTE: SWITCH CASES 0X8000 AND ONWARDS NEED TO BE CASTED AS A SHORT
    // TO HANDLE UNSIGNED

    private int[] fontset = {
        0xF0, 0x90, 0x90, 0x90, 0xF0, // 0, starting at location 0x50
        0x20, 0x60, 0x20, 0x20, 0x70, // 1, starting at location 0x55
        0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2, starting at location 0x5A
        0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3, starting at location 0x5F
        0x90, 0x90, 0xF0, 0x10, 0x10, // 4, starting at location 0x64
        0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5, starting at location 0x69
        0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6, starting at location 0x6E
        0xF0, 0x10, 0x20, 0x40, 0x40, // 7, starting at location 0x73
        0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8, starting at location 0x78
        0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9, starting at location 0x7D
        0xF0, 0x90, 0xF0, 0x90, 0x90, // A, starting at location 0x82
        0xE0, 0x90, 0xE0, 0x90, 0xE0, // B, starting at location 0x87
        0xF0, 0x80, 0x80, 0x80, 0xF0, // C, starting at location 0x8C
        0xE0, 0x90, 0x90, 0x90, 0xE0, // D, starting at location 0x91
        0xF0, 0x80, 0xF0, 0x80, 0xF0, // E, starting at location 0x96
        0xF0, 0x80, 0xF0, 0x80, 0x80, // F, starting at location 0x9B
    };
    
    // CONSTRUCTOR
    public chip8(){                         
        this.I = 0;                         // resets index register
        this.pc = 0x200;                    // pc starts at 0x200
        this.sp = 0;                        // resets stack pointer
        this.deltimer = 0;                  // resets delay timer 
        this.sndtimer = 0;                  // resets sound timer
        this.memory = new byte[4096];       // clears memory
        this.V = new short[16];              // clears V0 through VF
        this.stack = new short[16];         // clears stack
        this.display = new byte[32][64];    // clears display (64x32 resolution)
        this.keys = new boolean[16];        // initialized as a boolean array (false)
        
        for (int i = 0x50; i < 0xA0; i++) {     // writes fontset to memory 0x50-0x9F
            memory[i] = (byte) fontset[i-0x50]; // handles offset (memory[0x50] = fontset[0])      
        }
    }

    // LOAD ROM
    public void load(String filename) throws IOException{
        File file = new File(filename);                             // sets up file
        InputStream input = new FileInputStream(file);              // inputstream for reading binary
        byte data;                                                  // hold current byte                      
        for (int index = 0; index <= file.length()-1; index++) {    // writes data (bytewise) into memory locations from 0x200 ibwards
            data = (byte) input.read();
            memory[0x200 + index] = (byte) data;                           
        }
        input.close();                                              
    }  

    // FETCH/DECODE/EXECUTE
    public void cycle() {
        int opcode = (((memory[pc]) & 0x00FF) << 8) | ((memory[pc+1]) & 0x00FF);
        System.out.println("Opcode---------------------------------------------------:"+Integer.toHexString((((memory[pc]) & 0x00FF) << 8) | ((memory[pc+1]) & 0x00FF)));
        System.out.println("Real Opcode----------------------------------------------:"+Integer.toHexString(opcode));
        System.out.println(Integer.toHexString( opcode >> 12));
        System.out.println(Integer.toHexString( opcode >> 8));
        System.out.println(Integer.toHexString( opcode >> 4));
        System.out.println(Integer.toHexString(opcode & 0xF000));
        
        switch (opcode & 0xF000) {
            case 0x0000:
                switch (opcode & 0xFFF) {
                    case 0x0E0:
                        for (int i = 0; i < display.length; i++) {
                            for (int j = 0; j < display[i].length; j++) {
                                display[i][j] = 0;
                            }
                        }
                }

            case 0x6000:
                System.out.println("yuh");
                break;
            default:
                System.out.println("Instruction missing or invalid.");
                break;
        }
    }

    // Updates display/sound timers at a rate of 60hz (in ScreenPanel).
    public void cycleTimers() {
        if (deltimer > 0) {deltimer -= 1;}
        if (sndtimer > 0) {sndtimer -= 1;}  
    }

    // Given a display coordinate, return 0/1 if the pixel is on/off.
    public int getPixel(int x, int y) {
        return display[x][y];
    } 

    // Sets keys to a key-press array to process inputs (in ScreenPanel).
    public void setKeys(boolean[] k){
        keys = k;
    } 

    // Used to activate/deactivate sound buzzer (in ScreenPanel).
    public byte getSndTimer() {
        return sndtimer;
    }
}