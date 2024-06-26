package artisynth.demos.fem;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import artisynth.core.femmodels.FemElement3d;
import artisynth.core.femmodels.FemMeshComp;
import artisynth.core.femmodels.FemMuscleModel;
import artisynth.core.femmodels.MuscleBundle;
import artisynth.core.femmodels.EmbeddedFem;
import artisynth.core.materials.FemMaterial;
import artisynth.core.materials.LinearMaterial;
import artisynth.core.materials.MuscleMaterial;
import artisynth.core.materials.SimpleForceMuscle;
import artisynth.core.mechmodels.Collidable.Collidability;
import artisynth.core.mechmodels.CollisionBehavior;
import artisynth.core.mechmodels.MechModel;
import artisynth.core.mechmodels.RigidBody;
import artisynth.core.modelbase.ComponentList;
import artisynth.core.probes.NumericInputProbe;
import artisynth.core.renderables.LightComponent;
import artisynth.core.util.ArtisynthPath;
import artisynth.core.workspace.DriverInterface;
import artisynth.core.workspace.RootModel;
import artisynth.demos.tutorial.FemMuscleHeart;
import maspack.geometry.MeshFactory;
import maspack.geometry.PolygonalMesh;
import maspack.geometry.io.WavefrontReader;
import maspack.matrix.AxisAngle;
import maspack.matrix.Plane;
import maspack.matrix.Point3d;
import maspack.matrix.RigidTransform3d;
import maspack.matrix.Vector3d;
import maspack.properties.Property;
import maspack.render.Light;
import maspack.render.RenderProps;
import maspack.render.Renderer.ColorMixing;
import maspack.render.Renderer.Shading;
import maspack.render.GL.GLViewer;
import maspack.util.RandomGenerator;

/**
 * Simple demo showing a heart contracting.  Demonstrates the use of generating
 * an encapsulating FEM, and colliding with an embedded surface
 * 
 * @author Antonio
 */
public class EmbeddedHeart extends RootModel {

   public static boolean omitFromMenu = true;

   // Constructor
   public EmbeddedHeart() {}
   
