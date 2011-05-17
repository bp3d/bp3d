package jp.dbcls.bp3d.make;

This is incomplete.

import java.io.*;
import java.util.*;
import java.text.*;

import jp.dbcls.bp3d.*;
import jp.dbcls.bp3d.appendpoly.*;
import jp.dbcls.bp3d.kaorif.*;
import jp.dbcls.bp3d.util.Bp3dUtility;
import jp.dbcls.bp3d.util.StopWatch;
import jp.dbcls.bp3d.fma.*;
import jp.dbcls.bp3d.obj.*;

/**
 * Step1. データチェックスクリプト
 * 
 * OBJファイル, kaorif.xlsのチェックをする
 * チェックメソッドの呼び出しは、run()を参照のこと
 * 
 * @author mituhasi
 * 
 */
public class CheckBp3d {
	private final String DATADIR = Bp3dProperties
			.getString("bp3d.datadir")
			+ "/" + Bp3dProperties.getString("bp3d.dataversion");
	private String logDir;
	
	private OBJInfo objInfoEn;

	private SortedMap<String, OBJInfoEntry> notInList;
	private SortedMap<String, List<String>> multiplePiecesForOneEnLong;
	private SortedSet<Bp3dEntry> notExistsOBJ;
	private SortedMap<String, String> checkLeftRight;
	private SortedSet<String> noParent;
	private SortedSet<String> existsOBJButHasPart;

	private FMA fma;	
	private MakeBp3d0 bp3d;	
		
	public CheckBp3d() throws Exception {
		this.logDir = makeLogDir();
		
		ParseOBJName pon = new ParseOBJName(this.logDir);
		this.objInfoEn = pon.getOBJInfoEnLong();
				
		this.bp3d = new MakeBp3d0();
		this.fma = new FMA();
		
		FMAOBO fmaobo = new FMAOBO();
		Kaorif kaorif = new Kaorif();
				
		this.notInList = new TreeMap<String, OBJInfoEntry>();
		this.multiplePiecesForOneEnLong = new TreeMap<String, List<String>>();
		this.notExistsOBJ = new TreeSet<Bp3dEntry>();
		this.checkLeftRight = new TreeMap<String, String>();		
		this.noParent = new TreeSet<String>();		
		this.existsOBJButHasPart = new TreeSet<String>();
	}

	/**
	 *logディレクトリを作成
	 */
	public String makeLogDir() {
		String logDirStr = DATADIR + "/logs";
		File logDir = new File(logDirStr);
		if (!logDir.exists()) {
			logDir.mkdir();
		}
		logDirStr = logDirStr + "/MakeBp3d0";
		logDir = new File(logDirStr);
		Bp3dUtility.clean(logDir);
		if (!logDir.mkdir()) {
			System.err.println("MakeBp3d: Can't make log dir:" + logDirStr);
			System.exit(1);
		}

		return logDirStr;
	}
	
	/**
	 * 2. 1つのEnLongに複数のpieceが対応している場合、それをすべて出力する。 
	 * e.g.: bone 1, bone, bone 3
	 * 
	 */
	public void checkMultiplePiecesForOneEnLong() {
		for (OBJInfoEntry info : objInfoEn.values()) {
			String enLong = info.getEnLong();
			List<String> pNames = info.getPieceNames();
			List<String> logs = new ArrayList<String>();
			if (pNames.size() > 1) {
				for (int i = 0; i < pNames.size(); i++) {
					String log = enLong + "\t"
					+ pNames.get(i) + "\t" + info.getClyName(i) + "\t"
					+ info.getDirs().get(i);
					logs.add(log);
				}
				multiplePiecesForOneEnLong.put(enLong, logs);
			}
		}
	}

