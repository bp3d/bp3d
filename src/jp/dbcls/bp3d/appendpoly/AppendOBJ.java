package jp.dbcls.bp3d.appendpoly;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.*;

import jp.dbcls.bp3d.*;
import jp.dbcls.bp3d.util.StopWatch;

public class AppendOBJ {
	int vnumTotal; // ここまでappendしたvertexの合計数
	int fnumTotal; // ここまでappendしたfaceの合計数
	String outFile = "";
	List<String> vertices;
	List<String> faces;
	List<String> vns;

	public AppendOBJ() throws Exception {
		this.vertices = new ArrayList<String>();
		this.faces = new ArrayList<String>();
		this.vns = new ArrayList<String>();
		vnumTotal = 0;
	}

	public void setOutFile(String outFile) throws Exception {
		this.outFile = outFile;
	}

	public void append(String inFile) throws Exception {
		FileInputStream is = new FileInputStream(inFile);
		InputStreamReader in = new InputStreamReader(is, "MS932");
		BufferedReader br = new BufferedReader(in);
		String line;

		int vnum = 0;

		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("vn")) {
				vns.add(line);
			} else if (line.startsWith("v")) {
				vertices.add(line);
				vnum++;
			} else if (line.startsWith("f")) {
				if (line.contains("/")) { // f 32//32 1//1 4//4
					line = line.replaceFirst("^f[ ]+", "");
					String face = "f ";
					String[] vs = line.split("[ ]+");
					for (int i = 0; i < 3; i++) {
						String[] v3 = vs[i].split("[/]");
						int v = Integer.parseInt(v3[0]) + vnumTotal;
						face += +v + "//" + v + " ";
					}
					faces.add(face);
				} else { // f 24137 24138 24254
					String[] vs = line.split("[ ]+");
					String face = "f ";
					face += (Integer.parseInt(vs[1]) + vnumTotal) + " ";
					face += (Integer.parseInt(vs[2]) + vnumTotal) + " ";
					face += (Integer.parseInt(vs[3]) + vnumTotal);
					faces.add(face);
				}
				fnumTotal++;
			}
		}
		vnumTotal += vnum;

		br.close();
		in.close();
		is.close();
	}

	public void end() throws Exception {
		FileOutputStream fos;
		OutputStreamWriter out;
		BufferedWriter bw;

		fos = new FileOutputStream(this.outFile, false);
		out = new OutputStreamWriter(fos, "MS932");
		bw = new BufferedWriter(out);

		bw.write("# OBJ file output from BodyParts3D\n");
//		bw.write("# " + vnumTotal + " vertices\n");
//		bw.write("# " + fnumTotal + " triangles\n");
//		bw.write("# 1 pieces\n");
		bw.write("\n");
		for (String v : vertices) {
			bw.write(v + "\n");
		}
		for (String vn : vns) {
			bw.write(vn + "\n");
		}
		for (String f : faces) {
			bw.write(f + "\n");
		}
		bw.write("# end of OBJ file output from BodyParts3D\n");

		bw.close();
		out.close();
		fos.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		StopWatch sw = new StopWatch();
		sw.start();

		String inDir = "D:/ag/data/1.1.0909302000/FFMP/test/AppendOBJTest";
		String inFile;
		String outFile = "D:/ag/data/1.1.0909302000/FFMP/test/AppendOBJTest/090925-white matter of neuraxis by pure java.obj";

		AppendOBJ appender = new AppendOBJ();
		appender.setOutFile(outFile);
		inFile = inDir + "/090925-brain-l. white matter of neuraxis.obj";
		appender.append(inFile);
		inFile = inDir + "/090925-brain-r. white matter of neuraxis.obj";
		appender.append(inFile);
		appender.end();

		sw.stop();

		System.out.println("AppendOBJ completed: elapsed time="
				+ sw.getElapsedTimeSecs() + "sec");
	}
}
