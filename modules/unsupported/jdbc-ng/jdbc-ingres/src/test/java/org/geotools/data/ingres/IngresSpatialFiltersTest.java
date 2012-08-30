package org.geotools.data.ingres;

import org.geotools.jdbc.JDBCDataStoreAPITestSetup;
import org.geotools.jdbc.JDBCSpatialFiltersTest;

/**
 * 
 *
 * @source $URL$
 */
public class IngresSpatialFiltersTest extends JDBCSpatialFiltersTest {

	@Override
	protected JDBCDataStoreAPITestSetup createTestSetup() {
		return new IngresDataStoreAPITestSetup(new IngresTestSetup());
	}

}
