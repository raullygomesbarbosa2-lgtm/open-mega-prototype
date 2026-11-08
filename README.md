# Open Mega Prototype (0.2)

An independent Android research prototype built from scratch. It is **not a playable Mega Drive emulator** and does not include or reuse any existing emulator core, Libretro, or copied emulator implementation.

## What this version implements
- Native Android Java interface; min SDK 19 (Android 4.4), armeabi-v7a, no WebView.
- Original partial VDP data model with VRAM, CRAM, VSRAM, register storage and a software renderer for a limited mode-5 two-plane tile display (basic name-table attributes, palette, flips and scrolling).
- A simplified three-button controller data-port model and touch mapping. This is not yet connected to emulated 68000 memory-mapped IO.
- Native bitmap output currently displays a generated VDP diagnostic tile pattern, not a ROM's graphics or game footage.
- Local ROM picker is retained for inspection; files are not uploaded. No ROMs or copyrighted artwork are bundled.

## Limitations — not playable
The 68000 interpreter remains a tiny incomplete subset. The new VDP is not yet connected to CPU memory-mapped VDP ports/register writes, DMA, interrupts, timing, sprites, windows, full scrolling modes, or hardware status. Controller input is not yet exposed through the 68000 bus. There is no sound emulation. Selecting a ROM does not execute a game or generate its graphics. Commercial ROM compatibility is not supported and would require extensive original CPU and hardware implementation. The displayed generated pattern is only a renderer diagnostic, not fake gameplay.

## Build and tests
GitHub Actions runs JVM tests for VDP pixel generation, VRAM/CRAM/VSRAM masking and controller-port input behavior, then builds a debug APK. This is a free research prototype without ads, analytics, payment or subscriptions. Debug APK is not store-signed.
