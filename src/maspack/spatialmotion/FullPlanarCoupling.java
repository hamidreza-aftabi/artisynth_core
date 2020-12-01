/**
 * Copyright (c) 2014, by the Authors: John E Lloyd (UBC)
 *
 * This software is freely available under a 2-clause BSD license. Please see
 * the LICENSE file in the ArtiSynth distribution directory for details.
 */
package maspack.spatialmotion;

import maspack.matrix.RigidTransform3d;
import maspack.matrix.Vector3d;

/** 
 * Constraints a rigid body to 2D motion (with rotation) in a plane. This is
 * really just revolute coupling, relaxed to allow planar translation.
 */
public class FullPlanarCoupling extends RigidBodyCoupling {

   private static final double INF = Double.POSITIVE_INFINITY; 

   private double myMinX = -INF;
   private double myMaxX = INF;

   private double myMinY = -INF;
   private double myMaxY = INF;

   private double myMinTheta = -Math.PI;
   private double myMaxTheta = Math.PI;

   /**
    * Sets the maximum value of x for this planar coupling. The default
    * value is +infinity.
    * 
    * @param max
    * maximum value for x
    */
   public void setMaximumX (double max) {
      myMaxX = max;
   }

   /**
    * Returns the maximum value of x for this planar coupling.
    * 
    * @return maximum value for x
    */
   public double getMaximumX() {
      return myMaxX;
   }

   /**
    * Sets the minimum value of x for this planar coupling. The default
    * value is -infinity.
    * 
    * @param min
    * minimum value for x
    */
   public void setMinimumX (double min) {
      myMinX = min;
   }

   /**
    * Returns the minimum value of x for this planar coupling.
    * 
    * @return minimum value for x
    */
   public double getMinimumX() {
      return myMinX;
   }

   /**
    * Sets the maximum value of y for this planar coupling. The default
    * value is +infinity.
    * 
    * @param max
    * maximum value for y
    */
   public void setMaximumY (double max) {
      myMaxY = max;
   }

   /**
    * Returns the maximum value of y for this planar coupling.
    * 
    * @return maximum value for y
    */
   public double getMaximumY() {
      return myMaxY;
   }

   /**
    * Sets the minimum value of y for this planar coupling. The default
    * value is -infinity.
    * 
    * @param min
    * minimum value for y
    */
   public void setMinimumY (double min) {
      myMinY = min;
   }

   /**
    * Returns the minimum value of y for this planar coupling.
    * 
    * @return minimum value for y
    */
   public double getMinimumY() {
      return myMinY;
   }

   /**
    * Sets the maximum value of theta for this planar coupling. The default
    * value is PI.
    * 
    * @param max
    * maximum value for theta
    */
   public void setMaximumTheta (double max) {
      myMaxTheta = max;
   }

   /**
    * Returns the maximum value of theta for this planar coupling.
    * 
    * @return maximum value for theta
    */
   public double getMaximumTheta() {
      return myMaxTheta;
   }

   /**
    * Sets the minimum value of theta for this planar coupling. The default
    * value is -PI.
    * 
    * @param min
    * minimum value for theta
    */
   public void setMinimumTheta (double min) {
      myMinTheta = min;
   }

   /**
    * Returns the minimum value of theta for this planar coupling.
    * 
    * @return minimum value for theta
    */
   public double getMinimumTheta() {
      return myMinTheta;
   }

   public FullPlanarCoupling() {
      super();
   }

   @Override
   public int maxUnilaterals() {
      return 3;
   }

   @Override
   public int numBilaterals() {
      return 3;
   }

   @Override
   public void projectToConstraint (RigidTransform3d TGD, RigidTransform3d TCD) {
      TGD.set (TCD);
      TGD.R.rotateZDirection (Vector3d.Z_UNIT);
      TGD.p.z = 0;

      // Vector3d pdc = new Vector3d();
      // pdc.inverseTransform (TGD.R, TGD.p);
      // if (pdc.x < myMinX) {
      //    pdc.x = myMinX;
      // }
      // if (pdc.x > myMaxX) {
      //    pdc.x = myMaxX;
      // }
      // if (pdc.y < myMinY) {
      //    pdc.y = myMinY;
      // }
      // if (pdc.y > myMaxY) {
      //    pdc.y = myMaxY;
      // }
      // TGD.p.transform (TGD.R, pdc);
   }

   private double doGetX (RigidTransform3d TGD) {
      checkConstraintStorage();
      Vector3d pdc = new Vector3d();
      pdc.inverseTransform (TGD.R, TGD.p);
      return -pdc.x;
   }

   public double getX (RigidTransform3d TGD) {
      if (TGD != null) {
         // on entry, TGD is set to TCD. It is then projected to TGD
         projectToConstraint (TGD, TGD);
         return doGetX (TGD);
      }
      else {
         // simply read back coordinate settings
         checkConstraintStorage();
         return myConstraintInfo[3].coordinate;
      }
   }

   public void setX (
      RigidTransform3d TGD, double x) {
      checkConstraintStorage();
      if (TGD != null) {
         Vector3d pdc = new Vector3d();
         pdc.inverseTransform (TGD.R, TGD.p);
         pdc.z = 0;
         pdc.x = -x;
         TGD.p.transform (TGD.R, pdc);
      }
      myConstraintInfo[3].coordinate = x;
   }
   
