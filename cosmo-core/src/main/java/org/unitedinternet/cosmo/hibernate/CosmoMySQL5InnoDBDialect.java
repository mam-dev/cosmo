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

import java.sql.Types;

import org.hibernate.dialect.MariaDB53Dialect;

/**
 * Overrides default MariaDB53Dialect and uses decimal instead of numeric for numeric types. In MySQL5, numeric is
 * implemented as decimal, so event though numeric works, it is the same as decimal and schema validation was failing
 * because Hibernate was expecting numeric and was getting back decimal from the JDBC meta data.
 *
 */
public class CosmoMySQL5InnoDBDialect extends MariaDB53Dialect {
    public CosmoMySQL5InnoDBDialect() {
        super();
        registerColumnType(Types.NUMERIC, "decimal($p,$s)");
        /*
         * Fix for: "Wrong column type in begenda.attribute for column booleanvalue. Found: bit, expected: boolean" at
         * validation. See https://hibernate.atlassian.net/browse/HHH-6935
         */
        registerColumnType(Types.BOOLEAN, "tinyint");
    }
}
