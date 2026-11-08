package org.openmegaproto;

/** Original, deliberately limited Mega Drive VDP model: VRAM/CRAM/VSRAM storage and mode-5 plane renderer. */
final class Vdp {
    static final int WIDTH=320, HEIGHT=224;
    final byte[] vram=new byte[65536];
    final short[] cram=new short[64];
    final short[] vsram=new short[40];
    final int[] pixels=new int[WIDTH*HEIGHT];
    final int[] regs=new int[24];
    int renderedFrames;

    void reset(){ java.util.Arrays.fill(vram,(byte)0); java.util.Arrays.fill(cram,(short)0); java.util.Arrays.fill(vsram,(short)0); java.util.Arrays.fill(regs,0); regs[2]=0x30; regs[4]=7; regs[7]=0; render(); }
    void writeRegister(int index,int value){if(index>=0&&index<regs.length)regs[index]=value&255;}
    void writeVram(int address,int value){vram[address&0xffff]=(byte)value;}
    int readVram(int address){return vram[address&0xffff]&255;}
    void writeCram(int index,int value){cram[index&63]=(short)(value&0x0eee);}
    void writeVsram(int index,int value){vsram[index%vsram.length]=(short)(value&0x7ff);}
    int cramColor(int index){int c=cram[index&63]&0x0eee; return 0xff000000|(((c>>1)&7)*36<<16)|(((c>>5)&7)*36<<8)|(((c>>9)&7)*36);}
    private int tilePixel(int tile,int x,int y){int b=(tile&0x7ff)*32+y*4+(x>>1);int v=vram[b&0xffff]&255;return (x&1)==0?(v>>4):(v&15);}
    private void plane(int base,int x,int y,int plane,int[] out){
        int scrollBase=(regs[13]&0x3f)<<10;
        int hword=(readVram(scrollBase+y*4+plane*2)<<8)|readVram(scrollBase+y*4+plane*2+1);
        int sx=(x-(short)hword)&511;
        int sy=(y+(short)vsram[plane])&511;
        int tx=sx>>3,ty=sy>>3;
        int entry=base+((ty*64+tx)*2); int name=((vram[entry&0xffff]&255)<<8)|(vram[(entry+1)&0xffff]&255);
        int px=sx&7,py=sy&7;if((name&0x0800)!=0)px=7-px;if((name&0x1000)!=0)py=7-py;
        int col=tilePixel(name&0x7ff,px,py);if(col!=0)out[0]=cramColor((((name>>13)&3)*16)+col);
    }
    private int scrollReg(int plane){return plane==0?13:13;}
    /** Draws a simple 320x224 mode-5 display: backdrop and two unwindowed 64x64-cell tile planes. */
    void render(){
        int backdrop=cramColor(regs[7]&63),baseA=((regs[2]&0x38)<<10),baseB=((regs[4]&7)<<13);
        int[] one=new int[1];
        for(int y=0;y<HEIGHT;y++)for(int x=0;x<WIDTH;x++){
            one[0]=backdrop;plane(baseB,x,y,1,one);plane(baseA,x,y,0,one);pixels[y*WIDTH+x]=one[0];
        }
        renderedFrames++;
    }
    /** Deterministic generated pattern for exercising this renderer without claiming ROM gameplay. */
    void loadDiagnosticPattern(){
        reset();
        writeCram(0,0);writeCram(1,0x0eee);writeCram(2,0x00e0);writeCram(3,0x0e00);writeCram(4,0x000e);writeCram(5,0x0ee0);writeCram(6,0x00ee);writeCram(7,0x0e0e);
        int baseA=(regs[2]&0x38)<<10;
        for(int t=1;t<8;t++)for(int y=0;y<8;y++)for(int x=0;x<8;x++){
            int color=((x+y+t)%7)+1;int a=(t*32+y*4+x/2)&0xffff;int old=vram[a]&255;vram[a]=(byte)((x&1)==0?((color<<4)|(old&15)):((old&240)|color));
        }
        for(int ty=0;ty<28;ty++)for(int tx=0;tx<40;tx++){
            int tile=1+(tx/5+ty/4)%7;int a=baseA+(ty*64+tx)*2;vram[a]=(byte)(tile>>8);vram[(a+1)&0xffff]=(byte)tile;
        }
        render();
    }
}
