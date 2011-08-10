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
package org.geotools.gui.swing.style.sld;

import java.awt.Component;
import org.geotools.gui.swing.style.StyleElementEditor;
import org.geotools.map.MapLayer;
import org.geotools.styling.LabelPlacement;
import org.geotools.styling.LinePlacement;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.StyleBuilder;

/**
 * Label placement panel
 * 
 * @author Johann Sorel
 *
 * @source $URL: http://svn.osgeo.org/geotools/tags/2.6.2/modules/unsupported/widgets-swing-pending/src/main/java/org/geotools/gui/swing/style/sld/JLabelPlacementPane.java $
 */
public class JLabelPlacementPane extends javax.swing.JPanel implements StyleElementEditor<LabelPlacement> {

    private MapLayer layer = null;
    private LabelPlacement placement = null;

    /** Creates new form JPointPlacementPanel */
    public JLabelPlacementPane() {
        initComponents();
    }

    public void setLayer(MapLayer layer) {
        this.layer = layer;
        guiLine.setLayer(layer);
        guiPoint.setLayer(layer);
    }

    public MapLayer getLayer() {
        return layer;
    }

    public void setEdited(LabelPlacement target) {
        placement = target;

        if (placement != null) {

            if(target instanceof LinePlacement){
                jrbLine.setSelected(true);
                guiLine.setEdited( (LinePlacement)target);
            }else if(target instanceof PointPlacement){
                jrbPoint.setSelected(true);
                guiPoint.setEdited( (PointPlacement)target);
            }
        }

    }

    public LabelPlacement getEdited() {

        if (placement == null) {
            placement = new StyleBuilder().createPointPlacement();
        }

        apply();
        return placement;
    }

    public void apply() {
        if (placement != null) {
            if(jrbLine.isSelected()){
                placement = guiLine.getEdited();
            }else{
                placement = guiPoint.getEdited();
            }
        }
    }

    public Component getComponent() {
        return this;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        grpType = new javax.swing.ButtonGroup();
        guiPoint = new org.geotools.gui.swing.style.sld.JPointPlacementPane();
        guiLine = new org.geotools.gui.swing.style.sld.JLinePlacementPane();
        jrbLine = new javax.swing.JRadioButton();
        jrbPoint = new javax.swing.JRadioButton();

        setOpaque(false);

        guiPoint.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        guiPoint.setOpaque(false);

        guiLine.setOpaque(false);

        grpType.add(jrbLine);
        jrbLine.setSelected(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/geotools/gui/swing/style/sld/Bundle"); // NOI18N
        jrbLine.setText(bundle.getString("lineplacement")); // NOI18N

        grpType.add(jrbPoint);
        jrbPoint.setText(bundle.getString("pointplacement")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jrbLine)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(guiLine, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(jrbPoint)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(guiPoint, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jrbLine)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiLine, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, 18)
                .add(jrbPoint)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(guiPoint, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup grpType;
    private org.geotools.gui.swing.style.sld.JLinePlacementPane guiLine;
    private org.geotools.gui.swing.style.sld.JPointPlacementPane guiPoint;
    private javax.swing.JRadioButton jrbLine;
    private javax.swing.JRadioButton jrbPoint;
    // End of variables declaration//GEN-END:variables
}
