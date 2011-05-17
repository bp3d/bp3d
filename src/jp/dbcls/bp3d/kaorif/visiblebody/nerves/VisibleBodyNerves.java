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
package jp.dbcls.bp3d.kaorif.visiblebody.nerves;

import java.io.*;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import jp.dbcls.bp3d.*;
import jp.dbcls.bp3d.fma.*;
import jp.dbcls.bp3d.ta.*;
import jp.dbcls.bp3d.util.Bp3dUtility;
import jp.dbcls.bp3d.kaorif.Abbrev;

/**
 * visibleBodyの対応表(http://www.visiblebody.com/downloads/vb_release_notes_vessels.txt)を読み込み
 * TA, FMAとのマッピングを行う
 * 
 * @author ag
 *
 */
public class VisibleBodyNerves {
	final String INFILE = Bp3dProperties.getString("bp3d.datadir") + 
		"/kaorif/termlist/visibleBody/vb_release_notes.txt";
	final String MANUALLY_MAPPED = Bp3dProperties.getString("bp3d.datadir") + 
		"/kaorif/termlist/visibleBody/nervousSystem/manuallyMapped.xls";		
	
	List<VisibleBodyEntry> entries;
	List<VisibleBodyManuallyMapped> manuallyMapped;
	
	Map<String, VisibleBodyEntry> fmaId2entry;
		
	Map<String, Set<String>> memberOf;
	TA ta;
		
	Bp3d bp3d;
	FMAOBO fmaobo;
	FMA fma;
	Abbrev abbrev;
	
	public VisibleBodyNerves() throws Exception{
		this.entries = new ArrayList<VisibleBodyEntry>();
		this.manuallyMapped = new ArrayList<VisibleBodyManuallyMapped>();
		this.fmaId2entry = new HashMap<String, VisibleBodyEntry>();		
		this.memberOf = new HashMap<String, Set<String>>();
		this.fmaobo = new FMAOBO();
		this.fma = new FMA(fmaobo);
		this.ta = new TA(fmaobo);
		this.abbrev = new Abbrev();

		readManuallyMapped();		
		readFile();
		
		for(VisibleBodyEntry vbEnt : this.entries){
			findFMAEntry(vbEnt);
			findTAEntry(vbEnt);
		}		
	}

	/**
	 * 全FMAIDの集合を返す
	 * @return
	 */
	public Set<String> getAllFMAIds(){
		return this.fmaId2entry.keySet();
	}
	
	/**
	 * termをもつエントリが含まれるか判定する	
	 * @param fmaId
	 * @return
	 */
	public boolean contains(String term){		
		return this.fmaId2entry.containsKey(fmaobo.get(term));
	}
		
	public FMAOBO getFmaobo() {
		return fmaobo;
	}
	
	public List<VisibleBodyEntry> getEntries() {
		return entries;
	}

