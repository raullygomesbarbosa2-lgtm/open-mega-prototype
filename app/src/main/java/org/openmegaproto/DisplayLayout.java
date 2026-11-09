package org.openmegaproto;

/** Pure dp-coordinate layout shared by drawing and touch hit-testing. */
final class DisplayLayout {
 static final int VIDEO_WIDTH=320, VIDEO_HEIGHT=224;
 static final int X=0,Y=1,Z=2,A=3,B=4,C=5;
 final float width,height,videoLeft,videoTop,videoRight,videoBottom,joyX,joyY,joyRadius,faceRadius,startX,startY;
 final float[] buttonX=new float[6],buttonY=new float[6];
 DisplayLayout(float w,float h){
  width=Math.max(1,w);height=Math.max(1,h);boolean landscape=width>height;
  // Landscape gets nearly the full width, capped by available height so the
  // 320:224 image remains exact-aspect and the complete control pad stays below.
  float maxW=landscape?width*.94f:width*.98f,maxH=height*(landscape?.66f:.46f);
  float scale=Math.min(maxW/VIDEO_WIDTH,maxH/VIDEO_HEIGHT),vw=VIDEO_WIDTH*scale,vh=VIDEO_HEIGHT*scale;
  float cx=width*.5f,cy=height*(landscape?.36f:.265f);
  videoLeft=cx-vw*.5f;videoRight=cx+vw*.5f;videoTop=cy-vh*.5f;videoBottom=cy+vh*.5f;
  joyX=width*.20f;joyY=height*(landscape?.86f:.77f);
  joyRadius=landscape?Math.max(12,Math.min(22,Math.min(height*.05f,width*.06f))):Math.max(12,Math.min(38,Math.min(height*.085f,width*.11f)));
  faceRadius=landscape?Math.max(10,Math.min(18,Math.min(height*.04f,width*.045f))):Math.max(10,Math.min(25,Math.min(height*.052f,width*.065f)));
  float fx=width*.70f,fy=height*(landscape?.86f:.77f),dx=faceRadius*2.35f,dy=faceRadius*1.25f;
  // Spacing exceeds the visible and touch radii, so none of the six targets overlap.
  buttonX[X]=fx-dx;buttonY[X]=fy-dy;buttonX[Y]=fx;buttonY[Y]=fy-dy;buttonX[Z]=fx+dx;buttonY[Z]=fy-dy;
  buttonX[A]=fx-dx;buttonY[A]=fy+dy;buttonX[B]=fx;buttonY[B]=fy+dy;buttonX[C]=fx+dx;buttonY[C]=fy+dy;
  startX=fx;startY=fy+faceRadius*(landscape?2.65f:3.15f);
 }
 boolean hitJoystick(float x,float y){float dx=x-joyX,dy=y-joyY,r=joyRadius*1.8f;return dx*dx+dy*dy<=r*r;}
 boolean hitButton(int i,float x,float y){float dx=x-buttonX[i],dy=y-buttonY[i],r=faceRadius*1.12f;return dx*dx+dy*dy<=r*r;}
 boolean hitStart(float x,float y){return Math.abs(x-startX)<=faceRadius*1.45f&&Math.abs(y-startY)<=faceRadius*.65f;}
}
