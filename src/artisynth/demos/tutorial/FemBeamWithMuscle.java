package artisynth.demos.tutorial;

import java.awt.Color;
import java.io.IOException;

import maspack.render.RenderProps;
import artisynth.core.femmodels.FemMarker;
import artisynth.core.femmodels.FemModel3d;
import artisynth.core.materials.SimpleAxialMuscle;
import artisynth.core.mechmodels.Muscle;
import artisynth.core.mechmodels.Particle;
import artisynth.core.mechmodels.Point;

public class FemBeamWithMuscle extends FemBeam {

   // Creates a point-to-point muscle
   protected Muscle createMuscle () {
      Muscle mus = new Muscle (/*name=*/null, /*restLength=*/0);
      mus.setMaterial (
         new SimpleAxialMuscle (/*stiffness=*/20, /*damping=*/10, /*maxf=*/10));
      RenderProps.setLineStyle (mus, RenderProps.LineStyle.ELLIPSOID);
      RenderProps.setLineColor (mus, Color.RED);
      RenderProps.setLineRadius (mus, 0.03);
      return mus;
   }

   // Creates a FEM Marker
   protected FemMarker createMarker (
      FemModel3d fem, double x, double y, double z) {
      FemMarker mkr = new FemMarker (/*name=*/null, x, y, z);
      setSphereRendering (mkr, Color.BLUE, 0.02);
      fem.addMarker (mkr);
      return mkr;
   }

   public void build (String[] args) throws IOException {

      // Create simple FEM beam
      super.build (args);

      // Add a particle fixed in space 
      Particle p1 = new Particle (/*mass=*/0, -length/2, 0, 2*width);
      mech.addParticle (p1);
      p1.setDynamic (false);
      setSphereRendering (p1, Color.BLUE, 0.02);
      
      // Add a marker at the end of the model
      FemMarker mkr = createMarker (fem, length/2-0.1, 0, width/2);
      
      // Create a muscle between the point an marker 
      Muscle muscle = createMuscle();
      muscle.setPoints (p1, mkr);
      mech.addAxialSpring (muscle);
   }
   
   // Sets points to render as spheres
   protected void setSphereRendering (Point pnt, Color color, double r) {
      RenderProps.setPointColor (pnt, color);
      RenderProps.setPointStyle (pnt, RenderProps.PointStyle.SPHERE);
      RenderProps.setPointRadius (pnt, r);
   }

}