	/**
	 * termからエントリを取得する
	 * @param term
	 * @param av
	 * @return
	 */
	public VisibleBodyEntry getEntry(String term) {
		for(VisibleBodyEntry vbe : entries){
			if(vbe.getName().equalsIgnoreCase(term)){
				return vbe;
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param term
	 * @param av
	 * @return
	 */
	public VisibleBodyManuallyMapped getManuallyMappedEntry(String term, String av) {
		for(VisibleBodyManuallyMapped mm : this.manuallyMapped){							
			if(mm.getOriginal().equalsIgnoreCase(term) 
					&& mm.getAv().equals(av)){
				return mm;
			}
		}
		
		return null;
	}

	
	/**
	 * FMAにマッチするようにNameをいじる(ルールベース)
	 * @param Name
	 */
	public String replaceName(String name){
		/**  
		 * CN 05 (V) Trigeminal, R -> Trigeminal, R
		 */		
		if(name.startsWith("CN") || name.startsWith("cn")){
			name = name.substring(name.indexOf(")") + 2);
		}

		/** 
		 * C04-C08 Dorsal root ganglia (cervical), L
		 * -> Dorsal root ganglia (cervical), L
		 */
		name = name.replaceFirst("^C0[0-9-C]+ ", "");	
		
		/** 
		 * Dorsal root ganglia (cervical), L
		 * -> Dorsal root ganglia, L
		 */		
		name = Bp3dUtility.truncateParenthesis(name);
				
		return name;
	}
	
	/**
	 * 手作業でマッピングしたリスト(manuallyMapped.txt)を読み込む
	 * @throws Exception
	 */
	public void readManuallyMapped() throws Exception {
		POIFSFileSystem filein = new POIFSFileSystem(new FileInputStream(
				this.MANUALLY_MAPPED));
		HSSFWorkbook wb = new HSSFWorkbook(filein);
		HSSFSheet sheet = wb.getSheet("manuallyMapped");

		for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
			HSSFRow row = sheet.getRow(i);

			HSSFCell cell = null;
			
			String original = "";
			cell = row.getCell(0);
			if (cell != null) {
				original = cell.getRichStringCellValue().toString().trim();
			}

			String av = "";
			cell = row.getCell(1);
			if (cell != null) {
				av = cell.getRichStringCellValue().toString().trim();
			}
		
			String renamed = "";
			cell = row.getCell(2);
			if (cell != null) {
				renamed = cell.getRichStringCellValue().toString().trim();
			}
			
			String remark = "";
			for(int j = 3; j < row.getLastCellNum(); j++){
				cell = row.getCell(j);
				if (cell != null) {
					remark += cell.getRichStringCellValue().toString().trim() + "\t";					
				}
			}									
			
			VisibleBodyManuallyMapped mm = new VisibleBodyManuallyMapped();
			mm.setOriginal(original);
			mm.setAv(av);
			mm.setRenamed(renamed);
			mm.setRemark(remark);
			
			this.manuallyMapped.add(mm);
			
		}
	}
	
	/**
	 * vb_release_notes.txtを読み込む
	 * @throws Exception
	 */
	public void readFile() throws Exception {
		FileInputStream is = new FileInputStream(this.INFILE);
		InputStreamReader in = new InputStreamReader(is, "MS932");
		BufferedReader br = new BufferedReader(in);
		String line;

		/** Maleがくるまで読み飛ばす **/
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if(line.startsWith("Male")){
				line = br.readLine();
				if(line.startsWith("--------------")){ break; }
			}
		}

		/** Nervous Systemがくるまで読み飛ばす **/
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if(line.startsWith("Nervous System")){
				break;
			}
		}

		/** Centralがくるまで読み飛ばす **/
		while ((line = br.readLine()) != null) {
			line = line.trim();
			if(line.startsWith("Central")){
				break;
			}
		}
		
		boolean isPeripheral = false;
		
		while ((line = br.readLine()) != null) {									
			line = line.replace(".", ","); // . R -> , R
			
			if(line.startsWith("#")){ continue; }
			
			String nameOrg = line.trim();						

			/** Peripheralフラグを立てる **/
			if(nameOrg.equals("Peripheral")){
				isPeripheral = true;
			}
						
			/** Eyeの直前まで読み込む **/
			if(nameOrg.equals("Eyes")){
				break;
			}
			
			int indent = line.indexOf(nameOrg);
			String name = nameOrg;
			String modifier = "";
			String lr = "";
			String coreName = "";
									
			/** 手作業でマッピングしたFMAに置換**/
//			VisibleBodyManuallyMapped mm = getManuallyMappedEntry(nameOrg, av);
			VisibleBodyManuallyMapped mm = null;
			if(mm != null){
//				System.out.println("hit getManuallyMappedEntry=" + nameOrg + "-" + av);
				coreName = mm.getRenamed();
			}else{
				/** FMAにマッチするようにNameをいじる(ルールベース) **/
				name = replaceName(name);								

				/** すべて小文字にする**/
				name = name.toLowerCase();
				
				/** 末尾の,L/Rを切り取り、left/rightフラグをたてる **/
				if(name.endsWith(", r")|| name.endsWith(", right side")){
					lr = "right";
					coreName = name.replaceFirst(", r$", "");
					coreName = coreName.replaceFirst(", right side$", "");
				}else if(name.endsWith(", l") || name.endsWith(", left side")){
					lr = "left";
					coreName = name.replaceFirst(", l$", "");
					coreName = coreName.replaceFirst(", left side$", "");
				}else{
					coreName = name;
				}
								
				/*** nerve　を最後に追加 **/
				if(isPeripheral && !coreName.endsWith("nerve") 
						&& !coreName.endsWith("nerves")){
					coreName += " nerve";
				}				
			}					
			
			/** 
			 * 	VisibleBodyEntryを作成して、entriesリストに入れる
			 */
			VisibleBodyEntry thisEntry = getEntry(name);
			
			/**
			 * en2entryハッシュに存在しない場合、新規にVisibleBodyEntryオブジェクト作成
			 */
			if(thisEntry == null){
				thisEntry = new VisibleBodyEntry();
				thisEntry.setName(nameOrg);
				thisEntry.setCoreName(coreName);
				thisEntry.setIndent(indent);
				thisEntry.setLeftRight(lr.trim());
				thisEntry.setModifier(modifier.trim());
				thisEntry.setManuallyMapped(mm);
			}
			
			this.entries.add(thisEntry);
		}
		
		br.close();
		in.close();
	}
			
