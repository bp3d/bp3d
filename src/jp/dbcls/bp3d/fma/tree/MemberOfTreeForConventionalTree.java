package jp.dbcls.bp3d.fma.tree;

import java.util.*;

import jp.dbcls.bp3d.fma.*;
import jp.dbcls.bp3d.ta.TA;
import jp.dbcls.bp3d.util.StopWatch;

/**
 * Bp3dのconventional treeのmember-ofを作る
 * 
 * @author ag
 *
 */
public class MemberOfTreeForConventionalTree extends ExtendedMemberOfTree {	
	TA ta;
	
	public MemberOfTreeForConventionalTree() throws Exception{}
	
	public MemberOfTreeForConventionalTree(FMAOBO fmaobo, TA ta) throws Exception{
		super(fmaobo);
		this.ta = ta;
	}
		
	/**
	 * getAncestorsToTA
	 * member-ofの祖先を深さ優先で探索する
	 * TAのエントリに到着時点で探索を打ち切る
	 */

	public Set<FMAOBOEntry> getAncestorsToTA(FMAOBOEntry ent) {
		// 途中結果リストをクリア
		ancestors.clear();

		Set<FMAOBOEntry> results = new HashSet<FMAOBOEntry>();
				
		/** ent自身がTAのエントリの場合は親からたどり始める **/
		if(ta.contains(ent.getName()) || ta.contains(ent.getId())){
			for(FMAOBOEntry parent : getParents(ent)){
				results.addAll(getAncestorsToTALoop(parent));
			}
		}else{ // そうでない場合は自分自身からたどり始める
			results.addAll(getAncestorsToTALoop(ent));
			results.remove(ent); // 自分自身を除く
		}
			
		return results;
	}
	
	/**
	 * getAncestorsToTAの再帰部分
	 */
	public Set<FMAOBOEntry> getAncestorsToTALoop(FMAOBOEntry ent) {										
		Set<FMAOBOEntry> results = new HashSet<FMAOBOEntry>();
		results.add(ent); // 自分自身をresultに加える
		
		/** TAのエントリにぶつかったらそれ以上祖先をたどらない **/		
		if(ta.contains(ent.getName()) || ta.contains(ent.getId())){
			return results;
		}
					
		for (FMAOBOEntry parent : getParents(ent)) {
			if(parent == null){ continue; }  // if ent is "anatomical entity"
			
			String pId = parent.getId();
			if (ancestors.containsKey(pId)) { // 既に計算済みのものにヒットした場合
				results.addAll(ancestors.get(pId));
			} else {				
				results.addAll(getAncestorsToTALoop(parent));
			}
		}

		ancestors.put(ent.getId(), results); // 完成したものはcomplete リストに入る

		return results;
	}

	/**
	 * memberOfを使ってentの親を探す。親はTAエントリでなくてはならない
	 * @param ent
	 * @return
	 */
	public Set<FMAOBOEntry> getTAParents(FMAOBOEntry ent) {		
		return retainTA(getAncestorsToTA(ent));
	}
	
	/**
	 * TAに含まれるFMAエントリの集合だけにする
	 * @param ents
	 * @return
	 */
	public Set<FMAOBOEntry> retainTA(Set<FMAOBOEntry> ents) {
		Set<FMAOBOEntry> results = new HashSet<FMAOBOEntry>();
		
		for(FMAOBOEntry ent : ents){
			if(ta.contains(ent.getId()) || ta.contains(ent.getName())){
				results.add(ent);
			}
		}
		
		return results;
	}
	
	/**
	 * member-ofの祖先のうち、TAのエントリであるものだけの集合を取り出す
	 * @param ent
	 * @return
	 */
	public Set<FMAOBOEntry> getTAAncestors(FMAOBOEntry ent) {												
		/** 最初のTAで探索を打ち切らないのでスーパクラスのgetAncestorsを呼び出す **/
		return retainTA(getAncestors(ent));
	}
			
	/**
	 * サンプルコード
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		StopWatch s = new StopWatch();
		s.start();

		FMAOBO fmaobo = new FMAOBO();
		TA ta = new TA(fmaobo);
		MemberOfTreeForConventionalTree memberOfTree = 
			new MemberOfTreeForConventionalTree(fmaobo, ta);
/**
		{
			FMAOBOEntry lung = fmaobo.getByName("left fusiform gyrus");
			lung = fmaobo.getByName("pronator quadratus");
					
			Set<String> display = new HashSet<String>();
			for(FMAOBOEntry ans : memberOfTree.getTAParents(lung)){
				display.add(ans.getName());
			}						
			System.out.println("TA parents of pronator quadratus=" + display);

		display.clear();
		for(FMAOBOEntry ans : memberOfTree.getTAAncestors(lung)){
			display.add(ans.getName());
		}						
		System.out.println("TA ancestors of pronator quadratus=" + display);
	
		display.clear();
		for(FMAOBOEntry ans : memberOfTree.getAncestors(lung)){
			display.add(ans.getName());
		}						
		System.out.println("TA ancestors of pronator quadratus by super=" + display);		
		}
**/
		
		{
//			String startName = "trapezius";
			String startName = "dorsal digital vein of right hand";

			if(!fmaobo.contains(startName)){
				System.out.println(startName + " is not contained in FMAOBO");				
			}
			FMAOBOEntry start = fmaobo.getByName(startName);
						
			Set<String> display = new HashSet<String>();
//			for(FMAOBOEntry ans : memberOfTree.getTAParents(start)){
//			for(FMAOBOEntry ans : memberOfTree.getTAAncestors(start)){
			for(FMAOBOEntry ans : memberOfTree.getAncestors(start)){
				display.add(ans.getName());
			}						
			System.out.println("TA parents of " + startName + "=" + display);
		}
		
		s.stop();

		System.out.println("elapsed time=" + s.getElapsedTimeSecs());
	}

}
