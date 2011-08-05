package jp.dbcls.bp3d.make;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.*;

import jp.dbcls.bp3d.*;
import jp.dbcls.bp3d.calcproperty.*;
import jp.dbcls.bp3d.obj.*;
import jp.dbcls.bp3d.util.Bp3dUtility;
import jp.dbcls.bp3d.util.StopWatch;

/**
 * Step2. データ変換スクリプト
 * 
 * 1. OBJファイルのリネーム(preferred name -> FMAID)と頂点法線の計算
 * 2. OBJファイルの体積を計算
 * 3. Organ Systemを計算
 * 4. レンダリングサーバ(windows)、Webインタフェース(linux）向けのパーツリストファイルを3つ出力する 
 * 
 * @author mituhasi
 */

public class MakeBp3d1 {
	/** 
	   * 　 時間のかかるOBJファイルのコピー、法線の計算をスキップする   (デバッグ用) 
	 *  運用時はfalse
	 * **/
	private boolean CALC_NORMAL = Boolean.parseBoolean(Bp3dProperties.getString("bp3d.calcnormal"));
	private boolean CALC_VOLUME = Boolean.parseBoolean(Bp3dProperties.getString("bp3d.calcvolume"));
	
	private final String DATADIR = Bp3dProperties.getString("bp3d.datadir")
	+ "/" + Bp3dProperties.getString("bp3d.dataversion");
	private final String EXPORTDIR = this.DATADIR + "/export/";
	private final String OBJDIR = EXPORTDIR + "obj_"
			+ Bp3dProperties.getString("bp3d.dataversion");
	private final String LINUXDIR = EXPORTDIR + "/"
			+ Bp3dProperties.getString("bp3d.dataversion");
	
	CalcVolume cv;
	Bp3d bp3d;

	File linuxDir; // linux向けにexportするファイルが出力されるディレクトリ
	File objDir; // objファイルを出力するディレクトリ(for windows)
	
	public MakeBp3d1() throws Exception {
		this.bp3d = new Bp3d();
	}

	/**
	 * 結果を出力するディレクトリを作成する
	 */
	private void makeOutputDir(){						
		/** export ディレクトリ作成 **/
		File expDir = new File(EXPORTDIR);
		if (!expDir.exists()) {
			expDir.mkdir();
		}

		/** export/obj ディレクトリ作成 **/
		this.objDir = new File(OBJDIR);
		Bp3dUtility.clean(objDir);
		if (!objDir.mkdir()) {
			System.err.print("MakeBp3d1: mkdir failed: " + objDir.getAbsolutePath());
			System.exit(1);
		}

		/** export/version number ディレクトリ for linux 作成 **/
		this.linuxDir = new File(LINUXDIR);
		Bp3dUtility.clean(linuxDir);

		if (!linuxDir.mkdir()) {
			System.err
					.print("MakeBp3d1: mkdir failed: " + linuxDir.getAbsolutePath());
			System.exit(1);
		}
	}
		
	/**
	 * objファイルの名前付け替え(英語からFMAID)と頂点法線の付与
	 * 
	 * @throws Exception
	 */
	private void addIdAndNormal() throws Exception {
		int count = 0;
		int totalEntries = bp3d.getAllEntries().size();
		AddNormal an = new AddNormal();
		
		for (Bp3dEntry bp3dEnt : bp3d.getAllEntries()){					
			/** COMPOSITE　TYPEの場合は必要なし **/
			if (!bp3dEnt.isComposite()){
				String id = bp3dEnt.getId();
						
				/** objファイルの名前付け替え(英語からFMAID) **/
				String objPathFFMP = bp3dEnt.getObjPath();

				/** OBJファイルがなければエラー **/
				if(!new File(objPathFFMP).exists()){
					System.err.println("MakeBp3d1: No OBJ found for " + bp3dEnt.getEn() + " at " + objPathFFMP);
				}
						
				String objPathExport = OBJDIR + "/" + id + ".obj";

				/** VTKを使う場合は、ファイルパスは全てasciiコードでなければならないので、
			   * FFMP/objの下からexport/objにコピーする **/
				if(CALC_NORMAL == true || CALC_VOLUME == true){
					Bp3dUtility.copy(objPathFFMP, objPathExport);
				}
				
				/** OBJファイルのパスをコピー後のものに置換する **/
				bp3dEnt.setObjPath(objPathExport);			
			
				/** normal情報を付与する (VTK利用)**/
				if(CALC_NORMAL == true){
					an.run(objPathExport, objPathExport);
				}
			}
			
			count++;
			
			if(count % 100 == 0){
				System.out.print("addIdAndNormal():" + count + "/" + totalEntries + " finished.\r" );
			}
		}
	}

