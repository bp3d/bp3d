package jp.dbcls.bp3d.kaorif;

import java.io.FileInputStream;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import jp.dbcls.bp3d.Bp3dProperties;

/**
 * 無視するobjファイルの情報を保持する
 * 
 * @author ag
 * 
 */
public class Ignore {
	List<IgnoreEntry> table;

	/** ピース(objファイル）名とclayファイル対応テーブル **/

	public Ignore() throws Exception {
		String xlsFile = Bp3dProperties.getString("bp3d.datadir") + "/"
				+ Bp3dProperties.getString("bp3d.dataversion")
				+ "/conf/kaorif.xls";
		String sheetName = "ignore";

		this.table = new ArrayList<IgnoreEntry>();

		readListXls(xlsFile, sheetName);
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
			boolean isIgnored = row.getCell(j++).getBooleanCellValue();

			String piece = row.getCell(j++).getRichStringCellValue().toString();
			String cly = row.getCell(j++).getRichStringCellValue().toString().trim();
			String comment = "";
			if (row.getCell(j) != null) {
				comment = row.getCell(j++).getRichStringCellValue().toString().trim();
			}

			IgnoreEntry ie = new IgnoreEntry();
			ie.setIgnored(isIgnored);
			ie.setApplied(false); // 実際に使われるとtrueになる
			ie.setPiece(piece);
			ie.setCly(cly);
			ie.setComment(comment);

			table.add(ie);
		}
	}

	/**
	 * ignore表を返す
	 * 
	 * @return
	 */
	public List<IgnoreEntry> getTable() {
		return table;
	}

	/**
	 * Ignoreするか判定する
	 * 
	 * @param en
	 * @param clyName
	 * @return
	 */
	public boolean isEgnored(String en, String clyName) {
		for (IgnoreEntry ie : table) {
			if (ie.isIgnored() && ie.getPiece().equals(en)
					&& ie.getCly().equals(clyName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * isAppliedをtrueにする
	 * 
	 * @param en
	 * @param clyName
	 */
	public void setApplied(String en, String clyName) {
		for (IgnoreEntry ie : table) {
			if (ie.getPiece().equals(en) && ie.getCly().equals(clyName)) {
				ie.setApplied(true);
			}
		}
	}

	public void displayList() {
		for (IgnoreEntry ie : table) {
			System.out.println(ie.getPiece() + "<->" + ie.getCly());
		}
	}

	public static void main(String[] args) throws Exception {
		Ignore ignore = new Ignore();
		ignore.displayList();
	}

}
