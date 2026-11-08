package org.openmegaproto;

import android.app.*;import android.os.*;import android.view.*;import android.graphics.*;import android.content.*;import android.net.Uri;import android.database.Cursor;import android.provider.OpenableColumns;import android.widget.*;import java.io.*;import java.util.*;

/** Independent visual research UI; controls are not connected to console hardware. */
public class MainActivity extends Activity {
 GameView view; static final int PICK=2;
 public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);view=new GameView(this);setContentView(view);}
 void chooseRom(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(i,PICK);}
 protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==PICK&&c==RESULT_OK&&d!=null)try{Uri u=d.getData();String name=displayName(u);if(!supported(name))throw new IOException("Formato não suportado. Use .bin, .gen, .md ou .smd");getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION);view.addRom(name,u); }catch(Exception e){view.toast="Não foi possível adicionar: "+e.getMessage();view.invalidate();}}
 String displayName(Uri u){Cursor c=getContentResolver().query(u,null,null,null,null);if(c!=null){try{if(c.moveToFirst()){int ix=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(ix>=0)return c.getString(ix);}}finally{c.close();}}return "ROM local";}
 boolean supported(String n){String s=n.toLowerCase(java.util.Locale.US);return s.endsWith(".bin")||s.endsWith(".gen")||s.endsWith(".md")||s.endsWith(".smd");}
 class GameView extends View {
  Paint p=new Paint(3); ArrayList<Rom> roms=new ArrayList<Rom>(); Machine machine=new Machine(); String search="",toast="",selectedName="Nenhuma ROM selecionada"; int selected=-1,palette=0; boolean aDown,bDown,xDown,yDown,leftDown,rightDown,upDown,downDown; float downX,downY; long frame=0; final int[] accents={0xff9dff00,0xff00f5c8,0xffff39d0};
  GameView(Context c){super(c);setFocusable(true);}
  void addRom(String n,Uri u){for(Rom r:roms)if(r.uri.equals(u)){selected=roms.indexOf(r);load(r);invalidate();return;}roms.add(new Rom(n,u));selected=roms.size()-1;load(roms.get(selected));toast="ROM adicionada à biblioteca local";invalidate();}
  void load(Rom r){selectedName=r.name; try{InputStream in=getContentResolver().openInputStream(r.uri);ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int k,total=0;while((k=in.read(b))>0){total+=k;if(total>32*1024*1024)throw new IOException("Arquivo excede 32 MB");out.write(b,0,k);}in.close();machine.load(out.toByteArray());toast="Carregada para análise — não é compatível para jogar";}catch(Exception e){toast="Erro ao ler ROM: "+e.getMessage();}}
  int W,H,accent;
  protected void onDraw(Canvas c){super.onDraw(c);W=getWidth();H=getHeight();accent=accents[palette];c.drawColor(0xff07110c); // subtle grid
   p.setStrokeWidth(1);p.setColor(0x183d6a36);for(int x=0;x<W;x+=30)c.drawLine(x,0,x,H,p);for(int y=0;y<H;y+=30)c.drawLine(0,y,W,y,p);
   // Header
   glowText(c,"OPEN MEGA",24,38,24,accent);p.setColor(0xffa8b8aa);p.setTextSize(10);c.drawText("RESEARCH DECK   /   BUILD 02",26,56,p);pill(c,W-151,21,131,27,"THEME  ◉",0xff11251a,accent);
   float left=24,top=75,mainW=W*.61f,mainH=H-100;
   panel(c,left,top,mainW,mainH);
   // abstract monitor/card
   float vx=left+15,vy=top+15,vw=mainW-30,vh=mainH*.47f;
   p.setShader(new LinearGradient(vx,vy,vx+vw,vy+vh,0xff10251a,0xff08120e,Shader.TileMode.CLAMP));c.drawRoundRect(vx,vy,vx+vw,vy+vh,13,13,p);p.setShader(null);
   p.setColor(0x6648ff00);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);for(int i=0;i<3;i++)c.drawRoundRect(vx+8+i*5,vy+8+i*5,vx+vw-8-i*5,vy+vh-8-i*5,12,12,p);p.setStyle(Paint.Style.FILL);
   // Generated abstract cover art, not game cover art
   p.setColor(0xff152b1a);Path shape=new Path();shape.moveTo(vx+vw*.61f,vy+vh*.18f);shape.lineTo(vx+vw*.85f,vy+vh*.52f);shape.lineTo(vx+vw*.64f,vy+vh*.81f);shape.lineTo(vx+vw*.4f,vy+vh*.48f);shape.close();c.drawPath(shape,p);p.setColor(accent);c.drawCircle(vx+vw*.63f,vy+vh*.49f,Math.min(vh*.19f,33),p);p.setColor(0xff07110c);c.drawCircle(vx+vw*.63f,vy+vh*.49f,Math.min(vh*.12f,21),p);p.setColor(0xffcaff9a);p.setTextSize(8);c.drawText("GENERATED ART  •  NOT OFFICIAL COVER",vx+12,vy+vh-12,p);
   p.setColor(0xffe7ffe1);p.setTextSize(17);c.drawText("LOCAL ROM LAB",vx+15,vy+28,p);p.setColor(0xffa7b9a8);p.setTextSize(10);c.drawText("68000 research display · no game compatibility",vx+15,vy+44,p);
   p.setColor(0xffb7c5b7);p.setTextSize(11);c.drawText("ACTIVE FILE",vx+4,vy+vh+19,p);p.setColor(0xffeaffdd);p.setTextSize(14);c.drawText(ellipsize(selectedName, (int)(vw/8)),vx+4,vy+vh+39,p);
   button(c,vx+vw-98,vy+vh+8,92,32,"LOAD ROM",accent);
   // library area
   float ly=vy+vh+53;p.setColor(accent);p.setTextSize(12);c.drawText("ROM LIBRARY",vx+3,ly,p);p.setColor(0xff779080);p.setTextSize(9);c.drawText(".BIN  .GEN  .MD  .SMD",vx+115,ly,p);
   float searchY=ly+12;panel(c,vx+2,searchY,vw-4,27);p.setColor(0xff95a99a);p.setTextSize(10);c.drawText("⌕  "+(search.length()==0?"Search library":search),vx+12,searchY+18,p);
   float listY=searchY+35;int shown=0;for(int i=0;i<roms.size()&&shown<4;i++){Rom r=roms.get(i);if(search.length()>0&&!r.name.toLowerCase().contains(search.toLowerCase()))continue;float yy=listY+shown*30;boolean sel=i==selected;panelFill(c,vx+2,yy,vw-4,25,sel?0xff183220:0xff0c1811);p.setColor(sel?accent:0xff526a59);c.drawCircle(vx+15,yy+12,4,p);p.setColor(0xffdeeadf);p.setTextSize(11);c.drawText(ellipsize(r.name,(int)(vw/9)),vx+27,yy+16,p);p.setColor(0xff6e8273);p.setTextSize(9);c.drawText("LOCAL",vx+vw-43,yy+16,p);shown++;}
   if(roms.size()==0){p.setColor(0xff829386);p.setTextSize(10);c.drawText("Your selected files appear here. No ROMs bundled.",vx+5,listY+16,p);}else if(shown==0){p.setColor(0xff829386);p.setTextSize(10);c.drawText("No matches — tap search to edit.",vx+5,listY+16,p);}
   // right-side controls mock
   float rx=left+mainW+18,rw=W-rx-24;panel(c,rx,top,rw,mainH);p.setColor(accent);p.setTextSize(12);c.drawText("TOUCH CONTROLS",rx+14,top+24,p);p.setColor(0xff7d9182);p.setTextSize(9);c.drawText("VISUAL DEMO ONLY",rx+14,top+40,p);
   float cy=top+mainH*.42f,rad=Math.min(27,rw*.12f); // d-pad
   p.setColor(0xff112219);c.drawCircle(rx+rw*.27f,cy,rad*1.8f,p);drawControl(c,rx+rw*.27f,cy-rad,"↑",upDown);drawControl(c,rx+rw*.27f,cy+rad,"↓",downDown);drawControl(c,rx+rw*.27f-rad,cy,"←",leftDown);drawControl(c,rx+rw*.27f+rad,cy,"→",rightDown);drawControl(c,rx+rw*.72f,cy-rad,"X",xDown);drawControl(c,rx+rw*.84f,cy,"A",aDown);drawControl(c,rx+rw*.60f,cy,"Y",yDown);drawControl(c,rx+rw*.72f,cy+rad,"B",bDown);
   p.setColor(0xffd1dfd3);p.setTextSize(10);c.drawText("A   B   X   Y",rx+14,top+mainH*.72f,p);p.setColor(0xff819286);p.setTextSize(9);c.drawText("Buttons animate on touch;",rx+14,top+mainH*.72f+17,p);c.drawText("not wired to emulation.",rx+14,top+mainH*.72f+30,p);
   p.setColor(0xffffd980);p.setTextSize(9);c.drawText("RESEARCH UI · NO GAME VIDEO / AUDIO",rx+14,top+mainH-15,p);
   p.setColor(0xff708174);p.setTextSize(9);c.drawText("CPU subset diagnostic only · no VDP, sound, or controller emulation",24,H-12,p);
   if(!toast.isEmpty()){p.setColor(0xffbaff80);p.setTextSize(9);c.drawText(ellipsize(toast,(int)(W/7)),W*.55f,H-12,p);}
  }
  void pill(Canvas c,float x,float y,float w,float h,String s,int bg,int fg){p.setColor(bg);c.drawRoundRect(x,y,x+w,y+h,h/2,h/2,p);p.setColor(fg);p.setTextSize(10);c.drawText(s,x+12,y+h/2+4,p);}\n  void panel(Canvas c,float x,float y,float w,float h){panelFill(c,x,y,w,h,0xff0b1710);p.setColor(0xff24402b);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);c.drawRoundRect(x,y,x+w,y+h,14,14,p);p.setStyle(Paint.Style.FILL);}
  void panelFill(Canvas c,float x,float y,float w,float h,int color){p.setColor(color);c.drawRoundRect(x,y,x+w,y+h,8,8,p);}
  void glowText(Canvas c,String s,float x,float y,float size,int color){p.setColor(color);p.setTextSize(size);p.setShadowLayer(12,color,0,0);c.drawText(s,x,y,p);p.clearShadowLayer();}
  void button(Canvas c,float x,float y,float w,float h,String s,int color){p.setColor(color);c.drawRoundRect(x,y,x+w,y+h,8,8,p);p.setColor(0xff07110c);p.setTextSize(10);c.drawText(s,x+10,y+h/2+4,p);}
  void drawControl(Canvas c,float x,float y,String t,boolean pressed){p.setColor(pressed?accent:0xff173021);c.drawCircle(x,y,17,p);p.setColor(pressed?0xff061008:0xffc4e4c8);p.setTextSize(12);c.drawText(t,x-4,y+4,p);}
  String ellipsize(String s,int max){if(max<4)max=4;return s.length()>max?s.substring(0,max-2)+"…":s;}
  public boolean onTouchEvent(MotionEvent e){float x=e.getX(),y=e.getY();if(e.getAction()==MotionEvent.ACTION_DOWN){downX=x;downY=y;updateButtons(x,y,true);invalidate();return true;}if(e.getAction()==MotionEvent.ACTION_MOVE){updateButtons(x,y,true);invalidate();return true;}if(e.getAction()==MotionEvent.ACTION_UP){if(y<59&&x>W-165){palette=(palette+1)%accents.length;toast="Tema neon alternado";}else if(x>W*.61f+20){/* visual demo controls */}else if(y>85&&y<H-20){float vx=39,mainW=W*.61f,vw=mainW-30,vy=90,vh=(H-100)*.47f;float buttonX=vx+vw-98, buttonY=vy+vh+8;if(x>=buttonX&&x<=buttonX+92&&y>=buttonY&&y<=buttonY+32){((MainActivity)getContext()).chooseRom();}else{float ly=vy+vh+53,searchY=ly+12,listY=searchY+35;if(y>=searchY&&y<=searchY+27){editSearch();}else if(y>=listY){int index=(int)((y-listY)/30);if(index>=0&&index<roms.size()){selected=index;load(roms.get(index));}}}}updateButtons(x,y,false);invalidate();return true;}return true;}
  void updateButtons(float x,float y,boolean on){aDown=bDown=xDown=yDown=leftDown=rightDown=upDown=downDown=false;if(!on||x<W*.61f+20)return;float rx=W*.61f+42,rw=W-rx-24,cy=75+(H-100)*.42f;float jx=rx+rw*.27f,dx=x-jx,dy=y-cy;if(Math.sqrt(dx*dx+dy*dy)<48){leftDown=dx< -6;rightDown=dx>6;upDown=dy< -6;downDown=dy>6;}float ax=x-(rx+rw*.72f),ay=y-cy;if(Math.sqrt(ax*ax+ay*ay)<39){if(ay< -7)xDown=true;else if(ay>7)bDown=true;else aDown=true;}if(Math.abs(x-(rx+rw*.60f))<22&&Math.abs(y-cy)<23)yDown=true;}
  void editSearch(){final EditText input=new EditText(getContext());input.setSingleLine(true);input.setText(search);new AlertDialog.Builder(getContext()).setTitle("Search ROM library").setView(input).setPositiveButton("Search",(d,w)->{search=input.getText().toString();invalidate();}).setNegativeButton("Cancel",null).show();}
 }
 static class Rom{String name;Uri uri;Rom(String n,Uri u){name=n;uri=u;}}
 static class Machine {byte[]rom=new byte[0],ram=new byte[65536];int[]d=new int[8],a=new int[8];int pc,sr=0x2700,frames;boolean stopped;String status="";void load(byte[]b){rom=b;pc=read32(0)&0xffffff;for(int i=0;i<8;i++){a[i]=d[i]=0;}stopped=false;frames=0;status="Loaded for diagnostic inspection only";}int rb(int x){x&=0xffffff;if(x<rom.length)return rom[x]&255;if(x>=0xff0000)return ram[x&65535]&255;return 0;}int rw(int x){return rb(x)<<8|rb(x+1);}int read32(int x){return rw(x)<<16|rw(x+2);}int fetch(){int v=rw(pc);pc=(pc+2)&0xffffff;return v;}void step(int count){if(rom.length<16||stopped)return;for(int k=0;k<count&&!stopped;k++){int op=fetch();if(op==0x4e71){}else if(op==0x4e72){sr=fetch();stopped=true;}else if((op&0xf100)==0x7000){int r=(op>>9)&7;d[r]=(byte)op;setNZ(d[r]);}else if((op&0xf000)==0x6000){int cond=(op>>8)&15;int disp=op&255;if(disp==0)disp=(short)fetch();else disp=(byte)disp;if(cond==0||cond==1){if(cond==1){a[7]-=4;}pc=(pc+disp)&0xffffff;}}else{status="Unsupported opcode 0x"+Integer.toHexString(op);stopped=true;}}frames++;}void setNZ(int v){sr&=~15;if(v==0)sr|=4;if(v<0)sr|=8;}}
}
