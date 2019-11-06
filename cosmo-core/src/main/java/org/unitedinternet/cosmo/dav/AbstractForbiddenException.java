package org.unitedinternet.cosmo.dav;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class AbstractForbiddenException extends ForbiddenException {
    String namespaceUri;
    String localName;

    public AbstractForbiddenException(String message, String namespaceUri, String localName) {
        super(message);
        this.namespaceUri = namespaceUri;
        this.localName = localName;
    }

    protected void writeContent(XMLStreamWriter writer)
        throws XMLStreamException {
        writer.writeStartElement(namespaceUri, localName);
        if (getMessage() != null) {
            writer.writeCharacters(getMessage());
        }
        writer.writeEndElement();
    }
}
