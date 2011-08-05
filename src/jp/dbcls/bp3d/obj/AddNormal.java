package jp.dbcls.bp3d.obj;

import jp.dbcls.bp3d.Bp3dProperties;
import jp.dbcls.bp3d.util.VTK2OBJ;
import vtk.*;

/**
 * 頂点法線をつける
 * objファイルに出力するとプログラムが落ちるデータがあるため、vtkフォーマットで保存
 * 別途、VTK2OBJ.javaでOBJファイルに変換する。
 * 
 * @author mituhasi
 *
 */
public class AddNormal {
	static {
		System.loadLibrary("vtkCommonJava");
		System.loadLibrary("vtkFilteringJava");
		System.loadLibrary("vtkIOJava");
		System.loadLibrary("vtkImagingJava");
		System.loadLibrary("vtkGraphicsJava");
		System.loadLibrary("vtkRenderingJava");
	}
	
	private final String TMPFILE = Bp3dProperties.getString("bp3d.tmpvtkfile");
			
	public AddNormal() {}

	public void run(String inOBJ, String outOBJ) throws Exception {
		vtkOBJReader reader = new vtkOBJReader();
		reader.SetFileName(inOBJ);
		reader.Update();

		vtkPolyDataNormals normals = new vtkPolyDataNormals();
		normals.SetInputConnection(reader.GetOutputPort());
		normals.ComputePointNormalsOn();
		normals.Update();

		vtkPolyDataWriter writer = new vtkPolyDataWriter();
		writer.SetInputConnection(normals.GetOutputPort());
		writer.SetFileName(TMPFILE);		
		writer.Write();
		
		VTK2OBJ v2o = new VTK2OBJ(TMPFILE, outOBJ);		
	}

	public static void main(String[] args) throws Exception {
		String inOBJ = Bp3dProperties.getString("bp3d.testdatadir") + "/101224-brain_superior_parietal_lobule_precuneus_L.obj";
		String outOBJ = Bp3dProperties.getString("bp3d.testdatadir") + "/101224-brain_superior_parietal_lobule_precuneus_L_out.obj";

		AddNormal an = new AddNormal();
		an.run(inOBJ, outOBJ);
	}
}