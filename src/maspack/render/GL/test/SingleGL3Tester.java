package maspack.render.GL.test;

import maspack.geometry.PolygonalMesh;


public class SingleGL3Tester extends MultiViewerTesterBase {

   protected static void addRenderObjects(MultiViewer tester) {

      addCube(tester);
      addAxes(tester);
      addTransRotator(tester);
      addCylinder(tester);

      PolygonalMesh bunny = loadStanfordBunny();
      addStanfordBunnies(tester, bunny);
      addSolidBunny(tester, bunny);
      addHalfBunny(tester, bunny);

   }

   public static void main(String[] args) {

      MultiViewer rot = new MultiViewer();
      rot.addGL3Viewer("GL3 Viewer", 30, 30, 640, 480);
      // rot.syncMouseListeners();

      addRenderObjects(rot);

      // adjust all windows to a specific size
      rot.setWindowSizes(640, 480);
      rot.autoFitViewers();

   }

}
