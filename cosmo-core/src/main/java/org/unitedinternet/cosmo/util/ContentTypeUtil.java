package org.unitedinternet.cosmo.util;

/**
 * Utilities methods for working with content types and encodings.
 * 
 * @author daniel grigore
 *
 */
public class ContentTypeUtil {
    /*
     * Note that these methods have been extracted from jackrabbit-jcr#IOUtil to avoid a dependency in pom.xml just for
     * only one class.
     */

    /**
     * Build a valid content type string from the given mimeType and encoding:
     * 
     * <pre>
     * &lt;mimeType&gt;; charset="&lt;encoding&gt;"
     * </pre>
     * 
     * If the specified mimeType is <code>null</code>, <code>null</code> is returned.
     *
     * @param mimeType
     * @param encoding
     * @return contentType or <code>null</code> if the specified mimeType is <code>null</code>
     */
    public static String buildContentType(String mimeType, String encoding) {
        String contentType = mimeType;
        if (contentType != null && encoding != null) {
            contentType += "; charset=" + encoding;
        }
        return contentType;
    }

    /**
     * Retrieve the mimeType from the specified contentType.
     *
     * @param contentType
     * @return mimeType or <code>null</code>
     */
    public static String getMimeType(String contentType) {
        String mimeType = contentType;
        if (mimeType == null) {
            // property will be removed.
            // Note however, that jcr:mimetype is a mandatory property with the
            // built-in nt:file nodetype.
            return mimeType;
        }
        // strip any parameters
        int semi = mimeType.indexOf(';');
        return (semi > 0) ? mimeType.substring(0, semi) : mimeType;
    }

    /**
     * Retrieve the encoding from the specified contentType.
     *
     * @param contentType
     * @return encoding or <code>null</code> if the specified contentType is <code>null</code> or does not define a
     *         charset.
     */
    public static String getEncoding(String contentType) {
        // find the charset parameter
        int equal;
        if (contentType == null || (equal = contentType.indexOf("charset=")) == -1) {
            // jcr:encoding property will be removed
            return null;
        }
        String encoding = contentType.substring(equal + 8);
        // get rid of any other parameters that might be specified after the charset
        int semi = encoding.indexOf(';');
        if (semi != -1) {
            encoding = encoding.substring(0, semi);
        }
        return encoding;
    }
}
