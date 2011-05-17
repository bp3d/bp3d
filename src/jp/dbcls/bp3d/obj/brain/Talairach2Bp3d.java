/**
The MIT License

Copyright (c) 2011, Database Center for Life Science (DBCLS)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
**/
package jp.dbcls.bp3d.obj.brain;

import java.io.File;

import jp.dbcls.bp3d.Bp3dProperties;
import jp.dbcls.bp3d.util.StopWatch;
import jp.dbcls.bp3d.util.VTK2OBJ;

import vtk.*;

/**
 * Talairach座標のOBJファイルをBp3d座標に変換します。
 * 
 * OUTDIR1, OUTDIR2は手動で作成してください。
 * @author mituhasi
 *
 */
public class Talairach2Bp3d {
	private final static String DATADIR = Bp3dProperties.getString("bp3d.datadir") 
	+ "/" + Bp3dProperties.getString("bp3d.dataversion");
	private final static String TMPFILE = Bp3dProperties.getString("bp3d.tmpvtkfile");	

	private final static String INDIR1 = DATADIR + "/FFMP/obj/101224-brain/0.01";
	private final static String OUTDIR1 = DATADIR + "/FFMP/talairach2bp3d/101224-brain/0.01";	
	private final static String INDIR2 = DATADIR + "/FFMP/obj/101224-brain/0.02";
	private final static String OUTDIR2 = DATADIR + "/FFMP/talairach2bp3d/101224-brain/0.02";	

	static {
		System.loadLibrary("vtkCommonJava");
		System.loadLibrary("vtkFilteringJava");
		System.loadLibrary("vtkIOJava");
		System.loadLibrary("vtkImagingJava");
		System.loadLibrary("vtkGraphicsJava");
		System.loadLibrary("vtkRenderingJava");
		System.loadLibrary("vtkInfovisJava");
		System.loadLibrary("vtkViewsJava");
	}
	
	public Talairach2Bp3d() throws Exception {}
	
	public void rotate(String inFile, String outFile) throws Exception {
		double pcX = -1.9411; // PC(posterior comissure)のX座標
		double pcY = -83.7473; // PC(posterior comissure)のY座標
		double pcZ = 1551.4592; // PC(posterior comissure)のZ座標
		double rotateAngle = 16.0; // 回転角度(X軸)
				 		
		vtkOBJReader reader = new vtkOBJReader();
		reader.SetFileName(inFile);
		
		vtkTransform trans = new vtkTransform();
		trans.Translate(pcX, pcY, pcZ);
		trans.RotateX(-rotateAngle);
		trans.Translate(-pcX, -pcY, -pcZ);
		
		
		vtkTransformPolyDataFilter tf = new vtkTransformPolyDataFilter();
		tf.SetInputConnection(reader.GetOutputPort());
		tf.SetTransform(trans);

		vtkPolyDataWriter writer = new vtkPolyDataWriter();
		writer.SetInputConnection(tf.GetOutputPort());
		writer.SetFileName(TMPFILE);		
		writer.Write();		
	
		VTK2OBJ v2o = new VTK2OBJ(TMPFILE, outFile);	
	}
		
	public void run() throws Exception {			
		File inDir = new File(INDIR1);		
		for(String inObj : inDir.list()){	
			String inObjPath = INDIR1 + "/" + inObj;
			String outObjPath = OUTDIR1 + "/" + inObj;
			System.out.println(inObjPath + "->" + outObjPath);

			rotate(inObjPath, outObjPath);			
		}				

		inDir = new File(INDIR2);		
		for(String inObj : inDir.list()){	
			String inObjPath = INDIR2 + "/" + inObj;
			String outObjPath = OUTDIR2 + "/" + inObj;
			System.out.println(inObjPath + "->" + outObjPath);

			rotate(inObjPath, outObjPath);			
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		StopWatch sw = new StopWatch();
		sw.start();

		Talairach2Bp3d t2b = new Talairach2Bp3d();
		t2b.run();			

		sw.stop();

		System.out.println("Talairach2Bp3d completed: elapsed time="
				+ sw.getElapsedTimeSecs() + "sec");		
	}
}