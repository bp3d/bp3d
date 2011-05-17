package jp.dbcls.bp3d.obj;

import java.io.*;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import jp.dbcls.bp3d.*;

/**
 * obj.conf, objEdit.confの情報を読み込んで保持するクラス
 * 
 * @author mituhasi
 * 
 */
public class OBJConf {
	private final String PATHSEPARATOR = Bp3dProperties
			.getString("bp3d.pathseparator"); // パスの区切り文字

	private final String DATADIR = Bp3dProperties.getString(
			"bp3d.datadir").replace("/", PATHSEPARATOR);
	private final String VERDIR = DATADIR + PATHSEPARATOR
			+ Bp3dProperties.getString("bp3d.dataversion");
	private final String OBJDIR = "FFMP" + PATHSEPARATOR + "obj" + PATHSEPARATOR
			+ "システム別クレイファイル";

	/*
	 * OBJファイルが存在するディレクトリのリストを保持するクラス
	 */
	List<String> objDirs;
	List<String> objEditDirs;

	public OBJConf() throws Exception {
		String systemClay = Bp3dProperties.getString("bp3d.datadir") + "/"
				+ Bp3dProperties.getString("bp3d.dataversion")
				+ "/conf/kaorif.xls";
		String objEditConf = Bp3dProperties.getString("bp3d.datadir")
				+ "/" + Bp3dProperties.getString("bp3d.dataversion")
				+ "/conf/objEdit.conf";

		this.objDirs = readKaorifSystemClay(systemClay);
		this.objEditDirs = readOBJConf(objEditConf);
	}

	/**
	 * @return the objDirs
	 */
	public Collection<String> getOBJDirs() {
		return objDirs;
	}

	/**
	 * @return the stlEditDirs
	 */
	public Collection<String> getOBJEditDirs() {
		return objEditDirs;
	}

	/**
	 * objDir(e.g. 090401-bone)がobj.conf, objEdit.confに含まれているかチェックする
	 * 
	 * @param objDirStr
	 * @return
	 */
	public boolean contains(String objDirStr) {
		for (String dirStr : objDirs) {
			if (dirStr.equals(objDirStr)) {
				return true;
			}
		}
		for (String dirStr : objEditDirs) {
			if (dirStr.equals(objDirStr)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * パス中のクレイファイル名(090108-genital system)取得
	 * 
	 * @param path
	 * @return
	 */
	private String getClayName(String path) {
		String[] tokens = path.split(PATHSEPARATOR + PATHSEPARATOR);
		String clyName = tokens[tokens.length - 2];

		return clyName;
	}

	/**
	 * kaorif.xlsのsystem_clay_listシートを読む
	 * 
	 * @throws Exception
	 */
	public List<String> readKaorifSystemClay(String systemClay)
			throws Exception {
		List<String> objDirs = new ArrayList<String>();

		POIFSFileSystem filein = new POIFSFileSystem(
				new FileInputStream(systemClay));
		HSSFWorkbook wb = new HSSFWorkbook(filein);
		HSSFSheet sheet = wb.getSheet("system_clay");

		for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
			int j = 0;
			HSSFRow row = sheet.getRow(i);

			boolean isUsed = row.getCell(j++).getBooleanCellValue();
			/** isUsed=FALSEの行は無視 **/
			if (isUsed == false) {
				continue;
			}

			j++;

			String ver = Double.toString(row.getCell(j++).getNumericCellValue());
			String dir = row.getCell(j++).getRichStringCellValue().toString().trim();
			String cly = row.getCell(j++).getRichStringCellValue().toString().trim()
					.replace(".cly", "");
			String coarseness = Double.toString(row.getCell(j++)
					.getNumericCellValue());

			String clyPath = this.DATADIR + PATHSEPARATOR + ver + PATHSEPARATOR
					+ this.OBJDIR + PATHSEPARATOR + dir + PATHSEPARATOR + cly
					+ PATHSEPARATOR + coarseness;

			objDirs.add(clyPath);
		}

		return objDirs;
	}

	/**
	 * obj.confを読んで、objファイルがあるディレクトリ名を取得する
	 * 
	 * @throws Exception
	 */
	private List<String> readOBJConf(String objConf) throws Exception {
		List<String> objDirs = new ArrayList<String>();

		FileInputStream is = new FileInputStream(objConf);
		InputStreamReader in = new InputStreamReader(is, "MS932");
		BufferedReader br = new BufferedReader(in);
		String path;

		while ((path = br.readLine()) != null) {
			path = path.trim();
			if (path.length() > 0 && !path.startsWith("#")) {
				path = path.trim();
				path = path.endsWith(PATHSEPARATOR) ? path.substring(0,
						path.length() - 1) : path;
				objDirs.add(path);
			}
		}

		return objDirs;
	}

	/**
	 * テストコード
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		OBJConf objConf = new OBJConf();
		for (String dirStr : objConf.getOBJDirs()) {
			System.out.println("objConf.getOBJDirs()=" + dirStr);
		}
		for (String dirStr : objConf.getOBJEditDirs()) {
			System.out.println("objConf.getOBJEditDirs()=" + dirStr);
		}
	}

}
