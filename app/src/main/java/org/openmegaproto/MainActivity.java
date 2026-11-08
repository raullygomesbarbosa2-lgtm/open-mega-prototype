package org.openmegaproto;

import android.app.*;import android.os.*;import android.view.*;import android.graphics.*;import android.content.*;import java.io.*;import java.nio.*;

/* Independent research prototype. Not a compatible Mega Drive emulator. */
public class MainActivity extends Activity {
 GameView view;
 public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);view=new GameView(this);setContentView(view);}
 void chooseRom(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,2);}
 protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==2&&c==-1&&d!=null)try{InputStream in=getContentResolver().openInputStream(d.getData());ByteArrayOutputStream o=new ByteArrayOutputStream();byte[] z=new byte[8192];int n;while((n=in.read(z))>0)o.write(z,0,n);view.machine.load(o.toByteArray());view.invalidate();}catch(Exception e){view.message="ROM read error: "+e.getMessage();}}
 class GameView extends View {
  Paint p=new Paint(3); Machine machine=new Machine(); long last; String message="Prototype: 68000 subset + basic VDP research"; float sx,sy; int held=0;
  GameView(Context c){super(c);setFocusable(true);}
  protected void onDraw(Canvas c){super.onDraw(c);c.drawColor(Color.rgb(8,12,24));float scale=Math.min(getWidth()/320f,getHeight()/224f);c.save();c.translate((getWidth()-320*scale)/2,(getHeight()-224*scale)/2);c.scale(scale,scale);machine.render(c,p);c.restore(); p.setColor(Color.WHITE);p.setTextSize(16);c.drawText(message,12,22,p);drawButton(c,42,getHeight()-70,"◀");drawButton(c,104,getHeight()-70,"▶");drawButton(c,getWidth()-132,getHeight()-70,"A");drawButton(c,getWidth()-78,getHeight()-70,"B");drawButton(c,getWidth()/2f-45,getHeight()-42,"LOAD ROM"); if(machine.rom.length==0){p.setColor(Color.YELLOW);p.setTextSize(18);c.drawText("No ROM loaded — select a homebrew ROM",24,getHeight()/2f,p);}else{machine.step(4000);postInvalidateDelayed(16);}}
  void drawButton(Canvas c,float x,float y,String s){p.setColor(0x99555566);c.drawCircle(x,y,25,p);p.setColor(Color.WHITE);p.setTextSize(19);c.drawText(s,x-12,y+7,p);}
  public boolean onTouchEvent(android.view.MotionEvent e){float x=e.getX(),y=e.getY();if(e.getAction()==0){if(y>getHeight()-95&&x>getWidth()/2f-95&&x<getWidth()/2f+20){((MainActivity)getContext()).chooseRom();return true;}if(x<getWidth()/3){held|=1;}else if(x>getWidth()*2/3){held|=2;}machine.buttons=held;return true;} if(e.getAction()==1||e.getAction()==3){held=0;machine.buttons=0;return true;}return true;}
 }
 static class Machine {
  byte[] rom=new byte[0],ram=new byte[65536]; int[] d=new int[8],a=new int[8]; int pc,sr=0x2700; int buttons,frames; boolean stopped; String status="";
  void load(byte[] b){rom=b;pc=read32(0)&0xffffff;for(int i=0;i<8;i++){a[i]=0;d[i]=0;}stopped=false;frames=0;status="Loaded "+b.length+" bytes; ROM header reset vector "+Integer.toHexString(pc);}
  int rb(int x){x&=0xffffff;if(x<rom.length)return rom[x]&255;if(x>=0xff0000)return ram[x&65535]&255;return 0;}
  int rw(int x){return rb(x)<<8|rb(x+1);} int read32(int x){return rw(x)<<16|rw(x+2);}
  void wb(int x,int v){x&=0xffffff;if(x>=0xff0000)ram[x&65535]=(byte)v;} void ww(int x,int v){wb(x,v>>8);wb(x+1,v);}
  int fetch(){int v=rw(pc);pc=(pc+2)&0xffffff;return v;}
  void step(int count){if(rom.length<16||stopped)return;for(int k=0;k<count&&!stopped;k++){int op=fetch();if(op==0x4e71){} else if(op==0x4e72){sr=fetch();stopped=true;} else if((op&0xf100)==0x7000){int r=(op>>9)&7;d[r]=(byte)op;setNZ(d[r]);} else if((op&0xf000)==0x6000){int cond=(op>>8)&15;int disp=op&255;if(disp==0)disp=(short)fetch();else disp=(byte)disp;if(cond==0||cond==1){if(cond==1){a[7]-=4;ww(a[7],pc>>16);ww(a[7]+2,pc);}pc=(pc+disp)&0xffffff;}} else if(op==0x4e75){pc=read32(a[7]);a[7]+=4;} else if((op&0xfff8)==0x4e70){/* RESET ignored */} else if((op&0xf1ff)==0x203c){int r=(op>>9)&7;d[r]=read32(pc);pc+=4;} else {status="Unsupported 68000 opcode 0x"+Integer.toHexString(op)+" at "+Integer.toHexString((pc-2)&0xffffff);stopped=true;}}
   frames++;
  }
  void setNZ(int v){sr&=~15;if(v==0)sr|=4;if(v<0)sr|=8;}
  void render(Canvas c,Paint p){c.drawColor(0xff10182a);p.setColor(0xff182a52);c.drawRect(0,0,320,224,p);p.setColor(0xffd9e8ff);p.setTextSize(12);c.drawText("OPEN MEGA — ENGINE EXPERIMENT",12,18,p);p.setTextSize(9);c.drawText("NOT COMPATIBLE: 68000 instruction subset only",12,33,p);c.drawText("No VDP tile/sprite renderer, Z80, YM2612 or PSG",12,46,p);c.drawText("ROM: "+(rom.length==0?"not loaded":rom.length+" bytes")+"  frame="+frames,12,64,p);c.drawText("68000 PC=$"+Integer.toHexString(pc)+" SR=$"+Integer.toHexString(sr),12,80,p);c.drawText("Buttons: "+buttons+"  RAM: 64 KiB",12,95,p);if(!status.isEmpty()){p.setColor(0xffffbd68);p.setTextSize(8);c.drawText(status,12,115,p);}p.setColor(0xff48649b);c.drawRect(0,190,320,224,p);p.setColor(0xffb9d5ff);p.setTextSize(9);c.drawText("Research display only — game graphics/audio are not emulated",8,207,p);}
 }
}