   // Model builder
   @Override
   public void build(String[] args) throws IOException {
      super.build(args);
      
      RandomGenerator.setSeed (0x1234);
      setMaxStepSize(0.005);
      
      // Root mechanical model
      MechModel mech = new MechModel("mech");
      mech.setGravity(0, 0, -9.8);
      addModel(mech);
      

      //-------------------------------------------------------------
      // HEART LOAD / ADD GEOMETRY
      //-------------------------------------------------------------
      
      // Heart surface mesh, with texture
      String heartFile = ArtisynthPath.getSrcRelativePath(FemMuscleHeart.class, "data/HumanHeart.obj");
      WavefrontReader wfr = new WavefrontReader(new File(heartFile));
      wfr.parse ();
      PolygonalMesh heartMesh = new PolygonalMesh();
      wfr.setMesh (heartMesh, "(null)");  // read null group
      heartMesh.triangulate();            // triangulate for interaction
      
      // FEM heart:
      FemMuscleModel heart = new FemMuscleModel("heart");
      EmbeddedFem.createVoxelizedFem(heart, heartMesh, RigidTransform3d.IDENTITY, 8, -1, 0);
      FemMeshComp embeddedHeart = heart.addMesh(heartMesh); // add real-looking mesh
      embeddedHeart.setName("embedded");
      
      // convex hull
      PolygonalMesh heartHull = MeshFactory.createConvexHull (heartMesh);
      FemMeshComp collisionHeart = heart.addMesh (heartHull);
      collisionHeart.setCollidable (Collidability.ALL);  // allow collision with external surfaces
      
      // Allow inverted elements (poor quality mesh)
      heart.setWarnOnInvertedElements(false);
      heart.setAbortOnInvertedElements(false);  
      
      // Convert unites to metres (original was cm)
      heart.scaleDistance(0.01);
      heart.setStiffnessDamping(0.02);
      
      // Set material properties
      heart.setDensity(1000);
      FemMaterial femMat = new LinearMaterial(10000, 0.33, true);
      MuscleMaterial muscleMat = new SimpleForceMuscle(2000.0);    // simple muscle
      heart.setMaterial(femMat);
      
      // Add heart to model
      mech.addModel(heart);
      
      //-------------------------------------------------------------
      // MUSCLE BUNDLES
      //-------------------------------------------------------------
      // One "long" direction muscle bundle
      // One "radial" muscle bundle
      
      // LONG BUNDLE
      
      // Compute the "long" direction of the heart
      PolygonalMesh hull = heart.getSurfaceMesh();
      RigidTransform3d trans = hull.computePrincipalAxes();
      Vector3d longAxis = new Vector3d();
      trans.R.getColumn(0, longAxis);        // first column of rotation
      
      // Create the long axis muscle bundle
      MuscleBundle longBundle = new MuscleBundle("long");
      for (FemElement3d elem : heart.getElements()) {
         longBundle.addElement(elem, longAxis);
      }
      longBundle.setMuscleMaterial(muscleMat);
      heart.addMuscleBundle(longBundle);
      
      // RADIAL BUNDLE
      
      // Compute a plane through centre of heart
      Plane plane = new Plane(longAxis, new Point3d(trans.p));
      Point3d centroid = new Point3d();
      Vector3d radialDir = new Vector3d();
      
      // Create the radial muscle bundle
      MuscleBundle radialBundle = new MuscleBundle("radial");
      for (FemElement3d elem : heart.getElements()) {
         elem.computeCentroid(centroid);
         
         // project to plane and compute radial direction
         plane.project(centroid, centroid);
         radialDir.sub(centroid, trans.p);
         radialDir.normalize();
         
         radialBundle.addElement(elem, radialDir);
      }
      radialBundle.setMuscleMaterial(muscleMat);
      heart.addMuscleBundle(radialBundle);
           
      //-------------------------------------------------------------
      // HEART RENDER PROPERTIES
      //-------------------------------------------------------------
      
      // Hide elements and nodes
      RenderProps.setVisible(heart.getElements(), true);
      RenderProps.setVisible(heart.getNodes(), false);
      
      RenderProps.setLineColor(radialBundle, Color.BLUE);
      RenderProps.setLineColor(longBundle, Color.RED);
      radialBundle.setDirectionRenderLen(0.1);
      longBundle.setDirectionRenderLen(0.1);
      RenderProps.setVisible(radialBundle, false);
      RenderProps.setVisible(longBundle, false);
      RenderProps.setVisible(heart.getSurfaceMeshComp(), false);
      RenderProps.setVisible (collisionHeart, false);
      
      // adjust heart mesh render properties
      RenderProps rprops = embeddedHeart.getRenderProps();
      rprops.getBumpMap().setScaling(0.01f);
      rprops.getColorMap().setSpecularColoring(false);  // don't modify specular
      rprops.setShading (Shading.SMOOTH);
      rprops.setFaceColor (new Color(0.8f,0.8f,0.8f));
      rprops.getColorMap ().setColorMixing (ColorMixing.MODULATE);
      rprops.setSpecular (new Color(0.4f, 0.4f, 0.4f));
      rprops.setShininess (128);

      //-------------------------------------------------------------
      // INPUT PROBES
      //-------------------------------------------------------------
      
      // Add heart probe
      addHeartProbe(longBundle, radialBundle);
      
      //-------------------------------------------------------------
      // RIGID TABLE AND COLLISION
      //-------------------------------------------------------------
      
      // Create a rigid box for the heart to fall on
      RigidBody box = RigidBody.createBox("box", 0.25, 0.25, 0.02, 0, /*addnormals*/ true);
      box.setPose(new RigidTransform3d(new Vector3d(0,0,-0.2), AxisAngle.IDENTITY));
      box.setDynamic(false);
      mech.addRigidBody(box);
      
      // adjust table render properties
      RenderProps.setShading(box, Shading.METAL);
      RenderProps.setSpecular(box, new Color(0.8f,0.8f,0.8f));
      
      // Enable collisions between the heart and table 
      CollisionBehavior cb = mech.setCollisionBehavior(collisionHeart, box, true);
      cb.setCompliance (1e-5);
      
   }
   
   protected void addHeartProbe(MuscleBundle longBundle, 
      MuscleBundle radialBundle) {
      
      Property[] props = new Property[2];
      props[0] = longBundle.getProperty("excitation");
      props[1] = radialBundle.getProperty("excitation");
      
      NumericInputProbe probe = new NumericInputProbe();
      probe.setInputProperties(props);
      
      double startTime = 1.0;
      double stopTime = 60.0;
      double cycleTime = 0.8;   // seconds per heart beat
      probe.setStartStopTimes(startTime, stopTime);
      
      // beat cycle 
      double [] beat0 = {0, 0};
      double [] beat1 = {0.9, 0};
      double [] beat2 = {0, 0.3};
      for (double t=0; t<stopTime-startTime; t+= cycleTime) {
         // NOTE: times are relative to "startTime"
         probe.addData(t, beat0);
         probe.addData(t+cycleTime*0.15, beat1);
         probe.addData(t+cycleTime*0.3, beat2);
         probe.addData(t+cycleTime*0.4, beat0);
      }
      
      addInputProbe(probe);
      
   }
   
   @Override
   public void attach(DriverInterface driver) {
      super.attach(driver);
      
      ComponentList<LightComponent> lights = new ComponentList<>(LightComponent.class, "lights");
      add(lights);
      
      GLViewer viewer = getMainViewer();
      if (viewer != null) {
         for (int i=0; i<viewer.numLights(); ++i) {
            Light light = viewer.getLight(i);
            LightComponent lc = new LightComponent(light);
            lights.add(lc);
         }
         viewer.setBackgroundColor(Color.WHITE);
      }
      
   }
   
}
