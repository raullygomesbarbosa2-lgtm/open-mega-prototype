package org.openmegaproto;
import org.junit.Test;import static org.junit.Assert.*;
public class Cpu68000Test {
 static class Mem implements Cpu68000.Bus {byte[] m=new byte[1<<20];int u(int a){return a&0xfffff;}public int read8(int a){return m[u(a)]&255;}public int read16(int a){return read8(a)<<8|read8(a+1);}public void write8(int a,int v){m[u(a)]=(byte)v;}public void write16(int a,int v){write8(a,v>>>8);write8(a+1,v);}int read32(int a){return read16(a)<<16|read16(a+2);}void words(int... w){int p=0;for(int v:w){write16(p,v);p+=2;}}}
 static Cpu68000 cpu(Mem m,int... code){m.words(code);Cpu68000 c=new Cpu68000(m);c.reset(0x80000,0);return c;}
 @Test public void workRamIsMirroredAcrossTheMegaDriveE00000ToFFFFFFWindow(){
  MainActivity.Machine m=new MainActivity.Machine();
  m.ww(0x00e00020,0x1234);
  assertEquals(0x12,m.rb(0x00ff0020));assertEquals(0x34,m.rb(0x00e10021));
  m.ww(0x00fffffe,0xabcd);
  assertEquals(0xabcd,m.rw(0x00e0fffe));
  byte[] rom=new byte[512];rom[0]=0;rom[1]=(byte)0xe0;rom[2]=0;rom[3]=0x10;rom[7]=8;
  int[] words={0x2f3c,0x1234,0x5678,0x4e71};
  for(int i=0;i<words.length;i++){rom[8+i*2]=(byte)(words[i]>>>8);rom[9+i*2]=(byte)words[i];}
  m.load(rom);m.step(1);
  assertEquals(0x00e0000c,m.a[7]);
  assertEquals(0x12345678,m.read32(0x00e0000c));
  assertEquals(0x12345678,m.read32(0x00ff000c));
 }
 @Test public void resetVectorsAreUsedByMachineAndInvalidVectorsRejected(){MainActivity.Machine m=new MainActivity.Machine();byte[] rom=new byte[512];rom[0]=0;rom[1]=(byte)0xff;rom[2]=(byte)0xff;rom[3]=0;rom[7]=(byte)0x20;rom[0x20]=0x70;rom[0x21]=0x2a;m.load(rom);assertEquals(0xffff00,m.a[7]);assertEquals(0x20,m.pc);m.step(1);assertEquals(42,m.d[0]);assertEquals(0x22,m.pc);try{m.load(new byte[512]);fail("zero reset vectors accepted");}catch(IllegalArgumentException expected){}}
 @Test public void invalidResetVectorErrorIncludesActualStackAndProgramCounter(){MainActivity.Machine m=new MainActivity.Machine();byte[] rom=new byte[512];rom[1]=(byte)0xff;rom[2]=(byte)0xff;rom[7]=9;try{m.load(rom);fail("odd reset PC should be rejected");}catch(IllegalArgumentException expected){assertTrue(expected.getMessage().contains("SP=$00FFFF00"));assertTrue(expected.getMessage().contains("PC=$00000009"));}}
 @Test public void moveImmediateToDregSetsSizeAndFlags(){Mem m=new Mem();Cpu68000 c=cpu(m,0x7000|((1)<<9)|0x80,0x303c,0x0000);c.step(1);assertEquals(0xffffff80,c.d[1]);assertEquals(8,c.sr&8);assertEquals(0,c.sr&5);}
 @Test public void moveAbsoluteWordToDregAndMoveAbsoluteLongStore(){Mem m=new Mem();m.write16(0x300,0x8001);Cpu68000 c=cpu(m,0x3039,0,0x0300,0x33fc,0x1234,0,0x0400);c.step(1);assertEquals(0x8001,c.d[0]&0xffff);assertEquals(8,c.sr&8);c.step(1);assertEquals(0x1234,m.read16(0x400));assertEquals(0,c.sr&8);assertEquals(0,c.sr&0x10);}
 @Test public void moveaAndLeaAndIndirectAddressModes(){Mem m=new Mem();m.write16(0x300,0x2345);Cpu68000 c=cpu(m,0x207c,0x1234,0x5678, // MOVEA.L #$12345678,A0
 0x41f9,0,0x0300, // LEA abs.l,A0
 0x3010,0x0000); // MOVE.W (A0),D0
 c.step(3);assertEquals(0x300,c.a[0]);assertEquals(0x2345,c.d[0]&0xffff);assertEquals(14,c.pc);}
 @Test public void branchConditionsAndBsrStackReturnAddress(){Mem m=new Mem();Cpu68000 c=cpu(m,0x7000,0x6702,0x7201,0x6002,0x7402,0x6102,0x4e71);c.step(1);assertTrue((c.sr&4)!=0);c.step(1);assertEquals(6,c.pc); // BEQ skips the following MOVEQ
 c.step(1);assertEquals(10,c.pc);c.step(1);assertEquals(14,c.pc);assertEquals(0x7fffc,c.a[7]);assertEquals(12,m.read32(0x7fffc));}
 @Test public void logicalArithmeticAndCompareFlags(){Mem m=new Mem();Cpu68000 c=cpu(m,0x7001,0x0680,0x7fff,0xffff,0x0c80,0x8000,0);c.step(2);assertEquals(0x80000000,c.d[0]);assertEquals(0x0a,c.sr&0x0f);c.step(1);assertEquals(0x04,c.sr&0x0f);}
 @Test public void addressArithmeticWordImmediateSignExtendsAndCmpaUsesLongFlags(){
  Mem m=new Mem();Cpu68000 c=cpu(m,0xd0fc,0xffff, // ADDA.W #-1,A0
   0x90fc,0xffff, // SUBA.W #-1,A0
   0xb0fc,0xffff); // CMPA.W #-1,A0
  c.a[0]=0;c.step(1);assertEquals(0x00ffffff,c.a[0]);
  c.step(1);assertEquals(0,c.a[0]);
  c.a[0]=0x00ffffff;c.sr|=0x10;c.step(1);
  assertEquals(0x00ffffff,c.a[0]);assertEquals(0x11,c.sr&0x1f); // 32-bit borrow, X preserved; N/Z/V clear
 }
 @Test public void unsupportedEncodingStopsWithAddressAndNoFalseProgress(){Mem m=new Mem();Cpu68000 c=cpu(m,0x4e70);c.step(3);assertTrue(c.stopped);assertTrue(c.status.contains("0x4e70"));assertTrue(c.status.contains("0x0"));assertEquals(2,c.pc);}
 @Test public void movemLongStoresRegistersAndPredecrementReversesMaskOrder(){
  Mem m=new Mem();Cpu68000 c=cpu(m,0x48d0,0x0003, // MOVEM.L D0-D1,(A0)
   0x48e1,0x0080); // MOVEM.L A0,-(A1), reversed mask order
  c.d[0]=0x12345678;c.d[1]=0x89abcdef;c.a[0]=0x300;c.a[1]=0x400;
  c.step(1);assertEquals(0x12345678,m.read32(0x300));assertEquals(0x89abcdef,m.read32(0x304));
  c.step(1);assertEquals(0x3fc,c.a[1]);assertEquals(0x300,m.read32(0x3fc));
 }
 @Test public void conditionRegisterArithmeticAndDbccUseCorrectWidths(){
  Mem m=new Mem();Cpu68000 c=cpu(m,0x003c,0x001f, // ORI #$1f,CCR
   0x023c,0x001e, // ANDI #$1e,CCR
   0xd240, // ADD.W D0,D1
   0x9041, // SUB.W D1,D0
   0x51c8,0xfffe); // DBF D0,-2
  c.d[0]=1;c.d[1]=0x0000ffff;
  c.step(1);assertEquals(0x1f,c.sr&0x1f);
  c.step(1);assertEquals(0x1e,c.sr&0x1f);
  c.step(1);assertEquals(0,c.d[1]&0xffff);assertEquals(0x15,c.sr&0x1f);
  c.step(1);assertEquals(1,c.d[0]&0xffff); // 1 - 0
  c.step(1);assertEquals(12,c.pc);assertEquals(0,c.d[0]&0xffff); // DBF decrements D0.W and branches to itself
  c.step(1);assertEquals(16,c.pc);assertEquals(0xffff,c.d[0]&0xffff); // DBF falls through when D0.W reaches -1
 }
 @Test public void rtsReturnsToLongAddressAndRestoresStack(){
  Mem m=new Mem();Cpu68000 c=cpu(m,0x4e75);m.write16(0x80000,0);m.write16(0x80002,0x1234);c.step(1);assertEquals(0x1234,c.pc);assertEquals(0x80004,c.a[7]);
 }
 @Test public void movemWordLoadsSignExtendedAndPostincrementsBaseOnce(){
  Mem m=new Mem();m.write16(0x300,0x8001);m.write16(0x302,0x7fff);
  Cpu68000 c=cpu(m,0x4c98,0x0003); // MOVEM.W (A0)+,D0-D1
  c.a[0]=0x300;c.step(1);assertEquals(0xffff8001,c.d[0]);assertEquals(0x7fff,c.d[1]);assertEquals(0x304,c.a[0]);
 }
}

