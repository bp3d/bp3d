/**
The MIT License

Copyright (c) 2010, Database Center for Life Science (DBCLS)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
**/
package jp.dbcls.bp3d.ta.bits;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;

import jp.dbcls.bp3d.*;
import jp.dbcls.bp3d.fma.*;
import jp.dbcls.bp3d.kaorif.CorrectionEntry;
import jp.dbcls.bp3d.ta.TAEntry;
import jp.dbcls.bp3d.ta.bits.TABitsEntry;
import jp.dbcls.bp3d.util.Bp3dUtility;

/**
 * 1. bitsさんが作成したTAJとFMAの対応表(o101_TAJwFMA.txt)を読み込む
 * 2. fmaobo2.txtに当ててFMAIDとの対応が新たに分かったものを付け加える。
 * 3. is_edit=ADD/DELETEは三橋が追加、削除
 * 4. 漢字とかなは、http://lifesciencedb.jp/lsdb.cgi?gg=dicよりダウンロードしたファイルから
 * @author ag
 *
 */
public class TABits {	
	final String INFILE = Bp3dProperties.getString("bp3d.datadir") + 
	Bp3dProperties.getString("bp3d.dataversion") + "/conf/TA/o101_TAJwFMA.xls";
	final String SHEET = "o101_TAJwFMA";

	final String E2JAFILE = Bp3dProperties.getString("bp3d.datadir") + 
		Bp3dProperties.getString("bp3d.dataversion") + "/conf/TA/TerminologiaAnatomicaJaponica13ed_sjis.csv";
	Map<String, String> japaneseEquivalentForm2kana = new HashMap<String, String>();
	Map<String, String> japaneseDisambiguatedForm2kana = new HashMap<String, String>();
	Map<String, String> en2kana = new HashMap<String, String>();
	Map<String, String> en2japaneseDisambiguatedForm = new HashMap<String, String>();
	
	final String OUTFILE = Bp3dProperties.getString("bp3d.datadir") + 
		Bp3dProperties.getString("bp3d.dataversion") + "/conf/TA/ta2fma.txt";

	List<TABitsEntry> entries = new ArrayList<TABitsEntry>();				
	
	FMAOBO fmaobo;
			
	public TABits() throws Exception{
		this.fmaobo = new FMAOBO();
		readTerminologiaAnatomicaJaponica13ed();
		readXls();
	}

	public TABits(FMAOBO fmaobo) throws Exception{
		this.fmaobo = fmaobo;
		readTerminologiaAnatomicaJaponica13ed();
		readXls();
	}
		
	/**
	 * TA->FMAのリスト(TAのIDの昇順)を返す
	 * @return
	 */
	public List<TABitsEntry> getEntries(){
		return entries;
	}
	
	
	/**
	 * TAの１エントリのオブジェクトを作る
	 * @return
	 * @throws Exception
	 */
	public TABitsEntry createTAEntry() throws Exception {
		return new TABitsEntry();
	}

	/**
	 * o101_TAJwFMA.xlsを読み込む
	 * @throws Exception
	 */
	public void readXls() throws Exception {
		POIFSFileSystem filein = new POIFSFileSystem(new FileInputStream(INFILE));
		HSSFWorkbook wb = new HSSFWorkbook(filein);
		HSSFSheet sheet = wb.getSheet(SHEET);

		for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {					
			HSSFRow row = sheet.getRow(i);
			
			int j = 0;
			String edit = "";
			if(row.getCell(j) != null){
				edit = row.getCell(j).getRichStringCellValue().toString().trim();						
			}
			j++;
			
			/** edit=DELETEは読み込まない **/
			if(edit.equals(TABitsEntry.DELETE)){
				continue;
			}
						
			String taId = row.getCell(j++).getRichStringCellValue().toString().trim();

			double taTab = 0.0;
			int cellType = row.getCell(j).getCellType();
			if(cellType == Cell.CELL_TYPE_NUMERIC){
				taTab = row.getCell(j++).getNumericCellValue();
			}else if(cellType == Cell.CELL_TYPE_STRING){
				taTab = Double.parseDouble(row.getCell(j++).getRichStringCellValue().toString().trim().replaceAll(">", ""));	
			}else{
				j++;
				System.out.println("[Error]@readXls.TA:Unknown CellType" + cellType);
			}
						
			String taKanji = row.getCell(j++).getRichStringCellValue().toString().trim();
			String taEn = row.getCell(j++).getRichStringCellValue().toString().trim().replace("[*]", "");

			String taKana = "";
			if(japaneseDisambiguatedForm2kana.containsKey(taKanji)){
				taKana = japaneseDisambiguatedForm2kana.get(taKanji);
			}else if(japaneseEquivalentForm2kana.containsKey(taKanji)){
				taKana = japaneseEquivalentForm2kana.get(taKanji);
			}else if(en2kana.containsKey(taEn)){
				taKana = en2kana.get(taEn);
			}
			
			List<String> fmaIds = Arrays.asList(row.getCell(j++).getRichStringCellValue().toString().replaceAll(":", "").trim().split("[|]"));
			String fmaOBOName = row.getCell(j++).getRichStringCellValue().toString().trim().replaceAll(":", "");
					
			TABitsEntry template = createTAEntry();
			template.setTaId(taId);
			template.setTaTab(taTab);
			template.setTaEn(taEn);
			template.setTaKanji(taKanji);
			template.setTaKana(taKana);
			
			if(fmaOBOName.contains("NONE")){	// FMAOBONAME="NONE"の場合は、TA英語とfmaobo2の対応を調べる						
				Set<FMAOBOEntry> hits = new HashSet<FMAOBOEntry>();				
				for(String en : Arrays.asList(taEn.split("[;]"))){
					en = en.replaceAll("[*]", "").trim();
					if(fmaobo.contains(en)){ //TA英語がfmaoboにあった場合
						hits.add(fmaobo.getByName(en));
					}
				}
				if(hits.size() == 0){  // FMAにエントリがない場合
					TABitsEntry ent = (TABitsEntry)template.clone();
					ent.setClassification(TABitsEntry.NOFMA);  
					entries.add(ent);					
				}else{
					for(FMAOBOEntry fmaEnt : hits){
						TABitsEntry ent = (TABitsEntry)template.clone();
						ent.setFma(fmaEnt);
						if(fmaIds.contains(fmaEnt.getId())){ 						
							ent.setClassification(TABitsEntry.IDENTICAL); // FMA列に対応するものがあった場合
						}else{
							ent.setClassification(TABitsEntry.NOTIDENTICAL); // FMA列に対応するものがない場合
						}
						entries.add(ent);					
					}
				}
			}else{  // FMAOBONAME="NONE"以外のままはそのままコピーする
				for(String fmaId : fmaIds){
					TABitsEntry ent = (TABitsEntry)template.clone();
					ent.setClassification(TABitsEntry.ORIGINAL);
					if(fmaobo.contains(fmaId)){
						ent.setFma(fmaobo.getById(fmaId));
						ent.setClassification(TABitsEntry.ORIGINAL);
					}else{
						ent.setClassification(TABitsEntry.NOFMAOBO2);
						System.out.println("[Warning]@TABits.readXLs:" + fmaId + ":" + ent.getTaEn() + " is not found in fmaobo2");
					}
					
					if(!edit.isEmpty()){
						ent.setEdit(edit);
					}
					
					entries.add(ent);
				}		
			}	
		}
	}
		
