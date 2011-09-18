package jp.dbcls.bp3d;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import jp.dbcls.bp3d.Bp3dEntry;
import jp.dbcls.bp3d.util.StopWatch;

/**
 * ConstructBp3d.javaで作成したbp3d.txt/memberOf.txtを読み込んで、
 * bp3dの便宜的(conventional)ツリーを作成する
 * 
 * @author mituhasi
 * 
 */

public class Bp3d {
	private final String DATADIR = Bp3dProperties
	.getString("bp3d.datadir")
	+ "/" + Bp3dProperties.getString("bp3d.dataversion");
	private String BP3DFILE = DATADIR + "/logs/MakeBp3d0/bp3d.txt";
	private String MOFILE = DATADIR + "/logs/MakeBp3d0/bp3dMemberOf.txt";
			
	/** ID->Bp3dEntry **/
	private SortedMap<String, Bp3dEntry> id2Entry;
	/** English->Bp3dEntry **/
	private SortedMap<String, Bp3dEntry> en2Entry;	
	
	/** children->set of parents **/
	/** parent->Set of children **/
	private Bp3dTree bp3dTree;
	private TraverseBp3d bp3dTraverse;

	/** 開発者（モデラー）向けのデータを出力する場合はtrue **/
	boolean forDeveloper = false;
	
	public Bp3d() throws Exception {
		this.id2Entry = new TreeMap<String, Bp3dEntry>();
		this.en2Entry = new TreeMap<String, Bp3dEntry>();
				
		this.bp3dTree = new Bp3dTree();
		this.bp3dTraverse = new TraverseBp3d(bp3dTree);
		
		/** bp3d.txtを読み込む　**/
		readBp3d();
		
		/** member-of階層を読み込む **/
		readMemberOf();				
	}

	public Bp3d(Boolean forDeveloper) throws Exception {
		this.forDeveloper = forDeveloper;
		this.id2Entry = new TreeMap<String, Bp3dEntry>();
		this.en2Entry = new TreeMap<String, Bp3dEntry>();
				
		this.bp3dTree = new Bp3dTree();
		
		/** bp3d.txtを読み込む　**/
		readBp3d();
		
		/** member-of階層を読み込む **/
		readMemberOf();		
	}
	
	public TraverseBp3d getBp3dTraverse() {
		return bp3dTraverse;
	}

	public void setBp3dTraverse(TraverseBp3d bp3dTraverse) {
		this.bp3dTraverse = bp3dTraverse;
	}

	/**
	 * term(ID/English)からBp3dEntryを得る
	 * @param id
	 * @return
	 */
	public Bp3dEntry getEntry(String term){		
		if(id2Entry.containsKey(term)){
			return id2Entry.get(term);
		}else if(en2Entry.containsKey(term)){
			return en2Entry.get(term);
		}else{
			return null;
		}	
	}

		
	/**
	 * 親エントリが存在するかチェックする
	 * @param id
	 * @return
	 */
	public boolean hasParent(String id){
		return bp3dTree.hasParent(id);
	}

	/**
	 * パーツidのparentsのID集合を取得する
	 * @param id
	 * @return
	 */
	public Set<String> getParents(String id){
		return bp3dTree.getParents(id);
	}

	/**
	 * パーツidのparentsの英語名集合を取得する
	 * @param id
	 * @return
	 */
	public Set<String> getParentsNames(String id){
		Set<String> ret = new HashSet<String>();
		for(String pid : getParents(id)){
			ret.add(pid);
		}
		return ret;
	}

	/**
	 * 子エントリが存在するかチェックする
	 * @param id
	 * @return
	 */
	public boolean hasChild(String id){
		return bp3dTree.hasChild(id);
	}

	/**
	 * パーツidのchildrenのID集合を取得する
	 * @param id
	 * @return
	 */
	public Set<String> getChildren(String id){		
		return bp3dTree.getChildren(id);
	}

	/**
	 * パーツidのchildrenの英語名集合を取得する
	 * @param id
	 * @return
	 */
	public Set<String> getChildrenNames(String id){
		Set<String> ret = new HashSet<String>();
		for(String cid : getChildren(id)){
			ret.add(cid);
		}
		return ret;
	}

