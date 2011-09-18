package jp.dbcls.bp3d.fma.tree;

import java.util.*;

import jp.dbcls.bp3d.fma.*;
import jp.dbcls.bp3d.util.StopWatch;

/**
 * member-of treeを拡張したtree構造
 * segment of organ A の下にorgan Bが存在したとき、organ B member of organ Aの関係を定義する
 *  
 * @author ag
 *
 */

public class ExtendedMemberOfTree extends TraverseFMA {		
	FMA fma;
	
	public ExtendedMemberOfTree() throws Exception{
		fma = new FMA(this.fmaobo);
	}
	

	public ExtendedMemberOfTree(FMAOBO fmaobo) throws Exception{
		super(fmaobo);
		fma = new FMA(fmaobo);
	}
	
	@Override
	public Set<FMAOBOEntry> getChildren(FMAOBOEntry ent) {
		Set<FMAOBOEntry> ret = new HashSet<FMAOBOEntry>();

		ret.addAll(ent.getReverseIsA());
		ret.addAll(ent.getHasPart());
		
		/** segment/subdivision of xxxx のエントリが存在すれば、そのchildrenも加える **/
		{
			List<String> regions = new ArrayList<String>();
			regions.add("segment of ");
			regions.add("subdivision of ");
			regions.add("musculature of ");
			regions.add("muscles of ");

			for(String region : regions){
				String name = regions + ent.getName();
				if(fmaobo.contains(name)){
					for(FMAOBOEntry segChild : fmaobo.getByName(name).getReverseIsA()){
						if(!segChild.getName().startsWith(region)){
							ret.add(segChild);
						}
					}	
				}
			}
		}
			
		/** "left B part of A" and "right B part of A" であれば
		 *  B partof A も加える。
		**/
		{
			for(FMAOBOEntry child : ent.getHasPart()){						
				if(fma.hasLeftPlusRight(child.getName())){
					ret.add(fma.getLeftPlusRight(child));
				}			
			}
		}
				
		/**
		 * musculature of A 　と　muscle of Aを同一視する
		 */
		{
			Map<String, String> synonyms = new HashMap<String, String>();
			synonyms.put("musculature of ", "muscle of ");
			synonyms.put("muscle of ", "musculature of ");
						
			for(String replacedStr: synonyms.keySet()){
				String synonymStr = "";				
				if(ent.getName().startsWith(replacedStr)){
					synonymStr = ent.getName().replace(replacedStr, synonyms.get(replacedStr));
					if(fmaobo.contains(synonymStr)){
						FMAOBOEntry synonym = fmaobo.get(synonymStr);
						ret.addAll(synonym.getReverseIsA());
						ret.addAll(synonym.getHasPart());
					}
				}
			}
		}
		
		return ret;
	}

	@Override
	public Set<FMAOBOEntry> getParents(FMAOBOEntry ent) {
		Set<FMAOBOEntry> ret = new HashSet<FMAOBOEntry>();
		
		FMAOBOEntry isA = ent.getIsA();				
		if(isA != null){		
			ret.add(isA);
		}
		
		ret.addAll(ent.getPartOf());		
		
		String organName = ent.getName();
		
		/** 
		 * organ A is-a/part-of segment/subdivision of organ B 
		 *  -> organ A is a member-of B
		 */
		{
			List<String> regions = new ArrayList<String>();
			regions.add("segment of ");
			regions.add("subdivision of ");
			regions.add("musculature of ");
			regions.add("muscles of ");
			
			for(String region : regions){
				if(organName.startsWith(region)){
					String regionLessName = organName.replace(region, "");
					if(isDebug){
						System.out.println("getParents.ExtendedMemberOftree=" + organName);
					}
					if(fmaobo.contains(regionLessName)){
						ret.add(fmaobo.getByName(regionLessName));
					}
				}
			}
		}

		/** 
		 * "left B is-a/part-of A" and "right B is-a/part-of of A" であれば
		 *  B member-of A も加える。
		**/
		{
			if(fma.hasLeft(organName) && fma.hasRight(organName)){
			
				FMAOBOEntry left = fma.getLeft(organName);
				FMAOBOEntry right = fma.getRight(organName);
				Set<FMAOBOEntry> toAdd = new HashSet<FMAOBOEntry>();
				toAdd.addAll(left.getPartOf());
				toAdd.retainAll(right.getPartOf());
				ret.addAll(toAdd);
				ret.remove(ent);  // ent自身が含まれている場合は除く（ループになる)
			
				if(isDebug == true && toAdd.size() > 0){
					System.out.println("Extended at getParents.ExtendedMemberOfTree for " + organName);
					System.out.println("left+right=" + organName);
					System.out.println("getLeft()=" + left.getName());
					System.out.println("getRight()=" + right.getName());
					display(left.getPartOf(), "left.getPartOf()=" + left.getName());
					display(right.getPartOf(), "right.getPartOf()=" + right.getName());
					display(toAdd, "toAdd(intersection of left/right)=");
					display(ret, "ret=");
				}
			}
		}
		/**
		 * musculature of A 　と　muscle of Aを同一視する
		 */
		{
			Map<String, String> synonyms = new HashMap<String, String>();
			synonyms.put("musculature of ", "muscle of ");
			synonyms.put("muscle of ", "musculature of ");
						
			for(String replacedStr: synonyms.keySet()){
				String synonymStr = "";				
				if(ent.getName().startsWith(replacedStr)){
					synonymStr = ent.getName().replace(replacedStr, synonyms.get(replacedStr));
					if(fmaobo.contains(synonymStr)){
						FMAOBOEntry synonym = fmaobo.get(synonymStr);
						if(isDebug){
							System.out.println("getParents.ExtendedMemberOfTree=" + ent.getName() + "==>" + synonym.getName());
						}
						ret.add(synonym.getIsA());
						ret.addAll(synonym.getPartOf());
					}
				}
			}
		}
		

				
		return ret;
	}
	
	
	private static void debug(String organName) throws Exception {
		FMAOBO fmaobo = new FMAOBO();
		ExtendedMemberOfTree memberOfTree = 
			new ExtendedMemberOfTree(fmaobo);
		memberOfTree.setDebug(true);
		
		FMAOBOEntry organ = fmaobo.getByName(organName);
				
		Set<String> display = new HashSet<String>();
		for(FMAOBOEntry ans : memberOfTree.getParents(organ)){
			display.add(ans.getName());
		}						
		System.out.println("ExtendedMemberOf parents of " + organName + "=" + display);

		display.clear();
		for(FMAOBOEntry ans : memberOfTree.getAncestors(organ)){
			display.add(ans.getName());
		}						
		System.out.println("ExtendedMemberOf ancestors of " + organName + "=" + display);
	}
	
	
	/**
	 * テストコード
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		StopWatch s = new StopWatch();
		s.start();

		String organName = "right splenius capitis";
		debug(organName);
		
		s.stop();

		System.out.println("elapsed time=" + s.getElapsedTimeSecs());
	}

}
