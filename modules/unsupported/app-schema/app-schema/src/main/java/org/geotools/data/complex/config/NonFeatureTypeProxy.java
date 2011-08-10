/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.data.complex.config;

import java.util.ArrayList;
import java.util.Collection;

import org.geotools.data.complex.FeatureTypeMapping;
import org.geotools.feature.type.ComplexFeatureTypeFactoryImpl;
import org.geotools.feature.type.ComplexFeatureTypeImpl;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.ComplexType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This class represents the fake feature type needed for feature chaining for properties that are
 * not features. When a non feature is mapped separately in app schema data access, it is regarded
 * as a feature since it would have a feature source.
 * 
 * @author Rini Angreani, Curtin University of Technology
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/app-schema/app-schema/src/main/java/org/geotools/data/complex/config/NonFeatureTypeProxy.java $
 */
public class NonFeatureTypeProxy extends ComplexTypeProxy implements FeatureType {

    /**
     * The attribute descriptors
     */
    private final Collection<PropertyDescriptor> descriptors;

    /**
     * The real type
     */
    private final ComplexType subject;

    /**
     * Sole constructor
     * 
     * @param type
     *            The underlying non feature type
     */
    public NonFeatureTypeProxy(final ComplexType type, final FeatureTypeMapping mapping) {
        super(type.getName(), null);

        subject = type;

        AttributeDescriptor originalTarget = mapping.getTargetFeature();
        int maxOccurs = originalTarget.getMaxOccurs();
        int minOccurs = originalTarget.getMinOccurs();
        boolean nillable = originalTarget.isNillable();
        Object defaultValue = originalTarget.getDefaultValue();
        Name name = originalTarget.getName();

        // create a new descriptor with the wrapped type and set it to the mapping
        ComplexFeatureTypeFactoryImpl typeFactory = new ComplexFeatureTypeFactoryImpl();
        AttributeDescriptor descriptor = typeFactory.createAttributeDescriptor(this, name,
                minOccurs, maxOccurs, nillable, defaultValue);
        descriptor.getUserData().putAll(originalTarget.getUserData());
        mapping.setTargetFeature(descriptor);
        // smuggle FEATURE_LINK descriptor
        descriptors = new ArrayList<PropertyDescriptor>(subject.getDescriptors()) {
            {
                add(ComplexFeatureTypeImpl.FEATURE_CHAINING_LINK);
            }
        };
    }

    /**
     * @see org.geotools.data.complex.config.ComplexTypeProxy#getSubject()
     */
    @Override
    public AttributeType getSubject() {
        return subject;
    }

    @Override
    public PropertyDescriptor getDescriptor(Name name) {
        if (name.equals(ComplexFeatureTypeImpl.FEATURE_CHAINING_LINK_NAME)) {
            return ComplexFeatureTypeImpl.FEATURE_CHAINING_LINK;
        }
        return super.getDescriptor(name);
    }

    @Override
    public Collection<PropertyDescriptor> getDescriptors() {
        return descriptors;
    }

    /**
     * Return only the schema descriptors
     * @return
     */
    public Collection<PropertyDescriptor> getTypeDescriptors() {
        return subject.getDescriptors();
    }

    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return null;
    }

    public GeometryDescriptor getGeometryDescriptor() {
        return null;
    }
}
