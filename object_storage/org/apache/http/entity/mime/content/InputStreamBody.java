package org.apache.http.entity.mime.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MIME;
import org.apache.http.util.Args;

public class InputStreamBody extends AbstractContentBody {
    private final String filename;
    private final InputStream in;

    @Deprecated
    public InputStreamBody(InputStream in, String mimeType, String filename) {
        this(in, ContentType.create(mimeType), filename);
    }

    public InputStreamBody(InputStream in, String filename) {
        this(in, ContentType.DEFAULT_BINARY, filename);
    }

    public InputStreamBody(InputStream in, ContentType contentType, String filename) {
        super(contentType);
        Args.notNull(in, "Input stream");
        this.in = in;
        this.filename = filename;
    }

    public InputStreamBody(InputStream in, ContentType contentType) {
        this(in, contentType, null);
    }

    public InputStream getInputStream() {
        return this.in;
    }

    public void writeTo(OutputStream out) throws IOException {
        Args.notNull(out, "Output stream");
        try {
            byte[] tmp = new byte[4096];
            while (true) {
                int l = this.in.read(tmp);
                if (l == -1) {
                    break;
                }
                out.write(tmp, 0, l);
            }
            out.flush();
        } finally {
            this.in.close();
        }
    }

    public String getTransferEncoding() {
        return MIME.ENC_BINARY;
    }

    public long getContentLength() {
        return -1;
    }

    public String getFilename() {
        return this.filename;
    }
}
