package jp.dbcls.bp3d.calcproperty;

/**
 * 各パーツの体積を計算します。
 * 
 * VTK利用のため、OBJファイルパスは、ascii　codeでなければならず、
 * export/obj_#Bp3dProperties.getString("bp3d.dataversion");
 * の下にあることを前提とします。
 * 
 * @author mituhasi
 * 
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import jp.dbcls.bp3d.*;

import vtk.*;

public class CalcVolume {
	Bp3d bp3d;

	static {
		System.loadLibrary("vtkCommonJava");
		System.loadLibrary("vtkFilteringJava");
		System.loadLibrary("vtkIOJava");
		System.loadLibrary("vtkImagingJava");
		System.loadLibrary("vtkGraphicsJava");
		System.loadLibrary("vtkRenderingJava");
		try {
			System.loadLibrary("vtkHybridJava");
		} catch (Throwable e) {
			System.out.println("cannot load vtkHybrid, skipping...");
		}
	}

	/**
	 * invoked in MakeBp3d1
	 * 
	 * @throws Exception
	 */
	public CalcVolume(Bp3d bp3d) throws Exception {
		this.bp3d = bp3d;
	}
	
	/**
	 * objPathで示されたOBJファイルの体積を計算する
	 * @param objPath
	 * @return
	 */
	public double calcOBJVolume(String objPath) {		
		vtkOBJReader reader = new vtkOBJReader();
		reader.SetFileName(objPath);

		vtkTriangleFilter triangleFilter = new vtkTriangleFilter();
		triangleFilter.SetInputConnection(reader.GetOutputPort());

		vtkMassProperties mass = new vtkMassProperties();
		mass.SetInputConnection(triangleFilter.GetOutputPort());
		mass.Update();

		double cm3 = mass.GetVolume();
		cm3 = cm3 / 1000; // mm^3->cm^3に変換

		return cm3;
	}

	/**
	 * OBJファイルの足し算で定義される臓器の体積も含めて全臓器の体積計算
	 * 
	 * @throws Exception
	 */
	public void calcBp3dVolumes() throws Exception {		
		int count = 0;
		int totalEntries = bp3d.getAllEntries().size();
		
		for (Bp3dEntry ent : bp3d.getAllEntries()){			
			if(ent.getVolume() < 0){			
				double cm3 = 0.0;
				for (String id : bp3d.getPrimitiveOBJIds(ent)){
					Bp3dEntry cEnt = bp3d.getEntry(id);
					if(cEnt.getVolume() > 0){
						cm3 += cEnt.getVolume();
					}else{
						String objFile = bp3d.getEntry(id).getObjPath();
						File obj = new File(objFile);
						if(!obj.exists()){
							System.out.println("File not found at calcBp3dVolumes:" + id + "," + objFile);					
						}
						cm3 += calcOBJVolume(objFile);
					}
				}			
				ent.setVolume(cm3);
			}
			count++;
			
			if(count % 100 == 0){
				System.out.print("calcVolume():" + count + "/" + totalEntries + " finished.\r" );
			}		
		}
	}
	
	/**
	 * OBJファイルの体積をファイルに書き込む
	 * 
	 * @throws Exception
	 */
	public void writeOBJVolume(String filename) throws Exception {
		FileWriter out = new FileWriter(filename, false);
		BufferedWriter bw = new BufferedWriter(out);

		bw.write("OBJID" + "\t" + "volume" + "\n");

		for (Bp3dEntry ent : bp3d.getAllEntries()) {
			bw.write(ent.getId() + "\t" + ent.getVolume() + "\n");
		}

		bw.close();
		out.close();
	}	
}
