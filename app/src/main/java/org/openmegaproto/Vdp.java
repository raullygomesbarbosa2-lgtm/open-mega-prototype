package org.openmegaproto;

/** Original partial mode-5 VDP: command ports, VRAM/CRAM/VSRAM and two tile planes. */
final class Vdp {
    static final int WIDTH=320, HEIGHT=224;
    final byte[] vram=new byte[65536];
    final short[] cram=new short[64];
    final short[] vsram=new short[40];
    final int[] pixels=new int[WIDTH*HEIGHT];
    final int[] regs=new int[24];
    int renderedFrames;
    private int commandWord=-1, address, code;

    void reset(){ java.util.Arrays.fill(vram,(byte)0); java.util.Arrays.fill(cram,(short)0); java.util.Arrays.fill(vsram,(short)0); java.util.Arrays.fill(regs,0); regs[1]=0; regs[2]=0x30; regs[4]=7; regs[7]=0; regs[15]=2; commandWord=-1; address=code=0; render(); }
    /** Assemble the two-word VDP command; register writes cancel a pending command half. */
    void writeControl(int value){
        value &= 0xffff;
        if((value&0xc000)==0x8000){writeRegister((value>>>8)&31,value&255);commandWord=-1;return;}
        if(commandWord<0){commandWord=value;return;}
        long cmd=((long)commandWord<<16)|value;
        code=(int)(((cmd>>>30)&3)|((cmd>>>2)&0x3c));
        address=(int)(((cmd>>>16)&0x3fff)|((cmd&3)<<14));commandWord=-1;
    }
    /** VDP data port write. Supported command codes: VRAM write, CRAM write, VSRAM write. */
    void writeDataPort(int value){
        value &= 0xffff;
        if(code==1){writeVram(address,(value>>>8)&255);writeVram(address+1,value&255);}
        else if(code==3){writeCram(address>>>1,value);}
        else if(code==5){writeVsram(address>>>1,value);}
        address=(address+(regs[15]&255))&0xffff;
    }
    /** Data port reads VRAM with the current address; non-VRAM read command codes are unsupported. */
    int readDataPort(){int v=code==0?((readVram(address)<<8)|readVram(address+1)):0xffff;address=(address+(regs[15]&255))&0xffff;return v;}
    /**
     * Return the modeled idle status. FIFO-empty is set because writes are applied
     * synchronously; timing-dependent VBlank, HBlank, DMA and sprite flags remain clear.
     * Reading status also cancels an incomplete two-word control command, as on hardware.
     */
    int readStatusPort(){commandWord=-1;return 0x0200;}
    void writeRegister(int index,int value){if(index>=0&&index<regs.length)regs[index]=value&255;}
    void writeVram(int address,int value){vram[address&0xffff]=(byte)value;}
    int readVram(int address){return vram[address&0xffff]&255;}
    void writeCram(int index,int value){cram[index&63]=(short)(value&0x0eee);}
    void writeVsram(int index,int value){vsram[index%vsram.length]=(short)(value&0x7ff);}
    int cramColor(int index){int c=cram[index&63]&0x0eee; return 0xff000000|(((c>>1)&7)*36<<16)|(((c>>5)&7)*36<<8)|(((c>>9)&7)*36);}
    private int tilePixel(int tile,int x,int y){int b=(tile&0x7ff)*32+y*4+(x>>1);int v=vram[b&0xffff]&255;return (x&1)==0?(v>>4):(v&15);}
    private int planeSize(int code){return code==1?64:code==3?128:32;}
    private int scrollWord(int plane,int y,int x){
        int base=(regs[13]&0x3f)<<10, mode=regs[11]&3, line;
        if(mode==0)line=0; else if(mode==1)line=(y/8)*8; else if(mode==3)line=(y/2)*2; else line=y;
        int a=base+line*4+plane*2;
        return (short)((readVram(a)<<8)|readVram(a+1));
    }
    private int verticalScroll(int plane,int x){
        int index=(regs[11]&4)==0?plane:((x/16)*2+plane);
        return vsram[index%vsram.length]&0x7ff;
    }
    private void plane(int base,int x,int y,int plane,int[] color,boolean[] priority){
        int sx=(x-scrollWord(plane,y,x))&0x3ff;
        int sy=(y+verticalScroll(plane,x))&0x3ff;
        int width=planeSize(regs[16]&3),height=planeSize((regs[16]>>>4)&3);
        sx%=width*8; sy%=height*8;
        int tx=sx>>3,ty=sy>>3;
        int entry=base+((ty*width+tx)*2); int name=((vram[entry&0xffff]&255)<<8)|(vram[(entry+1)&0xffff]&255);
        int px=sx&7,py=sy&7;if((name&0x0800)!=0)px=7-px;if((name&0x1000)!=0)py=7-py;
        int col=tilePixel(name&0x7ff,px,py);if(col!=0){color[0]=cramColor((((name>>13)&3)*16)+col);priority[0]=(name&0x8000)!=0;}
    }
    /**
     * Partial mode-5 renderer: backdrop, two planes, tile flips/palettes/priority,
     * configured 32/64/128-cell plane sizes, and full/8-line/per-line/2-line H scroll.
     * Window, sprites, interlace, DMA and scanline timing are not modeled.
     */
    void render(){
        int backdrop=cramColor(regs[7]&63),baseA=((regs[2]&0x38)<<10),baseB=((regs[4]&7)<<13);
        int[] color=new int[1], bPixel=new int[1], aPixel=new int[1]; boolean[] bp=new boolean[1], ap=new boolean[1];
        boolean display=(regs[1]&0x40)!=0;
        for(int y=0;y<HEIGHT;y++)for(int x=0;x<WIDTH;x++){
            color[0]=backdrop;
            if(display){
                // Resolve priority explicitly: low B, low A, high B, high A.
                bp[0]=false;color[0]=backdrop;plane(baseB,x,y,1,color,bp);bPixel[0]=color[0];boolean bHigh=bp[0];
                ap[0]=false;color[0]=backdrop;plane(baseA,x,y,0,color,ap);aPixel[0]=color[0];boolean aHigh=ap[0];
                int bColor=bPixel[0],aColor=aPixel[0];

                color[0]=backdrop;
                if(!bHigh)color[0]=bColor;
                if(!aHigh&&aColor!=backdrop)color[0]=aColor;
                if(bHigh)color[0]=bColor;
                if(aHigh&&aColor!=backdrop)color[0]=aColor;
            }
            pixels[y*WIDTH+x]=color[0];
        }
        renderedFrames++;
    }
    /** Deterministic generated pattern for renderer tests, not a game image. */
    void loadDiagnosticPattern(){
        reset();writeRegister(1,0x40);
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
