# Open Mega Prototype (0.3 research build)

Independent Android research prototype built entirely from scratch. **Not a playable Mega Drive emulator.** No third-party emulator core, Libretro, or copied emulator implementation is used.

## Implemented
- Native Android Java UI; min SDK 19, armeabi-v7a, no WebView.
- Original partial VDP data model and limited mode-5 tile renderer; CPU 16-bit memory writes to VDP control/data ports now reach the VDP model.
- Tiny incomplete original 68000 interpreter; it supports a few instructions, including MOVE.W immediate to absolute-long, MOVE.W absolute-long to data register, and branches.
- Simplified active-low controller data-port read mapped at the CPU's A10000-A1001F region.
- Local ROM picker; ROMs stay local and no game ROM/art is bundled.

## Interactive authored demonstration (not commercial ROM playback)
The app starts a tiny hand-authored 68000 demonstration loop on its existing CPU/VDP/controller path. It polls the active-low controller port, branches on Up, writes one of two tile patterns through the emulated VDP data port, and renders the VDP output repeatedly on the Android view loop. The Up touch control selects a visibly different tile in the screen. A deterministic JVM test executes that authored program in two input states and checks both VRAM bytes and differing rendered frame buffers.

This is a narrow homebrew/demo program, not a commercial Mega Drive game emulator. Loading a selected ROM does not make it playable; it remains a limited CPU test path. The partial implementation still lacks broad 68000 behavior/addressing, correct VDP modes/timing/status/sprites/DMA/interrupts, Z80, YM2612/PSG sound, and cartridge mapping/protocols. No commercial ROM compatibility is claimed.

Major blockers to gameplay/compatibility remain: a broad 68000 instruction set and correct memory bus/addressing, proper VDP modes/timing/status/sprites/DMA/interrupts, Z80, YM2612/PSG sound, and cartridge mapping/protocols. No claims of broad compatibility are made.

## Build and tests
GitHub Actions runs JVM tests and builds a debug APK. This is a free research prototype without ads, analytics, payments or subscriptions. Debug APK is not store-signed.
