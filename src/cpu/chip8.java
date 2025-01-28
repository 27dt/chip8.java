package src.cpu;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.io.File;

public class chip8 {
    private short[] memory;                      // 4096 bytes
    private int[] V;                          // 16 8-bit Vx registers   =---- 05/16/24 - maybe its a signed issue, but switching this to SHORT fixes V overflows
    private int I;                            // 16-bit register to store mem address
    private int pc;                           // 16-bit program counter
    private short sp;                            // 8-bit stack pointer
    private int[] stack;                      // 16 16-bit values   
    private short deltimer;                      // 8 bit delay timer
    private short sndtimer;                      // 8 bit sound timer
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
        this.memory = new short[4096];       // clears memory
        this.V = new int[16];              // clears V0 through VF
        this.stack = new int[16];         // clears stack
        this.display = new byte[32][64];    // clears display (64x32 resolution)
        this.keys = new boolean[16];        // initialized as a boolean array (false)
        
        for (int i = 0x50; i < 0xA0; i++) {     // writes fontset to memory 0x50-0x9F
            memory[i] = (short) fontset[i-0x50]; // handles offset (memory[0x50] = fontset[0])      
        }
    }

    // Loads bytewise rom data into memory from file.
    public void load(String filename) throws IOException{
        File file = new File(filename);                             
        
        // InputStream for reading binary.
        InputStream input = new FileInputStream(file);              
        short data;
        
        // Writes bytewise data into memory locations from 0x200 onwards.
        for (int index = 0; index <= file.length()-1; index++) {
            data = (short) input.read();
            memory[0x200 + index] = (short) (data & 0x00FF);                           
        }
        input.close();                                              
    }  

    // Fetches, Decodes, and Executes current opcode at PC.
    public void cycle() {
        int opcode = (((memory[pc]) & 0x00FF) << 8) | ((memory[pc+1]) & 0x00FF);
        System.out.println("Opcode---------------------------------------------------:"+Integer.toHexString((((memory[pc]) & 0x00FF) << 8) | ((memory[pc+1]) & 0x00FF)));
        System.out.println("Real Opcode----------------------------------------------:"+Integer.toHexString(opcode));
        System.out.println(Integer.toHexString( opcode >> 12));
        System.out.println(Integer.toHexString( opcode >> 8));
        System.out.println(Integer.toHexString( opcode >> 4));
        System.out.println(Integer.toHexString(opcode & 0xF000));

        pc += 2;
        
        switch (opcode & 0xF000) {
            case 0x0000:
                switch (opcode & 0x0FFF) {
                    // 00E0: CLS (Clear display)
                    case 0x00E0:
                        for (int i = 0; i < display.length; i++) {
                            for (int j = 0; j < display[i].length; j++) {
                                display[i][j] = 0;
                            }
                        }
                        break;

                    // 00EE: RET (Return from subroutine)
                    case 0x00EE:
                        pc = (short) stack[sp];
                        stack[sp] = 0;
                        sp--;                                                  // IF BUSTED, MOVE THIS LINE TO FIRST IN THE CASE ==========================================
                        break;
                }
                break;

            // 1nnn: JP addr (Jump to location nnn)
            case 0x1000:
                pc = (short) (opcode & 0x0FFF);
                break;

            // 2nnn: CALL addr (Call subroutine at nnn)
            case 0x2000:
                sp++;
                stack[sp] = (short) pc;
                pc = (short) (opcode & 0x0FFF);
                break;

            // 3xkk: SE Vx, byte (Skip next instruction if V[x] = kk)
            case 0x3000:
                if (V[(opcode & 0x0F00) >> 8] == (opcode & 0x00FF)) {
                    pc += 2;
                }
                break;

            // 4xkk: SNE Vx, byte (Skip next instruction if V[x] != kk)
            case 0x4000:
                if (V[(opcode & 0x0F00) >> 8] != (opcode & 0x00FF)) {
                    pc += 2;
                }
                break;

            // 5xy0: SE Vx, Vy (Skip next instruction if V[x] = V[y])
            case 0x5000:
                if (V[(opcode & 0x0F00) >> 8] == V[(opcode & 0x00F0) >> 4]) {
                    pc += 2;
                }
                break;
            
            // 6xkk: LD V[x], byte (Sets V[x] = kk)
            case 0x6000:
                V[(opcode & 0x0F00) >> 8] = (short) (opcode & 0x00FF);
                break;

            // 7xkk: ADD V[x], byte (Sets V[x] += kk)
            case 0x7000:
                V[(opcode & 0x0F00) >> 8] = (short) (V[(opcode & 0x0F00) >> 8] + (opcode & 0x00FF));
                break;

            case 0x8000:
                switch (opcode & 0x000F) {
                    //
                    case 0x0000:
                        // ===============================================================MY BAD========================
                        break;
                    
                    //
                    case 0x0001:
                        // ===============================================================MY BAD========================
                        break;
                    
                    //
                    case 0x0002:
                        // ===============================================================MY BAD========================
                        break;
                    
                    //
                    case 0x0003:
                        // ===============================================================MY BAD========================
                        break;
                    
                    //
                    case 0x0004:
                        // ===============================================================MY BAD========================
                        break;
                    
                    //
                    case 0x0005:
                        // ===============================================================MY BAD========================
                        break;
                    
                    //
                    case 0x0006:
                        // ===============================================================MY BAD========================
                        break;
                    
                    //
                    case 0x0007:
                        // ===============================================================MY BAD========================
                        break;
                    
                    //
                    case 0x000E:
                        // ===============================================================MY BAD========================
                        break;
                }
                break;

            // 9xy0: SNE Vx, Vy (Skip next instruction if V[x] != V[y])
            case 0x9000:
                if (V[(opcode & 0x0F00) >> 8] != V[(opcode & 0x00F0) >> 4]) {
                    pc += 2;
                }
                break;

            // Annn: LD I, addr (Sets I = nnn)
            case 0xA000:
                I = (short) (opcode & 0xFFF);
                break;

            // Bnnn: JP V0, addr (Jump to location nnn + V[0])
            case 0xB000:
                pc = (short) V[0x0] + (opcode & 0x0FFF);
                break;

            // Cxkk: RND Vx, byte (Sets V[x] = random byte & kk)
            case 0xC000:
                Random rand = new Random();                
                V[(opcode & 0x0F00) >> 8] = (short) (rand.nextInt(0, 256) & (opcode & 0x00FF));
                break;        

            // Dxyn: DRW Vx, Vy, nibble (Draws sprites on screen)
            case 0xD000:
                //int x = (short) ((V[(opcode & 0x0F00) >> 8] % 64) & 0x00FF);  // x-coord  (CATCHALL)=====================================================================
                int x;                                                          // x-coord
                int y = (short) ((V[(opcode & 0x00F0) >> 4] % 32) & 0x00FF);    // y-coord
                int h = (short) (opcode & 0x000F);                              // sprite height
                V[0xF] = 0;                                                     // resets V[F]
                
                short current;                                                  // current byte
                for (int rows = 0; rows < h; rows++) {
                    current = (short) ((memory[I + rows]) & 0xFF);              // sets current as the current byte to draw
                    x = (short) ((V[(opcode & 0x0F00) >> 8] % 64) & 0x00FF);    // sets x = V[x]

                    for (int bit = 0; bit < 8; bit++) {                         // loops for 1 byte
                        if (((current & 0x80) >> 7) != 0) {                     // pulls first byte
                            if (display[y % 32][x % 64] == 1) {                 // if the corresponding pixel on, set V[F]
                                V[0xF] = 1;
                            }
                            display[y % 32][x % 64] ^= 1;                       // XORs byte onto the corresponding pixel
                        }
                        x++;                                                    // increments x to set up for next bit
                        current <<= 1;                                          // retrieves next bit in draw byte
                    }
                    y++;                                                        // increments y for next row
                }
                break;

            case 0xE000:
                switch (opcode & 0x00FF) {
                    // Ex9E: SKP Vx (Skip next instruction if key value V[x] pressed)
                    case 0x009E:
                        if (keys[V[(opcode & 0x0F00) >> 8]] == true) {
                            pc += 2;
                        }
                        break;
                    
                    // ExA1: SKNP Vx (Skip next instruction if key value V[x] not pressed)
                    case 0x00A1:
                        if (keys[V[(opcode & 0x0F00) >> 8]] == false) {
                            pc += 2;
                        }
                        break;
                }
                break;

            case 0xF000:
                switch (opcode & 0x00FF) {
                    // Fx07: LD Vx, DT (Set V[x] = delay timer)
                    case 0x0007:
                        V[(opcode & 0x0F00) >> 8] = (short) deltimer;
                        break;
                    
                    // Fx0A: LD Vx, K (Wait for key, store value of key in V[x])
                    case 0x000A:
                        // ===============================================================MY BAD========================
                        break;

                    // Fx15: LD DT, Vx (Set delay timer = V[x])
                    case 0x0015:
                        deltimer = (byte) (V[(opcode & 0x0F00) >> 8]);
                        break;

                    // Fx18: LD ST, Vx (Set sound timer = V[x])
                    case 0x0018:
                        sndtimer = (byte) (V[(opcode & 0x0F00) >> 8]);
                        break;

                    // Fx1E: ADD I, Vx (Set I += V[x])
                    case 0x001E:
                        I = (short) (I + V[(opcode & 0x0F00) >> 8]);
                        break;

                    // Fx29: LD F, Vx (Set I = location of sprite for digit V[x])
                    case 0x0029:
                        I = (short) (0x50 + (V[(opcode & 0x0F00) >> 8] * 5));    
                        break;
                    
                    // Fx33: LD B, Vx (Store BCD dep of V[x] in locations I, I+1, I+2)
                    case 0x0033:
                        // ===============================================================MY BAD========================
                        break;

                    // Fx55: LD [I], Vx (Store regs V[0]-V[x] in memory starting at location I)
                    case 0x0055:
                        // ===============================================================MY BAD========================
                        break;

                    // Fx65: LD Vx, [I] (Read regs V[0]-V[x] from memory starting at location I)
                    case 0x0065:
                        // ===============================================================MY BAD========================
                        break;
                }

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
    public short getSndTimer() {
        return sndtimer;
    }
}