/*
 * Copyright 2006 Open Source Applications Foundation
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
package org.unitedinternet.cosmo.dao.query.hibernate;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.abdera.i18n.text.UrlEncoding;
import org.springframework.stereotype.Repository;
import org.unitedinternet.cosmo.dao.query.ItemPathTranslator;
import org.unitedinternet.cosmo.model.CollectionItem;
import org.unitedinternet.cosmo.model.Item;

/**
 * Default implementation for ItempPathTranslator. This implementation expects paths to be of the format:
 * /username/parent1/parent2/itemname
 */
@Repository
public class DefaultItemPathTranslator implements ItemPathTranslator {

    @PersistenceContext
    private EntityManager em;

    public DefaultItemPathTranslator() {

    }

    /**
     * Finds item by the given path.
     *
     * @param path
     *            The given path.
     * @param root
     *            The collection item.
     * @return The expected item.
     */
    public Item findItemByPath(final String path, final CollectionItem root) {
        return (Item) findItemByPathInternal(path, root);
    }

    /**
     * {@inheritDoc}
     */
    public Item findItemParent(String path) {
        if (path == null) {
            return null;
        }

        int lastIndex = path.lastIndexOf("/");
        if (lastIndex == -1) {
            return null;
        }

        if ((lastIndex + 1) >= path.length()) {
            return null;
        }

        String parentPath = path.substring(0, lastIndex);

        return findItemByPath(parentPath);
    }

    /**
     * {@inheritDoc}
     */
    public String getItemName(String path) {
        if (path == null) {
            return null;
        }

        int lastIndex = path.lastIndexOf("/");
        if (lastIndex == -1) {
            return null;
        }

        if ((lastIndex + 1) >= path.length()) {
            return null;
        }

        return path.substring(lastIndex + 1);
    }

    /**
     * Finds item by the given path.
     *
     * @param session
     *            The current session.
     * @param path
     *            The given path.
     * @return The expected item.
     */
    @Override
    public Item findItemByPath(String path) {

        if (path == null || "".equals(path)) {
            return null;
        }

        if (path.charAt(0) == '/') {
            path = path.substring(1, path.length());
        }

        String[] segments = path.split("/");

        if (segments.length == 0) {
            return null;
        }
        String username = decode(segments[0]);

        String rootName = username;
        Item rootItem = findRootItemByOwnerAndName(username, rootName);

        // If parent item doesn't exist don't go any further
        if (rootItem == null) {
            return null;
        }

        Item parentItem = rootItem;
        for (int i = 1; i < segments.length; i++) {
            Item nextItem = findItemByParentAndName(parentItem, decode(segments[i]));
            parentItem = nextItem;
            // if any parent item doesn't exist then bail now
            if (parentItem == null) {
                return null;
            }
        }

        return parentItem;
    }

    /**
     * Finds item by path.
     *
     * @param path
     *            The given path.
     * @param root
     *            The collection root.
     * @return The expected item.
     */
    private Item findItemByPathInternal(String path, CollectionItem root) {

        if (path == null || "".equals(path)) {
            return null;
        }

        if (path.charAt(0) == '/') {
            path = path.substring(1, path.length());
        }

        String[] segments = path.split("/");

        if (segments.length == 0) {
            return null;
        }

        Item parentItem = root;
        for (int i = 0; i < segments.length; i++) {
            Item nextItem = findItemByParentAndName(parentItem, decode(segments[i]));
            parentItem = nextItem;
            // if any parent item doesn't exist then bail now
            if (parentItem == null) {
                return null;
            }
        }

        return parentItem;
    }

    protected Item findRootItemByOwnerAndName(String username, String name) {
        TypedQuery<Item> query = this.em.createNamedQuery("item.by.ownerName.name.nullParent", Item.class)
                .setParameter("username", username).setParameter("name", name);
        List<Item> items = query.getResultList();
        return items.size() > 0 ? items.get(0) : null;
    }

    protected Item findItemByParentAndName(Item parent, String name) {
        TypedQuery<Item> query = this.em.createNamedQuery("item.by.parent.name", Item.class)
                .setParameter("parent", parent).setParameter("name", name);
        List<Item> items = query.getResultList();
        return items.size() > 0 ? items.get(0) : null;
    }

    private static String decode(String urlPath) {
        try {
            return UrlEncoding.decode(urlPath, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}