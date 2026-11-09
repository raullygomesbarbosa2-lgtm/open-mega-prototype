# Open Mega Prototype (0.3 research build)

Independent Android research prototype built entirely from scratch. **Not a playable Mega Drive emulator.** No third-party emulator core, Libretro, or copied emulator implementation is used.

## Implemented
- Native Android Java UI; min SDK 19, armeabi-v7a, no WebView.
- Original partial VDP data model and mode-5 tile renderer. CPU writes use VDP two-word commands for VRAM/CRAM/VSRAM and VRAM reads use the read-command code. Rendering includes configurable plane dimensions, horizontal scroll modes, per-column VSRAM, tile flips/palettes and plane priority.
- A small original 68000 interpreter, explicitly not a complete CPU. Supported subset includes MOVE/MOVEA (register, immediate, indirect, postincrement, predecrement, displacement, indexed, absolute and PC-relative source modes as implemented); LEA, JMP/JSR, Bcc/BRA/BSR, MOVEQ, CLR/TST, immediate ORI/ANDI/SUBI/ADDI/EORI/CMPI, ADDQ/SUBQ, CMP/CMPA, NOP and STOP. Unsupported instruction/addressing encodings stop with an opcode/address diagnostic. Flags are covered by focused unit tests, not asserted to be exhaustive Motorola conformance.
- Reset vector validation/loading, local ROM picker, basic controller and limited VDP integration. ROM bytes are loaded, but this is not evidence that any commercial ROM can run.

## Scope and limitations
This is an independent from-scratch research prototype: no third-party emulator core, Libretro, or copied emulator implementation. It is **not a playable Mega Drive emulator** and does not claim commercial game compatibility. Unit tests validate discrete CPU semantics and a small authored CPU→VDP test program only; neither constitutes a game execution test. Major blockers include broad 68000 behavior, fuller/correct system bus and cartridge mapping, VDP modes/timing/status/sprites/DMA/interrupts, Z80, YM2612/PSG sound, and peripheral protocols.

## Build and tests
GitHub Actions runs JVM tests and builds a debug APK. This is a free research prototype without ads, analytics, payments or subscriptions. Debug APK is not store-signed.

## Content and licensing
No ROM, BIOS, game, or other copyrighted game data is included or distributed by this project. The MIT license in `LICENSE` applies only to the original project code authored for this repository; it does not cover any ROM, BIOS, game, trademark, or other third-party material. Only load content you own or are otherwise authorized to use, and check the laws and licenses that apply where you live. This is not legal advice and does not make every use lawful in every jurisdiction.

The project code is provided without warranty, including without any guarantee that it builds, works, or is compatible with any game. This prototype is not advertised as playable and no compatibility is promised.

