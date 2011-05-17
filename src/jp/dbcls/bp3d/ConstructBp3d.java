package jp.dbcls.bp3d;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;

import jp.dbcls.bp3d.appendpoly.AppendOBJ;
import jp.dbcls.bp3d.fma.FMAOBO;
import jp.dbcls.bp3d.fma.FMAOBOEntry;
import jp.dbcls.bp3d.fma.tree.MemberOfTreeForConventionalTree;
import jp.dbcls.bp3d.Bp3dEntry;
import jp.dbcls.bp3d.kaorif.*;
import jp.dbcls.bp3d.obj.*;
import jp.dbcls.bp3d.ta.*;
import jp.dbcls.bp3d.util.Bp3dUtility;
import jp.dbcls.bp3d.util.StopWatch;

/**
 * FMAOBO/TA/kaorif.xlsのデータから、Bp3d Conventional Treeを作成する
 * 
 * @author mituhasi
 * 
 */

public class ConstructBp3d {
	protected final String DATADIR = Bp3dProperties
		.getString("bp3d.datadir") + "/" + Bp3dProperties.getString("bp3d.dataversion");
	protected final String APPENDDIR = DATADIR + "/FFMP/append/";		
	
	private final String LOGFILE = DATADIR + "/logs/MakeBp3d0.log";
	private final Logger logger = Logger.getLogger(this.getClass().getName());
		
	protected Kaorif kaorif;
	protected FMAOBO fmaobo;
	protected TA ta;
	protected En2Ja en2ja;
	protected MemberOfTreeForConventionalTree memberOfTree;
	protected OBJInfo objInfo;
	
	/** ID->Bp3dEntry **/
	protected SortedMap<String, Bp3dEntry> id2Entry;
	/** English->Bp3dEntry **/
	protected SortedMap<String, Bp3dEntry> en2Entry;
	
	/** children->set of parents (ID) **/
	/** parent->Set of children (ID) **/
	protected Bp3dTree bp3dTree;
	protected TraverseBp3d bp3dTraverse;
	
	protected int anonymousId = 1;
	
	public ConstructBp3d() throws Exception {					
		this.kaorif = new Kaorif();
		this.fmaobo = new FMAOBO();
		this.ta = new TA(fmaobo);
		this.en2ja = new En2Ja();
		this.memberOfTree = 
			new MemberOfTreeForConventionalTree(fmaobo, ta);
		this.bp3dTree = new Bp3dTree();
		
		ParseOBJName pon = new ParseOBJName();
		this.objInfo = pon.getOBJInfoEnLong();
		
		this.id2Entry = new TreeMap<String, Bp3dEntry>();
		this.en2Entry = new TreeMap<String, Bp3dEntry>();

		FileHandler fh = new FileHandler(LOGFILE);
    fh.setFormatter(new java.util.logging.SimpleFormatter());
    logger.addHandler(fh);
		
		run();
	}
	
	protected void run() throws Exception {		
		/** FFMP/appendedディレクトリ作成  **/
		makeOutputDir();
		
		/** TAのエントリを対応するFMAエントリに変更してパーツリストに追加する **/
		addTAEntries();		
		System.out.println("addTAEntries():NumOfEntries=" + id2Entry.size());
		
		/** kaorif.xlsのエントリとmember-ofを追加 **/
		addKaorifEntries();
		System.out.println("addKaorifEntries():NumOfEntries=" + id2Entry.size());					
		
		/** OBJファイルディレクトリをスキャンして、OBJファイル１つからなる
		 * パーツ(primitive parts)リストを作成する **/
		addOBJAsPrimitive();
		System.out.println("addOBJAsPrimitive():NumOfEntries=" + id2Entry.size());					
		
		/** TAのインデント情報をmember-of階層として取り込む **/
		makeMemberOfBasedOnTA();		
		
		/** kaorif.xlsのmember-ofを追加 **/
		addKaorifMemberOf();
		
		/** FMAのmember-ofとTAのインデント情報を使って、member-of関係を作成する **/
		makeMemberOf();
		
		bp3dTraverse = new TraverseBp3d(bp3dTree);
		
		/** 冗長なmember-ofを削除する **/
		trimRedundantMemberOf();
		
		/** leafにOBJを持たないmember-ofを削除する →公開用のTree向け**/
		trimNoOBJMemberOf();							
		
		/** Bp3dEntryの分類を割り当てる **/
		assignBp3dEntryType();

		/** 最終更新日を取得する **/
		calcLastUpadte();
								
		/** 
		 * COMPOSITE_PRIMITIVE, COMPOSITE_ANONYMOUSをレンダリング時に組み合わせるのではなく、OBJファイルとして実物化する。 
		 * ※trimAnonymousParts()の直前に呼び出す 
		 **/
		materializeCompositeParts();
		
		/** 最後に、AnonymousParts(ID=ANON**)を削除する **/
		trimAnonymousParts();
		
		/** bp3d.txt, memberOf.txtを出力する**/
		export();		
	}

