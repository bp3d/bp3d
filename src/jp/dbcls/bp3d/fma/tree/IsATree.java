package jp.dbcls.bp3d.fma.tree;

import java.util.*;
import jp.dbcls.bp3d.fma.*;

public class IsATree extends TraverseFMA {

	public IsATree() throws Exception {}
	
	public IsATree(FMAOBO fmaobo) throws Exception {
		super(fmaobo);
	}

	@Override
	public Set<FMAOBOEntry> getChildren(FMAOBOEntry ent) {
		Set<FMAOBOEntry> children= new HashSet<FMAOBOEntry>();
		children.addAll(ent.getReverseIsA());
		
		return children;
	}
	
	@Override
	public Set<FMAOBOEntry> getParents(FMAOBOEntry ent) {
		Set<FMAOBOEntry> parents= new HashSet<FMAOBOEntry>();
		parents.add(ent.getIsA());
		
		return parents;
	}

	public FMAOBOEntry getParent(FMAOBOEntry ent) {
		if(ent == null){
			return null;
		}
		
		return ent.getIsA();
	}

}
