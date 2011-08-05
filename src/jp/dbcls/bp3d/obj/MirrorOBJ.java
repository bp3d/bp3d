package jp.dbcls.bp3d.obj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.*;

import jp.dbcls.bp3d.Bp3dProperties;
import jp.dbcls.bp3d.util.StopWatch;

/**
 * MirrorOBJ
 * OBJファイルを読み込んで正中面で対象なOBJファイルを作る。
 * 
 * @author mitsuhashi
 *
 */
public class MirrorOBJ {

	public MirrorOBJ() throws Exception {}
	
	/**
	 * inFileのmirrorをoutFileとして出力する
	 * @param inFile
	 * @param outFile
	 * @throws Exception
	 */
	public void mirror(String inFile, String outFile) throws Exception {
		FileInputStream is = new FileInputStream(inFile);
		InputStreamReader in = new InputStreamReader(is, "MS932");
		BufferedReader br = new BufferedReader(in);

		FileOutputStream fos = new FileOutputStream(outFile, false);
		OutputStreamWriter out = new OutputStreamWriter(fos, "MS932");
		BufferedWriter bw = new BufferedWriter(out);
		
		String line;

		while ((line = br.readLine()) != null) {
			line = line.trim();
			if (line.startsWith("v")) {
				String[] coords = line.split("[ ]+");
				double x = Double.parseDouble(coords[1]);
				double y = Double.parseDouble(coords[2]);
				double z = Double.parseDouble(coords[3]);
				x = -x;
				bw.write("v " + x + " " + y + " " + z + "\n");
			} else{
				bw.write(line + "\n");
			}
		}

		br.close();
		in.close();
		is.close();

		bw.close();
		out.close();
		fos.close();	
	}

	/**
	 * Brachium of left superior colliculusをミラーして、brachium of right superior colliculusを作る
	 */
	private void mirrorBrachiumOfRightSupColliculus() throws Exception {
		String inFile = "C:/bp3d/data/3.0/FFMP/obj/101224-brain/0.01/101224-brain_brachium of left superior colliculus.obj";
		String outFile = "C:/bp3d/data/3.0/FFMP/obj/101224-brain/0.01/101224-brain_brachium of right superior colliculus.obj";

		this.mirror(inFile, outFile);
	}
	
	
	/**
	 * テストルーチン
	 * @throws Exception
	 */
	private void test() throws Exception {
		
		String inFile = Bp3dProperties.getString("bp3d.testdatadir") + "/101224-brain_brachium of left superior colliculus.obj";
		String outFile = Bp3dProperties.getString("bp3d.testdatadir") + "/101224-brain_brachium of right superior colliculus.obj";

		this.mirror(inFile, outFile);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		StopWatch sw = new StopWatch();
		sw.start();

		MirrorOBJ mirror = new MirrorOBJ();
//		mirror.test();
		mirror.mirrorBrachiumOfRightSupColliculus();
				
		sw.stop();

		System.out.println("MirrorOBJ completed: elapsed time="
				+ sw.getElapsedTimeSecs() + "sec");
	}
}