	/**
	 * FFMP/appendedディレクトリ作成 
	 */
	private void makeOutputDir(){
		File appendDir = new File(APPENDDIR);
		Bp3dUtility.clean(appendDir);
		if (appendDir.mkdir() == false) {
			System.err.println("ConstructBp3d: mkdir failed for " + APPENDDIR);
		}	
	}	
	
	/**
	 * TAのエントリを対応するFMAエントリに変更して追加する
	 */
	protected void addTAEntries() throws Exception {
		for (TAEntry taEnt : ta.getEntries()){
			for(FMAOBOEntry fmaEnt : ta.getFMAByTAId(taEnt.getTaId())){				
				addEntry(fmaEnt.getName());
			}
		}
	}

	/**
	 * TAのインデント情報をmember-of階層に取り込む
	 * ただし、FMAでchildren, parentの関係がある場合は取り込まない。
	 */
	protected void makeMemberOfBasedOnTA(){		
		TATree taTree = ta.getTree();

		for(String child : taTree.getMemberOfs().keySet()){
			for(String parent : taTree.getParents(child)){
				if(child.equals(parent)){
					
					System.out.println("[Warning]@makeMemberOfBasedOnTA.ConstructBp3d: child and parent are identical=" + child);					
				}else if(!memberOfTree.getAncestors(child).contains(parent) &&
							!memberOfTree.getAncestors(parent).contains(child)){
					bp3dTree.addMemberOf(child, parent);
					bp3dTree.addReverseMemberOf(parent, child);
					bp3dTree.addTAMemberOf(child, parent);
				}
			}
		}
	}
		
	/**
	 * kaorif.xlsのエントリをパーツリストに追加
	 */
	protected void addKaorifEntries(){
		for(Bp3dEntry ent : kaorif.getAllEntries()){
			this.id2Entry.put(ent.getId(), ent);
		}
	}
		
	/**
	 * OBJファイルディレクトリをスキャンして、OBJファイル１つからなるパーツリストを作成する
	 */
	protected void addOBJAsPrimitive() throws Exception {
		for (OBJInfoEntry objInfoEnt : objInfo.values()) {
			String enLong = objInfoEnt.getEnLong();			
			addEntry(enLong);
		}
	}

	
	/**
	 * IDのリストからOBJファイルが存在しないIDを削除する
	 * @param ids
	 * @return
	 */
	protected Set<String> retainOBJ(Set<String> ids){
		Set<String> ret = new HashSet<String>();
		
		for(String id : ids){
			String en = id2Entry.get(id).getEn();
			if(this.objInfo.containsKey(en)){
				ret.add(id);
			}
		}
		
		return ret;					
	}
	
	/**
	 * id2Entry/en2Entryに英語名enを持つ新しいBp3dEntryを追加する
	 * @param en
	 * @return
	 */
	protected void addEntry(String en) {			
		String id = "";
		
		try {						
			if(containedInFMAOrKaorif(en)){  // FMA/Kaorifに含まれるパーツ
				id = getId(en);
				if(fmaobo.contains(en) && !fmaobo.isPreferredName(en)){
					String msg = "Not FMA preferred name" + "\t" + en;
					logger.log(new LogRecord(Level.WARNING, msg));						
				}
				en = getName(id);  // FMAにエントリが存在する場合は、preferred nameになる
			}else if(objInfo.containsKey(en)){  // 後ほどappendされる無名パーツ
				id = getAnonymousId(en);
			}else{                              // あり得ないはず(原因：OBJファイルのスペルミス）		
				String msg = "Not FMA/Kaorif name:" + "\t" + en + "\t" + "Check *.obj and kaorif.xls";
				logger.log(new LogRecord(Level.SEVERE, msg));
				throw new Exception();
			}
											
			Bp3dEntry ent = new Bp3dEntry();			
			ent.setId(id);
			ent.setEn(en);
			
			if(ta.contains(id)){  		/** TA情報を追加 **/			
				Set<TAEntry> taEnts = ta.getTAByFmaId(id);
				Set<String> taKanjis = new HashSet<String>();
				for(TAEntry taEnt : taEnts){
					taKanjis.add(taEnt.getTaKanji());
				}
				ent.setKanji(Bp3dUtility.join(taKanjis, "/"));
				ent.setKana(" ");   /** TAのかな情報がない ので空白文字１文字**/
				Set<String> taIds = new TreeSet<String>();
				for(TAEntry taEnt : taEnts){
					taIds.add(taEnt.getTaId());
				}
				ent.setTaId(Bp3dUtility.join(taIds, "/"));			
			}else if(en2ja.contains(en)){  		/** TA情報がないエントリは、kaorif.xls/en2jaから漢字とかな取得**/
				ent.setKanji(en2ja.getKanji(en));
				ent.setKana(en2ja.getKana(en));
			}
				
			/** OBJファイルのパスとファイル作成日を追加 **/
			if(objInfo.containsKey(en)){
				ent.setLastUpdate(objInfo.getLastUpdate(en));
				ent.setObjPath(objInfo.getLatestFile(en));
			}
			
			id2Entry.put(id, ent);
			en2Entry.put(en, ent);		

		}catch (Exception e){}	
	}
	