	/**
	 * デバッグ出力つきfmaobo検索
	 * @param query
	 * @return
	 */
	public boolean containsFMAOBODebug(String query){
		if(fmaobo.contains(query)){
//			System.out.println("containsFMAOBODebug=true:" + query);
			return true;
		}else{
//			System.out.println("containsFMAOBODebug=false:" + query);
			return false;			
		}
	}

	public boolean containsFMAOBODebug(String query, VisibleBodyEntry vbe){
		if(fmaobo.contains(query)){
//			System.out.println("containsFMAOBODebug=true:" + query);
			return true;
		}else{
//			System.out.println("containsFMAOBODebug=false:" + query + "-" + vbe.getCoreName());
			return false;			
		}
	}

	/**
	 * FMAEntryを直接fma_obo2.txtから探して、VisibleBodyEntry.fmaにセットする
	 * @param vbEnt
	 */
	public void findFMAEntry(VisibleBodyEntry vbEnt){		
		/** 
		 * 0. ManuallyMapped.xlsに定義されている場合
		 *
		 * **/
		VisibleBodyManuallyMapped mm = vbEnt.getManuallyMapped();
		if(mm != null){
			String query = mm.getRenamed();
//			System.out.println("Manually Mapped=" + query);
			if(containsFMAOBODebug(query)){
				vbEnt.setFma(fmaobo.getByName(query));
				this.fmaId2entry.put(fmaobo.getByName(query).getId(), vbEnt);
			}
			vbEnt.setExactMatch(true);
			return;
		}
								
		/** 
		 * 1. ManuallyMapped.xlsに定義されていない場合
		 *
		 * **/
		{
			String coreName = vbEnt.getCoreName();
			
			/** left/rightを先頭につけて探す**/
			if(!vbEnt.getLeftRight().equals("")){
				String query = vbEnt.getLeftRight() + " " + coreName;
						
				if(containsFMAOBODebug(query)){
					vbEnt.setFma(fmaobo.getByName(query));
					this.fmaId2entry.put(fmaobo.getByName(query).getId(), vbEnt);
					vbEnt.setExactMatch(true);
					return;
				}
			}
		
			/** left/rightをつけずに探す**/
			{
				String query = coreName;
		
				if(containsFMAOBODebug(query)){
					vbEnt.setFma(fmaobo.getByName(query));
					this.fmaId2entry.put(fmaobo.getByName(query).getId(), vbEnt);
					
					if(vbEnt.getLeftRight().equals("")){ // そもそもleft/rightが付いていない場合
						vbEnt.setExactMatch(true);
					}else{
						vbEnt.setExactMatch(false);
					}
			
					return;
				}
			}
		}
	}


