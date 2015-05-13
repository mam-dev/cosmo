/*
 * Copyright 2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.hibernate;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.sql.ClobTypeDescriptor;
import org.w3c.dom.Element;

/**
 * Custom Hibernate type that persists a XML DOM Element to a CLOB field in
 * the database.
 */

public class XmlClobType extends AbstractSingleColumnStandardBasicType<Element> {
   
    private static final long serialVersionUID = 7798001623341253786L;

    public XmlClobType() {
        super(ClobTypeDescriptor.DEFAULT, DOMElementTypeDescriptor.INSTANCE);
    }

    @Override
    public String getName() {
        return "xml_clob";
    }
}