	/**
	 * term(ID/English)がBp3dに含まれているか判定する
	 * @param id
	 * @return
	 */
	public boolean contains(String term){		
		return (getEntry(term) == null ? false : true);
	}
		
	/**
	 * 全IDを返す
	 * 
	 * @return
	 */
	public Set<String> getAllIds(){		
		return id2Entry.keySet();
	}

	/**
	 * 全エントリを返す
	 * TAIDの昇順にソート、TAIDがないときはFMAIDがソートキー
	 * @return
	 */
	public List<Bp3dEntry> getAllEntries(){
		List<Bp3dEntry> ret = new ArrayList<Bp3dEntry>();
		for(Bp3dEntry bp3dEnt : id2Entry.values()){
			ret.add(bp3dEnt);
		}
		Collections.sort(ret, new Bp3dEntryComparator());
		
		return ret;
	}
	/**
	 * 全memberOfを返す
	 * @throws Exception
	 */
	public Map<String, Set<String>> getMemberOfs(){		
		return bp3dTree.getMemberOfs();
	}

	/**
	 * 全reverseMemberOfを返す
	 * @return
	 */
	public Map<String, Set<String>> getReverseMemberOfs(){		
		return bp3dTree.getReverseMemberOfs();
	}
		
	/**
	 * entを構成するOBJファイルのID集合を返す
	 * COMPOSITE_PRIMITIVEやCOMPOSITE_ANONYMOUSのパーツがOBJとして実物化した後なので
	 * そのようなパーツは展開しない。
	 * 
	 * @param ent
	 * @return
	 * @throws Exception
	 */
	public Set<String> getPrimitiveOBJIds(Bp3dEntry ent) throws Exception {		
		Set<String> ret = new HashSet<String>();
		
		String id = ent.getId();
		
		if(ent.isCompleted() || ent.isCompletedAnonymous() 
				|| ent.isCompositeAnonymous()){
			ret.add(id);
		}else{
			for(String cId : bp3dTree.getChildren(id)){
				ret.addAll(getPrimitiveOBJIds(getEntry(cId)));
			}
		}
		
		return ret;		
	}
	
	/**
	 * entを構成するOBJファイルのFMA Preferred　Name集合を返す
	 * @param ent
	 * @return
	 * @throws Exception
	 */
	public Set<String> getPrimitiveOBJNames(Bp3dEntry ent) throws Exception {		
		Set<String> ret = new TreeSet<String>();
		for(String id : getPrimitiveOBJIds(ent)){
			ret.add(getEntry(id).getEn());
		}
		return ret;				
	}
	
	
	/**
	 * bp3dEntが示すパーツが無名パーツを子供に持つか判定する
	 * @param bp3dEnt
	 * @return
	 */
	public boolean hasAnonymousParts(Bp3dEntry bp3dEnt){
		for(String cId : this.bp3dTree.getChildren(bp3dEnt.getId())){
			if(cId.startsWith("ANON")){
				return true;
			}
		}
		
		return false;		
	}


	/**
	 * bp3dEntと関連するmember-ofを削除する
	 * @param bp3dEnt
	 */
	public void remove(Bp3dEntry bp3dEnt){
		String id = bp3dEnt.getId();		
		id2Entry.remove(id);		
		en2Entry.remove(bp3dEnt.getEn());		

		for(String cid : this.bp3dTree.getChildren(id)){
			bp3dTree.removeMemberOf(cid, id);
			bp3dTree.removeReverseMemberOf(id, cid);
		}

		for(String pid : this.bp3dTree.getParents(id)){
			bp3dTree.removeMemberOf(id, pid);
			bp3dTree.removeReverseMemberOf(pid, id);
		}
	}
		
