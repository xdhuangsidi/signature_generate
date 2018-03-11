package org.apache.http.entity.mime;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.Args;

public class MultipartEntityBuilder {
    private static final String DEFAULT_SUBTYPE = "form-data";
    private static final char[] MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    private List<FormBodyPart> bodyParts = null;
    private String boundary = null;
    private Charset charset = null;
    private ContentType contentType;
    private HttpMultipartMode mode = HttpMultipartMode.STRICT;

    public static MultipartEntityBuilder create() {
        return new MultipartEntityBuilder();
    }

    MultipartEntityBuilder() {
    }

    public MultipartEntityBuilder setMode(HttpMultipartMode mode) {
        this.mode = mode;
        return this;
    }

    public MultipartEntityBuilder setLaxMode() {
        this.mode = HttpMultipartMode.BROWSER_COMPATIBLE;
        return this;
    }

    public MultipartEntityBuilder setStrictMode() {
        this.mode = HttpMultipartMode.STRICT;
        return this;
    }

    public MultipartEntityBuilder setBoundary(String boundary) {
        this.boundary = boundary;
        return this;
    }

    public MultipartEntityBuilder setMimeSubtype(String subType) {
        Args.notBlank(subType, "MIME subtype");
        this.contentType = ContentType.create("multipart/" + subType);
        return this;
    }

    @Deprecated
    public MultipartEntityBuilder seContentType(ContentType contentType) {
        return setContentType(contentType);
    }

    public MultipartEntityBuilder setContentType(ContentType contentType) {
        Args.notNull(contentType, "Content type");
        this.contentType = contentType;
        return this;
    }

    public MultipartEntityBuilder setCharset(Charset charset) {
        this.charset = charset;
        return this;
    }

    public MultipartEntityBuilder addPart(FormBodyPart bodyPart) {
        if (bodyPart != null) {
            if (this.bodyParts == null) {
                this.bodyParts = new ArrayList();
            }
            this.bodyParts.add(bodyPart);
        }
        return this;
    }

    public MultipartEntityBuilder addPart(String name, ContentBody contentBody) {
        Args.notNull(name, "Name");
        Args.notNull(contentBody, "Content body");
        return addPart(FormBodyPartBuilder.create(name, contentBody).build());
    }

    public MultipartEntityBuilder addTextBody(String name, String text, ContentType contentType) {
        return addPart(name, new StringBody(text, contentType));
    }

    public MultipartEntityBuilder addTextBody(String name, String text) {
        return addTextBody(name, text, ContentType.DEFAULT_TEXT);
    }

    public MultipartEntityBuilder addBinaryBody(String name, byte[] b, ContentType contentType, String filename) {
        return addPart(name, new ByteArrayBody(b, contentType, filename));
    }

    public MultipartEntityBuilder addBinaryBody(String name, byte[] b) {
        return addBinaryBody(name, b, ContentType.DEFAULT_BINARY, null);
    }

    public MultipartEntityBuilder addBinaryBody(String name, File file, ContentType contentType, String filename) {
        return addPart(name, new FileBody(file, contentType, filename));
    }

    public MultipartEntityBuilder addBinaryBody(String name, File file) {
        return addBinaryBody(name, file, ContentType.DEFAULT_BINARY, file != null ? file.getName() : null);
    }

    public MultipartEntityBuilder addBinaryBody(String name, InputStream stream, ContentType contentType, String filename) {
        return addPart(name, new InputStreamBody(stream, contentType, filename));
    }

    public MultipartEntityBuilder addBinaryBody(String name, InputStream stream) {
        return addBinaryBody(name, stream, ContentType.DEFAULT_BINARY, null);
    }

    private String generateBoundary() {
        StringBuilder buffer = new StringBuilder();
        Random rand = new Random();
        int count = rand.nextInt(11) + 30;
        for (int i = 0; i < count; i++) {
            buffer.append(MULTIPART_CHARS[rand.nextInt(MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
    }

    MultipartFormEntity buildEntity() {
        AbstractMultipartForm form;
        String boundaryCopy = this.boundary;
        if (boundaryCopy == null && this.contentType != null) {
            boundaryCopy = this.contentType.getParameter("boundary");
        }
        if (boundaryCopy == null) {
            boundaryCopy = generateBoundary();
        }
        Charset charsetCopy = this.charset;
        if (charsetCopy == null && this.contentType != null) {
            charsetCopy = this.contentType.getCharset();
        }
        List<NameValuePair> paramsList = new ArrayList(2);
        paramsList.add(new BasicNameValuePair("boundary", boundaryCopy));
        if (charsetCopy != null) {
            paramsList.add(new BasicNameValuePair("charset", charsetCopy.name()));
        }
        NameValuePair[] params = (NameValuePair[]) paramsList.toArray(new NameValuePair[paramsList.size()]);
        ContentType contentTypeCopy = this.contentType != null ? this.contentType.withParameters(params) : ContentType.create("multipart/form-data", params);
        List<FormBodyPart> bodyPartsCopy = this.bodyParts != null ? new ArrayList(this.bodyParts) : Collections.emptyList();
        switch (this.mode != null ? this.mode : HttpMultipartMode.STRICT) {
            case BROWSER_COMPATIBLE:
                form = new HttpBrowserCompatibleMultipart(charsetCopy, boundaryCopy, bodyPartsCopy);
                break;
            case RFC6532:
                form = new HttpRFC6532Multipart(charsetCopy, boundaryCopy, bodyPartsCopy);
                break;
            default:
                form = new HttpStrictMultipart(charsetCopy, boundaryCopy, bodyPartsCopy);
                break;
        }
        return new MultipartFormEntity(form, contentTypeCopy, form.getTotalLength());
    }

    public HttpEntity build() {
        return buildEntity();
    }
}
