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
        if (isZip(input) || name.endsWith(".zip")) {
            if (!isZip(input)) throw new IOException("arquivo .zip inválido ou incompleto (assinatura ZIP ausente)");
            return unzip(input);
        }
        if (input.length > MAX_ROM_BYTES) throw new IOException("ROM maior que 32 MB");

        boolean smdNamed = name.endsWith(".smd");
        if (couldBeSmd(input)) {
            byte[] deinterleaved = decodeSmd(input);
            // Do not trust the extension alone: only choose SMD decoding when the
            // deinterleaved payload has valid 68000 reset vectors or a SEGA header.
            boolean decodedRecognizable = validResetVectors(deinterleaved, 0)
                    || hasSegaHeader(deinterleaved, 0);
            boolean linearRecognizable = validResetVectors(input, 0)
                    || hasSegaHeader(input, 0);
            if (decodedRecognizable && !linearRecognizable) input = deinterleaved;
            else if (smdNamed && !linearRecognizable)
                throw new IOException("SMD: vetores/header não reconhecidos após deinterleave; arquivo pode estar truncado ou não ser SMD");
        } else if (smdNamed) {
            throw new IOException("SMD truncado ou com tamanho inválido");
        }
        if (input.length < 0x200) throw new IOException("imagem ROM truncada ou curta demais (menos de 512 bytes)");

        // A 512-byte copier header has no universal signature. Strip it only if
        // offset 0x200 has reset vectors acceptable to Machine.load and offset 0
        // does not. A SEGA string alone is not enough to discard user data.
        if (input.length > 0x200 && !validResetVectors(input, 0)
                && validResetVectors(input, 0x200)) input = slice(input, 0x200, input.length);

        boolean knownExtension = name.endsWith(".bin") || name.endsWith(".gen") || name.endsWith(".md") || name.endsWith(".smd");
        if (!knownExtension && !validResetVectors(input,0) && !hasSegaHeader(input,0))
            throw new IOException("conteúdo não parece ROM Mega Drive: vetores verificados em 0x000 e 0x200; nenhum reset válido nem cabeçalho SEGA no offset 0x000");
        return input;
    }

    private static boolean isZip(byte[] b) {
        return b.length >= 4 && b[0] == 'P' && b[1] == 'K'
                && ((b[2] == 3 && b[3] == 4) || (b[2] == 5 && b[3] == 6) || (b[2] == 7 && b[3] == 8));
    }

    private static byte[] unzip(byte[] archive) throws IOException {
        byte[] found = null;
        String firstError = null;
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(archive))) {
            ZipEntry entry;
            byte[] buffer = new byte[8192];
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int r;
                while ((r = zip.read(buffer)) != -1) {
                    if (out.size() + r > MAX_ROM_BYTES) throw new IOException("ROM dentro do ZIP passa de 32 MB");
                    out.write(buffer, 0, r);
                }
                byte[] candidate = out.toByteArray();
                String n = entry.getName().toLowerCase(Locale.US);
                boolean knownRomName = n.endsWith(".bin") || n.endsWith(".gen") || n.endsWith(".md") || n.endsWith(".smd");
                try {
                    byte[] decoded = decode(candidate, n);
                    if (!knownRomName && !validResetVectors(decoded,0) && !hasSegaHeader(decoded,0)) continue;
                    if (found != null) throw new IOException("ZIP contém mais de uma ROM; compacte uma ROM por arquivo");
                    found = decoded;
                } catch (IOException ex) {
                    if (ex.getMessage() != null && ex.getMessage().contains("mais de uma ROM")) throw ex;
                    if (firstError == null) firstError = ex.getMessage();
                }
            }
        } catch (java.util.zip.ZipException ex) {
            throw new IOException("arquivo ZIP corrompido ou sem conteúdo legível", ex);
        }
        if (found == null) throw new IOException("ZIP: não achei uma ROM Mega Drive reconhecível" + (firstError == null ? "" : ": " + firstError));
        return found;
    }

    private static boolean couldBeSmd(byte[] b) {
        return b.length > 0x420 && (b.length - 512) % 0x4000 == 0;
    }

    private static byte[] decodeSmd(byte[] in) throws IOException {
        if (!couldBeSmd(in)) throw new IOException("SMD truncado ou com tamanho inválido");
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

    private static boolean validResetVectors(byte[] data, int offset) {
        if (data.length < offset + 8) return false;
        long sp = u32(data, offset) & 0x00ffffffL;
        long pc = u32(data, offset + 4) & 0x00ffffffL;
        int payloadLength = data.length - offset;
        return (sp & 1) == 0
                && ((sp >= 0x00ff0000L && sp <= 0x00ffffffL)
                    || (sp >= 0x00e00000L && sp < 0x00e10000L))
                && (pc & 1) == 0 && pc >= 8
                && pc < Math.min(payloadLength, 0x400000);
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
