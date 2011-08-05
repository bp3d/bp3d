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
package jp.dbcls.bp3d.ta;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import jp.dbcls.bp3d.*;
import jp.dbcls.bp3d.fma.*;
import jp.dbcls.bp3d.util.*;
import jp.dbcls.bp3d.ta.bits.*;

/**
 * conf/ta2fma.txtを読み込んで、fmaid->TA情報の対応表をつくる
 * @author ag
 *
 */
public class TA {
	final String INFILE = Bp3dProperties.getString("bp3d.datadir") + 
	Bp3dProperties.getString("bp3d.dataversion") + "/conf/TA/ta2fma.txt";

	final String EXPORTFILE = Bp3dProperties.getString("bp3d.datadir") + 
		Bp3dProperties.getString("bp3d.dataversion") + "/conf/TA/taTree.txt";
	
	List<TAEntry> entries = new ArrayList<TAEntry>();
		
	Map<String, TAEntry> taId2entry = new TreeMap<String, TAEntry>();
	Map<String, Set<TAEntry>> fmaId2entry = new TreeMap<String, Set<TAEntry>>();
	Map<String, Set<FMAOBOEntry>> taId2fmaEntry = new TreeMap<String, Set<FMAOBOEntry>>();

	TATree taTree = new TATree();	
	
	FMAOBO fmaobo;
	
	public TA(FMAOBO fmaobo) throws Exception{
		this.fmaobo = fmaobo;		
		TABits taBits = new TABits();
		taBits.export();		
		readFile();
		indent2MemberOf();
	}
	
	/**
	 * key→{value1, value2}のリストに追加する
	 * @param key
	 * @param value
	 */
	private void addOne2N(Map<String, Set<TAEntry>>links, String key, TAEntry value){
		if(!links.containsKey(key)){
			links.put(key, new HashSet<TAEntry>());
		}
		links.get(key).add(value);
	}	

	/**
	 * key→{value1, value2}のリストに追加する
	 * @param key
	 * @param value
	 */
	private void addOne2N(Map<String, Set<FMAOBOEntry>>links, String key, FMAOBOEntry value){
		if(!links.containsKey(key)){
			links.put(key, new HashSet<FMAOBOEntry>());
		}
		links.get(key).add(value);
	}	
	
	/**
	 * fmaIDからTAのエントリを取得する
	 * @param fmaId
	 * @return
	 */
	public Set<TAEntry> getTAByFmaId(String fmaId){
		Set<TAEntry> ret = new HashSet<TAEntry>();
		if(fmaId2entry.containsKey(fmaId)){
			ret.addAll(fmaId2entry.get(fmaId));
		}
		return ret;
	}

	/**
	 * TAIDからfmaのエントリを取得する
	 * @param fmaId
	 * @return
	 */
	public Set<FMAOBOEntry> getFMAByTAId(String taId){
		Set<FMAOBOEntry> ret = new HashSet<FMAOBOEntry>();
		if(taId2fmaEntry.containsKey(taId)){
			ret.addAll(taId2fmaEntry.get(taId));
		}
		return ret;
	}
	
	/**
	 * ID(FMAID/TAID)のTAエントリが存在するか
	 * @param term
	 * @return
	 */
	public boolean contains(String id){
		return (fmaId2entry.get(id) == null && 
				taId2entry.get(id) == null ? false : true);
	}
		
	/**
	 * TA->FMAのリスト(TAのIDの昇順)を返す
	 * @return
	 */
	public List<TAEntry> getEntries(){
		return entries;
	}
		
	public TAEntry createTAEntry() throws Exception {
		return new TAEntry();
	}
			
	public void readFile() throws Exception {
		FileInputStream is = new FileInputStream(this.INFILE);
		InputStreamReader in = new InputStreamReader(is, "MS932");
		BufferedReader br = new BufferedReader(in);
		String line;
		
		while ((line = br.readLine()) != null) {
			if(line.startsWith("#")){ continue; }
			TAEntry ent = createTAEntry();
			String[] data = Pattern.compile("\t").split(line);
			String taId = data[0].trim();
			ent.setTaId(taId);
			double taTab = Double.parseDouble(data[1].trim().replaceAll(">", ""));
			ent.setTaTab(taTab);
			ent.setTaEn(data[2].trim());
			ent.setTaKanji(data[3].trim());
			ent.setTaKana(data[4].trim());

			this.taId2entry.put(taId, ent);
			this.entries.add(ent);
			
			String fmaId = data[5].trim();
						
			if(!fmaId.equals("null")){
				addOne2N(fmaId2entry, fmaId, ent);
				addOne2N(taId2fmaEntry, ent.getTaId(), fmaobo.getById(fmaId));
			}
		}
				
		br.close();
		in.close();
		is.close();
	}