   private double doGetY (RigidTransform3d TGD) {
      checkConstraintStorage();
      Vector3d pdc = new Vector3d();
      pdc.inverseTransform (TGD.R, TGD.p);
      return -pdc.y;
   }

   public double getY (RigidTransform3d TGD) {
      if (TGD != null) {
         // on entry, TGD is set to TCD. It is then projected to TGD
         projectToConstraint (TGD, TGD);
         return doGetY (TGD);
      }
      else {
         // simply read back coordinate settings
         checkConstraintStorage();
         return myConstraintInfo[4].coordinate;
      }
   }

   public void setY (
      RigidTransform3d TGD, double y) {
      checkConstraintStorage();
      if (TGD != null) {
         Vector3d pdc = new Vector3d();
         pdc.inverseTransform (TGD.R, TGD.p);
         pdc.z = 0;
         pdc.y = -y;
         TGD.p.transform (TGD.R, pdc);
      }
      myConstraintInfo[4].coordinate = y;
   }

   private double doGetTheta (RigidTransform3d TGD) {
      checkConstraintStorage();
      double theta = Math.atan2 (TGD.R.m01, TGD.R.m00);
      theta = findNearestAngle (myConstraintInfo[5].coordinate, theta);
      myConstraintInfo[5].coordinate = theta;
      return theta;
   }

   public double getTheta (RigidTransform3d TGD) {
      if (TGD != null) {
         // on entry, TGD is set to TCD. It is then projected to TGD
         projectToConstraint (TGD, TGD);
         return doGetTheta (TGD);
      }
      else {
         // simply read back coordinate settings
         checkConstraintStorage();
         return myConstraintInfo[5].coordinate;
      }
   }

   public void setTheta (
      RigidTransform3d TGD, double theta) {
      checkConstraintStorage();
      if (TGD != null) {
         Vector3d pdc = new Vector3d();
         pdc.inverseTransform (TGD.R, TGD.p);        
         TGD.R.setIdentity();
         double c = Math.cos (theta);
         double s = Math.sin (theta);
         TGD.R.m00 = c;
         TGD.R.m01 = s;
         TGD.R.m10 = -s;
         TGD.R.m11 = c;
         TGD.p.transform (TGD.R, pdc);
      }
      myConstraintInfo[5].coordinate = theta;
   }
   
   public void initializeConstraintInfo (ConstraintInfo[] info) {
      info[0].flags = (BILATERAL|LINEAR);
      info[1].flags = (BILATERAL|ROTARY);
      info[2].flags = (BILATERAL|ROTARY);   

      info[3].flags = (LINEAR);
      info[4].flags = (LINEAR);
      info[5].flags = (ROTARY);     

      info[0].wrenchC.set (0, 0, 1, 0, 0, 0);
      info[1].wrenchC.set (0, 0, 0, 1, 0, 0);
      info[2].wrenchC.set (0, 0, 0, 0, 1, 0);
   }

   @Override
   public void getConstraintInfo (
      ConstraintInfo[] info, RigidTransform3d TGD, RigidTransform3d TCD,
      RigidTransform3d XERR, boolean setEngaged) {

      myErr.set (XERR);
      setDistancesAndZeroDerivatives (info, 3, myErr);

      if (myMinX != -INF || myMaxX != INF) {
         double x = doGetX(TGD);
         ConstraintInfo liminfo = info[3];         
         if (setEngaged) {
            maybeSetEngaged (liminfo, x, myMinX, myMaxX);
         }
         if (liminfo.engaged != 0) {
            double y = doGetY(TGD);
            //pw.transform (TGD.R, pw);        
            liminfo.distance = getDistance (x, myMinX, myMaxX);
            liminfo.dotWrenchC.setZero();
            if (liminfo.engaged == 1) {
               liminfo.wrenchC.set (1, 0, 0, 0, 0, -y);
            }
            else if (liminfo.engaged == -1) {
               liminfo.wrenchC.set (-1, 0, 0, 0, 0, y);
            }
         }
      }

      if (myMinY != -INF || myMaxY != INF) {
         double y = doGetY(TGD);
         ConstraintInfo liminfo = info[4];  
         if (setEngaged) {
            maybeSetEngaged (liminfo, y, myMinY, myMaxY);
         }
         if (liminfo.engaged != 0) {
            double x = doGetX(TGD);
            liminfo.distance = getDistance (y, myMinY, myMaxY);
            liminfo.dotWrenchC.setZero();
            if (liminfo.engaged == 1) {
               liminfo.wrenchC.set (0, 1, 0, 0, 0, x);
            }
            else if (liminfo.engaged == -1) {
               liminfo.wrenchC.set (0, -1, 0, 0, 0, -x);
            }
         }
      }
      
      if (myMinTheta > -Math.PI || myMaxTheta < Math.PI) {
         double theta = doGetTheta(TGD);
         ConstraintInfo liminfo = info[5];  
         if (setEngaged) {
            maybeSetEngaged (liminfo, theta, myMinTheta, myMaxTheta);
         }
         if (liminfo.engaged != 0) {
            liminfo.distance = getDistance (theta, myMinTheta, myMaxTheta);
            liminfo.dotWrenchC.setZero();
            if (liminfo.engaged == 1) {
               liminfo.wrenchC.set (0, 0, 0, 0, 0, 1);
            }
            else if (liminfo.engaged == -1) {
               liminfo.wrenchC.set (0, 0, 0, 0, 0, -1);
            }
         }
      }
   }
}
