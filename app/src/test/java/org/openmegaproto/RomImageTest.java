package org.openmegaproto;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class RomImageTest {
    private byte[] validRom(int size) {
        byte[] b = new byte[size];
        b[0] = 0x00; b[1] = (byte)0xff; b[2] = (byte)0xff; b[3] = 0x00;
        b[4] = 0; b[5] = 0; b[6] = 0; b[7] = 8;
        return b;
    }

    @Test public void acceptsRawBinAndKeepsResetVectors() throws Exception {
        byte[] in = validRom(0x400);
        byte[] out = RomImage.decode(in, "game.BIN");
        assertArrayEquals(in, out);
        assertEquals(8, ((out[4]&255)<<24)|((out[5]&255)<<16)|((out[6]&255)<<8)|(out[7]&255));
    }

    @Test public void stripsHeaderlessCopierPrefixOnlyWhenPayloadHasPlausibleResetVectors() throws Exception {
        byte[] game=validRom(0x400);byte[] wrapped=new byte[game.length+0x200];
        System.arraycopy(game,0,wrapped,0x200,game.length);
        assertArrayEquals(game,RomImage.decode(wrapped,"dump.bin"));
    }

    @Test public void keepsRawBinWithoutValidVectorsForClearMachineDiagnostic() throws Exception {
        byte[] raw=new byte[0x400];raw[0x100]='S';raw[0x101]='E';raw[0x102]='G';raw[0x103]='A';
        assertArrayEquals(raw,RomImage.decode(raw,"broken.bin"));
    }

    @Test public void stripsOptionalSegaRomHeader() throws Exception {
        byte[] game = validRom(0x400);
        byte[] wrapped = new byte[game.length+0x200];
        game[0x100]='S';game[0x101]='E';game[0x102]='G';game[0x103]='A';
        System.arraycopy(game,0,wrapped,0x200,game.length);
        assertArrayEquals(game,RomImage.decode(wrapped,"dump.gen"));
    }

    @Test public void deinterleavesSmdBlocksToNormal68000ByteOrder() throws Exception {
        byte[] raw=validRom(0x4000);for(int i=0;i<raw.length;i++)raw[i]=(byte)(i*7+3);
        byte[] smd=new byte[512+0x4000];System.arraycopy("SEGA".getBytes("US-ASCII"),0,smd,8,4);
        for(int i=0;i<0x2000;i++){smd[512+0x2000+i]=raw[i*2];smd[512+i]=raw[i*2+1];}
        assertArrayEquals(raw,RomImage.decode(smd,"dump.smd"));
    }

    @Test public void extractsSingleSupportedRomFromZip() throws Exception {
        byte[] rom=validRom(0x400);ByteArrayOutputStream out=new ByteArrayOutputStream();
        ZipOutputStream zip=new ZipOutputStream(out);zip.putNextEntry(new ZipEntry("folder/game.bin"));zip.write(rom);zip.closeEntry();zip.close();
        assertArrayEquals(rom,RomImage.decode(out.toByteArray(),"archive.zip"));
    }


    @Test public void extractsAndDecodesSmdInZipEvenWithoutHeaderSignature() throws Exception {
        byte[] raw=validRom(0x4000);byte[] smd=new byte[512+0x4000];
        for(int i=0;i<0x2000;i++){smd[512+0x2000+i]=raw[i*2];smd[512+i]=raw[i*2+1];}
        ByteArrayOutputStream out=new ByteArrayOutputStream();ZipOutputStream zip=new ZipOutputStream(out);
        zip.putNextEntry(new ZipEntry("game.smd"));zip.write(smd);zip.closeEntry();zip.close();
        assertArrayEquals(raw,RomImage.decode(out.toByteArray(),"archive.zip"));
    }

    @Test public void refusesArchiveWithAmbiguousMultipleRoms() throws Exception {
        ByteArrayOutputStream out=new ByteArrayOutputStream();ZipOutputStream zip=new ZipOutputStream(out);
        for(String n:new String[]{"one.bin","two.gen"}){zip.putNextEntry(new ZipEntry(n));zip.write(validRom(0x400));zip.closeEntry();}zip.close();
        try{RomImage.decode(out.toByteArray(),"many.zip");fail("multiple ROMs should require a clear choice");}
        catch(java.io.IOException expected){assertTrue(expected.getMessage().contains("mais de uma ROM"));}
    }
}
