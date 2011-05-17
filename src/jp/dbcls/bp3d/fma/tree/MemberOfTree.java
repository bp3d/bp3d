package jp.dbcls.bp3d.fma.tree;

import java.util.*;

import jp.dbcls.bp3d.Bp3dTree;
import jp.dbcls.bp3d.fma.*;
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
	
	/**
	 * サンプルコード
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		StopWatch s = new StopWatch();
		s.start();

		FMAOBO fmaobo = new FMAOBO();
		MemberOfTree combinedFMA = new MemberOfTree(fmaobo);

		FMAOBOEntry lung = fmaobo.getByName("lung");
		lung = fmaobo.getByName("anterior compartment of forearm");	
		lung = fmaobo.getByName("pronator quadratus");
		lung = fmaobo.getByName("Left iliocostalis thoracis");
		lung = fmaobo.getByName("brachium of right inferior colliculus");

		Set<String> display = new TreeSet<String>();

		for(FMAOBOEntry child : combinedFMA.getChildren(lung)){
			display.add(child.getName());
		}								
		System.out.println("children of lung=" + display);		
		display.clear();
		
		for(FMAOBOEntry parent : combinedFMA.getParents(lung)){
			display.add(parent.getName());
		}						
		System.out.println("parents of lung=" + display);
		display.clear();
		
		for(FMAOBOEntry ans : combinedFMA.getAncestors(lung)){
			display.add(ans.getName());
		}						
		System.out.println("anscestors of lung=" + display);
		System.out.println("anscestors of lung=" + display.contains("midbrain"));
		
		
		s.stop();

		System.out.println("elapsed time=" + s.getElapsedTimeSecs());
	}

}
