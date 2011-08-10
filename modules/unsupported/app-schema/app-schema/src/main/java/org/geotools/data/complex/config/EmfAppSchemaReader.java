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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.xml.resolver.Catalog;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.xml.Binding;
import org.geotools.xml.Configuration;
import org.geotools.xml.SchemaIndex;
import org.geotools.xml.Schemas;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * Parses an application schema given by a gtxml {@link Configuration} into a set of
 * {@link AttributeType}s and {@link AttributeDescriptor}s.
 * <p>
 * All the XSD schema locations that comprise the application schema are obtained from the main
 * {@link Configuration} and its dependencies.
 * </p>
 * <p>
 * Of particular interest might be the {@link ApplicationSchemaConfiguration} object, which allows
 * to provide the location of the root xsd schema for a given application schema.
 * </p>
 * 
 * @author Gabriel Roldan
 * @version $Id: EmfAppSchemaReader.java 33540 2009-07-10 06:58:21Z groldan $
 * @source $URL:
 *         http://svn.geotools.org/geotools/branches/2.4.x/modules/unsupported/community-schemas
 *         /community
 *         -schema-ds/src/main/java/org/geotools/data/complex/config/EmfAppSchemaReader.java $
 * @since 2.4
 */
public class EmfAppSchemaReader {
    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(EmfAppSchemaReader.class.getPackage().getName());

    private Catalog oasisCatalog;

    private EmfAppSchemaReader() {
        // do nothing
    }

    public Catalog getCatalog() {
        return oasisCatalog;
    }

    public void setCatalog(final Catalog oasisCatalog) {
        this.oasisCatalog = oasisCatalog;
    }

    /**
     * Parses the GML schema represented by the <code>configuration</code>'s
     * {@link Configuration#getSchemaFileURL() schema location} into a {@link SchemaIndex}.
     * 
     * @param configuration
     *            configuration object used to access the XSDSchema to parse. This configuration
     *            object might contain {@link Binding}s
     * @throws IOException
     */
    public SchemaIndex parse(Configuration configuration) throws IOException {
        // find out the schemas involved in the app schema configuration
        final SchemaIndex appSchemaIndex = Schemas.findSchemas(configuration);

        return appSchemaIndex;
    }

    /**
     * Parses the gml schema referenced by <code>location</code> into a {@link SchemaIndex}
     * 
     * @param location
     *            the phisical location of the root xsd schema that comprises the application schema
     *            to parse.
     * @param resolvedSchemaLocations
     *            A map to hold schema URI location for each element name space so they can be
     *            imported in DescribeFeatureType
     * @throws IOException
     *             if any non recoverable problem occurs while parsing the application schema
     *             pointed out by <code>location</code> or one of its dependencies.
     */
    public SchemaIndex parse(final URL location, Map<String, String> resolvedSchemaLocations)
            throws IOException {

        final String nameSpace = findSchemaNamespace(location);

        final String schemaLocation = location.toExternalForm();

        if (resolvedSchemaLocations != null) {
            resolvedSchemaLocations.put(nameSpace, schemaLocation);
        }

        // if there's an Oasis catalog set, use it to resolve schema locations
        // (can be null)
        final Catalog catalog = getCatalog();

        final Configuration configuration = new CatalogApplicationSchemaConfiguration(nameSpace,
                schemaLocation, catalog);

        return parse(configuration);
    }

    /**
     * Finds out the targetNamespace of the xsd schema referenced by <code>location</code>
     * 
     * @param location
     * @return
     * @throws IOException
     */
    private String findSchemaNamespace(URL location) throws IOException {
        String targetNamespace = null;
        // parse some of the instance document to find out the
        // schema location
        if (getCatalog() != null) {
            String resolvedLocation = getCatalog().resolveURI(location.toExternalForm());
            if (resolvedLocation != null) {
                location = new URL(resolvedLocation);
            }
        }
        InputStream input = location.openStream();

        // create stream parser
        XmlPullParser parser = null;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);

            // parse root element
            parser = factory.newPullParser();
            parser.setInput(input, "UTF-8");
            parser.nextTag();

            // look for schema location
            for (int i = 0; i < parser.getAttributeCount(); i++) {
                if ("targetNamespace".equals(parser.getAttributeName(i))) {
                    targetNamespace = parser.getAttributeValue(i);
                    break;
                }
            }
            // reset input stream
            parser.setInput(null);
        } catch (XmlPullParserException e) {
            String msg = "Cannot find target namespace for schema document " + location;
            throw (RuntimeException) new RuntimeException(msg).initCause(e);
        } finally {
            input.close();
        }
        if (targetNamespace == null) {
            throw new IllegalArgumentException(
                    "Input document does not specifies a targetNamespace");
        }
        return targetNamespace;
    }

    public static EmfAppSchemaReader newInstance() {
        return new EmfAppSchemaReader();
    }

}
