package jp.dbcls.bp3d.kaorif;

import java.io.*;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.*;

import jp.dbcls.bp3d.*;
import jp.dbcls.bp3d.fma.FMAOBO;
import jp.dbcls.bp3d.obj.OBJInfo;
import jp.dbcls.bp3d.obj.ParseOBJName;
import jp.dbcls.bp3d.util.StopWatch;

/**
 * kaorif.txtを読み込むクラス
 * 
 * @author mituhasi
 * 
 */
public class Kaorif {
	private final String DATADIR = Bp3dProperties
	.getString("bp3d.datadir") + "/" + Bp3dProperties.getString("bp3d.dataversion");	
	private static final String INFILE = Bp3dProperties.getString("bp3d.datadir") + "/"
		+ Bp3dProperties.getString("bp3d.dataversion")
		+ "/conf/kaorif.xls";

	private final String LOGFILE = DATADIR + "/logs/" + this.getClass().getName() + ".log";
	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	private FMAOBO fmaobo = new FMAOBO();
	
	protected Set<Bp3dEntry> entries = new HashSet<Bp3dEntry>();
	protected Map<String, Bp3dEntry> ids = new TreeMap<String, Bp3dEntry>();
	protected Map<String, Bp3dEntry> ens = new TreeMap<String, Bp3dEntry>();
	
	protected Bp3dTree bp3dTree = new Bp3dTree();

	public Kaorif() throws Exception {
		FileHandler fh = new FileHandler(LOGFILE);
		fh.setFormatter(new java.util.logging.SimpleFormatter());
		logger.addHandler(fh);
		
		readFile();
	}

	public Set<Bp3dEntry> getAllEntries(){		
		return entries;
	}
		
	public Bp3dTree getBp3dTree(){		
		return bp3dTree;
	}
	
	/**
	 * termを持つエントリが含まれているかどうかを判定する
	 * 
	 * @param term
	 * @return
	 */
	public boolean contains(String term) {
		if (getEntry(term) == null) {
			return false;
		} else {
			return true;
		}
	}
		
	/**
	 * ID, en -> Bp3dEntryを取得する
	 * 
	 * @param term
	 * @return
	 */
	public Bp3dEntry getEntry(String term) {
		if (ids.containsKey(term)) {
			return ids.get(term);
		} else if (ens.containsKey(term.toLowerCase())) {
			return ens.get(term.toLowerCase());
		} else {
			return null;
		}
	}

	/**
	 * kaorif.xlsファイルを読み込む
	 * 
	 * @param dirname
	 * @throws Exception
	 */
	public void readFile() throws Exception {
		readMainFileXls();
		readPartFileXls();
	}

	/**
	 * kaorif.xlsファイルのkaorifシートを読み込む
	 * 
	 * @param filename
	 * @throws Exception
	 */
	private void readMainFileXls() throws Exception {
		POIFSFileSystem filein = new POIFSFileSystem(new FileInputStream(
				INFILE));
		HSSFWorkbook wb = new HSSFWorkbook(filein);

		List<String> sheetsToRead = new ArrayList<String>();
		sheetsToRead.add("kaorif");

		for (String sheetStr : sheetsToRead) {
			HSSFSheet sheet = wb.getSheet(sheetStr);

			for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
				HSSFRow row = sheet.getRow(i);
						
				Bp3dEntry ent = new Bp3dEntry();
				if(parseEntry(ent, row) == false){
					continue;
				}

				entries.add(ent);
				
				if (ent.getId() != null) {
					ids.put(ent.getId(), ent);
				}
				
				if(ent.getEn() != null){															
					if (ens.containsKey(ent.getEn())) {
						String msg = "\t" + ent.getEn() + "\t" + "duplicated.";
						logger.log(new LogRecord(Level.SEVERE, msg));
					}
					ens.put(toPreferredName(ent.getEn()), ent);
				}				
			}
		}
	}

	/**
	 * FMAのPreferred Nameに変換する
	 * @param en
	 * @return
	 */
	public String toPreferredName(String en){
		if(fmaobo.contains(en) && !fmaobo.isPreferredName(en)){
			String msg = "\t" + "Not FMA preferred name" + "\t" + en + "\t" + fmaobo.get(en).getName();
			logger.log(new LogRecord(Level.WARNING, msg));	
			return fmaobo.get(en).getName();	
		}
		return en;
	}
	
	/**
	 * KaorifPartのParentとして正しい英語名か判定する
	 * @param en
	 * @return
	 */
/**	
	public boolean isValidKaorifParent(String en){
		if(!(fmaobo.contains(en) || contains(en))){
			String msg = "\t" + "Not FMA/Kaorif name" + "\t" + en + "\t" + "in KaorifPart";
			logger.log(new LogRecord(Level.WARNING, msg));
			return false;
		}
		
		return true;			
	}
**/
	
	/**
	 * KaorifPartのChildとして正しい英語名か判定する
	 * @param en
	 * @return
	 */
/**	
	public boolean isValidKaorifChild(String en){
		if(!(fmaobo.contains(en) || contains(en) || objInfo.containsKey(en))){
			String msg = "\t" + "Not FMA/Kaorif name" + "\t" + en + "\t" + "in KaorifPart";
			logger.log(new LogRecord(Level.WARNING, msg));
			return false;
		}
		
		return true;
	}
**/	
	
	/**
	 * kaorif.xlsファイルのkaorifPartシートを読み込む
	 * 
	 * @param filename
	 * @throws Exception
	 */
	private void readPartFileXls() throws Exception {
		POIFSFileSystem filein = new POIFSFileSystem(new FileInputStream(
				INFILE));
		HSSFWorkbook wb = new HSSFWorkbook(filein);
		HSSFSheet sheet = wb.getSheet("kaorifPart");
		
		for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
			int j = 0;
			HSSFRow row = sheet.getRow(i);

			boolean isUsed = row.getCell(j++).getBooleanCellValue();
			/** isUsed=FALSEの行は無視 **/
			if (isUsed == false) {
				continue;
			}

			String child = toPreferredName(row.getCell(j++).getRichStringCellValue().toString()
					.trim().toLowerCase());
			String parent = toPreferredName(row.getCell(j++).getRichStringCellValue().toString()
					.trim().toLowerCase());
			
			bp3dTree.addMemberOf(child, parent);
			bp3dTree.addReverseMemberOf(parent, child);							
		}
	}

	
	/**
	 * kaorif.xlsのkaorifシートをパースする
	 * 
	 * @param row
	 * @throws Exception
	 */
	private boolean parseEntry(Bp3dEntry bp3dEnt, HSSFRow row) throws Exception {
		int i = 0;
		HSSFCell cell = null;

		boolean isUsed = row.getCell(i++).getBooleanCellValue();				
		if(isUsed == false){
			return false;
		}
		
		cell = row.getCell(i++);
		if (cell != null) {
			bp3dEnt.setId(cell.getRichStringCellValue().toString().trim());
		}
		bp3dEnt.setEn(row.getCell(i++).getRichStringCellValue().toString().trim()
				.toLowerCase());
		cell = row.getCell(i++);
		if (cell != null) {
			bp3dEnt.setKanji(cell.getRichStringCellValue().toString().trim());
		}
		cell = row.getCell(i++);
		if (cell != null) {
			bp3dEnt.setKana(cell.getRichStringCellValue().toString().trim());
		}
		
		return true;
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

		Kaorif kaorif = new Kaorif();
		System.out.println(kaorif.contains("anterior descending artery"));

		sw.stop();

		System.out.println("Kaorif completed: elapsed time="
				+ sw.getElapsedTimeSecs() + "sec");
	}
}