	/**
	 * 3. リスト(kaorif.txt)にあるのにOBJファイルが存在しないものをチェックする
	 * 
	 * @throws Exception
	 */
	public void notExistsOBJ() throws Exception {
		System.out.println("notExistsOBJ(): Kaorif.xls entries that are not found in OBJ.");

		for (Bp3dEntry ent : bp3d.getAllEntries()){
			/** 部分の足し算でできている場合はOK**/
			if (bp3d.getChildren(ent.getId()).size() > 0){
				continue;
			}
						
			/** OBJファイルが存在しない場合は、notExistsOBJに追加 **/			
			if (!objInfoEn.containsKey(ent.getEn())) {
				notExistsOBJ.add(ent);
			}
		}			
	}			
	
	/**
	 * 4. ファイル名のLeft/Rightラベルと実際のOBJファイルが対応しているかチェック
	 */
	public void checkLeftRight() throws Exception {
		IsLeftRight isLR = new IsLeftRight(fma, objInfoEn);
		
		for (OBJInfoEntry info : objInfoEn.values()) {
			String enLong = info.getEnLong();
			String objFile = info.getLatestFile();
						
			isLR.countLR(objFile);
			double ratio = isLR.getRatio();
			
			boolean isNSN = false;
			if(enLong.endsWith(", nsn")){ // ,nsnの場合はnsnをとって処理する
				enLong = enLong.replace(", nsn", "");
				isNSN = true;
			}
			
			String properName = isLR.getProperName(enLong);
			boolean isProperName = false;			
			isProperName = isLR.isProperName();

			boolean toBeCorrected = false; // if true, 名前を修正するべき			
			if(isNSN){
				enLong += ", nsn"; 
			}
			if(!enLong.equals(properName)){
				toBeCorrected = true;
			}
						
			DecimalFormat df = new DecimalFormat();
			df.applyPattern("0");

			// 小数点以下の桁数を指定
			// この引数にどちらも同じ値を入れると、
			// 固定小数値になります。
			df.setMaximumFractionDigits(2);
			df.setMinimumFractionDigits(2);

			String log = toBeCorrected + "\t" + enLong + "\t" + properName + "\t" 
				+ df.format(new Double(ratio)) + "\t" + isProperName;
			this.checkLeftRight.put(enLong, log);
		}
	}

	/**
	 * 5. 親がいないエントリをチェックする
	 */
	public void noParent() throws Exception {
		System.out.println("checkNoParents(): checking kaorif.xls entries without parent entries.");
		
		for (String id : bp3d.getAllIds()){
			if(!bp3d.hasParent(id)){
				noParent.add(id);
			}						
		}
	}

