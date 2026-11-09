package org.openmegaproto;

import org.junit.Test;
import static org.junit.Assert.*;

public class DisplayLayoutTest {
    @Test public void frameUsesMaximumAspectCorrectViewportWithCenteredBars() {
        for (float[] size : new float[][]{{320,640},{360,800},{393,852},{432,960},{1024,450},{1920,1080}}) {
            DisplayLayout l = new DisplayLayout(size[0], size[1]);
            float w=l.videoRight-l.videoLeft, h=l.videoBottom-l.videoTop;
            assertEquals(320f/224f,w/h,0.0001f);
            assertTrue(l.videoLeft>=0 && l.videoRight<=size[0]);
            assertTrue(l.videoTop>=0 && l.videoBottom<=size[1]);
            float scale=Math.min(size[0]/320f,size[1]/224f);
            assertEquals(320f*scale,w,0.01f);
            assertEquals(224f*scale,h,0.01f);
            assertEquals((size[0]-w)/2,l.videoLeft,0.01f);
            assertEquals((size[1]-h)/2,l.videoTop,0.01f);
        }
    }

    @Test public void controlsOverlayNearBottomAndFitScreenInBothOrientations() {
        for (float[] size : new float[][]{{320,640},{360,800},{393,852},{432,960},{1024,450},{1920,1080}}) {
            DisplayLayout l=new DisplayLayout(size[0],size[1]);
            float r=l.faceRadius*1.12f;
            assertTrue(l.joyY>size[1]*.7f);
            assertTrue(l.joyX-l.joyRadius*1.8f>=0 && l.joyX+l.joyRadius*1.8f<size[0]);
            assertTrue(l.joyY+l.joyRadius*1.8f<size[1]);
            assertTrue(l.startY+l.faceRadius*.65f<size[1]);
            for(int i=0;i<6;i++) {
                assertTrue(l.buttonX[i]-r>=0 && l.buttonX[i]+r<size[0]);
                assertTrue(l.buttonY[i]-r>=0 && l.buttonY[i]+r<size[1]);
                assertTrue(l.hitButton(i,l.buttonX[i],l.buttonY[i]));
                for(int j=i+1;j<6;j++) {
                    float dx=l.buttonX[i]-l.buttonX[j],dy=l.buttonY[i]-l.buttonY[j];
                    assertTrue("button hit regions must not overlap",dx*dx+dy*dy>=4*r*r);
                }
            }
            assertTrue(l.hitJoystick(l.joyX,l.joyY));
            assertTrue(l.hitStart(l.startX,l.startY));
        }
    }
}