	/**
	 * パーツリストファイルを出力する(bp3d.tree for Windows, bp3d.txt for Linux)
	 * 
	 * @param bp3dFile
	 *          A file to write
	 * @param code
	 *          UTF-8/MS932
	 * @throws Exception
	 */
	private void writeBp3dFile(String bp3dFile, String code) throws Exception {
		FileOutputStream fos = new FileOutputStream(bp3dFile, false);
		OutputStreamWriter osw = new OutputStreamWriter(fos, code);
		BufferedWriter bw = new BufferedWriter(osw);

		String retCode = "";
		if (code.equalsIgnoreCase("MS932")) {
			retCode = "\r\n";
		} else {
			retCode = "\n";
		}

		DecimalFormat df = new DecimalFormat("0.####");

		bw.write("#id" + "\t" + "en" + "\t" + "kanji" + "\t" + "kana" + "\t"
				+ "phase_number" + "\t" + "last_updated" + "\t" + "zmin" + "\t"
				+ "zmax" + "\t" + "volume" + "\t" + "organ_system" + "\t" + "parents"
				+ "\t" + "order_number" + "\t" + retCode);

		int orderNumber = 1;
		Map<String, Integer> id2Order = new HashMap<String, Integer>();
		for (Bp3dEntry ent : bp3d.getAllEntries()) {			
			id2Order.put(ent.getId(), orderNumber);
			orderNumber++;
		}
		
		for (Bp3dEntry ent : bp3d.getAllEntries()) {
			List<String> pEns = new ArrayList<String>();
			for (String id : bp3d.getParents(ent.getId())) {
				pEns.add(bp3d.getEntry(id).getEn());
				pEns.add(id2Order.get(id).toString());
			} 

			String kanji = ent.getKanji();
			String kana = ent.getKana();
			bw.write(ent.getId() + "\t" + ent.getEn() + "\t"
					+ (kanji == null || kanji.isEmpty() ? "　" : kanji) + "\t"
					+ (kana == null || kana.isEmpty() ? "　" : kana) + "\t" + 2 + "\t"
					+ ent.getLastUpdateString() + "\t" + 0 + "\t" + 0 + "\t"
					+ df.format(ent.getVolume()) + "\t" + ent.getOrganSystem() + "\t"
					+ Bp3dUtility.join(pEns, "\t") + "\t" + retCode);
		}

		bw.close();
		osw.close();
		fos.close();
	}
	
	/**
	 * パーツリストファイルを出力する(partsList3.txt for Linux)
	 * 
	 * @param partsList3
	 * @throws Exception
	 */
	private void writePartsList3(String partsList3) throws Exception {
		FileOutputStream fos = new FileOutputStream(partsList3, false);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);

		bw.write("\"ID\"\tEnglishParent\tEnglish\tJapaneseParent\tJapanese\tJapaneseKana\tDescriptionHTML\n");
		
		for(Bp3dEntry bp3dEnt : bp3d.getAllEntries()){
			String id = bp3dEnt.getId();
			String en = bp3dEnt.getEn();
			String kanji = (bp3dEnt.getKanji().isEmpty() ? "　" : bp3dEnt.getKanji());
			String kana = (bp3dEnt.getKana().isEmpty() ? "　" : bp3dEnt.getKana());

			if(en.equals("human body")){
				bw.write(id + "\t" + "\t" + en + "\t" + "\t" + kanji + "\t" + kana + "\t" + "" + "\n");
			}
			
			for(String pId : bp3d.getParentsNames(id)){	
				Bp3dEntry parent = bp3d.getEntry(pId);
				String pEn = parent.getEn();
				String pKanji = (parent.getKanji().isEmpty() ? "" : parent.getKanji());
				bw.write(id + "\t" + pEn + "\t" + en + "\t" +
						pKanji + "\t" + kanji + "\t" + kana + "\t" + "" + "\n");
			}
		}

		bw.close();
		osw.close();
		fos.close();
	}
	
	/**
	 * 一連の作業を呼び出す
	 * @throws Exception
	 */
	private void run() throws Exception {						
		StopWatch sw = new StopWatch();
		sw.start();

		/** 結果を出力するディレクトリを作成する  **/
		if(CALC_NORMAL == true || CALC_VOLUME == true){
			makeOutputDir();
		}
						
		/** objファイルの名前付け替え(英語からFMAID)と頂点法線の付与 **/
		addIdAndNormal();			
		
		System.out.println("addInAndNormal() finished at " + sw.getElapsedTimeSecs() + "sec");
		
		/** 各OBJの体積を求める **/
		if(CALC_VOLUME == true){
			CalcVolume cv = new CalcVolume(bp3d);
			cv.calcBp3dVolumes();
		}
			
		System.out.println("calcVolume() finished at " + sw.getElapsedTimeSecs() + "sec");
				
		/** 各パーツのOrganSystemを求める**/
		CalcOrganSystem co = new CalcOrganSystem(bp3d);
		co.run();
				
		System.out.println("calcLastUpdate() finished at " + sw.getElapsedTimeSecs() + "sec");

		
		/** 
		 * 
		 * レンダリングサーバ(windows)、Webインタフェース(linux）向けのパーツリストファイルを3つ出力する 
		 * 
		 * **/
		/** bp3d.tree for windows **/	
		String bp3dFileForWin = this.OBJDIR + "/bp3d.tree";
		writeBp3dFile(bp3dFileForWin, "MS932");
	
		/** bp3d.txt for linux **/		
		String bp3dFileForUnix = this.LINUXDIR + "/bp3d.txt";
		writeBp3dFile(bp3dFileForUnix, "UTF-8");

		/** PartsList3.txt for linux **/		
		writePartsList3(this.LINUXDIR + "/PartsList3.txt");

		System.out.println("writeBp3dFile() finished at " + sw.getElapsedTimeSecs() + "sec");
				
		sw.stop();	
	}

	public static void main(String[] args) throws Exception {
		MakeBp3d1 mkbp3d1 = new MakeBp3d1();
		mkbp3d1.run();
	}
}