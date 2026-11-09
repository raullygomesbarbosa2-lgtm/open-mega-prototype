package org.openmegaproto;

import org.junit.Test;
import static org.junit.Assert.*;

/** CPU-to-port integration regressions for a subset of VDP command/render behavior. */
public class VdpHardwarePathTest {
    private byte[] program(int... words) {
        byte[] rom=new byte[0x400]; rom[1]=(byte)0xff; rom[2]=(byte)0xff; rom[7]=8;
        for(int i=0;i<words.length;i++){rom[8+i*2]=(byte)(words[i]>>>8);rom[9+i*2]=(byte)words[i];}
        return rom;
    }
    private int[] moveWord(int value,int address){return new int[]{0x33fc,value,(address>>>16)&0xffff,address&0xffff};}
    private int[] append(int[] a,int[] b){int[] r=new int[a.length+b.length];System.arraycopy(a,0,r,0,a.length);System.arraycopy(b,0,r,a.length,b.length);return r;}

    @Test public void cpuCommandsWriteVramCramAndVscrollAndRenderPlanePixel(){
        int[] w={};
        // Plane A base = C000, 32x32 cells, display enabled; palette entry 1 is white.
        w=append(w,moveWord(0x8230,0x00c00004));
        w=append(w,moveWord(0x8144,0x00c00004)); // display on
        w=append(w,moveWord(0xc002,0x00c00004)); // CRAM write, palette index 1
        w=append(w,moveWord(0x0000,0x00c00004));
        w=append(w,moveWord(0x0eee,0x00c00000)); // palette 1 white
        w=append(w,moveWord(0x4020,0x00c00004)); // VRAM write at address 0x20
        w=append(w,moveWord(0x0000,0x00c00004));
        w=append(w,moveWord(0xffff,0x00c00000)); // tile 1 first row pixels
        w=append(w,moveWord(0x4000,0x00c00004)); // plane A name table at C000
        w=append(w,moveWord(0x0003,0x00c00004));
        w=append(w,moveWord(0x0001,0x00c00000));
        MainActivity.Machine m=new MainActivity.Machine();m.load(program(w));m.step(100);
        assertEquals("CPU's VDP port writes reach VRAM",0xff,m.vdp.readVram(0x20));
        assertEquals("CPU's VDP port writes reach CRAM",0x0eee,m.vdp.cram[1]&0xffff);
        m.vdp.render();
        assertEquals("rendered plane-A tile is white",0xffffffff,m.vdp.pixels[0]);
        assertNotEquals("empty neighbor remains backdrop",0xffffffff,m.vdp.pixels[8]);
    }

    @Test public void twoWordCommandDecodeAndAddressIncrementWrapAreStable(){
        Vdp v=new Vdp();v.writeRegister(15,2);
        v.writeControl(0x4020);v.writeControl(0x0000); // VRAM write at address 0x20
        v.writeDataPort(0x1234);v.writeDataPort(0xabcd);
        assertEquals(0x12,v.readVram(0x20));assertEquals(0x34,v.readVram(0x21));
        assertEquals(0xab,v.readVram(0x22));assertEquals(0xcd,v.readVram(0x23));
        v.writeControl(0x0020);v.writeControl(0x0000); // VRAM read command code 0, address 0x20
        assertEquals(0x1234,v.readDataPort());
    }
}
