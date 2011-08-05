package jp.dbcls.bp3d.obj;

import java.io.*;
import java.util.*;

import jp.dbcls.bp3d.*;
import jp.dbcls.bp3d.kaorif.*;
import jp.dbcls.bp3d.util.StopWatch;

/**
 * OBJファイル名を分解し、臓器名、情報を取り出します。
 * 
 * @author mituhasi
 * 
 */
public class ParseOBJName {
	private Collection<String> objDirs = null;
	private Collection<String> objEditDirs = null;

	private OBJInfo objInfoEnLong = null; // 英語名(LongForm)とOBJファイル情報の対応表
	private Abbrev abbrev2Long = null; // 省略形とLong formの対応表(abbrev.txt)
	private Correction correction = null; // OBJファイル名の修正
	private Ignore ignore = null; // OBJファイル名の修正

	private String logDir = ""; // logディレクトリ

	public ParseOBJName() throws Exception {
		this.objInfoEnLong = new OBJInfo();

		OBJConf objConf = new OBJConf();
		this.objDirs = objConf.getOBJDirs();
		this.objEditDirs = objConf.getOBJEditDirs();
		this.abbrev2Long = new Abbrev();
		this.correction = new Correction();
		this.ignore = new Ignore();

		makeOBJList();
	}

	public ParseOBJName(String logDir) throws Exception {
		this.objInfoEnLong = new OBJInfo();

		OBJConf objConf = new OBJConf();
		this.objDirs = objConf.getOBJDirs();
		this.objEditDirs = objConf.getOBJEditDirs();
		this.abbrev2Long = new Abbrev();
		this.correction = new Correction();
		this.ignore = new Ignore();

		makeOBJList();
		
		this.logDir = logDir;
		write(); // ログを出力
	}

	/**
	 * dir名からClayファイルの日付を取り出す e.g. 110113-stomack -> 2011/01/13
	 * 
	 * @param dirname
	 * @return
	 * @throws Exception
	 */
	public String getClayDate(String dirname) throws Exception {
		String yyyy = "20" + dirname.substring(0, 2);
		String mm = dirname.substring(2, 4);
		String dd = dirname.substring(4, 6);
		String clayDate = yyyy + "/" + mm + "/" + dd;

		return clayDate;
	}

	/**
	 * 臓器名の取得 filename=解剖学用語 {p[0-9]}-以下コメント p+数字は、partに分割している場合
	 * 
	 * @param filename
	 * @param dirname
	 * @return
	 */
	public String getOrganName(String filename, String dirname) {
		String separator = "";
		if (filename.contains(dirname + "_")) {
			separator = "_";
		} else if (filename.contains(dirname + "-")) {
			separator = "-";
		} else {
			System.err.println("getOrganName:Invalid filename=" + filename);
		}

		String organName = filename.replace(dirname + separator, "");
		String[] tkns = organName.split(separator);

		/**
		 * sparator以降はコメントと判断してトリミングする
		 * SPLのファイル名は単語区切りがseparatorと同じなので、コメントと区別できない。separatorを使ったコメントはないと判断
		 * 
		 * String[] tkns = organName.split(separator); organName = tkns[0].trim();
		 **/

		return organName;
	}

	/**
	 * 臓器名（LongName)から,left/rightの記述があるか判定する
	 * 
	 * @param organNameLong
	 * @return
	 */
	public String getLeftRight(String organNameLong) {
		if (organNameLong.contains("left") && organNameLong.contains("right")) {
			return "conflict";
		} else if (organNameLong.contains("left")) {
			return "left";
		} else if (organNameLong.contains("right")) {
			return "right";
		} else {
			return "nothing";
		}
	}

