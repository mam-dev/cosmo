/*
 * Copyright 2008 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test StringPropertyUtils
 *
 */
public class StringPropertyUtilsTest {
    
    /**
     * Tests get child keys.
     * 
     * @throws Exception
     *             - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetChildKeys() throws Exception {
        String[] testKeys = {"a.b.c", "a.b.d", "a.b.d.foo", "a.e.f.g.h.i"};
        
        String[] childKeys = StringPropertyUtils.getChildKeys("a", testKeys);
        Assert.assertEquals(2, childKeys.length);
        verifyContains(childKeys, "b");
        verifyContains(childKeys, "e");
        
        childKeys = StringPropertyUtils.getChildKeys("a.", testKeys);
        Assert.assertEquals(2, childKeys.length);
        verifyContains(childKeys, "b");
        verifyContains(childKeys, "e");
        
        childKeys = StringPropertyUtils.getChildKeys("a.b", testKeys);
        Assert.assertEquals(2, childKeys.length);
        verifyContains(childKeys, "c");
        verifyContains(childKeys, "d");
        
        childKeys = StringPropertyUtils.getChildKeys("a.b.d", testKeys);
        Assert.assertEquals(1, childKeys.length);
        verifyContains(childKeys, "foo");

        childKeys = StringPropertyUtils.getChildKeys("a.b.d.foo", testKeys);
        Assert.assertEquals(0, childKeys.length);
        
        childKeys = StringPropertyUtils.getChildKeys("ldksf", testKeys);
        Assert.assertEquals(0, childKeys.length);
       
    }
    /**
     * Tests get child properties.
     * @throws Exception - if something is wrong this exception is thrown.
     */
    @Test
    public void testGetChildProperties() throws Exception {
        HashMap<String, String> testProps = new HashMap<String, String>();
        testProps.put("a.b.c", "foo1");
        testProps.put("a.b.d", "foo2");
        testProps.put("a.b.e.f", "foo3");
        
        Map<String, String> childProps = StringPropertyUtils.getChildProperties("a.b", testProps);
        Assert.assertEquals(2, childProps.size());
        Assert.assertEquals("foo1", childProps.get("c"));
        Assert.assertEquals("foo2", childProps.get("d"));
        
        childProps = StringPropertyUtils.getChildProperties("a.b.c", testProps);
        Assert.assertEquals(0, childProps.size());
        
        childProps = StringPropertyUtils.getChildProperties("a", testProps);
        Assert.assertEquals(0, childProps.size());
        
        childProps = StringPropertyUtils.getChildProperties("afsdfasd", testProps);
        Assert.assertEquals(0, childProps.size());
       
    }
    
    /**
     * Verify contains.
     * @param strs strs.
     * @param str str.
     */
    private void verifyContains(String[] strs, String str) {
        for(String s: strs) {
            if(s.equals(str)) {
                return;
            }
        }
        
        Assert.fail("String " + str + " not found");
    }
    
}           
