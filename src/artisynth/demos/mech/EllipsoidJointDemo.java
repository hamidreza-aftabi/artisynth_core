/**
 * Copyright (c) 2023, by the Authors: John E Lloyd (UBC), Ian Stavness (USask)
 *
 * This software is freely available under a 2-clause BSD license. Please see
 * the LICENSE file in the ArtiSynth distribution directory for details.
 */
package artisynth.demos.mech;

import java.awt.Color;
import java.io.IOException;

import artisynth.core.gui.ControlPanel;
import artisynth.core.mechmodels.EllipsoidJoint;
import artisynth.core.mechmodels.MechModel;
import artisynth.core.mechmodels.RigidBody;
import artisynth.core.workspace.DriverInterface;
import artisynth.core.workspace.RootModel;
import maspack.matrix.AxisAlignedRotation;
import maspack.matrix.RigidTransform3d;
import maspack.render.RenderProps;
import maspack.render.Renderer.AxisDrawStyle;

public class EllipsoidJointDemo extends RootModel {

   public void build (String[] args) throws IOException {

      // create mech model and set rigid body damping parameters
      MechModel mech = new MechModel ("mech");
      mech.setFrameDamping (50.0);
      mech.setRotaryDamping (10.0);
      addModel (mech);

      double size = 1.0; // size parameter
      double a = size, b = size/2, c = size/2;

      // create base box at centre of ellipsoid joint
      RigidBody base = RigidBody.createBox (
         "base", size/8, size/8, size/8, /*density=*/1000.0);
      RigidTransform3d XBW = new RigidTransform3d ();
      XBW.R.set (0, 1, 0, 0, 0, 1, 1, 0, 0);
      base.setPose (XBW);
      base.setAxisLength (size*2);
      base.setDynamic (false);
      mech.addRigidBody (base);

      // create box to slide on the ellipsoid
      RigidBody box = RigidBody.createBox (
         "box", size/4, size/4, size/16, /*density=*/1000.0);
//      box.translateCoordinateFrame (new Point3d(0,0,-size/16));
      box.setAxisLength (size);
      RigidTransform3d XAW = new RigidTransform3d();
//      XAW.R.set (0, 0, 1, 0, 0, -1, 0, 1, 0);
      XAW.R.mulRotX90 ();
      XAW.p.set(0, -c-size/16/2, 0);
//      XAW.p.set(0, -c, 0);
      box.setPose (XAW);
      mech.addRigidBody (box);

      // Add an ellipsoid joint between the box and the base
      RigidTransform3d TDB = new RigidTransform3d ();
      RigidTransform3d TCA = new RigidTransform3d ();
      TCA.p.set (0, 0, -size/16/2);
      
      EllipsoidJoint joint = new EllipsoidJoint (
         box, TCA, base, TDB, a, b, c);
      mech.addBodyConnector (joint);
      // set coordinate ranges
      joint.setMinX (-180);
      joint.setMaxX (180);
      joint.setMinY (-90);
      joint.setMaxY (90);
      joint.setMinTheta (-180);
      joint.setMaxTheta (180);
      joint.setMinPhi (-180);
      joint.setMaxPhi (180);
      
      joint.setCoordinate (0, 0);
      joint.setCoordinate (1, 0);
      joint.setCoordinate (2, 0);
//      joint.setCoordinate (3, 0);
      
      getMainViewer ().setAxialView (AxisAlignedRotation.NX_Z);

      // tilt the entire model by 45 degrees about the x axis, so the box will
      // slide under gravity
//      mech.transformGeometry (
//         new RigidTransform3d (0, 0, 0, 0, 0, Math.PI/16));

      // set rendering properties
      joint.setAxisLength (0.5*size); // draw frame C
      joint.setDrawFrameC (AxisDrawStyle.ARROW);
      joint.setDrawFrameD (AxisDrawStyle.ARROW);
      joint.setShaftLength (0.4*size); // draw the shaft
      joint.setShaftRadius (0.02*size);
      RenderProps.setFaceColor (joint, Color.BLUE); // set colors
      RenderProps.setFaceColor (box, new Color (0.5f, 1f, 1f));

      // create control panel to interactively adjust properties
      ControlPanel panel = new ControlPanel();
      panel.addWidget (joint, "x");
      panel.addWidget (joint, "xRange");
      panel.addWidget (joint, "y");
      panel.addWidget (joint, "yRange");
      panel.addWidget (joint, "theta");
      panel.addWidget (joint, "thetaRange");
      panel.addWidget (joint, "phi");
      panel.addWidget (joint, "phiRange");
      panel.addWidget (joint, "drawFrameC");
      panel.addWidget (joint, "drawFrameD");
      panel.addWidget (joint, "axisLength");
      panel.addWidget (joint, "linearCompliance");
      panel.addWidget (joint, "rotaryCompliance");
      panel.addWidget (joint, "compliance");
      panel.addWidget (joint, "damping");
      addControlPanel (panel);
   }

   @Override
   public void attach (DriverInterface driver) {
      // TODO Auto-generated method stub
      super.attach (driver);
      driver.getViewer ().setAxialView (AxisAlignedRotation.NX_Z);
   }
   
   
}