	/**
	 * term (ID/English)に対応するBp3dEntryを返す
	 * 
	 * @param term
	 * @return
	 */
	public Bp3dEntry getEntry(String term){
		if(en2Entry.containsKey(term)){
			return this.en2Entry.get(term);
		}
		return this.id2Entry.get(term);
	}
		
	/**
	 * IDから英語名を取得する
	 * @param id
	 * @return
	 */
	private String getName(String id){
		String name = null;

		boolean isNSN = false;
		if (id.endsWith("nsn")){
			isNSN = true;
			id = id.replaceFirst("nsn$", "");
		}
		
		if (fmaobo.contains(id)) {
			if(fmaobo.getById(id) == null){
				name = id;
			}else{
				name = fmaobo.getById(id).getName();
			}			
		} else if(kaorif.contains(id)){
			name = kaorif.getEntry(id).getEn();
		} 

		if(isNSN){
			name += ", nsn";
		}
		
		return name;
	}

	
	/**
	 * 英語名からIDを取得する
	 * 
	 * @param en
	 * @return
	 */
	private String getId(String en) {
		String id = null;
		
		boolean isNSN = false;
		if (en.endsWith(", nsn") || en.endsWith(",nsn")) {
			isNSN = true;
			en = en.replaceFirst(", nsn$", "");
			en = en.replaceFirst(",nsn$", "");
			en = en.trim();
		}

		if (fmaobo.contains(en)) { // FMAIDが見つかった場合
			if(fmaobo.getByName(en) == null){
				id = en;
			}else{
				id = fmaobo.getByName(en).getId();
			}
		} else if(kaorif.contains(en)){			// kaorf.txtにIDが見つかった場合
			id = kaorif.getEntry(en).getId();			
		}
		
		/** NSNの場合はIDの最後にnsnをつける **/
		if (isNSN) { 
			id += "nsn";
		}
				
		return id;
	}
	
	/**
	 * term(ID or English)がFMAOBO/kaorif.xlsの
	 * どちらかに含まれているを判定する
	 * 
	 * @param en
	 * @return
	 */
	public boolean containedInFMAOrKaorif(String term){
		return (getId(term) == null && getName(term) == null) ? false : true;
	}
	
	/**
	 * FMAのmemberOf情報からmemberOf情報を作る
	 * @throws Exception
	 */
	protected void makeMemberOf() throws Exception {		
		for (String id : id2Entry.keySet()){
			if(!fmaobo.contains(id)){
				continue;
			}

			for(FMAOBOEntry parent : memberOfTree.getTAParents(fmaobo.getById(id))){				
				bp3dTree.addMemberOf(id, parent.getId());
				bp3dTree.addReverseMemberOf(parent.getId(), id);
			}
		}
	}

	/**
	 * 一時的につける無名ＩＤを発行する
	 * @param en
	 * @return
	 */
	protected String getAnonymousId(String en){
		String id = "ANON" + this.anonymousId++;
		return id;
	}
		
