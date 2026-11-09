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
    @Test public void spriteTableRendersOpaquePixelsAndPreservesTransparency(){
        Vdp v=new Vdp(); v.reset();
        v.writeRegister(1,0x40);v.writeRegister(5,0x1e); // enable display; H32 SAT at 0x3c00
        v.writeCram(1,0x000e);
        for(int i=0;i<32;i++)v.writeVram(32+i,0x11); // tile 1 is opaque palette index 1
        writeWords(v,0x3c00,new int[]{128,0,1,128}); // at (0,0), single tile
        v.render();
        assertEquals(v.cramColor(1),v.pixels[0]);
        assertEquals(v.cramColor(0),v.pixels[9]);
    }

    @Test public void linkedSpriteHeadHasPriorityAndFlipUsesTilePixelCoordinates(){
        Vdp v=new Vdp(); v.reset(); v.writeRegister(1,0x40);v.writeRegister(5,0x1e);
        v.writeCram(1,0x000e);v.writeCram(2,0x00e0);
        for(int i=0;i<32;i++){v.writeVram(32+i,0x11);v.writeVram(64+i,0x22);}
        // Head links to sprite 1; first sprite is in front where they overlap.
        writeWords(v,0x3c00,new int[]{128,1,1,128, 128,0,2,128});
        v.render();assertEquals(v.cramColor(1),v.pixels[0]);

        // Horizontal flip of tile 1 still maps the all-opaque pattern; place at x=8.
        v.reset();v.writeRegister(1,0x40);v.writeRegister(5,0x1e);v.writeCram(1,0x000e);
        for(int i=0;i<32;i++)v.writeVram(32+i,0x11);
        writeWords(v,0x3c00,new int[]{128,0,0x0801,136});
        v.render();assertEquals(v.cramColor(1),v.pixels[8]);
    }

    private static void writeWords(Vdp v,int base,int[] words){
        for(int i=0;i<words.length;i++){v.writeVram(base+i*2,words[i]>>>8);v.writeVram(base+i*2+1,words[i]);}
    }

    @Test public void vramAndCramWritesAreMaskedAndReadable(){
        Vdp v=new Vdp();v.writeVram(0x10001,0xab);assertEquals(0xab,v.readVram(1));
        v.writeCram(65,0xffff);assertEquals(0x0eee,v.cram[1]&0xffff);
        v.writeVsram(40,0x7ff);assertEquals(0x7ff,v.vsram[0]&0xffff);
    }
    @Test public void sixButtonYIsDistinctAndAppearsInExtendedThPhase(){
        Controller c=new Controller();c.set(false,false,false,false,false,false,false,false,true,false,false,false);
        c.writeData(0);c.writeData(0x40);c.writeData(0);c.writeData(0x40);c.writeData(0); // six-button identify sequence
        assertEquals("Y has its own active-low bit in extended phase",0,(c.readData()&2));
        assertNotEquals("Y is not aliased to the three-button down input",0,(c.readData()&1));
    }

    @Test public void padInputsAreActiveLowAndRespectThPhase(){
        Controller c=new Controller();assertEquals(0x7f,c.readData());
        c.set(true,false,true,true,true,false,false,false);
        int high=c.readData();assertEquals(0,(high&1));assertNotEquals(0,(high&0x10)); // A is sampled in the low TH phase.
        c.writeData(0);int low=c.readData();assertEquals(0,(low&4));assertEquals(0,(low&8));
        assertEquals(0,(low&0x10)); // A is active-low in the low phase.
    }
}
