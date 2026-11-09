package org.openmegaproto;

/** Pure dp-coordinate layout shared by drawing and touch hit-testing. */
final class DisplayLayout {
 static final int VIDEO_WIDTH=320, VIDEO_HEIGHT=224;
 static final int X=0,Y=1,Z=2,A=3,B=4,C=5;
 final float width,height,videoLeft,videoTop,videoRight,videoBottom,joyX,joyY,joyRadius,faceRadius,startX,startY;
 final float[] buttonX=new float[6],buttonY=new float[6];
 final float menuLeft,menuTop,menuRight,menuBottom;
 DisplayLayout(float w,float h){
  width=Math.max(1,w);height=Math.max(1,h);boolean landscape=width>height;
  menuLeft=width-54;menuTop=12;menuRight=width-12;menuBottom=50;
  // Fit the complete 320x224 frame to all available screen space. The canvas
  // remains centered and aspect-correct; unused space is naturally black.
  float scale=Math.min(width/VIDEO_WIDTH,height/VIDEO_HEIGHT),vw=VIDEO_WIDTH*scale,vh=VIDEO_HEIGHT*scale;
  videoLeft=(width-vw)*.5f;videoRight=videoLeft+vw;videoTop=(height-vh)*.5f;videoBottom=videoTop+vh;
  // Controls overlay the frame near the bottom, leaving the menu reachable at top-right.
  float frameW=videoRight-videoLeft,frameH=videoBottom-videoTop;
  // Anchor all touch controls to the rendered frame, so portrait letterboxing
  // never leaves the game image separated from its controls.
  joyX=videoLeft+frameW*.20f;joyY=videoTop+frameH*.82f;
  joyRadius=Math.max(8,Math.min(24,Math.min(frameH*.085f,frameW*.055f)));
  faceRadius=Math.max(8,Math.min(18,Math.min(frameH*.052f,frameW*.042f)));
  float fx=videoLeft+frameW*.76f,fy=videoTop+frameH*.78f,dx=faceRadius*2.35f,dy=faceRadius*1.25f;
  buttonX[X]=fx-dx;buttonY[X]=fy-dy;buttonX[Y]=fx;buttonY[Y]=fy-dy;buttonX[Z]=fx+dx;buttonY[Z]=fy-dy;
  buttonX[A]=fx-dx;buttonY[A]=fy+dy;buttonX[B]=fx;buttonY[B]=fy+dy;buttonX[C]=fx+dx;buttonY[C]=fy+dy;
  startX=fx;startY=fy+faceRadius*(landscape?2.65f:3.15f);
 }
 boolean hitMenu(float x,float y){return x>=width-64&&x<=width&&y>=0&&y<=60;}
 boolean hitJoystick(float x,float y){float dx=x-joyX,dy=y-joyY,r=joyRadius*1.8f;return dx*dx+dy*dy<=r*r;}
 boolean hitButton(int i,float x,float y){float dx=x-buttonX[i],dy=y-buttonY[i],r=faceRadius*1.12f;return dx*dx+dy*dy<=r*r;}
 boolean hitStart(float x,float y){return Math.abs(x-startX)<=faceRadius*1.45f&&Math.abs(y-startY)<=faceRadius*.65f;}
}
