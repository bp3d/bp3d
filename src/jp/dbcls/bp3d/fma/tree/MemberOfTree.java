package jp.dbcls.bp3d.fma.tree;

import java.util.*;

import jp.dbcls.bp3d.Bp3dTree;
import jp.dbcls.bp3d.fma.*;
import jp.dbcls.bp3d.ta.TA;
import jp.dbcls.bp3d.util.StopWatch;

public class MemberOfTree extends TraverseFMA {		
	public MemberOfTree() throws Exception{}
	
	public MemberOfTree(FMAOBO fmaobo) throws Exception{
		super(fmaobo);
	}
	
	@Override
	public Set<FMAOBOEntry> getChildren(FMAOBOEntry ent) {
		Set<FMAOBOEntry> ret = new HashSet<FMAOBOEntry>();
		ret.addAll(ent.getReverseIsA());
		ret.addAll(ent.getHasPart());
		return ret;
	}

	@Override
	public Set<FMAOBOEntry> getParents(FMAOBOEntry ent) {
		Set<FMAOBOEntry> ret = new HashSet<FMAOBOEntry>();
		ret.add(ent.getIsA());
		ret.addAll(ent.getPartOf());
		return ret;
	}
	
	private static void debug(String organName) throws Exception {
		FMAOBO fmaobo = new FMAOBO();
		MemberOfTree memberOfTree = 
			new MemberOfTree(fmaobo);
		memberOfTree.setDebug(true);
		
		FMAOBOEntry organ = fmaobo.getByName(organName);
				
		Set<String> display = new HashSet<String>();
		for(FMAOBOEntry ans : memberOfTree.getParents(organ)){
			display.add(ans.getName());
		}						
		System.out.println("MemberOf parents of " + organName + "=" + display);

		display.clear();
		for(FMAOBOEntry ans : memberOfTree.getAncestors(organ)){
			display.add(ans.getName());
		}						
		System.out.println("MemberOf ancestors of " + organName + "=" + display);
	}
	
	
	/**
	 * テストコード
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		StopWatch s = new StopWatch();
		s.start();

		String organName = "Physical anatomical entity";		
		debug(organName);
		
		s.stop();

		System.out.println("elapsed time=" + s.getElapsedTimeSecs());
	}

}
