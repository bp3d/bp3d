package jp.dbcls.bp3d.kaorif;

import jp.dbcls.bp3d.Bp3dEntry;

import java.util.regex.*;
import org.apache.poi.hssf.usermodel.*;

public class KaorifEntry extends Bp3dEntry {
	protected boolean isUsed; // Bp3dに使われている場合=1, 使われていない場合=0

	public KaorifEntry() throws Exception {
	}

	/**
	 * kaorif.txtファイルを読み込む
	 * 
	 * @param filename
	 * @throws Exception
	 */
	public void parseEntry(String line) throws Exception {
		String[] data = Pattern.compile("\t").split(line);

		if (data.length < 3) {
			System.out.println("parseEntry:" + line + " is ignored.");
			return;
		}

		int idx = 0;
		this.isUsed = data[idx++].trim().equalsIgnoreCase("true") ? true : false;
		this.id = data[idx].trim().length() == 0 ? null : data[idx].trim();
		idx++;
		this.en = data[idx++].trim().toLowerCase().replaceAll("\"", "");
		this.kanji = data[idx++].trim();
		this.kana = data[idx++].trim();
		this.organSystem = data[idx++].trim();
	}

	/**
	 * kaorif.xlsのkaorifシートをパースする
	 * 
	 * @param row
	 * @throws Exception
	 */
	public void parseEntry(HSSFRow row) throws Exception {
		int i = 0;
		HSSFCell cell = null;

		this.isUsed = row.getCell(i++).getBooleanCellValue();
		cell = row.getCell(i++);
		if (cell != null) {
			this.id = cell.getRichStringCellValue().toString().trim();
		}
		this.en = row.getCell(i++).getRichStringCellValue().toString().trim()
				.toLowerCase();
		cell = row.getCell(i++);
		if (cell != null) {
			this.kanji = cell.getRichStringCellValue().toString().trim();
		}
		cell = row.getCell(i++);
		if (cell != null) {
			this.kana = cell.getRichStringCellValue().toString().trim();
		}
		this.organSystem = row.getCell(i).getRichStringCellValue().toString()
				.trim();
	}

	/**
	 * @return the isUsed
	 */
	public boolean isUsed() {
		return isUsed;
	}

	public void display() {
		System.out.print("id=" + getId());
		System.out.print(",isUsed=" + isUsed());
		System.out.print(",en=" + getEn());
		System.out.print(",kanji=" + getKanji());
		System.out.print(",kana=" + getKana());
		System.out.print(",volume=" + getVolume());
	}

	public static void main(String[] args) throws Exception {
		KaorifEntry ent = new KaorifEntry();
		String line = "TRUE" + "\t" + "FMA62004" + "\t" + "medulla oblongata"
				+ "\t" + "" + "\t" + "延髄" + "\t" + "えんずい" + "\t" + "1" + "\t"
				+ "nervous system" + "\t";
		ent.parseEntry(line);
		ent.display();
	}

}
