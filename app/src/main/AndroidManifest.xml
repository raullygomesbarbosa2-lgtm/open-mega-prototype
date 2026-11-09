package org.openmegaproto;

import android.app.*;import android.os.*;import android.view.*;import android.graphics.*;import android.content.*;import android.net.Uri;import android.database.Cursor;import android.provider.OpenableColumns;import java.io.*;import java.util.*;

/** Native portrait demo screen with responsive display and touch controls. */
public class MainActivity extends Activity {
    static final int PICK=2;
    GameView view;
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);view=new GameView(this);setContentView(view);}
    void chooseRom(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(i,PICK);}
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==PICK&&c==RESULT_OK&&d!=null)try{Uri u=d.getData();String name=displayName(u);if(name==null||name.trim().isEmpty())name="ROM local";getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION);view.addRom(name,u);}catch(Exception e){view.toast="Falha ao abrir arquivo: "+e.getMessage();view.invalidate();}}
    String displayName(Uri u){Cursor c=getContentResolver().query(u,null,null,null,null);if(c!=null){try{if(c.moveToFirst()){int ix=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(ix>=0)return c.getString(ix);}}finally{c.close();}}return "ROM local";}

    class GameView extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        final Machine machine=new Machine();
        final Bitmap video=Bitmap.createBitmap(Vdp.WIDTH,Vdp.HEIGHT,Bitmap.Config.ARGB_8888);
        final int white=0xffeef2f6, muted=0xff9da9b7, button=0x663c4a5b;
        float d=1,W,H,sx,sy,sw,sh,joyX,joyY,joyR,faceX,faceY,faceR;
        boolean up,down,left,right,a,b,cButton,xButton,yButton,zButton,start,menuOpen=false,cheatScreen=false,hasRom=false;
        String romName="Nenhuma ROM carregada",toast="",status="NÚCLEO ORIGINAL EM DESENVOLVIMENTO";
        GameView(Context c){super(c);d=getResources().getDisplayMetrics().density;setFocusable(true);machine.vdp.reset();refreshVideo();}
        void refreshVideo(){video.setPixels(machine.vdp.pixels,0,Vdp.WIDTH,0,0,Vdp.WIDTH,Vdp.HEIGHT);}
        void addRom(String name,Uri uri){
            try(InputStream in=getContentResolver().openInputStream(uri)){
                if(in==null)throw new IOException("arquivo indisponível");ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n;
                while((n=in.read(buf))>0){if(out.size()+n>RomImage.MAX_ROM_BYTES+1024*1024)throw new IOException("arquivo ou ZIP maior que 33 MB");out.write(buf,0,n);}
                byte[] bytes=RomImage.decode(out.toByteArray(),name);
                machine.load(bytes);romName=name;hasRom=true;status="ROM reconhecida; compatibilidade depende do núcleo parcial";toast="ROM reconhecida e enviada ao núcleo original.";
            }catch(Exception e){hasRom=false;machine.reset();toast="ROM não reconhecida: "+e.getMessage();}
            invalidate();
        }
        void layout(){W=getWidth()/d;H=getHeight()/d;sx=0;sy=0;sw=W;sh=H;joyX=Math.max(48,W*.095f);joyY=H*.72f;joyR=Math.min(37,H*.095f);faceX=Math.min(W-48,W*.905f);faceY=H*.72f;faceR=Math.min(26,H*.075f);}
        @Override protected void onDraw(Canvas raw){super.onDraw(raw);raw.save();raw.scale(d,d);layout();Canvas g=raw;
            machine.updatePad(a,b,cButton,xButton,yButton,zButton,start,left,right,up,down);if(hasRom&&!machine.stopped)machine.step(1200);machine.vdp.render();refreshVideo();if(hasRom&&machine.stopped)status="Parou em opcode ainda não suportado: "+machine.status;
            g.drawColor(0xff05070a);
            if(!cheatScreen){p.setColor(0xff000000);g.drawRect(sx,sy,sx+sw,sy+sh,p);p.setFilterBitmap(false);g.drawBitmap(video,null,new RectF(sx,sy,sx+sw,sy+sh),p);
                if(!hasRom||machine.stopped){p.setColor(0xcc080b10);g.drawRect(sx,sy+sh*.40f,sx+sw,sy+sh*.60f,p);p.setColor(white);p.setTextAlign(Paint.Align.CENTER);p.setTextSize(Math.min(14,sh*.06f));String line=hasRom?"EXECUÇÃO PAROU — NÚCLEO INCOMPLETO":"SELECIONE UMA ROM NO MENU";g.drawText(line,W/2,sy+sh*.49f,p);p.setTextSize(Math.min(10,sh*.04f));String detail=hasRom?machine.status:"Compatibilidade geral ainda não implementada";g.drawText(ellipsize(detail,(int)(W/9)),W/2,sy+sh*.55f,p);p.setTextAlign(Paint.Align.LEFT);}
                drawJoystick(g);drawFaceButtons(g);drawMenuButton(g);
            }else drawCheatPage(g);
            if(!toast.isEmpty()){p.setColor(white);p.setTextSize(10);p.setTextAlign(Paint.Align.CENTER);g.drawText(ellipsize(toast,(int)(W/7)),W/2,H-10,p);p.setTextAlign(Paint.Align.LEFT);}
            postInvalidateDelayed(33);raw.restore();}
        void drawMenuButton(Canvas g){p.setColor(0x990b1018);g.drawRoundRect(W-54,12,W-12,50,12,12,p);p.setColor(white);p.setTextSize(23);g.drawText("≡",W-43,40,p);}
        void drawJoystick(Canvas g){p.setColor(button);g.drawCircle(joyX,joyY,joyR*1.65f,p);drawDir(g,joyX,joyY-joyR,"↑",up);drawDir(g,joyX,joyY+joyR,"↓",down);drawDir(g,joyX-joyR,joyY,"←",left);drawDir(g,joyX+joyR,joyY,"→",right);}
        void drawDir(Canvas g,float x,float y,String t,boolean held){p.setColor(held?0xffd8dee6:0x884c5b6b);g.drawRoundRect(x-joyR*.43f,y-joyR*.43f,x+joyR*.43f,y+joyR*.43f,8,8,p);p.setColor(held?0xff12171d:white);p.setTextSize(18);g.drawText(t,x-6,y+6,p);}
        void drawFaceButtons(Canvas g){drawButton(g,faceX-faceR*1.3f,faceY-faceR*.65f,"X",xButton);drawButton(g,faceX+faceR*1.3f,faceY-faceR*.65f,"A",a);drawButton(g,faceX,faceY+faceR*.85f,"B",b);drawButton(g,faceX-faceR*1.3f,faceY+faceR*.85f,"Y",yButton);drawButton(g,faceX+faceR*1.3f,faceY+faceR*.85f,"C",cButton);float x=faceX-29,y=faceY+faceR*2.0f;p.setColor(start?0xffd8dee6:0x884c5b6b);g.drawRoundRect(x,y,x+58,y+22,11,11,p);p.setColor(start?0xff10151c:white);p.setTextSize(9);g.drawText("START",x+13,y+15,p);}
        void drawButton(Canvas g,float x,float y,String label,boolean held){p.setColor(held?0xffd8dee6:button);g.drawCircle(x,y,faceR,p);p.setColor(held?0xff11161d:white);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));p.setTextSize(13);g.drawText(label,x-5,y+5,p);p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));}
        void drawCheatPage(Canvas g){g.drawColor(0xff080b10);p.setColor(white);p.setTextSize(22);g.drawText("CHEATS",24,48,p);p.setColor(muted);p.setTextSize(13);g.drawText("Não disponíveis ainda: o núcleo não executa ROMs.",24,82,p);g.drawText("Toque em ≡ e escolha Voltar ao jogo.",24,106,p);drawMenuButton(g);}
        String ellipsize(String s,int max){if(max<4)max=4;return s.length()>max?s.substring(0,max-1)+"…":s;}
        boolean inCircle(float x,float y,float cx,float cy,float r){float dx=x-cx,dy=y-cy;return dx*dx+dy*dy<=r*r;}
        void clear(){up=down=left=right=a=b=cButton=xButton=yButton=zButton=start=false;machine.updatePad(false,false,false,false,false,false,false,false,false,false,false);}
        void track(MotionEvent e,int skip){clear();for(int i=0;i<e.getPointerCount();i++){if(i==skip)continue;float x=e.getX(i)/d,y=e.getY(i)/d;
                if(inCircle(x,y,joyX,joyY,joyR*1.8f)){float dx=x-joyX,dy=y-joyY;if(Math.abs(dx)>Math.abs(dy)*.55f){left|=dx<0;right|=dx>0;}if(Math.abs(dy)>Math.abs(dx)*.55f){up|=dy<0;down|=dy>0;}}
                if(inCircle(x,y,faceX-faceR*1.3f,faceY-faceR*.65f,faceR*1.15f))xButton=true;if(inCircle(x,y,faceX+faceR*1.3f,faceY-faceR*.65f,faceR*1.15f))a=true;if(inCircle(x,y,faceX,faceY+faceR*.85f,faceR*1.15f))b=true;if(inCircle(x,y,faceX-faceR*1.3f,faceY+faceR*.85f,faceR*1.15f))yButton=true;if(inCircle(x,y,faceX+faceR*1.3f,faceY+faceR*.85f,faceR*1.15f))cButton=true;if(x>=faceX-32&&x<=faceX+32&&y>=faceY+faceR*1.7f&&y<=faceY+faceR*2.7f)start=true;
            }machine.updatePad(a,b,cButton,xButton,yButton,zButton,start,left,right,up,down);}
        void showMenu(){new AlertDialog.Builder(MainActivity.this).setTitle("Menu do jogo").setItems(new String[]{"Carregar outra ROM","Cheats","Voltar ao jogo"},(dlg,which)->{if(which==0)chooseRom();else if(which==1){cheatScreen=true;toast="";invalidate();}else{cheatScreen=false;invalidate();}}).show();}
        @Override public boolean onTouchEvent(MotionEvent e){float x=e.getX()/d,y=e.getY()/d;int action=e.getActionMasked();if(action==MotionEvent.ACTION_DOWN){if(x>W-64&&y<60){showMenu();return true;}track(e,-1);invalidate();return true;}if(action==MotionEvent.ACTION_MOVE||action==MotionEvent.ACTION_POINTER_DOWN){track(e,-1);invalidate();return true;}if(action==MotionEvent.ACTION_POINTER_UP){track(e,e.getActionIndex());invalidate();return true;}if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL){clear();invalidate();return true;}return true;}
    }
 static class Machine {
  byte[]rom=new byte[0],ram=new byte[65536];int[]d=new int[8],a=new int[8];int pc,sr=0x2700,frames;boolean stopped;String status="";
  final Vdp vdp=new Vdp();final Controller pad=new Controller();final Cpu68000 cpu=new Cpu68000(new Cpu68000.Bus(){public int read8(int x){return rb(x);}public int read16(int x){return rw(x);}public void write8(int x,int v){wb(x,v);}public void write16(int x,int v){ww(x,v);}});
  void reset(){rom=new byte[0];java.util.Arrays.fill(ram,(byte)0);java.util.Arrays.fill(d,0);java.util.Arrays.fill(a,0);pc=0;sr=0x2700;frames=0;stopped=true;status="Nenhuma ROM carregada";vdp.reset();}
  void load(byte[]b){if(b==null||b.length<0x200)throw new IllegalArgumentException("ROM muito pequena ou truncada");rom=b;int initialPc=read32(4)&0xffffff;int initialSp=read32(0)&0xffffff;if((initialPc&1)!=0||initialPc<8||initialPc>=b.length||(initialSp&1)!=0||(initialSp<0xff0000&&!(initialSp>=0xe00000&&initialSp<0xe10000)))throw new IllegalArgumentException("vetores iniciais do 68000 inválidos; confirme que o arquivo é uma ROM Mega Drive sem cabeçalho SMD");java.util.Arrays.fill(ram,(byte)0);cpu.reset(initialSp,initialPc);sync();stopped=false;frames=0;status="ROM carregada no núcleo original ainda incompleto";vdp.reset();}
  int rb(int x){x&=0xffffff;if(x<rom.length)return rom[x]&255;if(x>=0xe00000)return ram[x&65535]&255;return 0;}
  int rw(int x){x&=0xffffff;if(x>=0xa10000&&x<=0xa1001f)return pad.readData();if((x&0xffffe0)==0xc00000)return ((x&4)==0)?vdp.readDataPort():0;return rb(x)<<8|rb(x+1);}
  int read32(int x){return rw(x)<<16|rw(x+2);}int fetch(){int v=rw(pc);pc=(pc+2)&0xffffff;return v;}
  void wb(int x,int v){x&=0xffffff;if(x>=0xe00000)ram[x&65535]=(byte)v;}
  void ww(int x,int v){x&=0xffffff;v&=0xffff;if((x&0xfffff8)==0xc00000){if((x&4)==0)vdp.writeDataPort(v);else vdp.writeControl(v);}else if((x&0xfffffc)==0xa10000){if((x&3)==1)pad.writeData(v);}else if(x>=0xe00000){ram[x&65535]=(byte)(v>>8);ram[(x+1)&65535]=(byte)v;}}
  int readIoData(){return pad.readData();}void updatePad(boolean ab,boolean bb,boolean cb,boolean left,boolean right,boolean up,boolean down){updatePad(ab,bb,cb,false,false,false,false,left,right,up,down);}void updatePad(boolean ab,boolean bb,boolean cb,boolean xb,boolean yb,boolean zb,boolean start,boolean left,boolean right,boolean up,boolean down){pad.set(up,down,left,right,ab,bb,cb,xb,yb,zb,start,false);}
  void sync(){System.arraycopy(cpu.d,0,d,0,8);System.arraycopy(cpu.a,0,a,0,8);pc=cpu.pc;sr=cpu.sr;stopped=cpu.stopped;status=cpu.status;}
  void step(int count){if(rom.length<16||stopped)return;cpu.step(count);sync();frames++;}
 }
}