	/**
	 * チェック結果をMakeBp3dに出力
	 * 
	 * @throws Exception
	 */
	public void write() throws Exception {
		String logFile;
		FileOutputStream fos;
		OutputStreamWriter out;
		BufferedWriter bw;

		logFile = logDir + "/1.notInList.txt";

		if (this.notInList.values().size() > 0) {
			fos = new FileOutputStream(logFile, false);
			out = new OutputStreamWriter(fos, "MS932");
			bw = new BufferedWriter(out);

			bw.write("en" + "\t" + "enCorrected" + "\t" + "latest cly" + "\t"
					+ "fmaId" + "\t" + "enLong" + "\t" + "latest piece" + "\n");

			for (OBJInfoEntry objInfo : this.notInList.values()) {
				bw.write(objInfo.getEn() + "\t" + objInfo.getEnCorrected() + "\t"
						+ objInfo.getLatestClyName() + "\t" + objInfo.getFmaId() + "\t"
						+ objInfo.getEnLong() + "\t" + objInfo.getLatestPieceName() + "\n");
			}

			bw.close();
			out.close();
			fos.close();
		}

		logFile = logDir + "/2.multiplePiecesForOneOrgan.txt";

		if (this.multiplePiecesForOneEnLong.keySet().size() > 0) {
			fos = new FileOutputStream(logFile, false);
			out = new OutputStreamWriter(fos, "MS932");
			bw = new BufferedWriter(out);

			bw.write("isEdit" + "\t" + "enLong" + "\t" + "piece" + "\t" + "clyname"
					+ "\t" + "absolute path" + "\n");

			for (String enLong : this.multiplePiecesForOneEnLong.keySet()) {
				for (String log : this.multiplePiecesForOneEnLong.get(enLong)) {
					bw.write(log + "\n");
				}
			}
			bw.close();
			out.close();
			fos.close();
		}
		
		logFile = logDir + "/3.notExistsOBJ.txt";

		if (this.notExistsOBJ.size() > 0) {
			fos = new FileOutputStream(logFile, false);
			out = new OutputStreamWriter(fos, "MS932");
			bw = new BufferedWriter(out);

			bw.write("en" + "\t" + "kanji" + "\n");

			for (Bp3dEntry bp3d : this.notExistsOBJ) {
				bw.write(bp3d.getEn() + "\t" + bp3d.getKanji() + "\n");
			}

			bw.close();
			out.close();
			fos.close();
		}
		
		logFile = logDir + "/4.checkLeftRight.txt";

		if (this.checkLeftRight.values().size() > 0) {
			fos = new FileOutputStream(logFile, false);
			out = new OutputStreamWriter(fos, "MS932");
			bw = new BufferedWriter(out);

			bw.write("toBeCorrected" + "\t" + "enLong" + "\t" 
					+ "properName" + "\t" 
					+ "ratio" + "\t" + "isProperName" + "\n"); 

			for (String enLong : this.checkLeftRight.keySet()) {
				bw.write(this.checkLeftRight.get(enLong) + "\n");
			}

			bw.close();
			out.close();
			fos.close();
		}			
		
		logFile = logDir + "/5.noParent.txt";

		if (this.noParent.size() > 0) {
			fos = new FileOutputStream(logFile, false);
			out = new OutputStreamWriter(fos, "MS932");
			bw = new BufferedWriter(out);

			bw.write("id" + "\t" + "en" + "\t" + "parent candidates" + "\n");

			for (String id : this.noParent) {
				bw.write(id + "\t" + bp3d.getEntry(id).getEn() + "\n");
			}

			bw.close();
			out.close();
			fos.close();
		}

		logFile = logDir + "/6.existsOBJButHasPart.txt";

		if (this.existsOBJButHasPart.size() > 0) {
			fos = new FileOutputStream(logFile, false);
			out = new OutputStreamWriter(fos, "MS932");
			bw = new BufferedWriter(out);

			bw.write("part" + "\n");

			for (String enParent : this.existsOBJButHasPart) {
				bw.write(enParent + "\n");
			}

			bw.close();
			out.close();
			fos.close();
		}
	}

	/**
	 * チェックメソッドの呼び出し
	 * 
	 * @throws Throwable
	 */
	public void run() throws Throwable {		
		/** 1. OBJファイルに対応するエントリがfma_obo2.txtかkaorif.xlsに存在するかチェック **/
//		notInList();
		
		/** 2. 1つのEnLongに複数のpieceが対応している場合、それをすべて出力する。 **/
		checkMultiplePiecesForOneEnLong();
				
		/** 3. kaorif.txtに存在するプリミティブだがOBJが存在しないもののチェック **/
//		notExistsOBJ();
		
		/** 4. objファイルのLeft/Rightとファイル名のleft/rightが対応しているかチェック **/
		checkLeftRight();
		
		/** 5. member-of関係の親がない臓器をチェック **/
		noParent();
								
		/** 6. 結果をMakeBp3d0ディレクトリの各ファイルに出力 **/
		write();
	}

	public static void main(String[] args) throws Throwable {
		StopWatch sw = new StopWatch();
		sw.start();

		CheckBp3d mkBp3d0 = new CheckBp3d();
		mkBp3d0.run();

		sw.stop();

		System.out.println("MakeBp3d1 completed: elapsed time="
				+ sw.getElapsedTimeSecs() + "sec");
	}
}
