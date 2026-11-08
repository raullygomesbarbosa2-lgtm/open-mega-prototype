# Open Mega Proto (0.1)

An independent Android research prototype, built from scratch; it does **not** reuse or contain a commercial emulator core or Libretro. It is deliberately **not described as playable**. Current implementation is a minimal 68000 execution experiment and a diagnostic UI, not a Mega Drive emulator.

## Implemented
- Android app (Java, min SDK 19 / Android 4.4, armeabi-v7a target) with a document picker to load a locally owned/homebrew binary and touch UI.
- Minimal original 68000 interpreter subset: NOP, STOP, MOVEQ, BRA/BSR, RTS, MOVE.L immediate to data register; 24-bit ROM and upper RAM address mapping; reset vector startup and basic N/Z flags.
- Diagnostic display reports PC/status and unsupported opcodes.

## Not implemented / limitations
No Mega Drive hardware compatibility, VDP tile/sprite/plane rendering, CRAM/palette, DMA, Z80, YM2612/PSG audio, controller protocol, cartridge mapper, accurate memory map, interrupt/timing model, SRAM, save states, or usable game rendering. Buttons currently only expose a basic state; they are not mapped to the console's input hardware. This cannot run commercial games. No BIOS or ROM is bundled.

## Build
`./gradlew assembleDebug` (Android Gradle Plugin 8.2.2, SDK 34). The build has not been verified in this environment. Any APK should be treated as a UI/CPU-subset prototype, not a playable emulator.
