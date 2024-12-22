package src.cpu;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.io.File;

public class chip8 {
    private byte[] memory;                      // 4096 bytes
    private short[] V;                           // 16 8-bit Vx registers   =---- 05/16/24 - maybe its a signed issue, but switching this to SHORT fixes V overflows
    private short I;                            // 16-bit register to store mem address
    private short pc;                           // 16-bit program counter
    private byte sp;                            // 8-bit stack pointer
    private short[] stack;                      // 16 16-bit values   
    // private short opcode;                     ERROR 1: 16 bits for opcode
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
        //this.opcode = 0;                  ERROR 1: resets opcode
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
        input.close();                                              // closes inputstream
    }  

    // FETCH/DECODE/EXECUTE
    public void cycle() {
        //ERROR1: BECAUSE I CAN'T DO UNSIGNED, ANY 0X0xxx MESSES UP. OPCODE DOESN'T WORK.
        //opcode = (short) ((memory[pc] << 8) | memory[pc + 1]); // fetches instruction from memory (shifts first byte 8-bits left, and ORs with second byte, to form 16-bit/2 byte opcode)
        //System.out.println("byte1: " + (memory[pc] << 8) + " byte2: " + memory[pc + 1] + " mixed: " + (memory[pc] << 8 | memory[pc+1]));

        //System.out.println("CURRENT OPCODE: " + Integer.toHexString(opcode&0xFFFF) + " MADE UP OF B1: " + Integer.toHexString(memory[pc]&0xFF) + " AND B2: " + Integer.toHexString(memory[pc+1]&0xFF));
        // NOTE: IF THINGS DONT WORK, CONSIDERING UPDATING PC IN EACH SWITCH CASE INSTEAD
        //pc += 2;                                                // increments pc for next instruction
        
        //ERROR 1 CHANGES:
        //short hb1 = (short) ((opcode & 0xF000));                // pulls first half-byte from opcode
        //short x = (short) ((opcode & 0x0F00) >> 8);             // pulls second half-byte from opcode (reg x)
        //short y = (short) ((opcode & 0x00F0) >> 4);             // pulls third half-byte from opcode (reg y)
        //short n = (short) (opcode & 0x000F);                    // pulls fourth half-byte from opcode (typically n)
        //short nnn = (short) (opcode & 0x0FFF);                  // pulls nnn (addr) from opcode
        //short kk = (short) (opcode & 0x00FF);                   // pulls kk from opcode
        
        byte hb1 = (byte) ((memory[pc] & 0xF0) >> 4);                               // pulls half-byte 1 from memory[pc]
        byte x = (byte) ((memory[pc] & 0x0F));                                      // pulls half-byte 2 from memory[pc]
        byte y = (byte) ((memory[pc+1] & 0xF0) >> 4);                               // pulls half-byte 3 from memory[pc+1]
        byte n = (byte) ((memory[pc+1] & 0x0F));                                    // pulls half-byte 4 from memory[pc+1]
        byte kk  = (byte) ((memory[pc+1]) & 0xFF);                                  // pulls second byte for kk value
        short nnn = (short) (((memory[pc] & 0x0F) << 8) |((memory[pc+1] & 0xFF) )); // pulls half-bytes 2-4 for nnn value        pc+=2;
        
        pc+=2;                                                                      // increment pc

        //DEBUG LINE
        //System.out.println("OPCODE: " + hb1  + " | " + x + " | " + y + " | " + n);

        // NOTE: IN THE FUTURE, MAKE OPTION TO TOGGLE BETWEEN DIFFERENT CHIP8 SHIFT (8XY6/8XYE)
        // NOTE: FOR 8XY6 AND 8XYE, THE IMPLEMENTATIONS VX = VX SHR 1 AND VX = VX SHL 1 ARE USED
        
        switch(hb1) {
            case 0x0:                                    // FIRST HALF-BYTE 0
                switch (nnn) {
                    case 0x00E0:                                // clears display (CLS)
                        for (int i = 0; i < display.length; i++) {
                            for (int j = 0; j < display[i].length; j++) {
                                display[i][j] = 0;
                            }
                        }
                        System.out.println("0X0000: DISPLAY CLEAR");
                        break;
                    case 0x00EE:                                // returns from subroutine (RET)
                        sp--;                                   // decrements sp
                        pc = (short) stack[sp];                         // places value at stack[sp] in pc
                        stack[sp] = 0;                          // "pops" stack[sp] (sets to 0)
                        System.out.println("0X00EE: RETURN FROM SUBROUTINE: PC = " + pc);
                        break;
                    default:
                        break;
                }
                break;
            case 0x1:                                    //  FIRST HALF-BYTE 1
                pc = (short) nnn;                                       // jump to location nnn (JP addr)
                //System.out.println("0X1NNN: JUMP TO NNN: PC = " + pc);---------------------------------------------------------------------- THIS IS OFF FOR NOW TO I CAN SEE
                break;
            case 0x2:                                    // FIRST HALF-BYTE 2
                stack[sp] = (short) pc;                                 // pushes pc to stack   (CALL addr)
                sp++;                                           // increments sp
                pc = (short) nnn;                                       // pc jumps to nnn
                System.out.println("0X2000: CALL SUBROUTINE: PC = " + pc);
                break;
            case 0x3:                            // FIRST HALF-BYTE 3/4  (SE Vx, byte || SNE Vx, byte)                
                if (V[x] == kk) {               // skips next instruction if 0x3000 && Vx = kk
                    pc = (short) (pc + 2);  
                }
                System.out.println("0X3000: SKIP IF VX = KK: V" + x + " = " + V[x] + " KK = " + kk);
                break;

            case 0x4:
                if (V[x] != kk) {
                    pc = (short) (pc + 2);
                }
                System.out.println("0x4000: SKIP IF VX != KK: V" + x + " = " + V[x] + " KK = " + kk);
                break;

            case 0x5:                                    // FIRST HALF-BYTE 5 (SE Vx, Vy)
                if (V[x] == V[y]) {                             // skips next instruction if Vx = Vy
                    pc = (short) ((pc + 2) & 0x00FF);
                }    
                System.out.println("0x5000: SKIP IF VX = VY: V" + x + " = " + V[x] + " V" + y + " = " + V[y]);
                break; 
            case 0x6:                                    // FIRST HALF-BYTE 6 (LD Vx, byte)
                //V[x] = (short) (kk & 0x00FF);                               // sets Vx = kk
                V[x] = (short) (kk & 0xFFFF);
                System.out.println("0x6000: SETS VX = KK: V" + x + " = " + V[x] + " KK = " + kk);
                break;
            case 0x7:                                    // FIRST HALF-BYTE 7 (ADD Vx, byte)
                //V[x] += (short) (kk & 0x00FF);                              // sets Vx += kk
                V[x] = (short) ((V[x] + kk) & 0x00FF);
                System.out.println("0x7000: SETS VX += KK: V" + x + " = " + V[x] + " KK = " + kk);
                break;
            case (short) 0x8:                            // FIRST HALF-BYTE 8
                switch (n) {
                    case 0x0000:                                // set Vx = Vy (LD Vx, Vy)
                        System.out.println("----------------------------");
                        System.out.println("BEFORE: V" + x + " = " + V[x] + " V" + y + " = " + V[y]);
                        
                        V[x] = (short) (V[y] & 0x00FF);
                        
                        System.out.println("0x8xy0: SETS VX = VY: V" + x + " = " + V[x] + " V" + y + " = " + V[y]);
                        System.out.println("----------------------------");
                        break;
                    case 0x0001:                                // set Vx = Vx | Vy (OR Vx, Vy)
                        System.out.println("----------------------------");
                        System.out.println("BEFORE: V" + x + " = " + V[x] + " V" + y + " = " + V[y]);
                        
                        V[x] = (short) ((V[x] | V[y]) & 0x00FF);
                        
                        System.out.println("0x8xy1: SETS VX = VX|VY: V" + x + " = " + V[x] + " V" + y + " = " + V[y]);
                        System.out.println("----------------------------");
                        break;
                    case 0x0002:                                // set Vx = Vx & Vy (AND Vx, Vy)
                        System.out.println("----------------------------");
                        System.out.println("BEFORE: V" + x + " = " + V[x] + " V" + y + " = " + V[y]);

                        V[x] = (short) ((V[x] & V[y]) & 0x00FF);
                        
                        System.out.println("0x8xy2: SETS VX = VX&VY: V" + x + " = " + V[x] + " V" + y + " = " + V[y]);
                        System.out.println("----------------------------");
                        break;
                    case 0x0003:                                // set Vx = Vx ^ Vy (XOR Vx, Vy)
                        System.out.println("----------------------------");
                        System.out.println("BEFORE: V" + x + " = " + V[x] + " V" + y + " = " + V[y]);
                        
                        V[x] = (short) ((V[x] ^ V[y]) & 0x00FF);
                        
                        System.out.println("0x8xy3: SETS VX = VX^VY: V" + x + " = " + V[x] + " V" + y + " = " + V[y]);
                        System.out.println("----------------------------");
                        break;
                    case 0x0004:                                // set Vx = Vx + Vy (ADD Vx, Vy)
                        System.out.println("----------------------------");
                        System.out.println("BEFORE: V" + x + " = " + V[x] + " V" + y + " = " + V[y]);

                        V[x] = (short) ((V[x] + V[y]) & 0x00FF);
                        
                        if (((V[x] + V[y]) & 0x00FF) > 255) {                    // if result > 255, set Vf to 1 (carry)
                            V[0xF] = 1;
                        }
                        else {
                            V[0xF] = 0;
                        }                          // if result < 255, reset Vf to 0

                        System.out.println("0x8xx4: SETS VX = VX+VY: V" + x + " = " + V[x] + " V" + y + " = " + V[y] + "Vf = " + V[0xF]);
                        System.out.println("----------------------------");
                        break;
                    case 0x0005:                                // set Vx = Vx - Vy (SUB Vx, Vy)
                        System.out.println("----------------------------");
                        System.out.println("BEFORE: V" + x + " = " + V[x] + " V" + y + " = " + V[y]);

                        V[0xF] = 0;

                        if (V[x] > V[y]) {                          // NOT borrow, so Vf = 1
                            V[0xF] = 1; 
                        }
                        //else {                                      // // borrow, so Vf = 0
                            //V[0xF] = 0;
                        //}
                        
                        V[x] = (short) ((V[x] - V[y]) & 0x00FF);

                        System.out.println("0x8xy5: SETS VX = VX-VY: V" + x + " = " + V[x] + " V" + y + " = " + V[y] + "Vf = " + V[0xF]);
                        System.out.println("----------------------------");
                        break;
                    case 0x0006:                                // set Vx = Vx SHR 1 (SHR Vx {, Vy})
                        System.out.println("----------------------------");
                        System.out.println("BEFORE: V" + x + " = " + V[x]);

                        V[0xF] = (short) (V[x] & 0x01);              // Vf is set to lsb of Vx
                        V[x] = (short) (V[x] >> 1);                  // Vx is shifted right by 1
                        
                        System.out.println("0x8xy6: SETS VX >>= 1: V" + x + " = " + V[x] + "Vf = " + V[0xF]);
                        System.out.println("----------------------------");
                        break;
                    case 0x0007:                                // set Vx = Vy - Vx (SUBN Vx, Vy)
                        System.out.println("----------------------------");
                        System.out.println("BEFORE: V" + x + " = " + V[x] + " V" + y + " = " + V[y]);
                        
                        V[0xF] = 0;

                        if (V[y] > V[x]) {
                            V[0xF] = 1;   
                        }
                        else {
                            V[0xF] = 0;
                        }

                        V[x] = (short) ((V[y] - V[x]) & 0x00FF);
                        
                        System.out.println("0x8xy7: SETS VX = VY-VX: V" + x + " = " + V[x] + " V" + y + " = " + V[y] + "Vf = " + V[0xF]);
                        System.out.println("----------------------------");
                        break;
                    case 0x000E:                                // set Vx = Vx SHL 1 (SHL Vx {, Vy})
                        System.out.println("----------------------------");
                        System.out.println("BEFORE: V" + x + " = " + V[x]);

                        V[0xF] = (short) ((V[x] >> 7) & 0x01);       // Vf is set to msb of Vx (shift msb to lsb)
                        V[x] = (short) (V[x] << 1);                  // Vx is shifted left by 1
                        
                        System.out.println("0x8xyE: SETS VX <<= 1: V" + x + " = " + V[x] + "Vf = " + V[0xF]);
                        System.out.println("----------------------------");
                        break;
                    default:
                        break;
                }
                break;
            case (short) 0x9:                            // FIRST HALF-BYTE 9 (SNE Vx, Vy)
                if (V[x] != V[y]) {                             // skips next instruction if Vx != Vy
                    pc += (short) 2;
                }
                System.out.println("0x9000: SKIP IF VX != VY: V" + x + " = " + V[x] + " V" + y + " = " + V[y]);  
                break;
            case (short) 0xA:                            // FIRST HALF-BYTE A (LD I, addr)
                I = (short) nnn;                                        // set I = nnn
                System.out.println("0xA000: SETS I=nnn: I=0x" + Integer.toHexString(I) + " nnn=" + Integer.toHexString(nnn));
                break;
            case (short) 0xB:                            // FIRST HALF-BYTE B (JP V0, addr)
                pc = (short) (nnn + V[0x0]);                    // set pc = (nnn + V0)
                System.out.println("0xB000: SETS pc=(nnn+V0): pc=" + pc + " nnn=" + nnn + " V0=" + V[0x0]);
                break;                                   
            case (short) 0xC:                            // FIRST HALF-BYTE C (RND Vx, byte)
                Random rand = new Random();                     // creates random object
                V[x] = (byte) (rand.nextInt(0, 256) & kk);      // sets Vx = random(0, 256) & kk
                System.out.println("0xC000: SETS V[x] = random(0,256): V" + x + " = " + V[x]);
                break;                                   
            case (short) 0xD:                            // FIRST HALF-BYTE D (DRW Vx, Vy, nibble)   
                /* HOW DOES DISPLAY WORK?
                 * the value at address I holds the bytes that form the sprite
                 * lets say I = 0x257, and the bytes from 0x257 onwards are 0xFF, 0xE0, 0xCC
                 * n will be given in the instruction. this corresponds to a height of n pixels.
                 * coincedentally, there are three bytes of interest at 0x257, 0x258, 0x259
                 * the ycoord and xcoord are found first, by doing V[y] % 32 for ycoord, and V[x] % 64 for xcoord.
                 * we set up a loop for the pixel rows, and save the current byte (0xff) into a variable
                 * the xcoord is reset. we then do current & 0x80 >> 7, to get the first bit in the byte, and shift it to the front
                 * for 0xff, this gives us [1]1111111 -> [1]0000000 -> 0000000[1]
                 * if the pixel at (xcoord,ycoord) is already on, set vf. then xor the pixel onto the screen, using the coordinates
                 * xcoord is incremented every time, to draw the next pixel. current is shifted left 1, to get rid of the byte we just drew
                 * ycoord is incremented after the entire row has been drawn, in order to process the next row */
            
                short xcoord = (short) ((V[x] % 64) & 0x00FF);               // gets x-coord
                short ycoord = (short) ((V[y] % 32) & 0x00FF);               // gets y-coord
                short height = (short) (n & 0x00FF);                         // gets sprite height
                V[0xF] = 0;                                     // resets Vf
                short current;                                   // going to hold current byte (for drawing)

                for (int rows = 0; rows < height; rows++) {     // loops for n rows
                    current = (short) ((memory[I + rows]) & 0xFF);              // sets current as the current byte to draw
                    xcoord = (short) ((V[x] % 64) & 0xFF);                    // sets xcoord using modulo

                    for (int bit = 0; bit < 8; bit++) {             // loops for 8 bits = 1 byte
                        if (((current & 0x80) >> 7) != 0) {             // pulls first byte
                            if (display[ycoord % 32][xcoord % 64] == 1) {         // if the corresponding screen pixel is 1, set Vf
                                V[0xF] = 1;
                            }
                            display[ycoord % 32][xcoord % 64] ^= 1;           // XORs the byte onto the corresponding pixel
                        }
                        //System.out.println("xcoord: " + xcoord);
                        xcoord++;                                   // increments xcoord to set up for next bit
                        current <<= 1;                              // shifts current 1 place left, to move next bit to draw
                    }
                    //System.out.println("ycoord: " + ycoord);
                    ycoord++;                                       // increments ycoord to process next layer of sprite
                }
                System.out.println("0xD000: DISPLAY: V" + x + " = " + V[x] + " V" + y + " = " + V[y]);            // holy shit boss, i think we implemented it (2025-05-03 1:35 am)
                break;
            case (short) 0xE:                           // FIRST HALF-BYTE E
                switch(kk) {
                    case (byte) 0x009E:                         // skip instruction if key with value of Vx is pressed (SKP Vx)
                        if (keys[V[x]] == true) {
                            pc += (short) 2;
                        }
                        System.out.println("0xEX9E: V" + x + " = " + V[x] + " Key V" + x + " pressed: " + keys[V[x]]);
                        break;
                    case (byte) 0x00A1:                         // skip instruction if key with value of Vx is not pressed (SKNP Vx)
                        if (keys[V[x]] == false) {
                            pc += (short) 2;
                        }
                        System.out.println("0xEXA1: V" + x + " = " + V[x] + " Key V" + x + " pressed: " + keys[V[x]]);    
                        break;
                    default:
                        break;
                }
                break;
            case (short) 0xF:                            // FIRST HALF-BYTE F
                switch(kk) {
                    case 0x0007:                                // set vx = delay timer value (LD Vx, DT)
                        V[x] = (short) deltimer;
                        System.out.println("0xFX07: SETS VX = DELTIMER, V" + x + " = " + V[x] + ", DELTIMER: " + deltimer);
                        break;
                    case 0x000A:                                //-----------later---------------------------------------
                        System.out.println("0xFX0A: NOT IMPLEMENTED\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
                        break;
                    case 0x0015:                                // sets delay timer = vx (LD DT, Vx)
                        deltimer = (byte) (V[x] & 0x00FF);
                        System.out.println("0xFX15: SETS DELTIMER = V[X], DELTIMER: " + deltimer + ", V" + x + " = " + V[x]);
                        break;
                    case 0x0018:                                // sets sound timer = vx (LD ST, Vx)
                        sndtimer = (byte) (V[x] & 0x00FF);
                        System.out.println("0xFX18: SETS SNDTIMER = V[X], SNDTIMER: " + sndtimer + ", V" + x + " = " + V[x]);
                        break;
                    case 0x001E:                                // sets I = I + Vx (ADD I, Vx)
                        I = (short) (I + V[x]);
                        System.out.println("0xFX1E: SETS I += V[X], I: " + I + ", V" + x + " = " + V[x]);
                        break;
                    case 0x0029:                                // sets I = memory location of character in vx (LD F, Vx)
                        I = (short) (0x50 + (V[x] * 5));                        
                        System.out.println("0xFX29: I = LOCATION OF CHAR IN V[x], I: " + I + ", V" + x + " = " + V[x]);
                        break;
                    case 0x0033:                                // break vx into three digits, and store at I, I+1, I+2 (LD B, Vx)
                        int value = V[x];

                        memory[I] = (byte) (value / 100);
                        memory[I+1] = (byte) (Math.abs((value % 100) / 10));
                        memory[I+2] = (byte) (Math.abs(value % 10));

                        System.out.println("0xFX33: V" + x + " = " + V[x] + " I = " + memory[I] + " I+1 = " + memory[I+1] + " I+2 = " + memory[I+2]);
                        break;
                    case 0x0055:                                // store registers v0-vx (LD [I], Vx)
                        for (int index = 0; index <= x; index++) {
                            memory[I + index] = (byte) V[index];
                        }
                        System.out.println("0xFX65: REGISTERS V0 TO V" + x + " STORED AT I=" + I);   
                        break;
                    case 0x0065:                                // load registers v0-vx (LD Vx, [I])
                        for (int index = 0; index <= x; index++) {
                            V[index] = (short) ((memory[I + index]) & 0x00FF);
                        }
                        System.out.println("0xFX65: REGISTERS V0 TO V" + x + " LOADED FROM I=" + I);
                        break;
                    default: 
                        break;
                }
                break;
            default:
                // DO SOMETHING HERE
                break;
        }
        
    }

    public void cycleTimers() {
        // UPDATES DISPLAY/SOUND TIMER (IS CALLED 60 TIMES A SECOND IN THE SCREENPANEL)
        if (deltimer > 0) {deltimer -= 1;}
        if (sndtimer > 0) {sndtimer -= 1;}  
    }

    // GIVEN AN X and Y VALUE, 0/1 IS RETURNED IF THE PIXEL IS ON/OFF
    public int getPixel(int x, int y) {
        return display[x][y];
    } 

    // sets keys to specified array. used in screenpanel to set keyboard input variables to chip8 cpu
    public void setKeys(boolean[] k){
        keys = k;
    } 

    // for printing, debugging
    public boolean[] getKeys() {
        return keys;
    }

    // for sound timer. for now.
    public byte getsndtimer() {
        return sndtimer;
    }
}