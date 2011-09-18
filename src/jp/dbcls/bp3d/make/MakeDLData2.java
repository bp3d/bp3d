package jp.dbcls.bp3d.make;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import jp.dbcls.bp3d.util.*;
import jp.dbcls.bp3d.*;

/**
 * データベースアーカイブ向けののobjデータを作成する 
 * 1. 複数のobjでできているパーツはそれらを足し算して作る 
 * 2. ポリゴンファイルは、OUTDIRに作成される
 * 
 * @author mituhasi
 * 
 */
public class MakeDLData2 {
	private static String DEL = "\t";
	
	private Bp3d bp3d;
	private TraverseBp3d bp3dTraverse;
	
	private String DATA_VERSION = Bp3dProperties.getString("bp3d.dataversion");
	private String DATADIR = Bp3dProperties.getString("bp3d.datadir")
			+ "/" + DATA_VERSION;
	private String OUTDIR = DATADIR + "/dbarchive/export";
	private String MASTERDIR = DATADIR + "/dbarchive/master";
		
	public MakeDLData2() throws Exception {		
		this.bp3d = new Bp3d();
		this.bp3dTraverse = bp3d.getBp3dTraverse();
	}

	/**
	 * ダウンロードパーツリストファイル(partsList.txt, partsList_e.txt)を作成する
	 * 
	 * @throws Exception
	 */
	public void writePartsList() throws Exception {
		String downloadListFile = this.OUTDIR + "/parts_list_e.txt"; // ダウンロードするパーツのリスト(英語)

		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(
				downloadListFile), "utf-8");

		osw.write("\"id\"" + DEL + "en\n");

		for (Bp3dEntry ent : bp3d.getAllEntries()) {
			osw.write(ent.getId() + DEL + ent.getEn() + "\n");
		}

		osw.close();

		String downloadListFileJa = this.OUTDIR + "/parts_list.txt"; // ダウンロードするパーツのリスト

		osw = new OutputStreamWriter(new FileOutputStream(downloadListFileJa),
				"MS932");

		osw.write("\"id\"" + DEL + "en" + DEL + "kanji" + DEL + "kana\n");

		for (Bp3dEntry ent : bp3d.getAllEntries()) {
			osw.write(ent.getId() + DEL + ent.getEn() + DEL + ent.getKanji() + DEL
					+ ent.getKana() + "\n");
		}

		osw.close();
	}

	/**
	 * conventionalPartOf.txtファイルをOUTDIRに作成する
	 * 
	 * @throws Exception
	 */
	public void makeConventionalPartOf() throws Exception {
		String partOfFile = OUTDIR + "/conventional_part_of.txt";
		OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(
				partOfFile), "utf-8");

		osw.write("\"id\"" + DEL + "name" + DEL + "part id" + DEL + "part name"
				+ "\n");

		for (Bp3dEntry ent : bp3d.getAllEntries()) {
			for (String cid : bp3d.getChildren(ent.getId())){
				osw.write(ent.getId() + DEL + ent.getEn() + DEL + cid + DEL
						+ bp3d.getEntry(cid).getEn() + "\n");
			}
		}
		osw.close();
	}

	/**
	 * 動的に生成しないファイル(READMEなど)をOUTDIRにコピー
	 * 
	 * @throws Exception
	 */
	private void copyDescriptionFiles() throws Exception {
		Bp3dUtility.copy(MASTERDIR + "/README_e.html", OUTDIR + "/README_e.html");
		Bp3dUtility.copy(MASTERDIR + "/README.html", OUTDIR + "/README.html");
		
		Bp3dUtility.copy(MASTERDIR + "/release_" + DATA_VERSION + ".html",
				OUTDIR + "/release_"  + DATA_VERSION + ".html");

		Bp3dUtility.copy(MASTERDIR + "/release_" + DATA_VERSION + "_e.html",
				OUTDIR + "/release_"  + DATA_VERSION + "_e.html");

		Bp3dUtility.copy(MASTERDIR + "/coordinate_system.png",
				OUTDIR + "/coordinate_system.png");
	}
	
	/**
	 * 複数のobjでできているパーツ(composite part)のリストを作成
	 * @throws Exception
	 */
	private void makeCompositePartsList() throws Exception {
		File cpList = new File(OUTDIR + "/composite_parts.txt");
		FileOutputStream fos = new FileOutputStream(cpList, false);
		OutputStreamWriter out = new OutputStreamWriter(fos, "utf-8");
		BufferedWriter bw = new BufferedWriter(out);
		
		bw.write("composite id" + DEL + "composite name" + DEL +
				"primitive id" + DEL + "primitive name" + "\n");
		
		for (String id : bp3d.getAllIds()) {
			String name = bp3d.getEntry(id).getEn();
			for (String cid : bp3dTraverse.getOffsprings(id)){
				if(cid.equals(id)){ continue; }
				String cname = bp3d.getEntry(cid).getEn();
				bw.write(id + DEL + name + DEL + cid + DEL + cname + "\n");
			}
		}

		bw.close();
		out.close();
		fos.close();		
	}
	
	/**
	 * 一連の処理を実行する
	 * @throws Exception
	 */
	public void run() throws Exception {				
		/** OUTDIR/composite_parts.txtを作成 **/
		makeCompositePartsList();

		/** OUTDIR/parts_list.txtを作成**/
		writePartsList();
		
		/** OUTDIR/conventional_part_of.txtを作成**/
		makeConventionalPartOf();
		
		/** README.htmlなどをMASTERDIR->OUTDIRにコピー **/
		copyDescriptionFiles();			
	}

	public static void main(String[] args) throws Exception {
		StopWatch sw = new StopWatch();
		sw.start();

		MakeDLData2 mkdl2 = new MakeDLData2();
		mkdl2.run();

		sw.stop();
		System.out.println("MakeDLData2 completed: elapsed time="
				+ sw.getElapsedTimeSecs() + "sec");
	}
}
