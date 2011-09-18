package jp.dbcls.bp3d.fma.tree;

import java.util.*;

import org.eclipse.core.runtime.Path;

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
	boolean isDebug = false;
	
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
	 * @return the isDebug
	 */
	public boolean isDebug() {
		return isDebug;
	}

	/**
	 * @param isDebug the isDebug to set
	 */
	public void setDebug(boolean isDebug) {
		this.isDebug = isDebug;
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
		if(isDebug()){
			System.out.println("-------getOffsprings.TraverseFMA=" + ent.getName() + "----");
		}
		
		offsprings.clear();
				
		Set<FMAOBOEntry> results = new HashSet<FMAOBOEntry>();

		/** ループをチェックするために、pathを記録する **/
		Stack<String> path = new Stack<String>();
		
		results = getOffspringsLoop(ent, path);
		results.remove(ent); // 自分自身を除く
		
		return results;			
	}
	
	public Set<FMAOBOEntry> getOffspringsLoop(FMAOBOEntry ent, Stack<String> path) {
		if(isDebug()){
			System.out.println("getOffspringsLoop.TraverseFMA=" + ent.getName());
		}
		
		Set<FMAOBOEntry> children = getChildren(ent);
		
		Set<FMAOBOEntry> results = new HashSet<FMAOBOEntry>();
		results.add(ent); // 自分自身をresultに加える

		/** ループのチェック **/
		if(path.contains(ent.getId())){
			path.push(ent.getId());
			System.out.println("loop found at getOffspringsLoop.TraverseFMA:" + displayLoopPath(path));
			return results;
		}
		path.push(ent.getId());
		
		
		for (FMAOBOEntry child : children) {
			String cId = child.getId();
			if (offsprings.containsKey(cId)) { // 既に計算済みのものにヒットした場合
				results.addAll(offsprings.get(cId));
			} else {
				results.addAll(getOffspringsLoop(child, path));
			}
		}

		offsprings.put(ent.getId(), results); // 完成したものはcomplete リストに入る

		path.pop();
		
		return results;
	}

	/**
	 * fmaIdの祖先を取得する
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

		/** ループをチェックするために、pathを記録する **/
		Stack<String> path = new Stack<String>();
		
		Set<FMAOBOEntry> results = new HashSet<FMAOBOEntry>();		
		results = getAncestorsLoop(ent, path); 
		results.remove(ent); // 自分自身を除く
				
		return results;		
	}
	
	public Set<FMAOBOEntry> getAncestorsLoop(FMAOBOEntry ent, Stack<String> path) {								
		if(isDebug()){
			System.out.println("getAncestorsLoop.TraverseFMA=" + ent.getId() + "=" + ent.getName());
			System.out.println("path=" + path);
		}
		
		Set<FMAOBOEntry> results = new HashSet<FMAOBOEntry>();
		results.add(ent); // 自分自身をresultに加える

		/** ループのチェック **/
		if(path.contains(ent.getId())){
			path.push(ent.getId());
			System.out.println("loop found at getAncestorsLoop.TraverseFMA:" + displayLoopPath(path));
			return results;
		}
		path.push(ent.getId());

				
		for (FMAOBOEntry parent : getParents(ent)) {
			if(parent == null){ continue; }  // if ent is "anatomical entity"
			
			String pId = parent.getId();
			if (ancestors.containsKey(pId)) { // 既に計算済みのものにヒットした場合
				results.addAll(ancestors.get(pId));
			} else {
				results.addAll(getAncestorsLoop(parent, path));
			}
		}

		ancestors.put(ent.getId(), results); // 完成したものはcomplete リストに入る

		path.pop();
		
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
	
	
	/**
	 * Loop Pathを表示する
	 * @param bp3dIds
	 */
	private List<String> displayLoopPath(List<String> ids){
		List<String> names = new ArrayList<String>();
		for(String id: ids){
			if(fmaobo.contains(id)){
				names.add(fmaobo.getById(id).getName());
			}else{
				names.add(id);
			}
		}
		return names;
	}
}
