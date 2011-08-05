package jp.dbcls.bp3d.make;

import java.io.*;
import java.util.*;

import jp.dbcls.bp3d.util.Bp3dUtility;
import jp.dbcls.bp3d.util.StopWatch;
import jp.dbcls.bp3d.util.ZipCompression;
import jp.dbcls.bp3d.*;

/**
 * MakeDLData1.java
 * 
 * export0.01/obj_${bp3d.dataversion}/*.objファイルをdbarchive/export/BodyParts3D_${bp3d.dataversion}_obj_99.zipファイルにまとめる
 * export0.05/obj_${bp3d.dataversion}/*.objファイルをdbarchive/export/BodyParts3D_${bp3d.dataversion}_obj_95.zipファイルにまとめる
 * 
 * 事前に1%OBJと5%OBJをそれぞれexport0.01とexport0.05に作成しておく必要あり。
 * 
 * @author mituhasi
 * 
 */
public class MakeDLData1 {	
	private String DATA_VERSION = Bp3dProperties
	.getString("bp3d.dataversion");
	private String DATADIR = Bp3dProperties.getString("bp3d.datadir")
	+ "/" + DATA_VERSION;
	private String EXPORTDIR = DATADIR + "/export";	
	private String OUTDIR = DATADIR + "/dbarchive/export";
	private List<Double> reductionRatios;
	
	/**
	 * コンストラクタ
	 * @throws Exception
	 */
	public MakeDLData1() throws Exception {
		this.reductionRatios = new ArrayList<Double>();
	}
	
	/**
	 * reduction rate設定
	 * @param rr
	 */
	public void addReductionRate(double rr){
		this.reductionRatios.add(rr);
	}
	
/**
 * zipファイル作成
 * @param reductionRate
 * @throws Exception
 */
	private void makeZip(double reductionRatio) throws Exception {		
		/**
		 * redutionRatio = after reduction / before reduction
		 * reductionRate = 1 - reductionRatio
		 */		
	  StringBuilder reductionRate = new StringBuilder();
	  Formatter formatter = new Formatter(reductionRate, Locale.US);
	  formatter.format("%.0f", (1 - reductionRatio) * 100);
		
		String polyDirStr = EXPORTDIR  + reductionRatio + "/obj_" + DATA_VERSION;
		String zipDirStr = OUTDIR + "/BodyParts3D_" + DATA_VERSION + "_obj_" + reductionRate;

		File zipDir = new File(zipDirStr);
		Bp3dUtility.clean(zipDir);
		zipDir.mkdir();
		
		System.out.println("copy polydir to zipdir:" + polyDirStr + "->" + zipDirStr);
		Bp3dUtility.copyDir(polyDirStr, zipDirStr);
		
		/** bp3d.tree ファイルを削除 **/
		File bp3dTree = new File(zipDirStr + "/bp3d.tree");
		bp3dTree.delete();		

		/** zip圧縮 **/
		ZipCompression comp = new ZipCompression();
		comp.doDirectory(zipDirStr);
		Bp3dUtility.clean(zipDir);
	}

	/**
	 * 各作業を呼び出す
	 * @throws Exception
	 */
	public void run() throws Exception {
		/** 0. OUTDIR作成 **/
		File outDir = new File(OUTDIR);
		Bp3dUtility.clean(outDir);
		outDir.mkdir();
		
		for(double rr : this.reductionRatios){
			makeZip(rr);
		}
	}

	public static void main(String[] args) throws Exception {
		StopWatch sw = new StopWatch();
		sw.start();
		
		MakeDLData1 mkdl1 = new MakeDLData1();
		mkdl1.addReductionRate(0.01);
		mkdl1.addReductionRate(0.05);
		mkdl1.run();

		sw.stop();
		System.out.println("MakeDLData1 completed: elapsed time="
				+ sw.getElapsedTimeSecs() + "sec");
	}
}
