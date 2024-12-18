# About
An object-oriented chip-8 emulator created using Java.

Uses Swing for graphics, and implements various Java interfaces
to handle timers and keyboard inputs. 

Developed to learn more about CPU emulation, and how bytes in rom files can be loaded into memory, to perform instructions and process
display, keyboard, timer, and sound actions from the rom.

# Usage
By editing the name of the loaded file under the `startEmulator` method in `ScreenPanel.java`, a new rom can be loaded when `cpu.java` is run. Some test roms can be found under the `roms` folder, but more can be added.

# Design
`cpu.java` is the entry point into the emulator. On execution of this file, a Swing JFrame window — `ScreenFrame.java` is created. Inside this JFrame is a JPanel — `ScreenPanel.java` — in which items are displayed on the screen. This is where the chip-8 cpu is initialized, roms files are accessed, and graphics are drawn.

The `chip8.java` file consists of memory and register initializations, and functions that load the rom into memory, perform a CPU cycle (decodes bytes of memory into instructions), set pixels to be drawn, updates timers, and handles keyboard input.

# Planning
- Make chip8.java more modular
- Organize file structure (src file, etc.)
- Consider implementing tests
- Get keyboard to work (TOP PRIORITY)
- Get Threading to work for timer handling
- Get sound to work
- Consider writing a fix to work around Java not having unsigned data types

FURTHER:
- Allow a user to change appearance of the pixels and background colours of the program
- Rom-loader built in that allows a user to select a rom from a drop-down list
- DEBUG features: change speed, pause emulation, cycle step by step, etc.