	public void readTerminologiaAnatomicaJaponica13ed() throws Exception {
		FileInputStream is = new FileInputStream(E2JAFILE);
		InputStreamReader in = new InputStreamReader(is, "MS932");
		BufferedReader br = new BufferedReader(in);
		String line = br.readLine(); // read header
						
		while ((line = br.readLine()) != null) {
			if(line.startsWith("#")){ continue; }
			String[] data = Pattern.compile(",").split(line);
			String en = data[1].trim();
			String japaneseEquivalentForm = data[2].trim().replaceAll("；",";");
			String japaneseDisambiguatedForm = data[3].trim().replaceAll("；",";");
			String kana = data[4].trim();
			en2kana.put(en, kana);
			japaneseEquivalentForm2kana.put(japaneseEquivalentForm, kana);
			japaneseDisambiguatedForm2kana.put(japaneseDisambiguatedForm, kana);
		}
		
		br.close();
		in.close();
		is.close();
	}
	
	
	/**
	 * 
	 * @param outfile
	 * @throws Exception
	 */
	public void export() throws Exception {	
		FileOutputStream os = new FileOutputStream(OUTFILE);
		OutputStreamWriter out = new OutputStreamWriter(os, "MS932");		
		BufferedWriter bw = new BufferedWriter(out);				
				
		bw.write("#TA_ID\tTAB\tENAME\tKANJI\tKANA\tFMA_ID\tFMA_En\tTYPE\n");
		
		for(TABitsEntry taEnt : this.getEntries()){
			bw.write(taEnt.getTaId() + "\t");
			bw.write(taEnt.getTaTab() + "\t");
			bw.write(taEnt.getTaEn() + "\t");
			bw.write(taEnt.getTaKanji() + "\t");
			bw.write(taEnt.getTaKana() + "\t");

			FMAOBOEntry fmaEnt = taEnt.getFma();
			if(fmaEnt != null){
				bw.write(fmaEnt.getId() + "\t" + fmaEnt.getName() + "\t");
			}else{
				bw.write("\t\t");
			}
			bw.write(taEnt.getClassification() + "\n");
		}
		
		bw.close();
		out.close();
		os.close();				
	}		

	
	
	/**
	 * FMAID->{TA_ID, TA_ID}の対応表をつくる
	 */
/**
	private void makeFmaId2TaId(){
		for(TABitsEntry ent : entries){
			if(ent.getFma() != null){
				addOne2N(this.taId2FmaId, ent.getTaId(), ent.getFma().getId());
			}
		}
		for(String taId : this.taId2FmaId.keySet()){
			if(taId2FmaId.get(taId).size() > 1){
				System.out.println(taId + "->" + Bp3dUtility.join(taId2FmaId.get(taId), "/"));
			}
		}
	}	
**/
	
	/**
	 * En->{TA_ID, TA_ID}の対応表をつくる
	 */	
/**
	private void makeEn2TaId(){
		for(TABitsEntry ent : entries){
			addOne2N(this.en2Entry, ent.getTaEn(), ent.getTaId());
		}
		for(String en : this.en2Entry.keySet()){
			if(en2Entry.get(en).size() > 1){
				System.out.println(en + "->" + Bp3dUtility.join(en2Entry.get(en), "/"));
			}
		}
	}
**/
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {		
		TABits ta = new TABits();
//		ta.makeFmaId2TaId();	
//		ta.makeEn2TaId();
		ta.export();
	}
}
