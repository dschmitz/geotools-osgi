/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.index;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Tag interface for collection that must be closed 
 * 
 * @author jesse
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/plugin/shapefile/src/main/java/org/geotools/index/CloseableCollection.java $
 */
public interface CloseableCollection<T> extends Collection<T>{

    /**
     * Close the collection so it cleans up its resources
     */
    void close() throws IOException;
    /**
     * Close the collection so it cleans up its resources
     */
    void closeIterator(Iterator<T> iter) throws IOException;
}
