package org.openmegaproto;

/** Original six-button pad input model with TH multiplexing (serial timing remains simplified). */
final class Controller {
    boolean up,down,left,right,a,b,c,x,y,z,start,mode;
    boolean th=true;
    int phase=0;
    void set(boolean up,boolean down,boolean left,boolean right,boolean a,boolean b,boolean c,boolean x,boolean y,boolean z,boolean start,boolean mode){
        this.up=up;this.down=down;this.left=left;this.right=right;this.a=a;this.b=b;this.c=c;
        this.x=x;this.y=y;this.z=z;this.start=start;this.mode=mode;
    }
    void set(boolean up,boolean down,boolean left,boolean right,boolean a,boolean b,boolean c,boolean start){set(up,down,left,right,a,b,c,false,false,false,start,false);}
    void writeData(int value){boolean next=(value&0x40)!=0;if(next!=th){if(!next)phase=(phase+1)%4;th=next;}}
    int readData(){
        int value=0x7f;
        if(phase==3&&!th){if(z)value&=~1;if(y)value&=~2;if(x)value&=~4;if(mode)value&=~8;if(a)value&=~0x10;if(start)value&=~0x20;}
        else {if(up)value&=~1;if(down)value&=~2;
            if(th){if(left)value&=~4;if(right)value&=~8;if(b)value&=~0x10;if(c)value&=~0x20;}
            else {if(left)value&=~4;if(right)value&=~8;if(a)value&=~0x10;if(start)value&=~0x20;}}
        return value;
    }
}