	/**
	 * TAのインデント情報をmember-of階層に取り込む
	 */
	private void indent2MemberOf(){		
		List<TAEntry> entries = getEntries();
		
		/** entriesの先頭がROOT_PARTである、human body **/
		TAEntry parentTA = entries.get(0);	
		entries.remove(0);
				
		TAEntry prevTA = parentTA.clone();

		Stack<TAEntry> ancestors = new Stack<TAEntry>();
								
		for(TAEntry ent : entries){
			double indent = ent.getTaTab();
			double indentPrev = prevTA.getTaTab();
						
			if(indentPrev > indent){
				do{
					parentTA = ancestors.pop();
				}while(parentTA.getTaTab() >= indent);
				ancestors.push(parentTA);
			}else if(indentPrev < indent){
				parentTA = prevTA;
				ancestors.push(parentTA);
			}
			
			for(FMAOBOEntry fmaEnt : getFMAByTAId(ent.getTaId())){
				for(FMAOBOEntry fmaParent : getFMAByTAId(parentTA.getTaId())){
					/** parent, childが同一のFMAIDになった場合、member-of階層に加えない **/
					if(fmaEnt == fmaParent){
						System.out.println("[Warning]@TA:Ignore an indent because the FMAID of a child and a parent is identical:" + 
								fmaEnt.getId() + ",child=" + ent.getTaId() + ",parent=" + parentTA.getTaId());
						continue;
					}
					taTree.addMemberOf(fmaEnt.getId(), fmaParent.getId());
					taTree.addReverseMemberOf(fmaParent.getId(), fmaEnt.getId());
				}
			}

			prevTA = ent;
		}
	}
	
	public TATree getTree(){
		return taTree;
	}
	
	public void display(){
		for(TAEntry tfe : entries){
			String taId = tfe.getTaId();
			System.out.print(taId + ",");
			System.out.print(tfe.getTaTab() + ",");
			System.out.print(tfe.getTaKanji() + ",");
			System.out.print(tfe.getTaKana() + ",");
			System.out.print(tfe.getTaEn() + ",");
			
			Set<String> fmaIds = new HashSet<String>();

			for(FMAOBOEntry fmaEnt : getFMAByTAId(taId)){
				fmaIds.add(fmaEnt.getId());
			}
			System.out.print(Bp3dUtility.join(fmaIds, "|"));

			Set<String> fmaEns = new HashSet<String>();
			for(FMAOBOEntry fmaEnt : this.getFMAByTAId(taId)){
				fmaEns.add(fmaEnt.getName());
			}
			System.out.println(Bp3dUtility.join(fmaEns, "|"));
		}
	}	
	
	private List<String> displayTAList(Set<String> fmaIds){
		List<String> ret = new ArrayList<String>();
		for(String fmaId : fmaIds){
			ret.add(fmaobo.getById(fmaId).getName());
		}
		return ret;
	}

	public void export(){				
		for(String fmaId : fmaId2entry.keySet()){
			if(fmaId2entry.get(fmaId).size() > 1){
				for(TAEntry taEnt : fmaId2entry.get(fmaId)){
				System.out.println(fmaId + "\t" + taEnt.getTaId() + "\t" 
						+ taEnt.getTaEn() + "\t" + taEnt.getTaKanji());
				}
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		TA ta = new TA(new FMAOBO());
		System.out.println(ta.contains("FMA61906"));
//		ta.export();
		ta.display();
		TATree tree = ta.getTree();
		String ROOT_PART = "FMA20394"; // human body
		System.out.println("parant of ROOT_PART=" + tree.getParents(ROOT_PART));				
		System.out.println("chilren of ROOT_PART=" + ta.displayTAList(tree.getChildren(ROOT_PART)));				
	}
}
