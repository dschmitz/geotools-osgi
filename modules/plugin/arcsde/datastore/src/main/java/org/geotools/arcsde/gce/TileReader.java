/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2009, Open Source Geospatial Foundation (OSGeo)
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
 *
 */
package org.geotools.arcsde.gce;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.util.logging.Logging;

import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeRasterTile;
import com.esri.sde.sdk.client.SeRow;

/**
 * Offers an iterator like interface to fetch ArcSDE raster tiles.
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @since 2.5.4
 * @version $Id: TileReader.java 32718 2009-03-30 01:58:05Z groldan $
 * @source $URL$
 */
@SuppressWarnings( { "nls" })
final class TileReader {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.gce");

    private final int bitsPerSample;

    private final Rectangle requestedTiles;

    private final Dimension tileSize;

    private final int tileDataLength;

    private final SeRow row;

    private final int pixelsPerTile;

    private final int numberOfBands;

    private SeRasterTile nextTile;

    private boolean started;

    public static TileReader getInstance(final SeRow row, final int bitsPerSample,
            final int numberOfBands, final Rectangle requestedTiles, final Dimension tileSize) {
        return new TileReader(row, bitsPerSample, numberOfBands, requestedTiles, tileSize);
    }

    /**
     * 
     * @param row
     * @param imageDimensions
     *            the image size, x and y are the offsets, width and height the actual width and
     *            height, used to ignore incomming pixel data as appropriate to fit the image
     *            dimensions
     * @param bitsPerSample
     * @param numberOfBands2
     * @param requestedTiles
     */
    private TileReader(final SeRow row, int bitsPerSample, int numberOfBands,
            final Rectangle requestedTiles, Dimension tileSize) {
        this.row = row;
        this.bitsPerSample = bitsPerSample;
        this.numberOfBands = numberOfBands;
        this.requestedTiles = requestedTiles;
        this.tileSize = tileSize;
        this.pixelsPerTile = tileSize.width * tileSize.height;
        this.tileDataLength = (int) Math
                .ceil(((double) pixelsPerTile * (double) bitsPerSample) / 8D);
    }

    /**
     * @return number of bits per sample
     */
    public int getBitsPerSample() {
        return bitsPerSample;
    }

    /**
     * @return numbre of bands being fetched
     */
    public int getNumberOfBands() {
        return numberOfBands;
    }

    /**
     * @return number of pixels per tile over the X axis
     */
    public int getTileWidth() {
        return tileSize.width;
    }

    /**
     * @return number of pixels per tile over the Y axis
     */
    public int getTileHeight() {
        return tileSize.height;
    }

    /**
     * @return number of tiles being fetched over the X axis
     */
    public int getTilesWide() {
        return requestedTiles.width;
    }

    /**
     * @return number of tiles being fetched over the Y axis
     */
    public int getTilesHigh() {
        return requestedTiles.height;
    }

    /**
     * @return number of bytes in the raw pixel content of a tile, not taking into account any
     *         trailing bitmask data.
     */
    public int getBytesPerTile() {
        return tileDataLength;
    }

    /**
     * @return whether there are more tiles to fetch
     * @throws IOException
     */
    public boolean hasNext() throws IOException {
        if (!started) {
            try {
                nextTile = row.getRasterTile();
                started = true;
                if (nextTile == null) {
                    LOGGER.fine("No tiles to fetch at all, releasing connection");
                }
            } catch (SeException e) {
                throw new ArcSdeException(e);
            }
        }
        return nextTile != null;
    }

    /**
     * Fetches a tile and fills {@code tileData} with its raw pixel data packaged as bytes according
     * to the number of bits per sample
     * 
     * @param tileData
     *            a possibly {@code null} array where to store the next tile data. If {@code null} a
     *            new byte[] of length {@link #getBytesPerTile()} will be allocated and filled up
     *            with the raw tile pixel data.
     * @return the bitmask data, or an empty array if the tile is full
     * @throws IOException
     * @throws {@link IllegalArgumentException} if tileData is not null and its size is less than
     *         {@link #getBytesPerTile()}
     */
    public byte[] next(byte[] tileData) throws IOException {
        if (tileData == null) {
            throw new IllegalArgumentException("tileData is null");
        }

        final SeRasterTile tile;

        if (hasNext()) {
            tile = nextTile();
        } else {
            throw new IllegalStateException("There're no more tiles to fetch");
        }

        final byte[] bitMaskData = tile.getBitMaskData();

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(" >> Fetching " + tile + " - bitmask: " + bitMaskData.length
                    + " has more: " + hasNext());
        }

        final int numPixels = tile.getNumPixels();

        if (0 == numPixels) {
            LOGGER.finer("tile contains no pixel data, skipping: " + tile);
            final byte NO_DATA_MASK;
            /*
             * HACK this is hacky cause I'm guessing a sensible value for no data tiles. The full
             * solution however implies the ability to use an actual no-data value and hence to
             * promote band sample types on the fly in order to make room for an extra value holding
             * the no-data value. Promotions may be from 1-bit and 4-bit to 8-bit, 8-bit to 16-bit.
             * I don't think 16,32,64 bit should/could be promoted but it would be easier to choose
             * an unused value as the no-data value anyways.
             */
            if (bitsPerSample == 1) {
                NO_DATA_MASK = (byte) 0x00;
            } else {
                NO_DATA_MASK = (byte) 0xFF;
            }
            Arrays.fill(tileData, NO_DATA_MASK);
        } else if (pixelsPerTile == numPixels) {
            final byte[] rawTileData = tile.getPixelData();

            System.arraycopy(rawTileData, 0, tileData, 0, tileDataLength);

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("returning " + numPixels + " pixels data packaged into "
                        + tileDataLength + " bytes for tile [" + tile.getColumnIndex() + ","
                        + tile.getRowIndex() + "]");
            }
        } else {
            throw new IllegalStateException("Expected pixels per tile == " + pixelsPerTile
                    + " but got " + numPixels + ": " + tile);
        }

        return bitMaskData;
    }

    private SeRasterTile nextTile() throws IOException {
        if (nextTile == null) {
            throw new EOFException("No more tiles to read");
        }
        SeRasterTile curr = nextTile;
        try {
            nextTile = row.getRasterTile();
            if (nextTile == null) {
                LOGGER.finer("There're no more tiles to fetch");
            }
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
        return curr;
    }

}
