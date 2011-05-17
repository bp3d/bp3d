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
package jp.dbcls.bp3d.util;

import java.io.*;
import java.util.*;

import jp.dbcls.bp3d.Bp3dProperties;

/**
 * VTKフォーマットのファイルをOBJフォーマットに変換します
 * 
 * @author mituhasi
 *
 */
public class VTK2OBJ {
	private final static String CREDIT = Bp3dProperties.getString("bp3d.credit");		

	private List<String> vertices;
	private List<String> normals;
	private List<String> faces;

	public VTK2OBJ(String inFile, String outFile) throws Exception {
		this.vertices = new ArrayList<String>();
		this.normals = new ArrayList<String>();
		this.faces = new ArrayList<String>();

		load(inFile);
		export(outFile);
	}

	private void load(String inFile) throws Exception {
		FileInputStream is = new FileInputStream(inFile);
		InputStreamReader in = new InputStreamReader(is, "MS932");
		BufferedReader br = new BufferedReader(in);
		String line;
		String type = "NONE";

		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("POINTS")) { // POINTS #points float
				type = "POINTS";
			} else if (line.startsWith("POLYGONS")) { // POLYGONS #polygons
				type = "POLYGONS";
			} else if (line.startsWith("NORMALS")) {
				type = "NORMALS";
			} else if (line.length() == 0) {
				type = "NONE";
			}else{
				if (type.equals("POINTS")) { // -38.3781 -130.677 1539.83 -39.7727
																		// -128.788 1540.37 -38.0781 -150.178 1544.
					String[] vCoord = line.split("[ ]+");
					for (int i = 0; i < vCoord.length; i += 3) {
						vertices.add("v " + vCoord[i] + " " + vCoord[i + 1] + " "
								+ vCoord[i + 2] + "\n");
					}
				} else if (type.equals("POLYGONS")) { // 3 2 0 3
					String[] idx = line.split("[ ]+");
					int f1 = Integer.parseInt(idx[1]) + 1;
					int f2 = Integer.parseInt(idx[2]) + 1;
					int f3 = Integer.parseInt(idx[3]) + 1;
					faces.add("f " + f1 + "//" + f1 + " " + f2 + "//" + f2
					               + " " + f3 + "//" + f3 + "\n");
				} else if (type.equals("NORMALS")) { // 0.992557 -0.119413 -0.023914
																								// 0.0137949 0.997613 0.0676653
																								// 0.945732 0.305093 0.111848
					String[] vNormal = line.split("[ ]+");
					for (int i = 0; i < vNormal.length; i += 3) {
						normals.add("vn " + vNormal[i] + " " + vNormal[i + 1] + " "
							+ vNormal[i + 2] + "\n");
					}
				}
			}
		}
		
		br.close();
		in.close();
		is.close();
	}

	public void export(String outFile) throws Exception {			
		FileOutputStream fos = new FileOutputStream(outFile, false);
		OutputStreamWriter out = new OutputStreamWriter(fos, "MS932");
		BufferedWriter bw = new BufferedWriter(out);
	
		bw.write("# Wavafront OBJ file of BodyParts3D\n");
		bw.write("# " + CREDIT + "\n");				
				
		for(int i = 0; i < vertices.size(); i++){
			if(normals.size() > 0){
				bw.write(normals.get(i));
			}
			bw.write(vertices.get(i));
		}
		for(String f : faces){
			bw.write(f);
		}
		
		bw.close();
		out.close();
		fos.close();
	
	}
	
	public static void main(String[] args) throws Exception {
		String inVTK = "D:/bp3d/2.1/FFMP/calcNormalByMeshLab/out/101224-brain-orbital gyri straight gyrus.vtk";
		String outOBJ = "D:/bp3d/2.1/FFMP/calcNormalByMeshLab/out/101224-brain-orbital gyri straight gyrus.obj";

		VTK2OBJ v2o = new VTK2OBJ(inVTK, outOBJ);
	}
}
