/*
 * $RCSfile: AddCRIF.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:56:11 $
 * $State: Exp $
 */
package com.sun.media.jai.opimage;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;
import java.awt.image.renderable.RenderContext;
import javax.media.jai.CRIFImpl;
import javax.media.jai.ImageLayout;
import java.util.Map;

/**
 * A <code>CRIF</code> supporting the "Add" operation in the rendered
 * and renderable image layers.
 *
 * @see javax.media.jai.operator.AddDescriptor
 * @see AddOpImage
 *
 */
public class AddCRIF extends CRIFImpl {

    /** Constructor. */
    public AddCRIF() {
        super("add");
    }

    /**
     * Creates a new instance of <code>AddOpImage</code> in the rendered
     * layer. This method satisfies the implementation of RIF.
     *
     * @param paramBlock   The two source images to be added.
     * @param renderHints  Optionally contains destination image layout.
     */
    public RenderedImage create(ParameterBlock paramBlock,
                                RenderingHints renderHints) {
        // Get ImageLayout from renderHints if any.
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        

        return new AddOpImage(paramBlock.getRenderedSource(0),
                              paramBlock.getRenderedSource(1),
                              renderHints,
                              layout);
    }
}