	/** 
	* kaorif.xls/kaorifPartで定義されているパーツとmemberOf関係(Preferred nameで記述)を読み込む
	*/
	protected void addKaorifMemberOf() throws Exception {		
		/** 
		 * kaorifPartのparentに出てくるエントリをpartsListに登録
		 */
		Bp3dTree kaorifTree = kaorif.getBp3dTree();
		for(String parent : kaorifTree.getReverseMemberOfs().keySet()){
			String pId = "";

			if(!contains(parent)){
				addEntry(parent);
			}
			if(!contains(parent)){
				System.out.println("BBB=" + parent);
			}
			pId = getEntry(parent).getId();
			
		/**
			else if(fmaobo.contains(parent)){
				this.addEntry(parent);
				FMAOBOEntry pEnt = fmaobo.getByName(parent);
				pId = pEnt.getId();
				Bp3dEntry bp3dEnt = new Bp3dEntry();
				bp3dEnt.setId(pId);
				bp3dEnt.setEn(pEnt.getName());
				this.id2Entry.put(pId, bp3dEnt);
				this.en2Entry.put(pEnt.getName(), bp3dEnt);
			}else if(containedInFMAOrKaorif(parent)){ // kaorif.xls/kaorifに含まれる場合
				pEn = kaorif.getEntry(parent).getEn();
				pId = kaorif.getEntry(parent).getId();
				Bp3dEntry bp3dEnt = new Bp3dEntry();
				bp3dEnt.setId(pId);
				bp3dEnt.setEn(parent);
				this.id2Entry.put(pId, bp3dEnt);
				this.en2Entry.put(pEn, bp3dEnt);
			}else{
				System.out.println("[Error]@Bp3d:Parent in KaorifPart must be either FMA or kaorif entry=" + parent);
			}
**/

			/** 
			 * kaorifPartのmemberOfを登録
			 */
			for(String child : kaorifTree.getChildren(parent)){
				String cid = "";
				if(fmaobo.contains(child)){
					cid = fmaobo.getByName(child).getId();
				}else if(containedInFMAOrKaorif(child)){
					cid = kaorif.getEntry(child).getId();
				}else if(contains(child)){  // 後ほどappendされる無名パーツ(ID=ANON)
					cid = this.getEntry(child).getId();
				}else{
					System.out.println("[Error]@Bp3d:Child in KaorifPart must be either FMA ,kaorif or OBJ file entry=" + child);					
				}
				
				bp3dTree.addMemberOf(cid, pId);
				bp3dTree.addReverseMemberOf(pId, cid);
				bp3dTree.addKaorifMemberOf(cid, pId);								
			}
		}
	}


