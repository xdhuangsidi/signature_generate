package org.apache.http.entity.mime;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.Args;

public class FormBodyPart {
    private final ContentBody body;
    private final Header header;
    private final String name;

    FormBodyPart(String name, ContentBody body, Header header) {
        Args.notNull(name, "Name");
        Args.notNull(body, "Body");
        this.name = name;
        this.body = body;
        if (header == null) {
            header = new Header();
        }
        this.header = header;
    }

    @Deprecated
    public FormBodyPart(String name, ContentBody body) {
        Args.notNull(name, "Name");
        Args.notNull(body, "Body");
        this.name = name;
        this.body = body;
        this.header = new Header();
        generateContentDisp(body);
        generateContentType(body);
        generateTransferEncoding(body);
    }

    public String getName() {
        return this.name;
    }

    public ContentBody getBody() {
        return this.body;
    }

    public Header getHeader() {
        return this.header;
    }

    public void addField(String name, String value) {
        Args.notNull(name, "Field name");
        this.header.addField(new MinimalField(name, value));
    }

    @Deprecated
    protected void generateContentDisp(ContentBody body) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("form-data; name=\"");
        buffer.append(getName());
        buffer.append("\"");
        if (body.getFilename() != null) {
            buffer.append("; filename=\"");
            buffer.append(body.getFilename());
            buffer.append("\"");
        }
        addField(MIME.CONTENT_DISPOSITION, buffer.toString());
    }

    @Deprecated
    protected void generateContentType(ContentBody body) {
        ContentType contentType;
        if (body instanceof AbstractContentBody) {
            contentType = ((AbstractContentBody) body).getContentType();
        } else {
            contentType = null;
        }
        if (contentType != null) {
            addField("Content-Type", contentType.toString());
            return;
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append(body.getMimeType());
        if (body.getCharset() != null) {
            buffer.append(HTTP.CHARSET_PARAM);
            buffer.append(body.getCharset());
        }
        addField("Content-Type", buffer.toString());
    }

    @Deprecated
    protected void generateTransferEncoding(ContentBody body) {
        addField(MIME.CONTENT_TRANSFER_ENC, body.getTransferEncoding());
    }
}
