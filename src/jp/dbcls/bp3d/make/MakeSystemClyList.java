package jp.dbcls.bp3d.make;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import jp.dbcls.bp3d.util.StopWatch;
import jp.dbcls.bp3d.Bp3dProperties;
import jp.dbcls.bp3d.obj.OBJConf;

/**
 * FFMがインストールされているPCのクレイファイル保存ディレクトリのファイルリストを作成する
 * 
 * @author mituhasi
 * 
 */
public class MakeSystemClyList {
	private final String pathSeparator = Bp3dProperties
			.getString("bp3d.pathseparator"); // パスの区切り文字

	String root = Bp3dProperties.getString("bp3d.datadir").replace("/",
			pathSeparator) + pathSeparator 
			+ Bp3dProperties.getString("bp3d.dataversion");

	String clyDirStr;
	String objDirStr;

	/** kaorifにあるclay path, isUsedのペア **/
	Map<String, Clay> kaorif = new HashMap<String, Clay>();

	/** 出力結果 **/
	List<Clay> result = new ArrayList<Clay>();
	/** 新規に追加されたclay **/
	List<String> newClay = new ArrayList<String>();

	String outFile;
	OBJConf objConf;

	MakeSystemClyList() throws Exception {
		this.objDirStr = Bp3dProperties.getString("bp3d.objdir") + "/";
		this.objDirStr = this.objDirStr.replace("/", pathSeparator);

		this.objConf = new OBJConf();
		this.outFile = Bp3dProperties.getString("bp3d.datadir")
				+ pathSeparator + Bp3dProperties.getString("bp3d.dataversion")
				+ pathSeparator + "logs/clyList.txt";
	}

	/**
	 * kaorif.xls!system_clayシートの内容を保持する内部クラス
	 * 
	 * @author ag
	 * 
	 */
	class Clay {
		boolean isUsed = false;
		boolean isNew = false;
		String ver;
		String dir;
		String cly;
		String coarseness;
	}

	/**
	 * kaorif.xlsのsystem_clay_listシートを読む
	 * 
	 * @throws Exception
	 */
	public void readKaorif() throws Exception {
		String inFile = this.root + pathSeparator + "conf" + pathSeparator
				+ "kaorif.xls";
		POIFSFileSystem filein = new POIFSFileSystem(new FileInputStream(inFile));
		HSSFWorkbook wb = new HSSFWorkbook(filein);
		HSSFSheet sheet = wb.getSheet("system_clay");

		for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
			int j = 0;
			HSSFRow row = sheet.getRow(i);

			boolean isUsed = row.getCell(j++).getBooleanCellValue();
			j++;

			String ver = Double.toString(row.getCell(j++).getNumericCellValue());
			String dir = row.getCell(j++).getRichStringCellValue().toString().trim();
			String cly = row.getCell(j++).getRichStringCellValue().toString().trim();
			String coarseness = Double.toString(row.getCell(j++)
					.getNumericCellValue());

			String path = dir + pathSeparator + cly;

			Clay c = new Clay();
			c.isUsed = isUsed;
			c.ver = ver;
			c.dir = dir;
			c.cly = cly;
			c.coarseness = coarseness;

			kaorif.put(path, c);
		}
	}

	/**
	 * clyDirを再帰的に見て、clyファイルと対応する STLファイルのリストを作成する
	 * 
	 * @param dir
	 * @param path
	 * 
	 * @throws Exception
	 */
	public void list(File dir, String path) throws Exception {
		if (path.startsWith("\\")) {
			path = path.substring(1);
		}
		// System.out.println("path=" + path);

		for (File file : dir.listFiles()) {
			String fileName = file.getName();
			String dirName = path + pathSeparator + fileName;

			/** _versions ディレクトリは無視 **/
			if (fileName.endsWith("_versions")) {
				continue;
			}

			/** *.clyファイルの場合、OBJディレクトリが存在するか、stl.confに書かれているかチェック **/
			if (fileName.endsWith(".cly")) {
				Clay c;
				if (kaorif.containsKey(dirName)) {
					c = kaorif.get(dirName);
					c.isNew = false;
				} else {
					c = new Clay();
					c.isUsed = false;
					c.isNew = true;
					c.ver = Bp3dProperties.getString("bp3d.dataversion");
					c.dir = path;
					c.cly = fileName;
					this.newClay.add(c.dir + "\\" + c.cly);
				}
				result.add(c);
			}

			/** ディレクトリの場合、その下を見に行く **/
			if (file.isDirectory()) {
				list(file, dirName);
			}
		}
	}

	public void write() throws Exception {
		FileOutputStream fos = new FileOutputStream(this.outFile, false);
		OutputStreamWriter out = new OutputStreamWriter(fos, "MS932");
		BufferedWriter bw = new BufferedWriter(out);

		bw.write("isUsed" + "\t" + "isNew" + "\t" + "ver" + "\t" + "dir" + "\t"
				+ "cly" + "\t" + "coarseness" + "\n");

		for (Clay c : result) {
			bw.write(c.isUsed + "\t" + c.isNew + "\t" + c.ver + "\t" + c.dir + "\t"
					+ c.cly + "\t" + c.coarseness + "\n");
		}

		bw.close();
		out.close();
	}

	public void run() throws Exception {
		/** システム別ディレクトリをリストする **/
		this.clyDirStr = Bp3dProperties.getString("bp3d.claydir");
		/** pathの区切りをスラッシュからバックスラッシュにする **/
		this.clyDirStr = this.clyDirStr.replace("/", pathSeparator);

		readKaorif();
		list(new File(clyDirStr), "");
		write();

		if (newClay.size() > 0) {
			System.out.println(newClay.size() + " new clay files are found:");
			for (String path : newClay) {
				System.out.println(path);
			}
		} else {
			System.out.println("No new clay files are found.");
		}

	}

	public static void main(String[] args) throws Exception {
		StopWatch sw = new StopWatch();
		sw.start();

		MakeSystemClyList mkcl = new MakeSystemClyList();
		mkcl.run();

		sw.stop();

		System.out.println("MakeClyList completed: elapsed time="
				+ sw.getElapsedTimeSecs() + "sec");
	}
}
