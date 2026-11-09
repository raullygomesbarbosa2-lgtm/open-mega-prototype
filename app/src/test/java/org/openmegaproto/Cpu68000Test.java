package org.openmegaproto;
import org.junit.Test;import static org.junit.Assert.*;
public class Cpu68000Test {
 static class Mem implements Cpu68000.Bus {byte[] m=new byte[1<<20];int u(int a){return a&0xfffff;}public int read8(int a){return m[u(a)]&255;}public int read16(int a){return read8(a)<<8|read8(a+1);}public void write8(int a,int v){m[u(a)]=(byte)v;}public void write16(int a,int v){write8(a,v>>>8);write8(a+1,v);}void words(int... w){int p=0;for(int v:w){write16(p,v);p+=2;}}}
 static Cpu68000 cpu(Mem m,int... code){m.words(code);Cpu68000 c=new Cpu68000(m);c.reset(0x80000,0);return c;}
 @Test public void resetVectorsAreUsedByMachineAndInvalidVectorsRejected(){MainActivity.Machine m=new MainActivity.Machine();byte[] rom=new byte[512];rom[0]=(byte)0xff;rom[1]=(byte)0xff;rom[2]=0;rom[3]=0;rom[7]=(byte)0x20;rom[0x20]=0x70;rom[0x21]=0x2a;m.load(rom);assertEquals(0xffff00,m.a[7]);assertEquals(0x20,m.pc);m.step(1);assertEquals(42,m.d[0]);assertEquals(0x22,m.pc);try{m.load(new byte[512]);fail("zero reset vectors accepted");}catch(IllegalArgumentException expected){}}
 @Test public void moveImmediateToDregSetsSizeAndFlags(){Mem m=new Mem();Cpu68000 c=cpu(m,0x7000|((1)<<9)|0x80,0x303c,0x0000);c.step(1);assertEquals(0xffffff80,c.d[1]);assertEquals(8,c.sr&8);assertEquals(0,c.sr&5);}
 @Test public void moveAbsoluteWordToDregAndMoveAbsoluteLongStore(){Mem m=new Mem();m.write16(0x300,0x8001);Cpu68000 c=cpu(m,0x3039,0,0x0300,0x33fc,0x1234,0,0x0400);c.step(1);assertEquals(0x8001,c.d[0]&0xffff);assertEquals(8,c.sr&8);c.step(1);assertEquals(0x1234,m.read16(0x400));assertEquals(0,c.sr&8);assertEquals(0,c.sr&0x10);}
 @Test public void moveaAndLeaAndIndirectAddressModes(){Mem m=new Mem();m.write16(0x300,0x2345);Cpu68000 c=cpu(m,0x207c,0x1234,0x5678, // MOVEA.L #$12345678,A0
 0x41f9,0,0,0x0300, // LEA abs.l,A0
 0x3010,0x0000); // MOVE.W (A0),D0
 c.step(3);assertEquals(0x300,c.a[0]);assertEquals(0x2345,c.d[0]&0xffff);assertEquals(14,c.pc);}
 @Test public void branchConditionsAndBsrStackReturnAddress(){Mem m=new Mem();Cpu68000 c=cpu(m,0x7000,0x6702,0x7201,0x6002,0x7402,0x6102,0x4e71);c.step(1);assertTrue((c.sr&4)!=0);c.step(1);assertEquals(6,c.pc); // BEQ skips the following MOVEQ
 c.step(1);assertEquals(10,c.pc);c.step(1);assertEquals(14,c.pc);assertEquals(0x7fffc,c.a[7]);assertEquals(12,m.read32(0x7fffc));}
 @Test public void logicalArithmeticAndCompareFlags(){Mem m=new Mem();Cpu68000 c=cpu(m,0x7001,0x0680,0x7fff,0xffff,0x0c80,0x8000,0);c.step(2);assertEquals(0x80000000,c.d[0]);assertEquals(0x0a,c.sr&0x0f);c.step(1);assertEquals(0x04,c.sr&0x0f);}
 @Test public void unsupportedEncodingStopsWithAddressAndNoFalseProgress(){Mem m=new Mem();Cpu68000 c=cpu(m,0x4e70);c.step(3);assertTrue(c.stopped);assertTrue(c.status.contains("0x4e70"));assertTrue(c.status.contains("0x0"));assertEquals(2,c.pc);}
}