	/**
	 * OBJファイル名(e.g. 081219-life science
	 * body-kf-appendix.obj)をparseして、漢字、ディレクトリ名、最終更新日を取り出す
	 * 
	 * @param objFile
	 * @throws Exception
	 */
	public OBJInfoEntry parseFilename(File objFile) throws Exception {
		/**
		 * OBJファイルのパス名の例 bp3d.objdir/081219-life science body-kf/0.01/081219-life
		 * science body-kf-appendix.obj filename =
		 * "081219-life science body-kf[-_]appendix" dirname =
		 * "081219-life science body-kf"
		 */
		String filename = objFile.getName();
		String dirname = objFile.getParentFile().getParentFile().getName();

		/** OBJファイルの絶対パス **/
		String absolutePath = objFile.getAbsolutePath();

		/** *.obj終わらないものは無視, *.objで終わる場合は、.objを取る **/
		if (!filename.endsWith(".obj")) {
			System.err.println("No obj file found:" + filename);
			return null;
		} else {
			filename = filename.replaceFirst(".obj$", "");
		}

		/** 更新日の取得 **/
		String updateDate = getClayDate(dirname);

		/** 臓器名の取得 **/
		String en = getOrganName(filename, dirname);

		/** ignore listに入っていれば無視する **/
		if (ignore.isEgnored(en, dirname)) {
			ignore.setApplied(en, dirname);
			return null;
		}

		/** 正誤表による変換 **/
		String enCorrected = correction.doCorrect(en, dirname);

		/** 省略形からLongFormの変換 **/
		String longForm = abbrev2Long.getLongForm(enCorrected);

		/** LongFormからleft/rightを抽出する **/
		String leftRight = getLeftRight(longForm);
		
		/** ObjInfoオブジェクトにこれらの情報を詰め込む **/
		OBJInfoEntry objInfo = new OBJInfoEntry();
		objInfo.setEn(en);
		objInfo.setEnCorrected(enCorrected);
		objInfo.setEnLong(longForm);
		objInfo.setLeftRight(leftRight);
		objInfo.addDate(updateDate);
		objInfo.addDir(absolutePath);

		return objInfo;
	}

	/**
	 * ObjInfoSetの情報をファイル(ParseName.txt)に出力する
	 * 
	 * @throws Exception
	 */
	public void write() throws Exception {
		/**
		 * 最新のobjだけの情報を出力
		 * **/
		String logFile = logDir + "/ParseOBJName.log";
		FileWriter out = new FileWriter(logFile, false);
		BufferedWriter bw = new BufferedWriter(out);

		bw.write("enLong" + "\t" + "cly" + "\t" + "piece" + "\n");

		for (OBJInfoEntry obj : objInfoEnLong.values()) {
			bw.write(obj.getEnLong() + "\t" + obj.getLatestClyName() + "\t"
					+ obj.getLatestPieceName() + "\n");
		}

		bw.close();
		out.close();

		/**
		 * 全てのobjの情報を出力
		 * **/
		logFile = logDir + "/ParseOBJNameAll.log";
		out = new FileWriter(logFile, false);
		bw = new BufferedWriter(out);

		bw.write("en" + "\t" + "enCorrected" + "\t" + "enLong" + "\t"
				+ "lastUpdate" + "\t" + "cly" + "\t" + "piece" + "\n");

		for (OBJInfoEntry obj : objInfoEnLong.values()) {
			for (int i = 0; i < obj.getDates().size(); i++) {
				bw.write(obj.getEn() + "\t" + obj.getEnCorrected() + "\t"
						+ obj.getEnLong() + "\t" + obj.getDates().get(i) + "\t"
						+ obj.getClyName(i) + "\t" + obj.getPieceName(i) + "\n");
			}
		}

		bw.close();
		out.close();

		/**
		 * correctionを行った結果を出力
		 * **/
		logFile = logDir + "/correction.log";
		FileOutputStream fos = new FileOutputStream(logFile, false);
		bw = new BufferedWriter(new OutputStreamWriter(fos, "MS932"));

		bw.write("isUsed" + "\t" + "piece" + "\t" + "enCorrected" + "\t" + "cly"
				+ "\t" + "comment" + "\n");

		for (CorrectionEntry ce : correction.getTable().values()) {
			bw.write(ce.isUsed() + "\t" + ce.getPiece() + "\t" + ce.getCorrection()
					+ "\t" + ce.getCly() + "\t" + ce.getComment() + "\n");
		}

		bw.close();
		out.close();

		/**
		 * ignoreを行った結果を出力
		 */
		logFile = logDir + "/ignore.log";
		fos = new FileOutputStream(logFile, false);
		bw = new BufferedWriter(new OutputStreamWriter(fos, "MS932"));

		bw.write("isIgnored" + "\t" + "isApplied" + "\t" + "piece" + "\t" + "cly"
				+ "\t" + "comment" + "\n");

		for (IgnoreEntry ie : ignore.getTable()) {
			bw.write(ie.isIgnored() + "\t" + ie.isApplied() + "\t" + ie.getPiece()
					+ "\t" + ie.getCly() + "\t" + ie.getComment() + "\n");
		}

		bw.close();
		out.close();
	}