	/**
	 * bp3d.txtを読み込む
	 * @throws Exception
	 */
	public void readBp3d() throws Exception {
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		
		if(this.forDeveloper == true){
			BP3DFILE = DATADIR + "/logs/MakeBp3d0/bp3dDevel.txt";
		}else{
			BP3DFILE = DATADIR + "/logs/MakeBp3d0/bp3d.txt";
		}		
		
		FileInputStream is = new FileInputStream(BP3DFILE);
		InputStreamReader in = new InputStreamReader(is, "MS932");
		BufferedReader br = new BufferedReader(in);
		String line = br.readLine(); // read header
				
		while ((line = br.readLine()) != null) {
			if(line.startsWith("#")){ continue; }
			String[] data = Pattern.compile("\t").split(line);
			String id = data[0].trim();
			String en = data[1].trim();
			String kanji = data[2].trim();			
			String kana = data[3].trim();			
			String taId = data[4].trim();			
			Date lastUpdate = df.parse(data[5].trim());			
			Bp3dEntryType type = Bp3dEntryType.valueOf(data[6].trim());
			String objPath = data[7].trim();
			
			Bp3dEntry ent = new Bp3dEntry();
			ent.setId(id);
			ent.setEn(en);
			ent.setKanji(kanji);
			ent.setKana(kana);
			ent.setTaId(taId);
			ent.setType(type);
			ent.setLastUpdate(lastUpdate);
			ent.setObjPath(objPath);
			
			this.id2Entry.put(id, ent);
			this.en2Entry.put(en, ent);
		}
		
		br.close();
		in.close();
		is.close();
	}
	
	/**
	 * memberOf.txtを読み込む
	 * @throws Exception
	 */
	public void readMemberOf() throws Exception {			
		if(this.forDeveloper == true){
			MOFILE = DATADIR + "/logs/MakeBp3d0/bp3dMemberOfDevel.txt";
		}else{
			MOFILE = DATADIR + "/logs/MakeBp3d0/bp3dMemberOf.txt";
		}
		
		FileInputStream is = new FileInputStream(MOFILE);
		InputStreamReader in = new InputStreamReader(is, "MS932");
		BufferedReader br = new BufferedReader(in);
		String line = br.readLine(); // read header
					
		while ((line = br.readLine()) != null) {
			String[] data = Pattern.compile("\t").split(line);
			String child = data[0].trim();
			String parent = data[1].trim();
			String cId = getEntry(child).getId();
			String pId = getEntry(parent).getId();
			
			bp3dTree.addMemberOf(cId, pId);
			bp3dTree.addReverseMemberOf(pId, cId);
		}
		
		br.close();
		in.close();
		is.close();
	}
		
	/**
	 * parts listとmember ofリストを出力する
	 * @throws Exception
	 */
/**	
	public void export() throws Exception {
		FileOutputStream fos;
		OutputStreamWriter out;
		BufferedWriter bw;

		String logFile = DATADIR + "/logs/MakeBp3d0/bp3d.txt";

		fos = new FileOutputStream(logFile, false);
		out = new OutputStreamWriter(fos, "MS932");
		bw = new BufferedWriter(out);
					
		bw.write("id" + "\t" + "en" + "\n");

		for (String id : this.getAllIds()) {
			
		}

		bw.close();
		out.close();
		fos.close();

		logFile = DATADIR + "/logs/MakeBp3d0/memberOf.txt";

		fos = new FileOutputStream(logFile, false);
		out = new OutputStreamWriter(fos, "MS932");
		bw = new BufferedWriter(out);
					
		bw.write("child" + "\t" + "parent" + "\n");

		for (String child : this.getMemberOfs().keySet()) {
			for (String parent : this.getMemberOfs().get(child)) {
				
			}
		}

		bw.close();
		out.close();
		fos.close();			
	}
**/
	
	/**
	 * テストコード
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		StopWatch sw = new StopWatch();
		sw.start();
		
		boolean forDeveloper = false;
		Bp3d bp3d = new Bp3d(forDeveloper);

		Bp3dEntry compositePrimitive = bp3d.getEntry("hindbrain");		
		for(String en : bp3d.getPrimitiveOBJNames(compositePrimitive)){
			Bp3dEntry obj = bp3d.getEntry(en);
			System.out.println(obj.getType() + "->" + obj.getId() + "->" + en + "->" + obj.getObjPath());	
		}
				
		sw.stop();

		System.out.println("Bp3d completed: elapsed time="
				+ sw.getElapsedTimeSecs() + "sec");
	}			
}
