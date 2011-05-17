package jp.dbcls.bp3d.kaorif;

import java.io.FileInputStream;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import jp.dbcls.bp3d.Bp3dProperties;

public class Abbrev {
	private String listFile = Bp3dProperties.getString("bp3d.datadir")
			+ "/" + Bp3dProperties.getString("bp3d.dataversion")
			+ "/conf/kaorif.xls";

	Map<String, String> a2l;
	/** abbrev -> long form **/
	Map<String, String> l2a;

	/** long form -> abbrev **/

	public Abbrev() throws Exception {
		this.a2l = new TreeMap<String, String>();
		this.l2a = new TreeMap<String, String>();

		readListXls();
	}

	/**
	 * kaorif.xlsのabbrevシートをパースする
	 * 
	 * @param row
	 * @throws Exception
	 */
	public void readListXls() throws Exception {
		POIFSFileSystem filein = new POIFSFileSystem(new FileInputStream(
				this.listFile));
		HSSFWorkbook wb = new HSSFWorkbook(filein);
		HSSFSheet sheet = wb.getSheet("abbrev");

		for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
			HSSFRow row = sheet.getRow(i);
			if (row.getCell(2) == null) {
				continue;
			}
			String longForm = row.getCell(0).getRichStringCellValue().toString()
					.trim();
			String abbrev = row.getCell(2).getRichStringCellValue().toString().trim();
			if (abbrev.equals("")) {
				continue;
			}
			if (a2l.containsKey(abbrev)) {
				System.out.println("Abbrev: duplicate entry=" + abbrev);
			}
			a2l.put(abbrev, longForm);
			l2a.put(longForm, abbrev);
		}
	}

	public void displayList() {
		for (String abbrev : a2l.keySet()) {
			System.out.println(abbrev + "," + a2l.get(abbrev));
		}
	}

	/**
	 * 省略形からlongformを得る l. of/l ofのどちらでもleft ofに変換する
	 * 
	 * @param abbrev
	 * @return
	 */
	public String getLongForm(String abbrev) {
		String longForm = "";
		boolean isNSN = false;

		if (abbrev.endsWith(", nsn") || abbrev.endsWith(",nsn")) {
			abbrev = abbrev.replace(", nsn", "");
			isNSN = true;
		}

		for (String token : abbrev.split("[ ]+")) {
			String tokenDot = token + ".";

			if (a2l.containsKey(token)) {
				longForm += a2l.get(token) + " ";
			} else if (a2l.containsKey(tokenDot)) {
				longForm += a2l.get(tokenDot) + " ";
			} else {
				longForm += token + " ";
			}
		}
		longForm = longForm.trim();

		if (isNSN) {
			longForm += ", nsn";
		}

		return longForm;
	}

	/**
	 * longFormから省略形を取得する。
	 * 
	 * @param longForm
	 * @return
	 */
	public String getAbbrev(String longForm) {
		String abbrev = "";
		boolean isNSN = false;

		if (longForm.endsWith(", nsn")) {
			longForm = longForm.replace(", nsn", "");
			isNSN = true;
		}

		for (String token : longForm.split("[ ]+")) {
			if (l2a.containsKey(token)) {
				abbrev += l2a.get(token) + " ";
			} else {
				abbrev += token + " ";
			}
		}
		abbrev = abbrev.trim();

		if (isNSN) {
			abbrev += ", nsn";
		}

		return abbrev;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Abbrev abbrev = new Abbrev();
		// abbrev.displayList();

		System.out.println(abbrev
				.getLongForm("intervertebral disk of seventh thoracic vert"));
		System.out.println(abbrev.getLongForm("l.supraspinatus"));
		System.out.println(abbrev.getLongForm("r. costal cartilage, nsn"));
		System.out.println(abbrev.getLongForm("l. lung"));
		System.out.println(abbrev.getLongForm("l lung"));
		System.out.println(abbrev.getAbbrev("left  lung"));
		System.out.println(abbrev.getLongForm("middle cardiac V"));
		System.out.println(abbrev.getAbbrev("mid cardiac veins"));
		System.out.println(abbrev.getLongForm("pulmonary a"));
	}

}