	/**
	 * stlInfoSetEn (kaorif.txtの英語名に変換した英語名がキー）のstlInfoのハッシュを返す
	 * 
	 * @return
	 */
	public OBJInfo getOBJInfoEnLong() {
		return objInfoEnLong;
	}

	/**
	 * this.objDirStr以下のOBJファイル名をparseFilenameでパースする
	 */
	public void makeOBJList() throws Exception {
		/** システム別クレイファイル **/
		for (String objDirStr : objDirs) {
			boolean isParsed = false;

			File objDir = new File(objDirStr);

			if (!objDir.exists()) {
				System.err.println("OBJDir defined in obj.conf not found=" + objDirStr);
				System.exit(1);
			}

			for (File objFile : objDir.listFiles()) {
				if (objFile.isDirectory()) {
					continue;
				}
				OBJInfoEntry objInfo = parseFilename(objFile); // OBJファイル名のパース
				if(objInfo != null){
					objInfoEnLong.add(objInfo.getEnLong(), objInfo); // ObjInfoSetに追加
				}
				isParsed = true;
			}

			/** パースするべきOBJファイルがない場合に警告 **/
			if (isParsed == false) {
				System.err.println("ParseOBJName: no OBJs to parse at " + objDirStr);
			}
		}
				
		/** conf/objEdit.confに記述されたディレクトリのOBJを読み込む **/
		for (String objDirStr : objEditDirs) {
			boolean isParsed = false;

			objDirStr = objDirStr + "/";
			File objDir = new File(objDirStr);
			if (!objDir.exists()) {
				System.err.println("ParseOBJName: objFile not found " + objDirStr);
				continue;
			}

			for (File objFile : objDir.listFiles()) {
				if (objFile.isDirectory()) {
					continue;
				}
				OBJInfoEntry objInfo = parseFilename(objFile); // OBJファイル名のパース
				if(objInfo != null){
					objInfoEnLong.add(objInfo.getEnLong(), objInfo); // ObjInfoSetに追加
				}
				isParsed = true;

			}
			
			/** パースするべきOBJファイルがない場合に警告 **/
			if (isParsed == false) {
				System.err.println("ParseOBJName: no OBJs to parse at " + objDirStr);
			}
		}
	}

	/**
	 * テストコード
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		StopWatch sw = new StopWatch();
		sw.start();

		String logDir = Bp3dProperties.getString("bp3d.datadir") + "/"
				+ Bp3dProperties.getString("bp3d.dataversion") + "/logs/MakeBp3d0";

		ParseOBJName pon = new ParseOBJName(logDir);
		pon.write();
		
		sw.stop();

		System.out.println("ParseOBJName completed: elapsed time="
				+ sw.getElapsedTimeSecs() + "sec");
	}

}
