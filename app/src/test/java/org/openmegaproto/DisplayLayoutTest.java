package org.openmegaproto;

import org.junit.Test;
import static org.junit.Assert.*;

public class DisplayLayoutTest {
    @Test public void videoKeepsGenesisAspectAndStaysInsideViewport() {
        for (float[] size : new float[][]{{320,640},{360,800},{393,852},{432,960},{1024,450}}) {
            DisplayLayout l = new DisplayLayout(size[0], size[1]);
            float w = l.videoRight - l.videoLeft, h = l.videoBottom - l.videoTop;
            assertEquals(320f / 224f, w / h, 0.0001f);
            assertTrue(l.videoLeft >= 0 && l.videoRight <= size[0]);
            assertTrue(l.videoTop >= 0 && l.videoBottom <= size[1]);
            assertTrue("controls stay below the video", l.videoBottom < l.joyY);
        }
    }

    @Test public void landscapeUsesMoreOfTheScreenWithoutHidingControls() {
        DisplayLayout l = new DisplayLayout(1024, 450);
        float width=l.videoRight-l.videoLeft;
        assertTrue("landscape game viewport should be meaningfully larger", width > 440f);
        assertTrue(l.videoBottom < l.buttonY[DisplayLayout.X]-l.faceRadius*1.12f);
        assertTrue(l.startY+l.faceRadius*.65f < l.height);
        assertTrue(l.joyY+l.joyRadius*1.8f < l.height);
    }

    @Test public void sixButtonTargetsAreDistinctAndVisuallyOrdered() {
        for (float[] size : new float[][]{{320,640},{360,800},{393,852},{432,960},{1024,450}}) {
            DisplayLayout l = new DisplayLayout(size[0], size[1]);
            float hitRadius = l.faceRadius * 1.12f;
            for (int i=0;i<6;i++) {
                assertTrue(l.buttonX[i]-hitRadius >= 0);
                assertTrue(l.buttonX[i]+hitRadius <= size[0]);
                assertTrue(l.buttonY[i]-hitRadius > l.videoBottom);
                assertTrue(l.buttonY[i]+hitRadius < size[1]);
                float joyDx=l.buttonX[i]-l.joyX,joyDy=l.buttonY[i]-l.joyY;
                assertTrue("joystick hit region overlaps face button", joyDx*joyDx+joyDy*joyDy >= (l.joyRadius*1.8f+hitRadius)*(l.joyRadius*1.8f+hitRadius));
                for (int j=i+1;j<6;j++) {
                    float dx=l.buttonX[i]-l.buttonX[j],dy=l.buttonY[i]-l.buttonY[j];
                    assertTrue("overlapping face hit targets", dx*dx+dy*dy >= 4*hitRadius*hitRadius);
                }
            }
            assertTrue(l.buttonY[DisplayLayout.X] < l.buttonY[DisplayLayout.A]);
            assertTrue(l.buttonY[DisplayLayout.Y] < l.buttonY[DisplayLayout.B]);
            assertTrue(l.buttonY[DisplayLayout.Z] < l.buttonY[DisplayLayout.C]);
            for (int i=0;i<6;i++) {
                assertTrue(l.hitButton(i,l.buttonX[i],l.buttonY[i]));
                for (int j=0;j<6;j++) if (i!=j)
                    assertFalse("button center must hit only its own region",l.hitButton(j,l.buttonX[i],l.buttonY[i]));
            }
            assertTrue(l.hitStart(l.startX,l.startY));
        }
    }
}
