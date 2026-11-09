package org.openmegaproto;

/** Pure dp-coordinate layout shared by drawing and touch hit-testing. */
final class DisplayLayout {
 static final int VIDEO_WIDTH=320, VIDEO_HEIGHT=224;
 static final int X=0,Y=1,Z=2,A=3,B=4,C=5;
 final float width,height,videoLeft,videoTop,videoRight,videoBottom,joyX,joyY,joyRadius,faceRadius,startX,startY;
 final float[] buttonX=new float[6],buttonY=new float[6];
 DisplayLayout(float w,float h){
  width=Math.max(1,w);height=Math.max(1,h);boolean landscape=width>height;
  // Fit the complete 320x224 frame to all available screen space. The canvas
  // remains centered and aspect-correct; unused space is naturally black.
  float scale=Math.min(width/VIDEO_WIDTH,height/VIDEO_HEIGHT),vw=VIDEO_WIDTH*scale,vh=VIDEO_HEIGHT*scale;
  videoLeft=(width-vw)*.5f;videoRight=videoLeft+vw;videoTop=(height-vh)*.5f;videoBottom=videoTop+vh;
  // Controls overlay the frame near the bottom, leaving the menu reachable at top-right.
  joyX=width*.20f;joyY=height*.82f;
  joyRadius=landscape?Math.max(12,Math.min(24,Math.min(height*.065f,width*.055f))):Math.max(12,Math.min(38,Math.min(height*.085f,width*.11f)));
  faceRadius=landscape?Math.max(10,Math.min(18,Math.min(height*.052f,width*.042f))):Math.max(10,Math.min(25,Math.min(height*.052f,width*.065f)));
  float fx=width*.76f,fy=height*.78f,dx=faceRadius*2.35f,dy=faceRadius*1.25f;
  buttonX[X]=fx-dx;buttonY[X]=fy-dy;buttonX[Y]=fx;buttonY[Y]=fy-dy;buttonX[Z]=fx+dx;buttonY[Z]=fy-dy;
  buttonX[A]=fx-dx;buttonY[A]=fy+dy;buttonX[B]=fx;buttonY[B]=fy+dy;buttonX[C]=fx+dx;buttonY[C]=fy+dy;
  startX=fx;startY=fy+faceRadius*(landscape?2.65f:3.15f);
 }
 boolean hitJoystick(float x,float y){float dx=x-joyX,dy=y-joyY,r=joyRadius*1.8f;return dx*dx+dy*dy<=r*r;}
 boolean hitButton(int i,float x,float y){float dx=x-buttonX[i],dy=y-buttonY[i],r=faceRadius*1.12f;return dx*dx+dy*dy<=r*r;}
 boolean hitStart(float x,float y){return Math.abs(x-startX)<=faceRadius*1.45f&&Math.abs(y-startY)<=faceRadius*.65f;}
}
