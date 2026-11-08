package org.openmegaproto;

/** Three-button pad's simplified active-low data port and TH phase. Does not model serial timing. */
final class Controller {
    boolean up,down,left,right,a,b,c,start;
    boolean th=true;
    void set(boolean up,boolean down,boolean left,boolean right,boolean a,boolean b,boolean c,boolean start){
        this.up=up;this.down=down;this.left=left;this.right=right;this.a=a;this.b=b;this.c=c;this.start=start;
    }
    void writeData(int value){th=(value&0x40)!=0;}
    int readData(){
        int value=0x7f;
        if(!up)value&=~1;if(!down)value&=~2;
        if(th){if(!a)value&=~0x10;if(!start)value&=~0x20;}
        else {if(!left)value&=~4;if(!right)value&=~8;if(!b)value&=~0x10;if(!c)value&=~0x20;}
        return value;
    }
}
