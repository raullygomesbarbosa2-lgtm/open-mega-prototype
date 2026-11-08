package org.openmegaproto;

import org.junit.Test;
import static org.junit.Assert.*;

public class VdpControllerTest {
    @Test public void diagnosticPatternRendersNonBackdropPixels(){
        Vdp v=new Vdp();v.loadDiagnosticPattern();
        assertEquals(Vdp.WIDTH*Vdp.HEIGHT,v.pixels.length);
        int first=v.pixels[0],different=0;
        for(int color:v.pixels)if(color!=first)different++;
        assertTrue("tile pattern should modify the backdrop",different>1000);
        assertTrue(v.renderedFrames>0);
    }
    @Test public void vramAndCramWritesAreMaskedAndReadable(){
        Vdp v=new Vdp();v.writeVram(0x10001,0xab);assertEquals(0xab,v.readVram(1));
        v.writeCram(65,0xffff);assertEquals(0x0eee,v.cram[1]&0xffff);
        v.writeVsram(40,0x7ff);assertEquals(0x7ff,v.vsram[0]&0xffff);
    }
    @Test public void padInputsAreActiveLowAndRespectThPhase(){
        Controller c=new Controller();assertEquals(0x7f,c.readData());
        c.set(true,false,true,true,true,false,false,false);
        int high=c.readData();assertEquals(0,(high&1));assertEquals(0,(high&0x10));
        c.writeData(0);int low=c.readData();assertEquals(0,(low&4));assertEquals(0,(low&8));
        assertNotEquals(0,(low&0x10)); // B is not pressed in the low phase.
    }
}
