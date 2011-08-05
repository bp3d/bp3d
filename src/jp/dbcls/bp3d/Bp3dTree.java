package jp.dbcls.bp3d;

import java.util.*;

/**
 * Bp3dのmember-of情報を保持するクラス
 * 英語名ではなく、ID(FMA/BP)を持っている
 * 
 * @author mituhasi
 * 
 */

public class Bp3dTree {	
	/** children->set of parents **/
	private SortedMap<String, Set<String>> memberOfs;
	
	/** parent->Set of children **/
	private SortedMap<String, Set<String>> reverseMemberOfs;
	
	/** TAのindentから作ったmemberOfの集合 **/
	private Set<String> taMemberOf;

	/** kaorifPartから作ったmemberOfの集合 **/
	private Set<String> kaorifMemberOf;
	
	public Bp3dTree() throws Exception {		
		this.memberOfs = new TreeMap<String, Set<String>>();
		this.reverseMemberOfs = new TreeMap<String, Set<String>>();
		this.taMemberOf = new TreeSet<String>();
		this.kaorifMemberOf = new TreeSet<String>();
	}
		
	public void addTAMemberOf(String child, String parent){
		taMemberOf.add(child + "\t" + parent);
	}

	public boolean isTAMemberOf(String child, String parent){
		return taMemberOf.contains(child + "\t" + parent);
	}

	public void addKaorifMemberOf(String child, String parent){
		kaorifMemberOf.add(child + "\t" + parent);
	}

	public boolean isKaorifMemberOf(String child, String parent){
		return kaorifMemberOf.contains(child + "\t" + parent);
	}
	
	/**
	 * memberOfの関係を追加する
	 * @param child
	 * @param parent
	 */
	public void addMemberOf(String child, String parent){
		if(!memberOfs.containsKey(child)){
			memberOfs.put(child, new TreeSet<String>());
		}
		
		memberOfs.get(child).add(parent);		
	}

		/**
		 * memberOfの関係を削除する
		 * @param child
		 * @param parent
		 */
		public void removeMemberOf(String child, String parent){			
			memberOfs.get(child).remove(parent);		
			if(memberOfs.get(child).size() == 0){
				memberOfs.remove(child);
			}
		}
	
	/**
	 * reverseMemberOfの関係を追加する
	 * @param child
	 * @param parent
	 */
	public void addReverseMemberOf(String parent, String child){
		if(!reverseMemberOfs.containsKey(parent)){
			reverseMemberOfs.put(parent, new TreeSet<String>());
		}
		
		reverseMemberOfs.get(parent).add(child);		
	}

	/**
	 * reverseMemberOfの関係を削除する
	 * @param child
	 * @param parent
	 */
	public void removeReverseMemberOf(String parent, String child){			
		reverseMemberOfs.get(parent).remove(child);
		if(reverseMemberOfs.get(parent).size() == 0){
			reverseMemberOfs.remove(parent);
		}
	}
	
	/**
	 * 親エントリが存在するかチェックする
	 * @param id
	 * @return
	 */
	public boolean hasParent(String id){
		return memberOfs.containsKey(id);
	}

	/**
	 * パーツidのparentsのID集合を取得する
	 * @param id
	 * @return
	 */
	public Set<String> getParents(String id){
		Set<String> ret = new HashSet<String>();
		if(hasParent(id)){
			ret.addAll(memberOfs.get(id));
		}
		
		return ret;
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
		return reverseMemberOfs.containsKey(id);
	}

	/**
	 * パーツidのchildrenのID集合を取得する
	 * @param id
	 * @return
	 */
	public Set<String> getChildren(String id){
		Set<String> ret = new HashSet<String>();
		if(hasChild(id)){
			ret.addAll(reverseMemberOfs.get(id));
		}
		
		return ret;
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
	 * 全memberOfを返す
	 * @throws Exception
	 */
	public Map<String, Set<String>> getMemberOfs(){		
		return memberOfs;
	}

	/**
	 * 全reverseMemberOfを返す
	 * @return
	 */
	public Map<String, Set<String>> getReverseMemberOfs(){		
		return reverseMemberOfs;
	}
}
