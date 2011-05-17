package jp.dbcls.bp3d.kaorif;

import java.io.FileInputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import jp.dbcls.bp3d.Bp3dProperties;

public class Correction {
	Map<String, CorrectionEntry> table;

	/** 間違いピース名->正誤表のテーブル **/

	public Correction() throws Exception {
		String xlsFile = Bp3dProperties.getString("bp3d.datadir") + "/"
				+ Bp3dProperties.getString("bp3d.dataversion")
				+ "/conf/kaorif.xls";
		String sheetName = "correction";

		this.table = new TreeMap<String, CorrectionEntry>();

		readListXls(xlsFile, sheetName);
	}

	public Correction(String xlsFile, String sheetName) throws Exception {
		readListXls(xlsFile, sheetName);
	}

	/**
	 * 正誤表を返す
	 * 
	 * @return
	 */
	public Map<String, CorrectionEntry> getTable() {
		return table;
	}

	/**
	 * 正誤表を使ってピース名を修正する
	 * 
	 * @param en
	 * @return
	 */
	public String doCorrect(String en, String clyName) {
		String enCorrected;

		if (table.containsKey(en)) { // 正誤表 correction.txtに含まれている場合
			CorrectionEntry ce = table.get(en);
			ce.setCly(clyName);
			ce.setUsed(true);
			enCorrected = ce.getCorrection();
		} else {
			enCorrected = en;
		}

		/** 末尾についている数字を落とす **/
		enCorrected = Correction.trimTailNumber(enCorrected);
		/** ピース名に含まれる短縮形のピリオドの後にスペースがない場合は挿入 **/
		enCorrected = Correction.addSpaceAfterPunctuation(enCorrected, ".");
		/** ピース名に含まれるカンマの後にスペースがない場合は挿入 **/
		enCorrected = Correction.addSpaceAfterPunctuation(enCorrected, ",");

		return enCorrected;
	}

	/**
	 * kaorif.xlsのcorrectionシートをパースする
	 * 
	 * @param row
	 * @throws Exception
	 */
	public void readListXls(String xlsFile, String sheetName) throws Exception {
		POIFSFileSystem filein = new POIFSFileSystem(new FileInputStream(xlsFile));
		HSSFWorkbook wb = new HSSFWorkbook(filein);
		HSSFSheet sheet = wb.getSheet(sheetName);

		for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
			int j = 0;
			HSSFRow row = sheet.getRow(i);
			j++;
			String piece = row.getCell(j++).getRichStringCellValue().toString();
			String correction = row.getCell(j++).getRichStringCellValue().toString()
					.trim();
			String cly = row.getCell(j++).getRichStringCellValue().toString().trim();
			String comment = "";
			if (row.getCell(j) != null) {
				comment = row.getCell(j++).getRichStringCellValue().toString().trim();
			}

			if (table.containsKey(piece)) {
				System.out.println("Correction: duplicate correction=" + piece);
			}

			CorrectionEntry ce = new CorrectionEntry();
			ce.isUsed = false;
			ce.piece = piece;
			ce.correction = correction;
			ce.cly = cly;
			ce.comment = comment;

			table.put(piece, ce);
		}
	}

	public void displayList() {
		for (String en : table.keySet()) {
			System.out.println(en + "," + table.get(en));
		}
	}

	/**
	 * ピース名の末尾に数字がついているか判定する e.g. "090331-heart 6 "-> true
	 * 
	 * @param pieceName
	 * @return
	 */
	public static boolean hasTailNumber(String pieceName) {
		String regex = "[ ]+[0-9]*[ ]*$";
		Pattern pat = Pattern.compile(regex);
		Matcher matcher = pat.matcher(pieceName);
		return matcher.matches();
	}

	/**
	 * ピース名の末尾の数字をトリミングする e.g. "090331-heart 6 "-> "090331-heart"
	 * 
	 * @param pieceName
	 * @return
	 */
	public static String trimTailNumber(String pieceName) {
		String regex = "[ ]+[0-9]*[ ]*$";
		pieceName = pieceName.replaceFirst(regex, "");

		return pieceName;
	}

	/**
	 * ピース名に含まれる句読点の後にスペースがない場合は挿入する。 e.g. "090331-l.heart 6 "-> "090331-l. heart"
	 * 
	 * @param pieceName
	 * @param punc
	 * @return
	 */
	public static String addSpaceAfterPunctuation(String pieceName, String punc) {
		String ret = "";
		int fromIdx = 0;

		while (true) {
			int toIdx = pieceName.indexOf(punc, fromIdx);
			if (toIdx < 0) {
				break;
			}
			ret += pieceName.substring(fromIdx, toIdx + 1);
			fromIdx = toIdx + 1;
			/** 句読点の後がスペースでない場合は、スペースを挿入 **/
			if (!pieceName.startsWith(" ", fromIdx)) {
				ret += " ";
			}
		}
		ret += pieceName.substring(fromIdx);

		return ret;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		Correction correction = new Correction();
		// correction.displayList();
		System.out.println(correction.doCorrect("l.supraspinatus", "dummy.cly"));
		System.out.println(correction.doCorrect("gingiva of  the maxilla",
				"dummy.cly"));
		System.out.println(correction.doCorrect("pulmonary a", "dummy.cly"));

		/**
		 * String xlsFile = AgHelperProperties.getString("bp3d.datadir") + "/"
		 * + AgHelperProperties.getString("bp3d.dataversion") +
		 * "/090325-boneの修正.xls"; String sheetName = "スペルミス"; Correction correction2
		 * = new Correction(xlsFile, sheetName); correction2.displayList();
		 * System.out.println(correction2.getCorrection(
		 * "right eighth, ninth, elventh costal cartilage .nsn 3"));
		 **/
	}

}
