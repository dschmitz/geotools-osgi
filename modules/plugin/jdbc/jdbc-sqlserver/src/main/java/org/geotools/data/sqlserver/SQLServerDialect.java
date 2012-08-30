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
package org.geotools.data.sqlserver;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.util.Map;
import java.util.UUID;

import org.geotools.data.jdbc.FilterToSQL;
import org.geotools.factory.Hints;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.jdbc.BasicSQLDialect;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Dialect implementation for Microsoft SQL Server.
 * 
 * @author Justin Deoliveira, OpenGEO
 *
 *
 *
 *
 * @source $URL$
 */
public class SQLServerDialect extends BasicSQLDialect {

    private static final int DEFAULT_AXIS_MAX = 10000000;
    private static final int DEFAULT_AXIS_MIN = -10000000;

    public SQLServerDialect(JDBCDataStore dataStore) {
        super(dataStore);
    }
    
    @Override
    public boolean includeTable(String schemaName, String tableName,
            Connection cx) throws SQLException {
        return !("INFORMATION_SCHEMA".equals( schemaName ) || "sys".equals( schemaName ) );
    }
    
    @Override
    public String getGeometryTypeName(Integer type) {
        return "geometry";
    }
    
    @Override
    public void registerClassToSqlMappings(Map<Class<?>, Integer> mappings) {
        super.registerClassToSqlMappings(mappings);
        
        //override since sql server maps all date times to timestamp
        mappings.put( Date.class, Types.TIMESTAMP );
        mappings.put( Time.class, Types.TIMESTAMP );
    }
    
    @Override
    public void registerSqlTypeNameToClassMappings(
            Map<String, Class<?>> mappings) {
        super.registerSqlTypeNameToClassMappings(mappings);
        
        
        mappings.put( "geometry", Geometry.class );
        mappings.put( "uniqueidentifier", UUID.class );
    }
    
    @Override
    public void registerSqlTypeToSqlTypeNameOverrides(
            Map<Integer, String> overrides) {
        super.registerSqlTypeToSqlTypeNameOverrides(overrides);
        
        //force varchar, if not it will default to nvarchar which won't support length restrictions
        overrides.put( Types.VARCHAR, "varchar");
    }
    
    @Override
    public void postCreateTable(String schemaName, SimpleFeatureType featureType, Connection cx)
            throws SQLException, IOException {
        
        Statement st = cx.createStatement();
        try {
            //create spatial indexes for all geometry columns
            for (AttributeDescriptor ad : featureType.getAttributeDescriptors()) {
                if (ad instanceof GeometryDescriptor) {
                    String bbox = null;
                    GeometryDescriptor gd = (GeometryDescriptor) ad;
                    
                    //get the crs, and derive a bounds
                    //TODO: stop being lame and properly figure out the dimension and bounds, see 
                    // oracle dialect for the proper way to do it
                    if (gd.getCoordinateReferenceSystem() != null) { 
                        CoordinateReferenceSystem crs = gd.getCoordinateReferenceSystem();
                        CoordinateSystem cs = crs.getCoordinateSystem();
                        if (cs.getDimension() == 2) {
                            CoordinateSystemAxis a0 = cs.getAxis(0);
                            CoordinateSystemAxis a1 = cs.getAxis(1);
                            bbox = "(";
                            bbox += (Double.isInfinite(a0.getMinimumValue()) ? 
                                DEFAULT_AXIS_MIN : a0.getMinimumValue()) + ", ";
                            bbox += (Double.isInfinite(a1.getMinimumValue()) ?
                                DEFAULT_AXIS_MIN : a1.getMinimumValue()) + ", ";

                            bbox += (Double.isInfinite(a0.getMaximumValue()) ? 
                                DEFAULT_AXIS_MAX : a0.getMaximumValue()) + ", ";
                            bbox += Double.isInfinite(a1.getMaximumValue()) ?
                                DEFAULT_AXIS_MAX : a1.getMaximumValue();
                            bbox += ")";
                        }
                    }
                    
                    if (bbox == null) {
                        //no crs or could not figure out bounds
                        continue;
                    }
                    StringBuffer sql = new StringBuffer("CREATE SPATIAL INDEX ");
                    encodeTableName(featureType.getTypeName()+"_"+gd.getLocalName()+"_index", sql);
                    sql.append( " ON ");
                    encodeTableName(featureType.getTypeName(), sql);
                    sql.append("(");
                    encodeColumnName(null, gd.getLocalName(), sql);
                    sql.append(")");
                    sql.append( " WITH ( BOUNDING_BOX = ").append(bbox).append(")");
                    
                    LOGGER.fine(sql.toString());
                    st.execute(sql.toString());
                }
            }
        }
        finally {
            dataStore.closeSafe(st);
        }
    }
    
