package org.openmegaproto;

import android.app.*;import android.os.*;import android.view.*;import android.graphics.*;import android.content.*;import android.net.Uri;import android.database.Cursor;import android.provider.OpenableColumns;import java.io.*;import java.util.*;

/** Native portrait demo screen with responsive display and touch controls. */
public class MainActivity extends Activity {
    static final int PICK=2;
    GameView view;
    @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);view=new GameView(this);setContentView(view);}
    void chooseRom(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(i,PICK);}
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==PICK&&c==RESULT_OK&&d!=null)try{Uri u=d.getData();String name=displayName(u);if(!supported(name))throw new IOException("Formato não suportado. Selecione .bin, .gen, .md ou .smd");getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION);view.addRom(name,u);}catch(Exception e){view.toast="Falha ao abrir arquivo: "+e.getMessage();view.invalidate();}}
    String displayName(Uri u){Cursor c=getContentResolver().query(u,null,null,null,null);if(c!=null){try{if(c.moveToFirst()){int ix=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(ix>=0)return c.getString(ix);}}finally{c.close();}}return "ROM local";}
    boolean supported(String n){String s=n.toLowerCase(Locale.US);return s.endsWith(".bin")||s.endsWith(".gen")||s.endsWith(".md")||s.endsWith(".smd");}

    class GameView extends View {
        final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
        final Machine machine=new Machine();
        final Bitmap video=Bitmap.createBitmap(Vdp.WIDTH,Vdp.HEIGHT,Bitmap.Config.ARGB_8888);
        float density=1,W,H,screenX,screenY,screenW,screenH,controlY,dpadX,dpadY,dpadR,buttonR,actionX;
        String selectedName="DEMO ORIGINAL",toast="",notice="ROMs comerciais ainda não executam";
        boolean up,down,left,right,a,b,cButton,start;
        final int green=0xffa8ff60, panel=0xff111b27, dim=0xffa8b5c5;
        GameView(Context context){super(context);density=getResources().getDisplayMetrics().density;setFocusable(true);machine.loadInteractiveDemo();refreshVideo();}
        void refreshVideo(){video.setPixels(machine.vdp.pixels,0,Vdp.WIDTH,0,0,Vdp.WIDTH,Vdp.HEIGHT);}
        void addRom(String name,Uri uri){
            // Keep the known-good original demo on screen. This CPU/VDP prototype cannot execute commercial ROMs yet.
            try(InputStream in=getContentResolver().openInputStream(uri)){
                if(in==null)throw new IOException("arquivo indisponível");byte[] buf=new byte[8192];int n,total=0;while((n=in.read(buf))>0){total+=n;if(total>32*1024*1024)throw new IOException("arquivo maior que 32 MB");}
                selectedName=name;notice="Arquivo selecionado — execução de jogos ainda não implementada";toast="A demo continua na tela; a ROM não foi executada.";
            }catch(Exception e){toast="Não foi possível ler a ROM: "+e.getMessage();}
            invalidate();
        }
        void layout(float width,float height){
            W=width;H=height;
            float margin=12;
            screenX=margin;screenY=60;screenW=W-margin*2;
            screenH=screenW*Vdp.HEIGHT/Vdp.WIDTH;
            // Use all the available vertical space without cropping the Mega Drive's 4:3-ish landscape output.
            float maxScreenH=Math.max(120,H*.40f);
            if(screenH>maxScreenH){screenH=maxScreenH;screenW=screenH*Vdp.WIDTH/Vdp.HEIGHT;screenX=(W-screenW)/2;}
            controlY=Math.min(H-118,screenY+screenH+Math.max(102,(H-screenY-screenH)*.52f));
            dpadX=W*.25f;dpadY=controlY;dpadR=Math.min(40,W*.115f);
            actionX=W*.73f;buttonR=Math.min(26,W*.074f);
        }
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);canvas.save();canvas.scale(density,density);
            layout(getWidth()/density,getHeight()/density);
            machine.updatePad(a,b,cButton,start,left,right,up,down);machine.step(20);machine.vdp.render();refreshVideo();
            Canvas g=canvas;g.drawColor(0xff080d14);
            p.setStyle(Paint.Style.FILL);p.setColor(0xff111b27);g.drawRoundRect(8,8,W-8,49,12,12,p);
            p.setColor(green);p.setTextSize(18);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));g.drawText("MEGA DRIVE · DEMO",18,34,p);
            p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));p.setColor(dim);p.setTextSize(10);g.drawText("PROJETO ORIGINAL — AINDA EM DESENVOLVIMENTO",18,46,p);
            // Large, correctly proportioned native screen, using the full available phone width.
            p.setColor(0xff000000);g.drawRoundRect(screenX-3,screenY-3,screenX+screenW+3,screenY+screenH+3,8,8,p);
            p.setFilterBitmap(false);g.drawBitmap(video,null,new RectF(screenX,screenY,screenX+screenW,screenY+screenH),p);
            p.setColor(0xff253342);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);g.drawRoundRect(screenX-3,screenY-3,screenX+screenW+3,screenY+screenH+3,8,8,p);p.setStyle(Paint.Style.FILL);
            float infoY=screenY+screenH+17;
            p.setColor(0xffffd479);p.setTextSize(10);g.drawText("DEMO DE TESTE · SEGURE ↑ PARA MUDAR O PADRÃO",14,infoY,p);
            p.setColor(0xffdce6f2);p.setTextSize(11);g.drawText(ellipsize(selectedName,(int)(W/7)),14,infoY+20,p);
            p.setColor(0xff91a0b2);p.setTextSize(9);g.drawText(ellipsize(notice,(int)(W/6)),14,infoY+35,p);
            float pickerY=infoY+44; p.setColor(0xff263546);g.drawRoundRect(14,pickerY,W-14,pickerY+36,10,10,p);p.setColor(green);p.setTextSize(12);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));g.drawText("ESCOLHER ROM",26,pickerY+23,p);p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));p.setColor(dim);p.setTextSize(9);g.drawText("Abre o arquivo; este protótipo ainda não executa jogos",W*.42f,pickerY+22,p);
            float controlsTop=pickerY+55;
            p.setColor(dim);p.setTextSize(10);g.drawText("CONTROLES NA TELA",14,controlsTop,p);
            // Four-way virtual joystick: large touch region and visible directional arms.
            dpadY=Math.max(controlY,controlsTop+85);dpadR=Math.min(39,W*.115f);
            p.setColor(panel);g.drawCircle(dpadX,dpadY,dpadR*1.78f,p);
            drawPadArm(g,dpadX,dpadY-dpadR,"↑",up);drawPadArm(g,dpadX,dpadY+dpadR,"↓",down);drawPadArm(g,dpadX-dpadR,dpadY,"←",left);drawPadArm(g,dpadX+dpadR,dpadY,"→",right);
            // Three-button Mega Drive pad plus Start, all drawn and hit-tested in the same coordinates.
            float cx=actionX,cy=dpadY;
            drawAction(g,cx-buttonR*1.35f,cy-buttonR*.82f,"A",a);drawAction(g,cx+buttonR*1.35f,cy-buttonR*.82f,"B",b);drawAction(g,cx,cy+buttonR*.84f,"C",cButton);
            float sx=Math.max(16,cx-32),sy=dpadY+dpadR*1.55f;
            p.setColor(start?green:0xff334459);g.drawRoundRect(sx,sy,sx+64,sy+24,12,12,p);p.setColor(start?0xff10200b:0xffe3eaf2);p.setTextSize(10);g.drawText("START",sx+14,sy+16,p);
            if(!toast.isEmpty()){p.setColor(0xffffd479);p.setTextSize(9);g.drawText(ellipsize(toast,(int)(W/6)),14,H-12,p);}
            canvas.restore();
        }
        void drawPadArm(Canvas g,float x,float y,String label,boolean pressed){p.setColor(pressed?green:0xff40546c);g.drawRoundRect(x-dpadR*.46f,y-dpadR*.46f,x+dpadR*.46f,y+dpadR*.46f,8,8,p);p.setColor(pressed?0xff10200b:0xffedf3fa);p.setTextSize(20);g.drawText(label,x-6,y+7,p);}
        void drawAction(Canvas g,float x,float y,String label,boolean pressed){p.setColor(pressed?green:0xffd64e5a);g.drawCircle(x,y,buttonR,p);p.setColor(pressed?0xff10200b:0xffffffff);p.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));p.setTextSize(14);g.drawText(label,x-5,y+5,p);p.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));}
        String ellipsize(String s,int max){if(max<4)max=4;return s.length()>max?s.substring(0,max-1)+"…":s;}
        void clearInput(){up=down=left=right=a=b=cButton=start=false;machine.updatePad(false,false,false,false,false,false,false,false);}
        void updatePointers(MotionEvent event,int excluded){
            clearInput();
            for(int i=0;i<event.getPointerCount();i++){if(i==excluded)continue;float x=event.getX(i)/density,y=event.getY(i)/density;
                if(y>=dpadY-dpadR*1.8f&&y<=dpadY+dpadR*1.8f&&x>=dpadX-dpadR*1.8f&&x<=dpadX+dpadR*1.8f){float dx=x-dpadX,dy=y-dpadY;if(Math.abs(dx)<dpadR*.48f&&Math.abs(dy)<dpadR*.48f){}else{if(Math.abs(dx)>Math.abs(dy)*.55f){left|=dx<0;right|=dx>0;}if(Math.abs(dy)>Math.abs(dx)*.55f){up|=dy<0;down|=dy>0;}}}
                float cx=actionX,cy=dpadY;
                if(hit(x,y,cx-buttonR*1.35f,cy-buttonR*.82f,buttonR*1.35f))a=true;
                if(hit(x,y,cx+buttonR*1.35f,cy-buttonR*.82f,buttonR*1.35f))b=true;
                if(hit(x,y,cx,cy+buttonR*.84f,buttonR*1.35f))cButton=true;
                float sx=Math.max(16,cx-32),sy=dpadY+dpadR*1.55f;if(x>=sx&&x<=sx+64&&y>=sy&&y<=sy+24)start=true;
            }
            machine.updatePad(a,b,cButton,start,left,right,up,down);
        }
        boolean hit(float x,float y,float cx,float cy,float radius){float dx=x-cx,dy=y-cy;return dx*dx+dy*dy<=radius*radius;}
        @Override public boolean onTouchEvent(MotionEvent e){float x=e.getX()/density,y=e.getY()/density;
            int action=e.getActionMasked();
            if(action==MotionEvent.ACTION_DOWN){if(y>screenY+screenH+35&&y<screenY+screenH+90&&x>10&&x<W-10){((MainActivity)getContext()).chooseRom();return true;}updatePointers(e,-1);invalidate();return true;}
            if(action==MotionEvent.ACTION_POINTER_DOWN||action==MotionEvent.ACTION_MOVE){updatePointers(e,-1);invalidate();return true;}
            if(action==MotionEvent.ACTION_POINTER_UP){updatePointers(e,e.getActionIndex());invalidate();return true;}
            if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL){clearInput();invalidate();return true;}return true;
        }
    }
 static class Machine {byte[]rom=new byte[0],ram=new byte[65536];int[]d=new int[8],a=new int[8];int pc,sr=0x2700,frames;boolean stopped;String status="";final Vdp vdp=new Vdp();final Controller pad=new Controller();void load(byte[]b){rom=b;pc=read32(4)&0xffffff;for(int i=0;i<8;i++){a[i]=d[i]=0;}a[7]=read32(0);stopped=false;frames=0;status="Loaded for limited original 68000 instruction testing";vdp.reset();}void loadInteractiveDemo(){int[] w={0x31fc,0x4020,0x00c0,0x0004,0x31fc,0x0000,0x00c0,0x0004,0x3039,0x00a1,0x0003,0x0240,0x0001,0x660a,0x31fc,0x1100,0x00c0,0x0000,0x60ea,0x31fc,0x2200,0x00c0,0x0000,0x60e0};byte[] b=new byte[0x100];b[3]=(byte)0xff;b[7]=8;for(int i=0;i<w.length;i++){b[8+i*2]=(byte)(w[i]>>>8);b[9+i*2]=(byte)w[i];}load(b);vdp.loadDiagnosticPattern();status="Original interactive VDP homebrew diagnostic";}int rb(int x){x&=0xffffff;if(x<rom.length)return rom[x]&255;if(x>=0xff0000)return ram[x&65535]&255;return 0;}int rw(int x){if(x>=0xa10000&&x<=0xa1001f)return pad.readData();if((x&0xffffe0)==0xc00000)return ((x&4)==0)?vdp.readDataPort():0;return rb(x)<<8|rb(x+1);}int read32(int x){return rw(x)<<16|rw(x+2);}int fetch(){int v=rw(pc);pc=(pc+2)&0xffffff;return v;}void ww(int x,int v){x&=0xffffff;v&=0xffff;if((x&0xfffff8)==0xc00000){if((x&4)==0)vdp.writeDataPort(v);else vdp.writeControl(v);}else if((x&0xfffffc)==0xa10000){if((x&3)==1)pad.writeData(v);}else if(x>=0xff0000){ram[x&65535]=(byte)(v>>8);ram[(x+1)&65535]=(byte)v;}}int readIoData(){return pad.readData();}void updatePad(boolean aButton,boolean bButton,boolean xButton,boolean yButton,boolean left,boolean right,boolean up,boolean down){pad.set(up,down,left,right,aButton,bButton,xButton,yButton);}void step(int count){if(rom.length<16||stopped)return;for(int k=0;k<count&&!stopped;k++){int op=fetch();if(op==0x4e71){}else if(op==0x4e72){sr=fetch();stopped=true;}else if((op&0xf1ff)==0x31fc){int value=fetch(),addr=read32(pc);pc=(pc+4)&0xffffff;ww(addr,value);}else if((op&0xf1ff)==0x3039){int r=(op>>9)&7,addr=read32(pc);pc=(pc+4)&0xffffff;d[r]=(d[r]&0xffff0000)|rw(addr);setNZ((short)d[r]);}else if((op&0xf100)==0x7000){int r=(op>>9)&7;d[r]=(byte)op;setNZ(d[r]);}else if((op&0xfff8)==0x0240){int r=op&7;d[r]=(d[r]&0xffff0000)|((d[r]&0xffff)&fetch());setNZ((short)d[r]);}else if((op&0xf000)==0x6000){int cond=(op>>8)&15;int disp=op&255;if(disp==0)disp=(short)fetch();else disp=(byte)disp;boolean take=cond==0||cond==7&&(sr&4)!=0||cond==6&&(sr&4)==0;if(take){if(cond==1){a[7]-=4;}pc=(pc+disp)&0xffffff;}}else{status="Unsupported opcode 0x"+Integer.toHexString(op);stopped=true;}}frames++;}void setNZ(int v){sr&=~15;if(v==0)sr|=4;if(v<0)sr|=8;}}
}
