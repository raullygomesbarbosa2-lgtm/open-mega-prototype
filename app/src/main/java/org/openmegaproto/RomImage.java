package org.openmegaproto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Input normalization for common Mega Drive ROM dumps; does not emulate hardware. */
final class RomImage {
    static final int MAX_ROM_BYTES = 32 * 1024 * 1024;
    private RomImage() {}

    static byte[] decode(byte[] input, String filename) throws IOException {
        if (input == null || input.length == 0) throw new IOException("arquivo vazio");
        String name = filename == null ? "" : filename.toLowerCase(Locale.US);
        if (name.endsWith(".zip")) return unzip(input);
        else if (!(name.endsWith(".bin") || name.endsWith(".gen") || name.endsWith(".md") || name.endsWith(".smd")))
            throw new IOException("formato não reconhecido; use BIN, GEN, MD, SMD ou ZIP");
        if (input.length > MAX_ROM_BYTES) throw new IOException("ROM maior que 32 MB");
        if (name.endsWith(".smd") || looksLikeSmd(input)) input = decodeSmd(input);
        if (input.length < 0x200) throw new IOException("imagem ROM truncada ou curta demais");
        // Some copier/dump headers do not carry the literal SEGA marker. Prefer the
        // payload at +0x200 only when it has plausible 68000 vectors and the file
        // prefix does not, avoiding destructive guesses for ordinary raw BIN images.
        if (input.length > 0x400 && (!plausibleVectors(input,0) && plausibleVectors(input,0x200)
                || !hasSegaHeader(input,0) && hasSegaHeader(input,0x200))) input = slice(input,0x200,input.length);
        return input;
    }

    private static byte[] unzip(byte[] archive) throws IOException {
        byte[] found = null;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String n = entry.getName().toLowerCase(Locale.US);
                if (!(n.endsWith(".bin") || n.endsWith(".gen") || n.endsWith(".md") || n.endsWith(".smd"))) continue;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int r;
                while ((r = zip.read(buffer)) != -1) {
                    if (out.size() + r > MAX_ROM_BYTES) throw new IOException("ROM dentro do ZIP passa de 32 MB");
                    out.write(buffer, 0, r);
                }
                if (found != null) throw new IOException("ZIP contém mais de uma ROM; compacte uma ROM por arquivo");
                found = decode(out.toByteArray(), n);
            }
        }
        if (found == null) throw new IOException("não achei arquivo BIN/GEN/MD/SMD dentro do ZIP");
        return found;
    }

    private static boolean looksLikeSmd(byte[] b) {
        return b.length > 0x420 && b.length % 0x4000 == 512 && (b[8] == 'A' || b[8] == 'E') && b[9] == 'A';
    }

    private static byte[] decodeSmd(byte[] in) throws IOException {
        if (in.length < 0x420 || (in.length - 512) % 0x4000 != 0)
            throw new IOException("SMD truncado ou com tamanho inválido");
        int payload = in.length - 512;
        byte[] out = new byte[payload];
        for (int block = 0; block < payload; block += 0x4000) {
            for (int i = 0; i < 0x2000; i++) {
                out[block + 2 * i] = in[512 + block + 0x2000 + i];
                out[block + 2 * i + 1] = in[512 + block + i];
            }
        }
        return out;
    }

    private static boolean plausibleVectors(byte[] data,int offset) {
        if(data.length<offset+8)return false;
        long sp=u32(data,offset),pc=u32(data,offset+4);
        return (sp&1)==0&&(pc&1)==0&&pc>=8&&pc<data.length-offset&&(sp>=0x00ff0000L&&sp<=0x00ffffffL||sp>=0x00e00000L&&sp<0x00e10000L);
    }
    private static long u32(byte[] b,int p){return ((long)(b[p]&255)<<24)|((long)(b[p+1]&255)<<16)|((long)(b[p+2]&255)<<8)|(b[p+3]&255L);}
    private static boolean hasSegaHeader(byte[] data, int offset) {
        return data.length >= offset + 0x104 && data[offset + 0x100] == 'S' && data[offset + 0x101] == 'E'
                && data[offset + 0x102] == 'G' && data[offset + 0x103] == 'A';
    }

    private static byte[] slice(byte[] b, int start, int end) {
        byte[] out = new byte[end - start];
        System.arraycopy(b, start, out, 0, out.length);
        return out;
    }
}