    @Override
    public Integer getGeometrySRID(String schemaName, String tableName,
            String columnName, Connection cx) throws SQLException {
        
        StringBuffer sql = new StringBuffer("SELECT TOP 1 ");
        encodeColumnName(null, columnName, sql);
        sql.append( ".STSrid");
        
        sql.append( " FROM ");
        encodeTableName(schemaName, tableName, sql, true);
        
        sql.append( " WHERE ");
        encodeColumnName(null, columnName, sql );
        sql.append( " IS NOT NULL");
        
        dataStore.getLogger().fine( sql.toString() );
        
        Statement st = cx.createStatement();
        try {
            
            ResultSet rs = st.executeQuery( sql.toString() );
            try {
                if ( rs.next() ) {
                    return rs.getInt( 1 );
                }
                // the default sql server srid
                return 0;
            }
            finally {
                dataStore.closeSafe( rs );
            }
        }
        finally {
            dataStore.closeSafe( st );
        }
    }

    @Override
    public void encodeGeometryColumn(GeometryDescriptor gatt, String prefix,
            int srid, Hints hints, StringBuffer sql) {
        encodeColumnName( prefix, gatt.getLocalName(), sql );
        sql.append( ".STAsBinary()");
    }

    @Override
    public void encodeGeometryValue(Geometry value, int srid, StringBuffer sql)
            throws IOException {
        
        if ( value == null ) {
            sql.append( "NULL");
            return;
        }
        
        sql.append( "geometry::STGeomFromText('").append( value.toText() ).append( "',").append( srid ).append(")");
    }
    
    @Override
    public Geometry decodeGeometryValue(GeometryDescriptor descriptor,
            ResultSet rs, String column, GeometryFactory factory, Connection cx)
            throws IOException, SQLException {
       byte[] bytes = rs.getBytes(column);
       if(bytes == null) {
           return null;
       }
       try {
           return new WKBReader(factory).read(bytes);
       } catch ( ParseException e ) {
           throw (IOException) new IOException().initCause( e );
       }
    }
    
    Geometry decodeGeometry( String s, GeometryFactory factory ) throws IOException {
        if ( s == null ) {
            return null;
        }
        if ( factory == null ) {
            factory = new GeometryFactory();
        }
        
        String[] split = s.split( ":" );
        
        String  srid = split[0];
        
        Geometry g = null;
        try {
            g = new WKTReader(factory).read( split[1] );
        }
        catch ( ParseException e ) {
            throw (IOException) new IOException().initCause( e );
        }
        
        CoordinateReferenceSystem crs;
        try {
            crs = CRS.decode( "EPSG:" + srid );
        } 
        catch (Exception e ) {
            throw (IOException) new IOException().initCause( e );
        }
        
        g.setUserData( crs );
        return g;
    }

    @Override
    public void encodeGeometryEnvelope(String tableName, String geometryColumn,
            StringBuffer sql) {
        sql.append( "CAST(");
        encodeColumnName( null, geometryColumn, sql );
        sql.append( ".STSrid as VARCHAR)");
        
        sql.append( " + ':' + " );
        
        encodeColumnName( null, geometryColumn, sql );
        sql.append( ".STEnvelope().ToString()");
    }
    
