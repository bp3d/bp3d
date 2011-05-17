package jp.dbcls.bp3d.fma.tree;

import java.util.*;

import jp.dbcls.bp3d.fma.*;
import jp.dbcls.bp3d.util.Bp3dUtility;

/**
 * FMAのpart-of/is-a treeを使って、親子関係を計算。
 * part-of/is-aのどちらを使うかは、getChildren()/getParents()のサブクラス実装で決まる
 * 
 * @author mituhasi
 * 
 */
public abstract class TraverseFMA {
	protected FMAOBO fmaobo = null;
	protected Map<String, Set<FMAOBOEntry>> offsprings = new HashMap<String, Set<FMAOBOEntry>>();
	protected Map<String, Set<FMAOBOEntry>> ancestors = new HashMap<String, Set<FMAOBOEntry>>();

	public TraverseFMA() throws Exception {
		this.fmaobo = new FMAOBO();
	}

	public TraverseFMA(FMAOBO fmaobo) throws Exception {
		this.fmaobo = fmaobo;
	}
	
	/**
	 * 直接childrenの関係にあるFMAエントリを取得する
	 * 
	 * @param ent
	 * @return
	 */
	public abstract Set<FMAOBOEntry> getChildren(FMAOBOEntry ent);
	
	/**
	 * 直接parentの関係にあるFMAエントリを取得する
	 * 
	 * @param fmaId
	 * @return
	 */
	public abstract Set<FMAOBOEntry> getParents(FMAOBOEntry ent);

	public boolean hasChildren(FMAOBOEntry ent) {
		return getChildren(ent).size() > 0 ? true : false;
	}

	public boolean hasParents(FMAOBOEntry ent) {
		return getParents(ent).size() > 0 ? true : false;
	}
	
	public boolean hasChildren(String name) {
		return getChildren(fmaobo.getByName(name)).size() > 0 ? true : false;
	}

	public boolean hasParents(String name) {
		return getParents(fmaobo.getByName(name)).size() > 0 ? true : false;
	}
	
	/**
	 * 臓器名organの子孫をすべて取得する
	 * 
	 * @param fmaId
	 * @return
	 */
	public Set<FMAOBOEntry> getOffsprings(FMAOBOEntry ent) {
		offsprings.clear();
				
		Set<FMAOBOEntry> results = new HashSet<FMAOBOEntry>();

		results = getOffspringsLoop(ent);
		results.remove(ent); // 自分自身を除く
		
		return results;			
	}
	
	public Set<FMAOBOEntry> getOffspringsLoop(FMAOBOEntry ent) {
		Set<FMAOBOEntry> children = getChildren(ent);

		Set<FMAOBOEntry> results = new HashSet<FMAOBOEntry>();
		results.add(ent); // 自分自身をresultに加える

		for (FMAOBOEntry child : children) {
			String cId = child.getId();
			if (offsprings.containsKey(cId)) { // 既に計算済みのものにヒットした場合
				results.addAll(offsprings.get(cId));
			} else {
				results.addAll(getOffspringsLoop(child));
			}
		}

		offsprings.put(ent.getId(), results); // 完成したものはcomplete リストに入る

		return results;
	}

	/**
	 * fmaIddの祖先を取得する
	 * @param fmaId
	 * @return
	 */
	public Set<String> getAncestors(String fmaId){
		Set<String> results = new TreeSet<String>();
		
		if(fmaobo.contains(fmaId)){
			for(FMAOBOEntry aEnt : getAncestors(fmaobo.getById(fmaId))){
				results.add(aEnt.getId());
			}
		}

		return results;
	}
	
	/**
	 * FMAOBOEntry entの祖先を取得する
	 * @param ent
	 * @return
	 */
	public Set<FMAOBOEntry> getAncestors(FMAOBOEntry ent) {		
		ancestors.clear();
		
		Set<FMAOBOEntry> results = new HashSet<FMAOBOEntry>();		
		results = getAncestorsLoop(ent); 
		results.remove(ent); // 自分自身を除く
				
		return results;		
	}
	
	public Set<FMAOBOEntry> getAncestorsLoop(FMAOBOEntry ent) {								
		Set<FMAOBOEntry> results = new HashSet<FMAOBOEntry>();
		results.add(ent); // 自分自身をresultに加える
		
		for (FMAOBOEntry parent : getParents(ent)) {
			if(parent == null){ continue; }  // if ent is "anatomical entity"
			
			String pId = parent.getId();
			if (ancestors.containsKey(pId)) { // 既に計算済みのものにヒットした場合
				results.addAll(ancestors.get(pId));
			} else {
				results.addAll(getAncestorsLoop(parent));
			}
		}

		ancestors.put(ent.getId(), results); // 完成したものはcomplete リストに入る

		return results;
	}
	
	/**
	 * デバッグ出力用
	 * @param set
	 */
	public void display(Collection<FMAOBOEntry> set, String remark){
		List<String> names = new ArrayList<String>();
		for(FMAOBOEntry ent : set){
			names.add(ent.getName());
		}
		System.out.println(remark + "=" + Bp3dUtility.join(names, "/"));
	}
}