	/**
	 * TAを取得する 
	 * @param vbEnt
	 */
	public void findTAEntry(VisibleBodyEntry vbEnt){
		FMAOBOEntry fmaEnt = vbEnt.getFma();
		if (fmaEnt != null) {
			Set<TAEntry> taEnt = ta.getTAByFmaId(fmaEnt.getId());

			/** 対応するTAがない場合は一階層上のエントリを調べる **/
			if (taEnt.size() == 0) {
				taEnt = ta.getTAByFmaId(fmaEnt.getIsA().getId());
			}

			vbEnt.setTa(taEnt);
		}
	}
	
	/**
	 * ファイルに結果を出力する
	 * @param outfile
	 * @throws Exception
	 */
	public void export(String outfile) throws Exception {	
		FileOutputStream os = new FileOutputStream(outfile);
		OutputStreamWriter out = new OutputStreamWriter(os, "MS932");		
		BufferedWriter bw = new BufferedWriter(out);
		
//		System.out.println("Export Num Of Visible Bodies=" + getEntries().size());
		
		bw.write("INDENT\tNAME\tCORENAME\t\tTAID\tTAKanji\tFMAID\tFMAEN\tFMAEN\tREMARK\n");		
		
		for(VisibleBodyEntry vbEnt : getEntries()){
			bw.write(vbEnt.getIndent() + "\t");
			bw.write(vbEnt.getName() + "\t");

			/** corename **/
			bw.write(vbEnt.getCoreName() + "\t");
			
			/** TA **/
			List<String> taIds = new ArrayList<String>();
			List<String> taKanjis = new ArrayList<String>();
			for(TAEntry taEnt : vbEnt.getTa()){
				taIds.add(taEnt.getTaId());
				taKanjis.add(taEnt.getTaKanji());
			}

			bw.write(Bp3dUtility.join(taIds, "/") + "\t");
			bw.write(Bp3dUtility.join(taKanjis, "/") + "\t");

			bw.write("\t");
						
			FMAOBOEntry fmaEnt = vbEnt.getFma();					

			
			/** fmaid, fmaEn **/			
			if(fmaEnt == null){			
			}else{
				bw.write(fmaEnt.getId() + "\t");
				String en = fmaEnt.getName();
				bw.write(en + "\t");
				bw.write(abbrev.getAbbrev(en) + "\t");
			}
						
			bw.write("\n");
		}
		
		bw.close();
		out.close();
		os.close();
	}
	
	
	public void display(){
		for(VisibleBodyEntry vbe : entries){
			for(int i = 0; i < vbe.getIndent() + 1; i++){
				System.out.print(" ");
			}
//			System.out.println(vbe.getName() + " memberOf " + vbe.getUpper().getName());
			System.out.println(vbe.getName() + " memberOf " + memberOf.get(vbe.getName()));
		}
	}

	public void test(){		
		VisibleBodyEntry vbEnt = getEntry("Gonadal (testicular, internal spermatic), R");
		if(vbEnt == null){
			System.out.println("null");
		}else{
			System.out.println("test.renamed=" + vbEnt.getManuallyMapped().getRenamed());
		}
		findFMAEntry(vbEnt);
		System.out.println(vbEnt.getFma().getId());
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String termListFile = Bp3dProperties.getString("bp3d.datadir")
			+ "/kaorif/termlist/visibleBody/nervousSystem/termlist.txt";
		
		VisibleBodyNerves vb = new VisibleBodyNerves();
//	vb.display();
//	vb.test();

		vb.export(termListFile);
	}
}
