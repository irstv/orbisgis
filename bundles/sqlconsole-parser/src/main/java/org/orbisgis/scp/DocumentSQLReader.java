package org.orbisgis.scp;

import org.h2.util.ScriptReader;
import org.orbisgis.sqlparserapi.ScriptSplitter;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import java.io.IOException;
import java.io.Reader;

/**
 * Iterate over statements in document
 * @author Nicolas Fortin
 */
public class DocumentSQLReader implements ScriptSplitter {

    private Document document;
    private int position = 0;
    private ScriptReader scriptReader;
    private String statement;
    private String nextStatement;
    private boolean commentStatement;
    private boolean nextCommentStatement;

    public DocumentSQLReader(Document document) {
        this(document, false);
    }

    public DocumentSQLReader(Document document, boolean skipRemarks) {
        this.document = document;
        DocumentReader documentReader = new DocumentReader(document);
        scriptReader = new ScriptReader(documentReader);
        scriptReader.setSkipRemarks(skipRemarks);
        nextStatement = scriptReader.readStatement();
        nextCommentStatement = scriptReader.isInsideRemark();
    }

    @Override
    public boolean hasNext() {
        return nextStatement!=null;
    }

    @Override
    public int getLineIndex() {
        return getLineIndex(position);
    }

    @Override
    public int getLineIndex(int charOffset) {
        Element map = document.getDefaultRootElement();
        return map.getElementIndex(charOffset);
    }

    @Override
    public boolean isInsideRemark() {
        return commentStatement;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public String next() {
        if(statement != null) {
            position += statement.length() + 1;
        }
        statement = nextStatement;
        commentStatement = nextCommentStatement;
        nextStatement = scriptReader.readStatement();
        nextCommentStatement = scriptReader.isInsideRemark();
        return statement;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("It is a SQL reader");
    }

    /**
     * Wrap a Document into a Reader.
     */
    private static class DocumentReader extends Reader {
        private Document document;
        private int offset=0;

        /**
         * @param document Document to read
         */
        public DocumentReader(Document document) {
            this.document = document;
        }

        @Override
        public void close() throws IOException {
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            int totalLength = document.getLength();
            if(offset >= totalLength) {
                return -1;
            }
            int effectiveLen = Math.min(len, totalLength - offset);
            try {
                String text = document.getText(offset, effectiveLen);
                text.getChars(0,text.length(),cbuf,off);
            } catch (BadLocationException ex) {
                throw new IOException(ex);
            } catch(ArrayIndexOutOfBoundsException ex) {
                // Should not happen, this error give more information than stack trace
                throw new IOException("DocumentReader.read(buff,"+off+","+len+") offset="+offset, ex);
            }
            offset += effectiveLen;
            // return -1 if end of document
            return effectiveLen;
        }
    }
}
