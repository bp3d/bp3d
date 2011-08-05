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

public class En2Ja {
	Map<String, En2JaEntry> table;

	/** preferred name->漢字、かなのテーブル **/

	public En2Ja() throws Exception {
		String xlsFile = Bp3dProperties.getString("bp3d.datadir") + "/"
				+ Bp3dProperties.getString("bp3d.dataversion")
				+ "/conf/kaorif.xls";
		String sheetName = "en2ja";

		this.table = new TreeMap<String, En2JaEntry>();

		readListXls(xlsFile, sheetName);
	}

	public En2Ja(String xlsFile, String sheetName) throws Exception {
		readListXls(xlsFile, sheetName);
	}

	public boolean contains(String en){
		return table.containsKey(en);
	}
	
	public String getKanji(String en){
		if(contains(en)){
			return table.get(en).getKanji();
		}else{
			return null;
		}	
	}

	public String getKana(String en){
		if(contains(en)){
			return table.get(en).getKana();
		}else{
			return null;
		}	
	}
	
	/**
	 * kaorif.xlsのen2jaシートをパースする
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
			if(row.getCell(j++).getBooleanCellValue() == false){
				continue;
			}
			
			String en = row.getCell(j++).getRichStringCellValue().toString().trim();
			String kanji = row.getCell(j++).getRichStringCellValue().toString()
			.trim();
			String kana = row.getCell(j++).getRichStringCellValue().toString()
			.trim();
			
			En2JaEntry ej = new En2JaEntry();
			ej.en = en;
			ej.kanji = kanji;
			ej.kana = kana;

			table.put(en, ej);
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
/**	
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
//			句読点の後がスペースでない場合は、スペースを挿入 
			if (!pieceName.startsWith(" ", fromIdx)) {
				ret += " ";
			}
		}		
		ret += pieceName.substring(fromIdx);

		return ret;
	}
**/
}
