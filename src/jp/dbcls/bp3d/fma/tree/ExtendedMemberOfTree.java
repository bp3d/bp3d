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
	FMA fma = new FMA();
	
	public ExtendedMemberOfTree() throws Exception{}
	
	public ExtendedMemberOfTree(FMAOBO fmaobo) throws Exception{
		super(fmaobo);
	}

	@Override
	public Set<FMAOBOEntry> getChildren(FMAOBOEntry ent) {
		Set<FMAOBOEntry> ret = new HashSet<FMAOBOEntry>();

		ret.addAll(ent.getReverseIsA());
		ret.addAll(ent.getHasPart());
		
		/** segment of xxxx のエントリが存在すれば、そのchildrenも加える **/
		String name = "segment of " + ent.getName();
		if(fmaobo.contains(name)){
			for(FMAOBOEntry segChild : fmaobo.getByName(name).getReverseIsA()){
				if(!segChild.getName().startsWith("segment of ")){
					ret.add(segChild);
				}
			}
		}
		
		/** "left B part of A" and "right B part of A" であれば
		 *  B partof A も加える。
		**/
		for(FMAOBOEntry child : ent.getHasPart()){		
			if(fma.hasLeftPlusRight(child.getName())){
				ret.add(fma.getLeftPlusRight(child));
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
			/** 
		 * organ A is a segment of organ B 
		 *  -> organ A is a member of B
		 */
			String isAName = isA.getName();
			if(isAName.contains("segment of ")){
				isAName = isAName.replace("segment of ", "");
				if(fmaobo.contains(isAName)){
					ret.add(fmaobo.getByName(isAName));
				}
			}
		}

		/** 
		 * "left B part of A" and "right B part of A" であれば
		 *  B part of A も加える。
		**/
		String organName = ent.getName();
		if(fma.hasLeft(organName) && fma.hasRight(organName)){

			FMAOBOEntry left = fma.getLeft(organName);
			FMAOBOEntry right = fma.getRight(organName);
			Set<FMAOBOEntry> toAdd = new HashSet<FMAOBOEntry>();
			toAdd.addAll(left.getPartOf());
			toAdd.retainAll(right.getPartOf());
			ret.addAll(toAdd);
			ret.remove(ent);  // ent自身が含まれている場合は除く（ループになる)
/**
			if(toAdd.size() > 0){
				System.out.println("left+right=" + organName);
				System.out.println("getLeft()=" + left.getName());
				System.out.println("getRight()=" + right.getName());
				display(left.getPartOf(), "part of=" + left.getName());
				display(right.getPartOf(), "part of=" + right.getName());
				display(toAdd, "union");
			}
**/			
		}
		
		ret.addAll(ent.getPartOf());
				
		return ret;
	}
	
	/**
	 * サンプルコード
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		StopWatch s = new StopWatch();
		s.start();

		FMAOBO fmaobo = new FMAOBO();
		ExtendedMemberOfTree combinedFMA = new ExtendedMemberOfTree(fmaobo);

		FMAOBOEntry ent = fmaobo.getByName("lung");
		ent = fmaobo.getByName("anterior compartment of forearm");	
		ent = fmaobo.getByName("pronator quadratus");
//		ent = fmaobo.getByName("left anconeus");
		ent = fmaobo.getByName("anconeus");
		ent = fmaobo.getByName("midbrain tectum");
//		ent = fmaobo.getByName("midbrain");
		
		List<String> display = new ArrayList<String>();


		for(FMAOBOEntry child : combinedFMA.getChildren(ent)){
			display.add(child.getName());
		}								
		System.out.println("children=" + display);		
		display.clear();

		for(FMAOBOEntry parent : combinedFMA.getParents(ent)){
			display.add(parent.getName());
		}						
		System.out.println("parents=" + display);
		display.clear();

/**		
		for(FMAOBOEntry ans : combinedFMA.getAncestors(ent)){
			display.add(ans.getName());
		}						
		System.out.println("anscestors=" + display);
**/
		
		s.stop();

		System.out.println("elapsed time=" + s.getElapsedTimeSecs());
	}

}
