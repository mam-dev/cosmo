package org.unitedinternet.cosmo.hibernate;

import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;

import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.engine.jdbc.internal.CharacterStreamImpl;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.MutableMutabilityPlan;
import org.unitedinternet.cosmo.CosmoIOException;
import org.unitedinternet.cosmo.CosmoParseException;
import org.unitedinternet.cosmo.CosmoSqlException;
import org.unitedinternet.cosmo.CosmoXMLStreamException;
import org.unitedinternet.cosmo.util.DomReader;
import org.unitedinternet.cosmo.util.DomWriter;
import org.w3c.dom.Element;

/**
 * Adapter for Element type descriptor.
 * 
 * @author izidaru
 *
 */
public class DOMElementTypeDescriptor extends AbstractTypeDescriptor<Element>{
    
    private static final long serialVersionUID = -6983624376673029571L;
    public static final DOMElementTypeDescriptor INSTANCE = new DOMElementTypeDescriptor();
    
    public static class DOMElementMutabilityPlan extends MutableMutabilityPlan<Element> {
        
        private static final long serialVersionUID = 5780867797450076283L;
        public static final DOMElementMutabilityPlan INSTANCE = new DOMElementMutabilityPlan();

        public Element deepCopyNotNull(Element value) {
            return (Element) value.cloneNode(true);
        }
    }
    
    protected DOMElementTypeDescriptor() {
        super(Element.class, DOMElementMutabilityPlan.INSTANCE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean areEqual(Element one, Element another) {
        if (one==null || another==null) {
            return false;
        }
        return ((Element) one).isEqualNode((Element) another);
    }

    @Override
    public Element fromString(String string) {
        Element element = null;
        try {
            element = (Element) DomReader.read(string);
        } catch (ParserConfigurationException e) {
            throw new CosmoParseException(e);
        } catch (XMLStreamException e) {
            throw new CosmoXMLStreamException(e);
        } catch (IOException e) {
            throw new CosmoIOException(e);
        }
        return element;
    }

    @Override
    public String toString(Element value) {
        String elementString = null;
        try {
            elementString = DomWriter.write(value);
        } catch (XMLStreamException e) {
            throw new CosmoXMLStreamException(e);
        } catch (IOException e) {
            throw new CosmoIOException(e);
        }
        return  elementString;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <X> X unwrap(Element value, Class<X> type, WrapperOptions options) {
        if ( value == null ) {
            return null;
        }
        if ( Element.class.isAssignableFrom( type ) ) {
            return (X) value;
        }
        if ( CharacterStream.class.isAssignableFrom( type ) ) {
                try {
                    return (X) new CharacterStreamImpl(DomWriter.write(value));
                } catch (XMLStreamException e) {
                    throw new CosmoXMLStreamException(e);
                } catch (IOException e) {
                    throw new CosmoIOException(e);
                }
        }
        throw unknownUnwrap(type);
    }

    @Override
    public <X> Element wrap(X value, WrapperOptions options) {
        if ( value == null ) {
            return null;
        }
        if ( Element.class.isInstance( value ) ) {
            return (Element) value;
        }
        if (CharacterStream.class.isInstance(value)) {
            return null;
        }
        if (Clob.class.isInstance( value ) ) {
            try {
                return (Element)DomReader.read(((Clob)value).getCharacterStream());
            } catch (ParserConfigurationException e) {
                throw new CosmoParseException(e);
            } catch (XMLStreamException e) {
                //Apple PROPFIND request is broken
                //<E:calendar-color xmlns:E="http://apple.com/ns/ical/" xmlns:E="http://apple.com/ns/ical/">#711A76FF</E:calendar-color>                
                if(e.getMessage().contains("Duplicate declaration for namespace prefix")
                        || e.getMessage().contains("was already specified for element")){
                    return null;
                }                
                throw new CosmoXMLStreamException(e);
            } catch (IOException e) {
                throw new CosmoIOException(e);
            } catch (SQLException e) {
                throw new CosmoSqlException(e);
            }

        }
        throw unknownWrap( value.getClass() );
    }

}
