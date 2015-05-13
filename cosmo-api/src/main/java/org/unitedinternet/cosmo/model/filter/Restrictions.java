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
package org.unitedinternet.cosmo.model.filter;

/**
 * Provides convienent FilterCriteria 
 *
 */
public class Restrictions {
    public static FilterCriteria eq(Object value) {
        return new EqualsExpression(value);
    }
    
    public static FilterCriteria between(Object value1, Object value2) {
        return new BetweenExpression(value1, value2);
    }
    
    public static FilterCriteria neq(Object value) {
        EqualsExpression exp = new EqualsExpression(value);
        exp.setNegated(true);
        return exp;
    }
    
    public static FilterCriteria like(String value) {
        return new LikeExpression(value);
    }
    
    public static FilterCriteria nlike(String value) {
        LikeExpression exp = new LikeExpression(value);
        exp.setNegated(true);
        return exp;
    }
    
    public static FilterCriteria ilike(String value) {
        return new ILikeExpression(value);
    }
    
    public static FilterCriteria nilike(String value) {
        ILikeExpression exp = new ILikeExpression(value);
        exp.setNegated(true);
        return exp;
    }
    
    public static FilterCriteria isNull() {
        return new NullExpression();
    }
}