    @Override
    public Envelope decodeGeometryEnvelope(ResultSet rs, int column,
            Connection cx) throws SQLException, IOException {
        String s = rs.getString( column );
        Geometry g = decodeGeometry( s, null );
        if ( g == null ) {
            return null;
        }
        
        return new ReferencedEnvelope( g.getEnvelopeInternal(), (CoordinateReferenceSystem) g.getUserData() );
    }
    
    @Override
    public Object getNextAutoGeneratedValue(String schemaName,
            String tableName, String columnName, Connection cx)
            throws SQLException {
        
        StringBuffer sql = new StringBuffer("SELECT");
        sql.append( " IDENT_CURRENT('");
        encodeTableName(schemaName, tableName, sql, false);
        sql.append("')");
        sql.append( " + ");
        sql.append( " IDENT_INCR('");
        encodeTableName(schemaName, tableName, sql, false);
        sql.append("')");
        
        dataStore.getLogger().fine( sql.toString() );
        
        Statement st = cx.createStatement();
        try {
            ResultSet rs = st.executeQuery( sql.toString() );
            try {
                rs.next();
                return rs.getInt( 1 );
            }
            finally {
                dataStore.closeSafe( rs );
            }
        }
        finally {
            dataStore.closeSafe( st );
        }
         
    }
    
    @Override
    public FilterToSQL createFilterToSQL() {
        return new SQLServerFilterToSQL();
    }
    
    protected void encodeTableName(String schemaName, String tableName, StringBuffer sql, boolean escape) {
        if (schemaName != null) {
            if (escape) {
                encodeSchemaName(schemaName, sql);
            }
            else {
                sql.append(schemaName);
            }
            sql.append(".");
        }
        if (escape) {
            encodeTableName(tableName, sql);
        }
        else {
            sql.append(tableName);
        }
    }
    
    @Override
    public boolean isLimitOffsetSupported() {
        return true;
    }
    
    @Override
    public void applyLimitOffset(StringBuffer sql, int limit, int offset) {
        // if we have a nested query (used in sql views) we might have a inner order by,
        // check for the last closed )
        int lastClosed = sql.lastIndexOf(")");
        int orderByIndex = sql.lastIndexOf("ORDER BY");
        CharSequence orderBy;
        if(orderByIndex > 0 && orderByIndex > lastClosed) {
            // we'll move the order by into the ROW_NUMBER call
            orderBy = sql.subSequence(orderByIndex, sql.length());
            sql.delete(orderByIndex, orderByIndex + orderBy.length());
        } else {
            // ROW_NUMBER requires an order by clause, we need to feed it something
            orderBy = "ORDER BY CURRENT_TIMESTAMP";
        }
        
        // now insert the order by inside the select
        int fromStart = sql.indexOf("FROM");
        sql.insert(fromStart - 1, ", ROW_NUMBER() OVER (" + orderBy + ") AS _GT_ROW_NUMBER ");
        
        // and wrap inside a block that selects the portion we want
        sql.insert(0, "SELECT * FROM (");
        sql.append(") AS _GT_PAGING_SUBQUERY WHERE ");
        if(offset > 0) {
            sql.append("_GT_ROW_NUMBER > " + offset);
        }
        if(limit >= 0 && limit < Integer.MAX_VALUE) {
            int max = limit;
            if(offset > 0) {
                max += offset;
                sql.append(" AND ");
            }
            sql.append("_GT_ROW_NUMBER <= " + max);
        }
    }
    
    @Override
    public void encodeValue(Object value, Class type, StringBuffer sql) {
        if(byte[].class.equals(type)) {
            byte[] b = (byte[]) value;
            
            //encode as hex string
            sql.append("0x");
            for (int i=0; i < b.length; i++) {
                sql.append(Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 ));
            }
        } else {
            super.encodeValue(value, type, sql);
        }
    }
    
}
