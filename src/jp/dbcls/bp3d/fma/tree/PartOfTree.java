package jp.dbcls.bp3d.fma.tree;

import java.util.*;

import jp.dbcls.bp3d.fma.*;
import jp.dbcls.bp3d.util.StopWatch;

public class PartOfTree extends TraverseFMA {

	public PartOfTree() throws Exception {}
	
	public PartOfTree(FMAOBO fmaobo) throws Exception {
		super(fmaobo);
	}

	@Override
	public Set<FMAOBOEntry> getChildren(FMAOBOEntry ent) {
		Set<FMAOBOEntry> children = new HashSet<FMAOBOEntry>();
		children.addAll(ent.getHasPart());
		
		return children;
	}

	@Override
	public Set<FMAOBOEntry> getParents(FMAOBOEntry ent) {
		Set<FMAOBOEntry> parent= new HashSet<FMAOBOEntry>();
		parent.addAll(ent.getPartOf());

		return parent;
	}
	
	/**
	 * テストコード
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		StopWatch s = new StopWatch();
		s.start();

		FMAOBO fmaobo;
		PartOfTree partOf;

		fmaobo = new FMAOBO();
		partOf = new PartOfTree(fmaobo);
		
		String name = "trapezius";
//		String name = "heart";
		FMAOBOEntry organ = fmaobo.getByName(name);
		
		SortedSet<String> displayList = new TreeSet<String>();
		for(FMAOBOEntry ent : partOf.getChildren(organ)){
			displayList.add(ent.getName());
		}
		System.out.println("children=" + displayList);
		displayList.clear();
		
		for(FMAOBOEntry ent : partOf.getOffsprings(organ)){
			displayList.add(ent.getName());
		}
		System.out.println("offfsprings=" + displayList);
		displayList.clear();
		
		for(FMAOBOEntry ent : partOf.getParents(organ)){
			displayList.add(ent.getName());
		}
		System.out.println("parent=" + displayList);
		displayList.clear();

		for(FMAOBOEntry ent : partOf.getAncestors(organ)){
			displayList.add(ent.getName());
		}
		System.out.println("ancestors=" + displayList);
		displayList.clear();
				
		s.stop();

		System.out.println("elapsed time=" + s.getElapsedTimeSecs());
	}
	
}
