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
package jp.dbcls.bp3d.kaorif.visiblebody.vessels;

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
 * visibleBodyの対応表(http://www.visiblebody.com/downloads/vb_release_notes.txt)を読み込み
 * TA, FMAとのマッピングを行う
 * 
 * @author ag
 */
public class VisibleBodyVessels {
	final String INFILE = Bp3dProperties.getString("bp3d.datadir") + 
		"/kaorif/termlist/visibleBody/circulatorySystem/vb_release_notes_vessels.txt";
	final String ORDERED_BY_KAORIF = Bp3dProperties.getString("bp3d.datadir") + 
		"/kaorif/termlist/visibleBody/circulatorySystem/visibleBodyCirculartorySystem01.xls";	
	final String MANUALLY_MAPPED = Bp3dProperties.getString("bp3d.datadir") + 
		"/kaorif/termlist/visibleBody/circulatorySystem/manuallyMapped.xls";
	
	List<VisibleBodyEntry> entries;
	List<VisibleBodyManuallyMapped> manuallyMapped;
	
	Map<String, VisibleBodyEntry> fmaId2entry;
		
	Map<String, Set<String>> memberOf;
	TA ta;
		
	Bp3d bp3d;
	FMAOBO fmaobo;
	FMA fma;
	Abbrev abbrev;
	
	public VisibleBodyVessels() throws Exception{
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
		readOrderedByKaorif();
		
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
		if(fmaobo.contains(term)){
			return fmaId2entry.containsKey(fmaobo.get(term).getId());			
		}else{
			return false;
		}
	}
	
	public FMAOBO getFmaobo() {
		return fmaobo;
	}
	
	public List<VisibleBodyEntry> getEntries() {
		return entries;
	}

	/**
	 * termとav(artery/vein)をからエントリを取得する
	 * @param term
	 * @param av
	 * @return
	 */
	public VisibleBodyEntry getEntry(String term, String av) {
		for(VisibleBodyEntry vbe : entries){
			if(vbe.getName().equalsIgnoreCase(term) 
					&& vbe.getArteryVein().equals(av)){
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
		name = name.replace("internal iliac, anterior branch", "internal iliac, anterior division");
		name = name.replace("internal iliac, posterior branch", "internal iliac, posterior division");
		name = name.replace("thoracoacromial", "thoraco-acromial");			
				
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
	 * vb_release_notes_vessels.txtを読み込む
	 * @throws Exception
	 */
	public void readFile() throws Exception {
		FileInputStream is = new FileInputStream(this.INFILE);
		InputStreamReader in = new InputStreamReader(is, "MS932");
		BufferedReader br = new BufferedReader(in);
		String line;

		while ((line = br.readLine()) != null) {
			if(line.startsWith("Female")){
				line = br.readLine();
				if(line.startsWith("--------------")){ break; }
			}
		}

		String av = "";
		while ((line = br.readLine()) != null) {
			line = line.replace(".", ","); // . R -> , R
			
			if(line.startsWith("#")){ continue; }
			
			String nameOrg = line.trim();
			int indent = line.indexOf(nameOrg);
			String name = nameOrg;
			String modifier = "";
			String lr = "";
			String coreName = "";
			
			/** Digestive Systemの直前まで読み込む **/
			if(nameOrg.equals("Digestive System")){
				break;
			}
						
			/** Arteries/Veinsのis-a treeの下に入っているときは、フラグを立てる **/
			if(nameOrg.equals("Arteries")){
				av = "arteries";
			}else if(nameOrg.equals("Veins")){
				av = "veins";
			}
						
			/** 手作業でマッピングしたFMAに置換**/
			VisibleBodyManuallyMapped mm = getManuallyMappedEntry(nameOrg, av);
			if(mm != null){
//				System.out.println("hit getManuallyMappedEntry=" + nameOrg + "-" + av);
				coreName = mm.getRenamed();
			}else{
				/** すべて小文字にする**/
				name = name.toLowerCase();
			
				/** FMAにマッチするようにNameをいじる(ルールベース) **/
				name = replaceName(name);
			
				/** 末尾の,L/Rを切り取り、left/rightフラグをたてる **/
				if(name.endsWith(", r")){
					lr = "right";
					coreName = name.replaceFirst(", r$", "");
				}else if(name.endsWith(", l")){
					lr = "left";
					coreName = name.replaceFirst(", l$", "");
				}else{
					coreName = name;
				}

				/** 修飾語句を除く  **/
				String[] tokens = name.split(",");			
				if(lr.length() > 0 && tokens.length == 3){ // Thoracoacromial, acromial branch, Rのような場合
					modifier = tokens[tokens.length - 2].trim();
					coreName = coreName.replace(", " + modifier, "");
//				System.out.println("1:" +  name + "->" + modifier + "->" + coreName);
				}else if(lr.length() == 0 && tokens.length == 2){ // Thoracoacromial, acromial branchのような場合
					modifier = tokens[tokens.length - 1].trim();
					coreName = coreName.replace(", " + modifier, "");
//				System.out.println("2:" +  name + "->" + modifier + "->" + coreName);
				}
			}

			/** 
			 * 	VisibleBodyEntrywを作成して、entriesリストに入れる
			 */
			VisibleBodyEntry thisEntry = getEntry(name, av);
			
			/**
			 * en2entryハッシュに存在しない場合、新規にVisibleBodyEntryオブジェクト作成
			 */
			if(thisEntry == null){
				thisEntry = new VisibleBodyEntry();
				thisEntry.setName(nameOrg);
				thisEntry.setCoreName(coreName);
				thisEntry.setIndent(indent);
				thisEntry.setArteryVein(av.trim());
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
	 * this.ORDERED_BY_KAORIFを読み込み、
	 * 
	 * @throws Exception
	 */
	public void readOrderedByKaorif() throws Exception {
		POIFSFileSystem filein = new POIFSFileSystem(new FileInputStream(
				this.ORDERED_BY_KAORIF));
		HSSFWorkbook wb = new HSSFWorkbook(filein);
		HSSFSheet sheet = wb.getSheet("CirculatorySystem");

		for (int i = 1; i < sheet.getLastRowNum() + 1; i++) {
			HSSFRow row = sheet.getRow(i);
					
			boolean isRequired = row.getCell(0).getBooleanCellValue();
		
			String en = "";
			HSSFCell cell = row.getCell(1);
			if (cell != null) {
				en = cell.getRichStringCellValue().toString().trim();
			}

			String av = "";
			cell = row.getCell(2);
			if (cell != null) {
				av = cell.getRichStringCellValue().toString().trim();
			}
					
			VisibleBodyEntry vbe = getEntry(en, av);
			if(vbe != null){
				vbe.setOrderedByKaorif(isRequired);
			}else{
				System.out.println("readOrderedByKaorif not found=" + en + " " + av);
			}
		}

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
			System.out.println("Manually Mapped=" + query);
			if(containsFMAOBODebug(query)){
				vbEnt.setFma(fmaobo.getByName(query));
				this.fmaId2entry.put(fmaobo.getByName(query).getId(), vbEnt);
			}
			vbEnt.setExactMatch(true);
			return;
		}
						
		/** 
		 * 1. coreNameをリセットして、arteries/veinsを最後につけて探す 
		 * 
		 * **/
		{
			String coreName = vbEnt.getCoreName();
			String modifier = vbEnt.getModifier();
		
			/** artery/veinをつける **/
			if(vbEnt.getArteryVein().equals("arteries") && 
					!coreName.contains("artery")){
				coreName += " artery";
			}else if(vbEnt.getArteryVein().equals("veins") && 
				!coreName.contains("vein")){
				coreName += " vein";
			}

			/** left/rightを先頭につけて探す**/
			if(!vbEnt.getLeftRight().equals("")){
				String query = "";
				
				/** 修飾語を先頭につける **/
				if(modifier.contains("branch") || modifier.contains("division")){ // e.g. anterior branch
					query = modifier + " of " + vbEnt.getLeftRight() + " " + coreName;
				}else if(modifier.length() > 0){
					query = vbEnt.getLeftRight() + " " + modifier + " " + coreName;
				}else{
					query = vbEnt.getLeftRight() + " " + coreName;
				}

				if(containsFMAOBODebug(query, vbEnt)){
					vbEnt.setFma(fmaobo.getByName(query));
					this.fmaId2entry.put(fmaobo.getByName(query).getId(), vbEnt);
					vbEnt.setExactMatch(true);
					return;
				}
				
				/** branch of のleft/rightを先頭につける **/
				if(modifier.contains("branch") || modifier.contains("division")){ // e.g. anterior branch
					query = vbEnt.getLeftRight() + " " + modifier + " of " + coreName;					
				
					if(containsFMAOBODebug(query)){
						vbEnt.setFma(fmaobo.getByName(query));
						this.fmaId2entry.put(fmaobo.getByName(query).getId(), vbEnt);
						vbEnt.setExactMatch(true);
						return;
					}
				}								
			}
		
			/** left/rightをつけずに探す**/
			{
				String query = "";

				/** 修飾語を先頭につける **/
				if(modifier.contains("branch") || modifier.contains("division")){ // e.g. anterior branch
					query = modifier + " of " + coreName;
				}else if(modifier.length() > 0){
					query = modifier + " " + coreName;
				}else{
					query = coreName;
				}
		
				if(containsFMAOBODebug(query)){
					FMAOBOEntry ent = fmaobo.getByName(query);
					this.fmaId2entry.put(fmaobo.getByName(query).getId(), vbEnt);
					vbEnt.setFma(ent);

					if(vbEnt.getLeftRight().equals("")){ // そもそもleft/rightが付いていない場合
						vbEnt.setExactMatch(true);
					}else{						
						query = vbEnt.getLeftRight() + " " + ent.getName();  // left/rightをつけてexact matchをめざす						
						if(containsFMAOBODebug(query)){
							vbEnt.setExactMatch(true);
							this.fmaId2entry.put(fmaobo.getByName(query).getId(), vbEnt);
							vbEnt.setFma(fmaobo.getByName(query));
						}else{
							vbEnt.setExactMatch(false);
						}
					}			
					return;
				}
			}					
		}
		
		/**
		 * 2. Artery/Veinをつけずに探す
		 */
		{
			String coreName = vbEnt.getCoreName();
			String modifier = vbEnt.getModifier();

			/** left/rightを先頭につけて探す**/
			if(!vbEnt.getLeftRight().equals("")){
				String query;

				/** 修飾語を先頭につける **/
				if(modifier.contains("branch") || modifier.contains("division")){ // e.g. anterior branch
					query = modifier + " of " + vbEnt.getLeftRight() + " " + coreName;
				}else if(modifier.length() > 0){
					query = vbEnt.getLeftRight() + " " + modifier + " " + coreName;
				}else{
					query = vbEnt.getLeftRight() + " " + coreName;
				}
						
				if(containsFMAOBODebug(query)){
					vbEnt.setFma(fmaobo.getByName(query));
					this.fmaId2entry.put(fmaobo.getByName(query).getId(), vbEnt);
					vbEnt.setExactMatch(true);
					return;
				}

				/** brach of のleft/rightを先頭につける **/
				if(modifier.contains("branch") || modifier.contains("division")){ // e.g. anterior branch
					query = vbEnt.getLeftRight() + " " + modifier + " of " + coreName;					
				
					if(containsFMAOBODebug(query)){
						vbEnt.setFma(fmaobo.getByName(query));
						this.fmaId2entry.put(fmaobo.getByName(query).getId(), vbEnt);
						vbEnt.setExactMatch(true);
						return;
					}
				}			
			}
		
			/** left/rightをつけずに探す**/
			{
				String query = "";

				/** 修飾語を先頭につける **/
				if(modifier.contains("branch") || modifier.contains("division")){ // e.g. anterior branch
					query = modifier + " of " + coreName;
				}else if(modifier.length() > 0){
					query = modifier + " " + coreName;
				}else{
					query = coreName;
				}
		
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
		
		System.out.println("Export Num Of Visible Bodies=" + getEntries().size());
		
		bw.write("REQUIRED\tNAME\tAV\tEXACT\tTAID\tCORENAME\tFMAID\tFMAEN\tFMAEN\tREMARK\n");		
		
		for(VisibleBodyEntry vbEnt : getEntries()){			
			bw.write(vbEnt.isOrderedByKaorif() + "\t");			
			bw.write(vbEnt.getName() + "\t");
						
			bw.write(vbEnt.getArteryVein() + "\t");
			bw.write(vbEnt.isExactMatch() + "\t");
						
			FMAOBOEntry fmaEnt = vbEnt.getFma();					
			
			/** TAを取得する **/
			List<String> taIds = new ArrayList<String>();
			List<String> taKanjis = new ArrayList<String>();
			for(TAEntry taEnt : vbEnt.getTa()){
				taIds.add(taEnt.getTaId());
				taKanjis.add(taEnt.getTaKanji());
			}

			bw.write(Bp3dUtility.join(taIds, "/") + "\t");
			bw.write(Bp3dUtility.join(taKanjis, "/") + "\t");
			
			VisibleBodyManuallyMapped mm = vbEnt.getManuallyMapped();
			
			/** corename, fmaid, fmaEn **/			
			if(fmaEnt == null){
				bw.write(vbEnt.getCoreName() + "\t");
				bw.write("\t");
				if(mm == null){
//					System.out.println("bbb=" + vbEnt.getName() + "-" + vbEnt.getArteryVein());
					bw.write("\t\t");
				}else{
					String en = mm.getRenamed();
					bw.write(en + "\t");
					bw.write(abbrev.getAbbrev(en) + "\t");
				}
			}else{								
				bw.write(vbEnt.getCoreName() + "\t");
				bw.write(fmaEnt.getId() + "\t");
				String en = fmaEnt.getName();
				bw.write(en + "\t");
				bw.write(abbrev.getAbbrev(en) + "\t");
			}
			
			/** remark **/			
			if(mm != null){
				bw.write(abbrev.getAbbrev(mm.getRemark()) + "\t");
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
		VisibleBodyEntry vbEnt = getEntry("Gonadal (testicular, internal spermatic), R", "arteries");
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
			+ "/kaorif/termlist/visibleBody/circulatorySystem/termlist.txt";
		
		VisibleBodyVessels vb = new VisibleBodyVessels();
//	vb.display();
//				vb.test();
		vb.export(termListFile);
	}
}
