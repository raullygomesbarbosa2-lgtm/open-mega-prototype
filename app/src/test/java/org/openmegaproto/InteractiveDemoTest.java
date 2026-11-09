package org.openmegaproto;

import org.junit.Test;
import static org.junit.Assert.*;

/** Regression tests for loading user ROM bytes into the original, deliberately incomplete CPU/VDP core. */
public class InteractiveDemoTest {
    @Test public void loadsValidRomVectorsAndRunsSupportedCpuWritesToVdp() {
        int[] words={
            0x33fc,0x8230,0x00c0,0x0004,
            0x33fc,0x4020,0x00c0,0x0004,
            0x33fc,0x0000,0x00c0,0x0004,
            0x33fc,0xf00f,0x00c0,0x0000,
            0x33fc,0x4000,0x00c0,0x0004,
            0x33fc,0x0003,0x00c0,0x0004,
            0x33fc,0x0000,0x00c0,0x0004,
            0x33fc,0x0001,0x00c0,0x0000,
            0x4e72,0x2700
        };
        byte[] rom=new byte[0x200];
        rom[0]=0x00;rom[1]=(byte)0xff;rom[2]=(byte)0xff;rom[3]=0x00; // initial RAM stack at 00FFFF00
        rom[4]=0;rom[5]=0;rom[6]=0;rom[7]=8; // reset PC
        for(int i=0;i<words.length;i++){rom[8+i*2]=(byte)(words[i]>>>8);rom[9+i*2]=(byte)words[i];}
        MainActivity.Machine m=new MainActivity.Machine();
        m.load(rom);
        assertEquals(8,m.pc);
        assertFalse(m.stopped);
        m.step(words.length);
        assertEquals(0xf0,m.vdp.readVram(0x20));
        assertEquals(0x0f,m.vdp.readVram(0x21));
        assertTrue(m.stopped);
    }

    @Test public void rejectsTruncatedRomWithoutKeepingRunning() {
        MainActivity.Machine m=new MainActivity.Machine();
        try { m.load(new byte[32]); fail("short ROM must be rejected"); }
        catch(IllegalArgumentException expected) { assertTrue(expected.getMessage().contains("pequena")); }
    }

    @Test public void resetClearsRomAndStopsCpu() {
        MainActivity.Machine m=new MainActivity.Machine();m.reset();
        assertEquals(0,m.rom.length);assertTrue(m.stopped);assertEquals("Nenhuma ROM carregada",m.status);
    }
}
