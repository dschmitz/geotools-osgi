/*
 * $RCSfile: AddConstDescriptor.java,v $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.1 $
 * $Date: 2005/02/11 04:57:29 $
 * $State: Exp $
 */
package javax.media.jai.operator;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderableImage;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderableOp;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RenderableRegistryMode;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * An <code>OperationDescriptor</code> describing the "AddConst" operation.
 *
 * <p> The AddConst operation takes one rendered or renderable source
 * image and an array of double constants, and adds a constant to
 * every pixel of its corresponding band of the source.  If the number
 * of constants supplied is less than the number of bands of the
 * destination, then the constant from entry 0 is applied to all the
 * bands.  Otherwise, a constant from a different entry is applied to
 * each band.
 *
 * <p> By default, the destination image bound, data type, and number of
 * bands are the same as the source image. If the result of the operation
 * underflows/overflows the minimum/maximum value supported by the
 * destination data type, then it will be clamped to the minimum/maximum
 * value respectively.
 *
 * <p> The destination pixel values are defined by the pseudocode:
 * <pre>
 * if (constants.length < dstNumBands) {
 *     dst[x][y][b] = src[x][y][b] + constants[0];
 * } else {
 *     dst[x][y][b] = src[x][y][b] + constants[b];
 * }
 * </pre>
 *
 * <p><table border=1>
 * <caption>Resource List</caption>
 * <tr><th>Name</th>        <th>Value</th></tr>
 * <tr><td>GlobalName</td>  <td>AddConst</td></tr>
 * <tr><td>LocalName</td>   <td>AddConst</td></tr>
 * <tr><td>Vendor</td>      <td>com.sun.media.jai</td></tr>
 * <tr><td>Description</td> <td>Adds constants to an image.<td></tr>
 * <tr><td>DocURL</td>      <td>http://java.sun.com/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/operator/AddConstDescriptor.html</td></tr>
 * <tr><td>Version</td>     <td>1.0</td></tr>
 * <tr><td>arg0Desc</td>    <td>The constants to be added.</td></tr>
 * </table></p>
 *
 * <p><table border=1>
 * <caption>Parameter List</caption>
 * <tr><th>Name</th>      <th>Class Type</th>
 *                        <th>Default Value</th></tr>
 * <tr><td>constants</td> <td>double[]</td>
 *                        <td>{0.0}</td>
 * </table></p>
 *
 * @see javax.media.jai.OperationDescriptor
 */
public class AddConstDescriptor extends OperationDescriptorImpl {

    /**
     * The resource strings that provide the general documentation
     * and specify the parameter list for this operation.
     */
    private static final String[][] resources = {
        {"GlobalName",  "AddConst"},
        {"LocalName",   "AddConst"},
        {"Vendor",      "com.sun.media.jai"},
        {"Description", JaiI18N.getString("AddConstDescriptor0")},
        {"DocURL",      "http://java.sun.com/products/java-media/jai/forDevelopers/jai-apidocs/javax/media/jai/operator/AddConstDescriptor.html"},
        {"Version",     JaiI18N.getString("DescriptorVersion")},
        {"arg0Desc",    JaiI18N.getString("AddConstDescriptor1")}
    };

    /** The parameter name list for this operation. */
    private static final String[] paramNames = {
        "constants"
    };

    /**
     * The parameter class list for this operation.
     * The number of constants provided should be either 1, in which case
     * this same constant is applied to all the source bands; or the same
     * number as the source bands, in which case one contant is applied
     * to each band.
     */
    private static final Class[] paramClasses = {
        double[].class
    };

    /** The parameter default value list for this operation. */
    private static final Object[] paramDefaults = {
        new double[] {0.0}
    };

    /** Constructor. */
    public AddConstDescriptor() {
        super(resources, 1, paramClasses, paramNames, paramDefaults);
    }

    /** Returns <code>true</code> since renderable operation is supported. */
    public boolean isRenderableSupported() {
        return true;
    }

    /**
     * Validates the input parameter.
     *
     * <p> In addition to the standard checks performed by the
     * superclass method, this method checks that the length of the
     * "constants" array is at least 1.
     */
    protected boolean validateParameters(ParameterBlock args,
                                         StringBuffer message) {
        if (!super.validateParameters(args, message)) {
            return false;
        }

        int length = ((double[])args.getObjectParameter(0)).length;
        if (length < 1) {
            message.append(getName() + " " +
                           JaiI18N.getString("AddConstDescriptor2"));
            return false;
        }

        return true;
    }


    /**
     * Adds constants to an image.
     *
     * <p>Creates a <code>ParameterBlockJAI</code> from all
     * supplied arguments except <code>hints</code> and invokes
     * {@link JAI#create(String,ParameterBlock,RenderingHints)}.
     *
     * @see JAI
     * @see ParameterBlockJAI
     * @see RenderedOp
     *
     * @param source0 <code>RenderedImage</code> source 0.
     * @param constants The constants to be added.
     * May be <code>null</code>.
     * @param hints The <code>RenderingHints</code> to use.
     * May be <code>null</code>.
     * @return The <code>RenderedOp</code> destination.
     * @throws IllegalArgumentException if <code>source0</code> is <code>null</code>.
     */
    public static RenderedOp create(RenderedImage source0,
                                    double[] constants,
                                    RenderingHints hints)  {
        ParameterBlockJAI pb =
            new ParameterBlockJAI("AddConst",
                                  RenderedRegistryMode.MODE_NAME);

        pb.setSource("source0", source0);

        pb.setParameter("constants", constants);

        return JAI.create("AddConst", pb, hints);
    }

    /**
     * Adds constants to an image.
     *
     * <p>Creates a <code>ParameterBlockJAI</code> from all
     * supplied arguments except <code>hints</code> and invokes
     * {@link JAI#createRenderable(String,ParameterBlock,RenderingHints)}.
     *
     * @see JAI
     * @see ParameterBlockJAI
     * @see RenderableOp
     *
     * @param source0 <code>RenderableImage</code> source 0.
     * @param constants The constants to be added.
     * May be <code>null</code>.
     * @param hints The <code>RenderingHints</code> to use.
     * May be <code>null</code>.
     * @return The <code>RenderableOp</code> destination.
     * @throws IllegalArgumentException if <code>source0</code> is <code>null</code>.
     */
    public static RenderableOp createRenderable(RenderableImage source0,
                                                double[] constants,
                                                RenderingHints hints)  {
        ParameterBlockJAI pb =
            new ParameterBlockJAI("AddConst",
                                  RenderableRegistryMode.MODE_NAME);

        pb.setSource("source0", source0);

        pb.setParameter("constants", constants);

        return JAI.createRenderable("AddConst", pb, hints);
    }
}
