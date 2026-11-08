package org.openmegaproto;

import org.junit.Test;
import static org.junit.Assert.*;

/** Deterministic proof that the bundled, authored 68000 loop drives the VDP from controller input. */
public class InteractiveDemoTest {
    @Test public void demoCpuWritesDifferentTilePixelsForPadState() {
        MainActivity.Machine up = new MainActivity.Machine();
        up.loadInteractiveDemo();
        up.updatePad(true,false,false,false,false,false,false,false);
        up.step(8);

        MainActivity.Machine idle = new MainActivity.Machine();
        idle.loadInteractiveDemo();
        idle.updatePad(false,false,false,false,false,false,false,false);
        idle.step(8);

        assertEquals("A held direction selects the bright pattern through CPU port writes",0x11,up.vdp.readVram(0x20));
        assertEquals("The idle branch selects a different pattern through CPU port writes",0x22,idle.vdp.readVram(0x20));
        up.vdp.render(); idle.vdp.render();
        assertFalse("The VDP output should visibly differ for the two input states",java.util.Arrays.equals(up.vdp.pixels,idle.vdp.pixels));
    }
}
