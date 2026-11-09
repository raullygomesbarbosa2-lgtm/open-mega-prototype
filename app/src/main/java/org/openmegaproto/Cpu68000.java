package org.openmegaproto;

/** Small original 68000 interpreter. Deliberately incomplete; unsupported encodings stop explicitly. */
final class Cpu68000 {
    interface Bus { int read8(int address); int read16(int address); void write8(int address,int value); void write16(int address,int value); }
    final Bus bus; final int[] d=new int[8],a=new int[8]; int pc,sr=0x2700; boolean stopped; String status="";
    Cpu68000(Bus bus){this.bus=bus;}
    void reset(int initialSp,int initialPc){java.util.Arrays.fill(d,0);java.util.Arrays.fill(a,0);a[7]=initialSp&0xffffff;pc=initialPc&0xffffff;sr=0x2700;stopped=false;status="";}
    int fetch16(){int v=bus.read16(pc);pc=(pc+2)&0xffffff;return v&0xffff;}
    int fetch32(){int hi=fetch16();return (hi<<16)|fetch16();}
    void step(int count){for(int i=0;i<count&&!stopped;i++){int at=pc,op=fetch16();try{execute(op);}catch(Unsupported e){stopped=true;status="Unsupported opcode 0x"+Integer.toHexString(op)+" at 0x"+Integer.toHexString(at);}}}
    static final class Unsupported extends RuntimeException {}
    static void bad(){throw new Unsupported();}
    int mask(int size){return size==1?255:size==2?65535:-1;} int sign(int value,int size){return size==1?(byte)value:size==2?(short)value:value;}
    int inc(int reg,int size){return size==1&&reg==7?2:size;}
    final class Ea { final int mode,reg,size; int addr; boolean isReg,isAddr,imm; Ea(int m,int r,int s,boolean source){mode=m;reg=r;size=s; if(m==0)isReg=true;else if(m==1)isAddr=true;else if(m==2)addr=a[r];else if(m==3){addr=a[r];a[r]=(a[r]+inc(r,s))&0xffffff;}else if(m==4){a[r]=(a[r]-inc(r,s))&0xffffff;addr=a[r];}else if(m==5)addr=(a[r]+(short)fetch16())&0xffffff;else if(m==6)addr=indexed(a[r]);else if(m==7){if(r==0)addr=(short)fetch16()&0xffffff;else if(r==1)addr=fetch32()&0xffffff;else if(r==2&&source)addr=(pc+(short)fetch16())&0xffffff;else if(r==3&&source)addr=indexed(pc);else if(r==4&&source){imm=true;}else bad();}else bad();}
        int get(){if(imm)return size==4?fetch32():size==2?fetch16():(byte)fetch16();if(isReg)return d[reg]&mask(size);if(isAddr)return a[reg]&mask(size);return size==1?bus.read8(addr)&255:size==2?bus.read16(addr)&65535:bus.read16(addr)<<16|bus.read16(addr+2);}
        void put(int value){value&=mask(size);if(isReg){d[reg]=(d[reg]&~mask(size))|value;return;}if(isAddr){a[reg]=size==2?(short)value:value;return;}if(imm)bad();if(size==1)bus.write8(addr,value);else if(size==2)bus.write16(addr,value);else{bus.write16(addr,value>>>16);bus.write16(addr+2,value);}}
        boolean memory(){return !isReg&&!isAddr&&!imm;}
    }
    int indexed(int base){int ext=fetch16();if((ext&0x0100)!=0)bad();int ix=(ext>>>12)&7;int value=((ext&0x8000)!=0?a[ix]:d[ix]);if((ext&0x0800)==0)value=(short)value;return (base+(byte)ext+value)&0xffffff;}
    void execute(int op){
        if(op==0x4e71)return;
        if(op==0x4e72){sr=fetch16();stopped=true;return;}
        if((op&0xf100)==0x7000){int r=(op>>>9)&7;d[r]=(byte)op;logic(d[r],4);return;}
        if((op&0xf000)==0x6000){branch(op);return;}
        if((op&0xf1c0)==0x41c0){int m=(op>>>3)&7,r=op&7;Ea e=new Ea(m,r,4,false);if(!e.memory()||m==7&&r>3)bad();a[(op>>>9)&7]=e.addr;return;}
        if((op&0xffc0)==0x4ec0||(op&0xffc0)==0x4e80){int m=(op>>>3)&7,r=op&7;Ea e=new Ea(m,r,4,false);if(!e.memory()||m==7&&r>3)bad();if((op&0xffc0)==0x4e80){push32(pc);pc=e.addr;}else pc=e.addr;return;}
        int top=op>>>12;
        if(top==1||top==2||top==3){int size=top==1?1:top==2?4:2;int sm=(op>>>3)&7,sr0=op&7,dm=(op>>>6)&7,dr=(op>>>9)&7;
            if(dm==1){if(size==1)bad();Ea src=new Ea(sm,sr0,size,true);int v=src.get();a[dr]=size==2?(short)v:v;return;}
            Ea src=new Ea(sm,sr0,size,true);int v=src.get();Ea dst=new Ea(dm,dr,size,false);dst.put(v);logic(v,size);return;}
        if((op&0xf138)==0x0108){bad();} // MOVEP intentionally unsupported.
        if((op&0xff00)==0x4200||(op&0xff00)==0x4a00){int sizeCode=(op>>>6)&3;if(sizeCode==3)bad();int size=sizeCode==0?1:sizeCode==1?2:4;Ea e=new Ea((op>>>3)&7,op&7,size,false);if((op&0xff00)==0x4200){e.put(0);logic(0,size);}else logic(e.get(),size);return;}
        // Immediate arithmetic/logical operations (all sizes, general alterable EA).
        int immOp=op&0xff00; if(immOp==0x0000||immOp==0x0200||immOp==0x0400||immOp==0x0600||immOp==0x0a00||immOp==0x0c00){int sc=(op>>>6)&3;if(sc==3)bad();int size=sc==0?1:sc==1?2:4;int imm=size==4?fetch32():fetch16();if(size==1)imm&=255;Ea e=new Ea((op>>>3)&7,op&7,size,false);int old=e.get(),res;switch(immOp){case 0x0000:res=old|imm;e.put(res);logic(res,size);break;case 0x0200:res=old&imm;e.put(res);logic(res,size);break;case 0x0400:res=old-imm;e.put(res);arith(old,imm,res,size,false);break;case 0x0600:res=old+imm;e.put(res);arith(old,imm,res,size,true);break;case 0x0a00:res=old^imm;e.put(res);logic(res,size);break;default:res=old-imm;compare(old,imm,res,size);break;}return;}
        // ADDQ/SUBQ data/address register and memory.
        if((op&0xf000)==0x5000&&(op&0x00c0)!=0x00c0){int sc=(op>>>6)&3,size=sc==0?1:sc==1?2:4;int n=(op>>>9)&7;if(n==0)n=8;boolean sub=(op&0x0100)!=0;Ea e=new Ea((op>>>3)&7,op&7,size,false);int old=e.get(),res=sub?old-n:old+n;e.put(res);if(!e.isAddr)arith(old,n,res,size,!sub);return;}
        // CMP Dn,<ea> and CMPA.W/L
        if((op&0xf000)==0xb000){int opmode=(op>>>6)&7,dr=(op>>>9)&7;if(opmode==3||opmode==7){int size=opmode==3?2:4;Ea s=new Ea((op>>>3)&7,op&7,size,true);int v=s.get();int lhs=a[dr],res=lhs-v;compare(lhs,v,res,size);return;}if(opmode==0||opmode==1||opmode==2){int size=opmode==0?1:opmode==1?2:4;Ea s=new Ea((op>>>3)&7,op&7,size,true);int v=s.get(),lhs=d[dr]&mask(size);compare(lhs,v,lhs-v,size);return;}}
        bad();
    }
    boolean cond(int c){boolean cflag=(sr&1)!=0,v=(sr&2)!=0,z=(sr&4)!=0,n=(sr&8)!=0;switch(c){case 0:return true;case 1:return false;case 2:return !cflag&&!z;case 3:return cflag||z;case 4:return !cflag;case 5:return cflag;case 6:return !z;case 7:return z;case 8:return !v;case 9:return v;case 10:return !n;case 11:return n;case 12:return n==v;case 13:return n!=v;case 14:return !z&&n==v;case 15:return z||n!=v;default:return false;}}
    void branch(int op){int c=(op>>>8)&15,disp=op&255,base=pc;if(disp==0)disp=(short)fetch16();else disp=(byte)disp;if(c==1){push32(pc);pc=(base+disp)&0xffffff;}else if(cond(c))pc=(base+disp)&0xffffff;}
    void push32(int v){a[7]=(a[7]-4)&0xffffff;bus.write16(a[7],v>>>16);bus.write16(a[7]+2,v);}
    void logic(int value,int size){int v=value&mask(size);sr&=~0x0f;if(v==0)sr|=4;if((v&(size==1?0x80:size==2?0x8000:0x80000000))!=0)sr|=8;}
    void compare(int lhs,int rhs,int result,int size){int xbit=sr&0x10;arith(lhs,rhs,result,size,false);sr=(sr&~0x10)|xbit;}
    void arith(int lhs,int rhs,int result,int size,boolean add){int m=mask(size),signBit=size==1?0x80:size==2?0x8000:0x80000000;int x=lhs&m,y=rhs&m,r=result&m;boolean carry=add?((long)x+(long)y)>m:x<y;boolean overflow=add?((~(x^y)&(x^r)&signBit)!=0):(((x^y)&(x^r)&signBit)!=0);sr&=~0x1f;if(r==0)sr|=4;if((r&signBit)!=0)sr|=8;if(overflow)sr|=2;if(carry)sr|=0x11;}
}
