package org.apache.http.entity.mime;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Deprecated
public class HttpMultipart extends AbstractMultipartForm {
    private final HttpMultipartMode mode;
    private final List<FormBodyPart> parts;
    private final String subType;

    public /* bridge */ /* synthetic */ long getTotalLength() {
        return super.getTotalLength();
    }

    public /* bridge */ /* synthetic */ void writeTo(OutputStream x0) throws IOException {
        super.writeTo(x0);
    }

    public HttpMultipart(String subType, Charset charset, String boundary, HttpMultipartMode mode) {
        super(charset, boundary);
        this.subType = subType;
        this.mode = mode;
        this.parts = new ArrayList();
    }

    public HttpMultipart(String subType, Charset charset, String boundary) {
        this(subType, charset, boundary, HttpMultipartMode.STRICT);
    }

    public HttpMultipart(String subType, String boundary) {
        this(subType, null, boundary);
    }

    public HttpMultipartMode getMode() {
        return this.mode;
    }

    protected void formatMultipartHeader(FormBodyPart part, OutputStream out) throws IOException {
        Header header = part.getHeader();
        switch (this.mode) {
            case BROWSER_COMPATIBLE:
                AbstractMultipartForm.writeField(header.getField(MIME.CONTENT_DISPOSITION), this.charset, out);
                if (part.getBody().getFilename() != null) {
                    AbstractMultipartForm.writeField(header.getField("Content-Type"), this.charset, out);
                    return;
                }
                return;
            default:
                Iterator i$ = header.iterator();
                while (i$.hasNext()) {
                    AbstractMultipartForm.writeField((MinimalField) i$.next(), out);
                }
                return;
        }
    }

    public List<FormBodyPart> getBodyParts() {
        return this.parts;
    }

    public void addBodyPart(FormBodyPart part) {
        if (part != null) {
            this.parts.add(part);
        }
    }

    public String getSubType() {
        return this.subType;
    }

    public Charset getCharset() {
        return this.charset;
    }

    public String getBoundary() {
        return this.boundary;
    }
}
