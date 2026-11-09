package org.openmegaproto;

import org.junit.Test;
import static org.junit.Assert.*;

public class DisplayLayoutTest {
    private static final float[][] DEVICES = {
        {320,640}, {360,800}, {393,852}, {432,960},
        {640,320}, {800,360}, {852,393}, {960,432},
        {1024,450}, {1920,1080}
    };

    @Test public void viewportIsLargestAspectCorrectFitInEitherOrientation() {
        for (float[] size : DEVICES) {
            DisplayLayout l = new DisplayLayout(size[0], size[1]);
            float w=l.videoRight-l.videoLeft, h=l.videoBottom-l.videoTop;
            assertEquals("aspect",320f/224f,w/h,0.0001f);
            assertTrue(l.videoLeft>=0 && l.videoRight<=size[0]);
            assertTrue(l.videoTop>=0 && l.videoBottom<=size[1]);
            float scale=Math.min(size[0]/320f,size[1]/224f);
            assertEquals("maximal width",320f*scale,w,0.01f);
            assertEquals("maximal height",224f*scale,h,0.01f);
            assertEquals((size[0]-w)/2,l.videoLeft,0.01f);
            assertEquals((size[1]-h)/2,l.videoTop,0.01f);
        }
    }

    @Test public void overlayControlsAndMenuRemainVisibleAndHitTestable() {
        for (float[] size : DEVICES) {
            DisplayLayout l=new DisplayLayout(size[0],size[1]);
            float r=l.faceRadius*1.12f;
            assertTrue("joystick left",l.joyX-l.joyRadius*1.8f>=0);
            assertTrue("joystick right",l.joyX+l.joyRadius*1.8f<=size[0]);
            assertTrue("joystick bottom",l.joyY+l.joyRadius*1.8f<=size[1]);
            assertTrue("start bottom",l.startY+l.faceRadius*.65f<=size[1]);
            for(int i=0;i<6;i++) {
                assertTrue("button left",l.buttonX[i]-r>=0);
                assertTrue("button right",l.buttonX[i]+r<=size[0]);
                assertTrue("button top",l.buttonY[i]-r>=0);
                assertTrue("button bottom",l.buttonY[i]+r<=size[1]);
                assertTrue(l.hitButton(i,l.buttonX[i],l.buttonY[i]));
                for(int j=i+1;j<6;j++) {
                    float dx=l.buttonX[i]-l.buttonX[j],dy=l.buttonY[i]-l.buttonY[j];
                    assertTrue("button hit regions must not overlap",dx*dx+dy*dy>=4*r*r);
                }
            }
            assertTrue(l.hitJoystick(l.joyX,l.joyY));
            assertTrue(l.hitStart(l.startX,l.startY));
            assertTrue("menu center is reachable",l.hitMenu((l.menuLeft+l.menuRight)/2,(l.menuTop+l.menuBottom)/2));
            assertFalse("menu hit area stays on screen",l.hitMenu(-1,30));
            assertFalse("menu hit area stays above controls",l.hitMenu(size[0]-30,size[1]));
            assertTrue("joystick overlays game image",l.joyY<l.videoBottom && l.joyY>l.videoTop);
            for(int i=0;i<6;i++) assertTrue("face buttons overlay image",l.buttonY[i]>l.videoTop && l.buttonY[i]<l.videoBottom);
        }
    }
}