	/**
	 * bp3dEntが示すパーツが無名パーツを子供に持つか判定する
	 * @param bp3dEnt
	 * @return
	 */
	protected boolean hasAnonymousParts(Bp3dEntry bp3dEnt){
		for(String cId : this.bp3dTree.getChildren(bp3dEnt.getId())){
			if(cId.startsWith("ANON")){
				return true;
			}
		}
		
		return false;		
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
	 * term(English/id)がpartsListに含まれているか判定する
	 * @param id
	 * @return
	 */
	public boolean contains(String term){
		if(en2Entry.containsKey(term)){
			return true;
		}
		return id2Entry.containsKey(term);
	}
		
	/**
	 * 全IDを返す
	 * 
	 * @return
	 */
	public Set<String> getAllIds(){		
		Set<String> ret = new HashSet<String>();
		ret.addAll(id2Entry.keySet());
		return ret;
	}

	/**
	 * 全エントリを返す
	 * @return
	 */
	public Collection<Bp3dEntry> getAllEntries(){
		Collection<Bp3dEntry> ret = new HashSet<Bp3dEntry>();
		ret.addAll(id2Entry.values());
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
	 * OBJファイル情報を返す
	 * @return
	 */
	public OBJInfo getObjInfoSetEnLong() {
		return objInfo;
	}

	/**
	 * entを構成するOBJファイルのIDをすべて返す
	 * leafでない位置に存在するOBJ(nsnピース), Anonymousピースも返す
	 * @param ent
	 * @return
	 */	
	public Set<String> getPrimitiveOBJIds(Bp3dEntry ent){		
		Set<String> ret = new HashSet<String>();
		
		String id = ent.getId();
		String en = ent.getEn();

		if(objInfo.containsKey(en)){
			ret.add(id);
		}
		
		if(bp3dTree.hasChild(id)){	
			for(String oId : bp3dTraverse.getOffsprings(id)){				
				String oEn = this.getEntry(oId).getEn();
				if(objInfo.containsKey(oEn)){
					ret.add(oId);
				}
			}
		}
		
		return ret;		
	}

	/**
	 * 最終更新日を取得する
	 * @throws Exception
	 */
	public void calcLastUpadte() throws Exception {
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd");

		for(Bp3dEntry ent : getAllEntries()){				
			Date lastUpdate = df.parse("2000/01/01");
			
			for (String id : getPrimitiveOBJIds(ent)){
				Date thisDate = getEntry(id).getLastUpdate();
				if(thisDate.after(lastUpdate)){
					lastUpdate = thisDate;
				}
			}
			ent.setLastUpdate(lastUpdate);
		}		
	}
	
	
	/**
	 * 冗長なmember-of関係を削除する
	 * Remove A member of C if A member of B and B member of C
	 * 
	 */
	protected void trimRedundantMemberOf() throws Exception {		
		for(String cid : getAllIds()){
			for(String pid : getParents(cid)){
				/** edge cidからpidの他の経路があれば直接経路は削除する **/		
				bp3dTree.removeMemberOf(cid, pid);
				bp3dTree.removeReverseMemberOf(pid, cid);
				if(this.bp3dTraverse.getAncestors(cid).contains(pid)){
					//System.out.println("Path:" + cid + "->" + pid + " is redundant.");
				}else{
					bp3dTree.addMemberOf(cid, pid);
					bp3dTree.addReverseMemberOf(pid, cid);					
				}
			}				
		}
	}
	
	/**
	 * leafにOBJを持たないmember-ofを削除する 
	 */
	private void trimNoOBJMemberOf() throws Exception {
		TraverseBp3d tBp3d = new TraverseBp3d(bp3dTree);
				
		/** 削除するべきIDの集合 **/
		Set<String> toDelete = new HashSet<String>();		
		
		for(Bp3dEntry bp3dEnt : getAllEntries()){
			String en = bp3dEnt.getEn();
			String id = bp3dEnt.getId();
			
			/** OBJの場合はそのまま **/
			if(this.objInfo.containsKey(en)){
				continue;
			}
			
			/** leftにOBJを持たない場合は、親へのmember-ofを削除する **/
			if(retainOBJ(tBp3d.getOffsprings(id)).size() == 0){				
				for(String pid : getParents(id)){
					bp3dTree.removeMemberOf(id, pid);
					bp3dTree.removeReverseMemberOf(pid, id);
				}
				toDelete.add(id);
			}
		}

		/** partsListから削除　**/
		for(String id : toDelete){
			id2Entry.remove(id);
		}
	}

	/**
	 * composite partを以下の２条件のときに、レンダリング時に組み合わせるのではなく、
	 * OBJファイルとして実物化する。
	 * 
	 * １．composite part自身に対するOBJファイルが存在するとき
	 * ２．子供にanonymous parts(ID=ANNON)が存在するとき
	 * 
	 */
	protected void materializeCompositeParts() throws Exception {			
		Map<String, String> outObjPaths = new HashMap<String,String>(); // appendされたOBJファイルへのパス
				
		for(Bp3dEntry bp3dEnt : getAllEntries()){
			String en = bp3dEnt.getEn();

			if(bp3dEnt.isCompositePrimitive() || bp3dEnt.isCompositeAnonymous()){															
				AppendOBJ appender = new AppendOBJ();
				String objPath = this.APPENDDIR + en + ".obj";								
				appender.setOutFile(objPath);
				outObjPaths.put(en, objPath);
								
				for(String elementId : getPrimitiveOBJIds(bp3dEnt)){
					appender.append(getEntry(elementId).getObjPath());
				}
				appender.end();
			}
		}

		/** 
		 * 出力されたOBJファイルのパスをBp3dの各エントリに代入する
		 * ※最後に一括しておこなわないと、apppendしたファイルが別のファイルの要素になっている場合に問題が起きる
		 */
		for(String en : outObjPaths.keySet()){
			getEntry(en).setObjPath(outObjPaths.get(en));
		}
		
	}
	

	/** 
	 * Anonymous partsを削除する
	 ***/
	protected void trimAnonymousParts(){		
		for(Bp3dEntry bp3dEnt : this.getAllEntries()){
			String id = bp3dEnt.getId();
			String en = bp3dEnt.getEn();
			if(id.startsWith("ANON")){
				id2Entry.remove(id);
				en2Entry.remove(en);
			}else{
				for(String cid : bp3dTree.getChildren(id)){
					if(cid.startsWith("ANON")){
						bp3dTree.removeMemberOf(cid, id);
						bp3dTree.removeReverseMemberOf(id, cid);
					}
				}
			}				
		}
	}
	
	/**
	 * entがCompositePrimitiveか判定する
	 * @param ent
	 * @return
	 */

	public boolean isCompositePrimitive(Bp3dEntry ent){
		String id = ent.getId();
		String en = ent.getEn();
		if(hasChild(id) && objInfo.containsKey(en)){						
			return true;
		}else{
			return false;
		}		
	}

	/**
	 * 各パーツの分類を返す。
	 * 
	 * primitive: OBJファイル単独でそのパーツを表現している
	 * composite: 複数のprimitiveの和で定義される
	 * composite+primitive: 複数のprimitiveとそのパーツ自身を表すOBJ（部分データ, non-specified-name)の和で定義される
	 * 
	 * @param id
	 * @return
	 */
	public void assignBp3dEntryType(){
		for(Bp3dEntry ent : this.getAllEntries()){
			String id = ent.getId();
			String en = ent.getEn();
			if(isCompositePrimitive(ent)){						
				ent.setType(Bp3dEntryType.COMPOSITE_PRIMITIVE);
			}else if(objInfo.containsKey(en)){
				if(this.containedInFMAOrKaorif(en)){
					ent.setType(Bp3dEntryType.COMPLETED);
				}else{
					ent.setType(Bp3dEntryType.COMPLETED_ANONYMOUS);
				}
			}else if(hasChild(id) == false){
				ent.setType(Bp3dEntryType.NEED_TO_MAKE);
			}else{
				if(hasAnonymousParts(ent)){
					ent.setType(Bp3dEntryType.COMPOSITE_ANONYMOUS);
				}else{
					ent.setType(Bp3dEntryType.COMPOSITE);
				}
			}
		}
	}
		
	public Bp3dTree getBp3dTree() {
		return bp3dTree;
	}
	
	/**
	 * parts listとmember ofリストを出力する
	 * @throws Exception
	 */
	public void export() throws Exception {		
		FileOutputStream fos;
		OutputStreamWriter out;
		BufferedWriter bw;
		
		String logFile = DATADIR + "/logs/MakeBp3d0/bp3d.txt";

		fos = new FileOutputStream(logFile, false);
		out = new OutputStreamWriter(fos, "MS932");
		bw = new BufferedWriter(out);
					
		bw.write("id" + "\t" + "en" + "\t" + "kanji" + "\t" 
				+ "kana" + "\t" + "taId" + "\t" + "lastUpdate" + "\t" 
				+ "type" + "\t" + "objPath" + "\n");

		for (Bp3dEntry ent : this.getAllEntries()){
			bw.write(ent.getId() + "\t" + ent.getEn() + "\t" 
					+ ent.getKanji() + "\t" + ent.getKana() + "\t"
					+ ent.getTaId() + "\t"
					+ ent.getLastUpdateString() + "\t"					
					+ ent.getType() + "\t"
					+ ent.getObjPath() + "\n");
		}

		bw.close();
		out.close();
		fos.close();

		logFile = DATADIR + "/logs/MakeBp3d0/bp3dMemberOf.txt";

		fos = new FileOutputStream(logFile, false);
		out = new OutputStreamWriter(fos, "MS932");
		bw = new BufferedWriter(out);
					
		bw.write("child\tparent\ttype\n");

		for (String child : this.getMemberOfs().keySet()) {
			for (String parent : this.getMemberOfs().get(child)) {
				String type = "";
				if(bp3dTree.isTAMemberOf(child, parent)){
					type = "TA";
				}else if(bp3dTree.isKaorifMemberOf(child, parent)){
					type = "kaorif";
				}else{
					type = "FMA";
				}

				if(!contains(child) || !contains(parent)){
					System.out.println(child + "<->" + parent + "*");
				}
				
				bw.write(getEntry(child).getEn() + "\t" + getEntry(parent).getEn() + "\t"
						+ type + "\n");
			}
		}

		bw.close();
		out.close();
		fos.close();	
	}
	
	private void exportLog() throws Exception {
	}

	
	/**
	 * テストコード
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		StopWatch sw = new StopWatch();
		sw.start();

		ConstructBp3d bp3d = new ConstructBp3d();

		System.out.println("Number Of BodyParts=" + bp3d.getAllEntries().size());
		
		sw.stop();

		System.out.println("Bp3d completed: elapsed time="
				+ sw.getElapsedTimeSecs() + "sec");
	}	
}
