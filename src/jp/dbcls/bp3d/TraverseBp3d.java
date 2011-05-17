package jp.dbcls.bp3d;

import java.util.*;

import jp.dbcls.bp3d.fma.*;

/**
 * Bp3dTreeの階層をたどって、祖先や子孫のパーツを得るためのクラス
 * @author mituhasi
 * 
 */
public class TraverseBp3d {
	protected Bp3dTree bp3dTree;
	
	/**　各IDとのそのleafまでの子孫の対応表　**/
	protected Map<String, Set<String>> offsprings 
		= new HashMap<String, Set<String>>();

	/** 各IDとそのrootまでの祖先の対応表 **/
	protected Map<String, Set<String>> ancestors 
		= new HashMap<String, Set<String>>();
			
	private FMAOBO fmaobo;
		
	public TraverseBp3d(Bp3dTree bp3dTree) throws Exception {
		this.bp3dTree = bp3dTree;
		this.fmaobo = new FMAOBO();
	}
		
	/**
	 * 直接childrenの関係にあるエントリを取得する
	 * 
	 * @param ent
	 * @return
	 */
	public Set<String> getChildren(String id){
		return bp3dTree.getChildren(id);
	}

	/**
	 * 直接parentの関係にあるFMAエントリを取得する
	 * 
	 * @param fmaId
	 * @return
	 */
	public Set<String> getParents(String id){
		return bp3dTree.getParents(id);
	}

	/**
	 * 臓器名organの子孫をすべて取得する
	 * 
	 * @param fmaId
	 * @return
	 */
	public Set<String> getOffsprings(String id) {		
		ancestors.clear();
		
		if(offsprings.containsKey(id)){
			return offsprings.get(id);
		}
		
		/** ループをチェックするために、pathを記録する **/
		Stack<String> path = new Stack<String>();
		
		Set<String> results = getOffspringsLoop(id, path);
				
		results.remove(id); // 自分自身を除く

		offsprings.put(id, results); // 完成したものはcomplete リストに入る
		
		return results;			
	}
	
	public Set<String> getOffspringsLoop(String id, Stack<String> path) {
		Set<String> results = new HashSet<String>();

		/** ループのチェック **/
		if(path.contains(id)){
			path.push(id);
			System.out.println("loop found at getOffspringsLoop:" + displayLoopPath(path));
			return results;
		}
		path.push(id);
		
		results.add(id); // 自分自身をresultに加える
		
		for (String cId : getChildren(id)) {
			if (offsprings.containsKey(cId)) { // 既に計算済みのものにヒットした場合
				results.addAll(offsprings.get(cId));
			} else {
				results.addAll(getOffspringsLoop(cId, path));
			}
		}

		offsprings.put(id, results); // 完成したものはcomplete リストに入る

		path.pop();
		
		return results;
	}

	/**
	 * 臓器名organの親をすべて取得する
	 * 
	 * @param fmaId
	 * @return
	 */
	public Set<String> getAncestors(String id) {
		ancestors.clear();

		/** ループをチェックするために、pathを記録する **/
		Stack<String> path = new Stack<String>();
				
		Set<String> results = getAncestorsLoop(id, path);
		results.remove(id); // 自分自身を除く

		ancestors.put(id, results);
				
		return results;		
	}
	
	
	public Set<String> getAncestorsLoop(String id, Stack<String> path) {								
		Set<String> results = new HashSet<String>();
		
		/** ループのチェック **/
		if(path.contains(id)){
			path.add(id);
			System.out.println("loop found at getAncestorsLoop:" + displayLoopPath(path));
			return results;
		}
		path.add(id);		

		results.add(id); // 自分自身をresultに加える
		
		for (String pId : getParents(id)) {
			if(pId == null){ continue; }  // if ent is "anatomical entity"
			
			if (ancestors.containsKey(pId)) { // 既に計算済みのものにヒットした場合
				results.addAll(ancestors.get(pId));
			} else {
				results.addAll(getAncestorsLoop(pId, path));
			}
		}

		ancestors.put(id, results); // 完成したものはcomplete リストに入る

		path.pop();
		
		return results;
	}	
	
	/**
	 * Loop Pathを表示する
	 * @param fmaIds
	 */
	private List<String> displayLoopPath(List<String> fmaIds){
		List<String> fmaNames = new ArrayList<String>();
		for(String fmaId: fmaIds){
			fmaNames.add(fmaobo.getById(fmaId).getName());
		}
		return fmaNames;
	}
}
