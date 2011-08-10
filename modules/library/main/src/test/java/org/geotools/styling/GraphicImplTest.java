/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.styling;

<<<<<<< local
import java.io.StringReader;
=======
>>>>>>> other
import java.net.URL;

<<<<<<< local
import org.geotools.factory.CommonFactoryFinder;

=======
>>>>>>> other
import junit.framework.TestCase;

/**
 * This test case captures specific problems encountered with the GraphicImpl code.
 *
 *
<<<<<<< local
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/library/main/src/test/java/org/geotools/styling/GraphicImplTest.java $
=======
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/8.0-M1/modules/library/main/src/test/java/org/geotools/styling/GraphicImplTest.java $
>>>>>>> other
 */
public class GraphicImplTest extends TestCase {

	/**
	 * Checks if creating a Graphic with an ExternalGraphics works.
	 */
	public void testWithExternalGraphics() throws Exception {
		StyleBuilder sb = new StyleBuilder();
		
        URL urlExternal = getClass().getResource("/data/sld/blob.gif");
        ExternalGraphic extg = sb.createExternalGraphic(urlExternal, "image/svg+xml");
        Graphic graphic = sb.createGraphic(extg, null, null);
        
        
		assertEquals(1, graphic.graphicalSymbols().size());
	}
	
	/**
	 * Checks if the Displacement settings are exported to XML
	 */
	public void testDisplacement() throws Exception {
           StyleBuilder sb = new StyleBuilder();
           
           Graphic graphic;
           {
               graphic = sb.createGraphic();
               Displacement disp = sb.createDisplacement(10.1, -5.5);
               graphic.setDisplacement(disp);
           }
           
           Displacement disp = graphic.getDisplacement();
           assertNotNull(disp);
           
           assertEquals(disp.getDisplacementX().toString(),"10.1");
           assertEquals(disp.getDisplacementY().toString(),"-5.5");
           
	}
}
