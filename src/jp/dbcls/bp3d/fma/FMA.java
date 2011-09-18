package jp.dbcls.bp3d.fma;

import java.util.*;

import jp.dbcls.bp3d.*;
import jp.dbcls.bp3d.fma.tree.*;
import jp.dbcls.bp3d.util.StopWatch;

public class FMA {
	FMAOBO fmaobo;
	IsATree isA;
	MemberOfTree memberOf;
	
	public FMA() throws Exception {
		fmaobo = new FMAOBO();
		
		/** is-a treeをつくる **/
		isA = new IsATree(fmaobo);
		
		/**  member of treeを作る */
		memberOf = new MemberOfTree(fmaobo);
	}
	
	
	public FMA(FMAOBO fmaobo) throws Exception {
		this.fmaobo = fmaobo;  
		
		/** is-a treeをつくる **/
		isA = new IsATree(fmaobo);
		
		/**  member of treeを作る */
		memberOf = new MemberOfTree(fmaobo);		
	}
	
	
	public boolean contains(String organName){
		return fmaobo.contains(organName);
	}
	
	/**
	 * Left/Rightパーツを持っている場合、Left+Rightのパーツ名を返す
	 * @param organ FMAのエントリである前提 、left+Rightパーツでも、left、rightパーツでもよい。
	 * @return
	 * returns the left+right part if organ is either left+right, left or right part.
	 */
	public FMAOBOEntry getLeftPlusRight(FMAOBOEntry organ){
		FMAOBOEntry ret = null;

		if(organ == null){
			return ret;
		}
		String organName = organ.getName();
		
		if(organName.contains("left") || organName.contains("right")){
			FMAOBOEntry parent = isA.getParent(organ);
			ret = parent;
		}else{
			if(isA.hasChildren(organName)){
				for(FMAOBOEntry child : isA.getChildren(organ)){
					if(child.getName().contains("left"))
					ret = organ;
				}
			}
		}
		
		return ret;
	}

	/**
	 * left/rightを取り除いた名称のエントリがあれば、is-A関係がなくてもOKとする
	 * @param organ
	 * @return
	 */
	public FMAOBOEntry getLeftPlusRightWithoutIsA(FMAOBOEntry organ){		
		if(organ == null){
			return null;
		}
		String organName = organ.getName();
		if(organName.contains("left") ||organName.contains("right")){
			organName = organName.replace("left ", "");
			organName = organName.replace("right ", "");
		}
		
		return fmaobo.getByName(organName);		
	}
	
	/**
	 * Left+Rightのパーツ名を返す
   * @param organName (FMAのエントリでなくてもよい, left/right partが入力でもよい）
	 * @return
	 */
	public FMAOBOEntry getLeftPlusRight(String term){			
		return getLeftPlusRight(fmaobo.get(term));
	}

	/**
	 * Left+Rightのパーツ名を返す
   * @param organName (FMAのエントリでなくてもよい, left/right partが入力でもよい）
	 * @return
	 */
	public FMAOBOEntry getLeftPlusRightWithoutIsA(String organName){		
		if(fmaobo.contains(organName)){
			return getLeftPlusRightWithoutIsA(fmaobo.getByName(organName));
		}else{ /** left/rightがFMAに存在しないときは、left/rightを削除 **/
			organName = organName.replace("left ", "");
			organName = organName.replace("right ", "");			
			if(fmaobo.contains(organName)){
				return getLeftPlusRightWithoutIsA(fmaobo.getByName(organName));
			}
		}
		return null;
	}
	
	/**
	 * left+right partが存在するか判定する
	 * @param organ
	 * @return
	 */
	public boolean hasLeftPlusRight(String organName){
		return getLeftPlusRight(organName) == null ? false : true;
	}

	public boolean hasLeftPlusRightWithoutIsA(String organName){
		return getLeftPlusRightWithoutIsA(organName) == null ? false : true;
	}	
	
	/**
	 * returns the left part
	 * @param organName
	 * @return
	 * returns null if FMA does not contain term
	 * returns a left part if term is a left+right part
	 * returns null if term is a left part
	 * returns null if term is a right part
	 */
	public FMAOBOEntry getLeft(String term){		
		FMAOBOEntry ret = null;
		
		if(!fmaobo.contains(term)){
			return ret;
		}
				
//		Set<FMAOBOEntry> children = isA.getChildren(fmaobo.getByName(term));
		Set<FMAOBOEntry> children = isA.getChildren(fmaobo.get(term));

		/** children consist of a left and right part only **/
		if(children.size() != 2){
			return ret;
		}		

		for(FMAOBOEntry child : children){
			if(child.getName().contains("left")){
				ret = child;
			}
		}
		return ret;			
	}
	
	/**
	 * left partが存在するか判定する
	 * @param organ
	 * @return
	 */
	public boolean hasLeft(String term){
		return getLeft(term) == null ? false : true;
	}

	/**
	 * organNameがある臓器のleft partであるかを判定する
	 * @param organName
	 */
	public boolean isLeft(String organName){
		if(!hasLeftPlusRight(organName)){
			return false;
		}
		
		FMAOBOEntry lr = getLeftPlusRight(organName);
		FMAOBOEntry left = getLeft(lr.getName());

		if(left == null){
			return false;
		}
		
		return left.getName().equals(organName);
	}
	
	
	/**
	 * returns the right part
	 * @param organName
	 * @return
	 * returns null if FMA does not contain organName
	 * returns a right part if organName is a left+right part
	 * returns null if organName is a left part
	 * returns null if organName is a right part
	 */	
	public FMAOBOEntry getRight(String term){		
		FMAOBOEntry ret = null;
		
		if(!fmaobo.contains(term)){
			return ret;
		}
				
		Set<FMAOBOEntry> children = isA.getChildren(fmaobo.get(term));

		/** children consist of a left and right part only **/
		if(children.size() != 2){
			return ret;
		}		

		for(FMAOBOEntry child : children){
			if(child.getName().contains("right")){
				ret = child;
			}
		}
		return ret;			
	}
	
	/**
	 * right partが存在するか判定する
	 * @param organ
	 * @return
	 */
	public boolean hasRight(String term){
		return getRight(term) == null ? false : true;
	}
	
	/**
	 * organNameがある臓器のright partであるかを判定する
	 * @param organName
	 */	
	public boolean isRight(String organName){
		if(!hasLeftPlusRight(organName)){
			return false;
		}
		
		FMAOBOEntry lr = getLeftPlusRight(organName);
		FMAOBOEntry right = getRight(lr.getName());

		if(right == null){
			return false;
		}
		
		return right.getName().equals(organName);
	}
	
		
	/**
	 * 複数形の場合は単数形、単数形の場合は複数形が存在すれば返す
	 * @return
	 */
	public FMAOBOEntry convertSingularPlural(FMAOBOEntry ent){
		String name = ent.getName();
		
		if(name.startsWith("set of ") || name.endsWith("s")){	 // plural->singular
			return toSingular(ent);
		}else{   // singular->plural
			return toPlural(ent);
		}	
	}
	
	/**
	 * convert a singular form to a plural form
	 * @param ent
	 * @return
	 */
	public FMAOBOEntry toPlural(FMAOBOEntry ent){
		if(ent == null){ return ent;}
		
		String name = ent.getName();
		
		if(!(name.startsWith("set of ") || name.endsWith("s"))){	 // singular->plural
			if(name.endsWith(" artery")){
				name = name.replaceFirst(" artery$", " arteries");
			}else{
				name += "s"; 
			}
		
			if(!fmaobo.contains(name)){
				name = "set of " + name;
			}			
		}	
		
		return fmaobo.getByName(name);	
	}

	/**
	 * convert a plural form to a singular form
	 * @param ent
	 * @return
	 */
	public FMAOBOEntry toSingular(FMAOBOEntry ent){
		if(ent == null){ return ent;}
		
		String name = ent.getName();
		
		if(name.startsWith("set of ") || name.endsWith("s")){	 // plural->singular
			name = name.replaceFirst("^set of ", "");
			name = name.replaceFirst(" arteries$", " artery");
			name = name.replaceFirst("s$", "");
		}	
		
		return fmaobo.getByName(name);	
	}
	
	/**
	 * organNameが属するorganSystemを表示する
	 * @param organName
	 * @return
	 */
	public Set<String> getOrganSystem(String organName){
		Set<String> ret = new HashSet<String>();
		
		Set<String> systems = new HashSet<String>();
		systems.add("integumentary system");
		systems.add("respiratory system");
		systems.add("cardiovascular system");
		systems.add("nervous system");
		systems.add("alimentary system");
		systems.add("urinary system");
		systems.add("musculoskeletal system");
		systems.add("deep fascial system");
		systems.add("genital system");
				
		if(hasLeftPlusRight(organName)){
			FMAOBOEntry lr = getLeftPlusRight(organName);
			for(FMAOBOEntry ans : memberOf.getAncestors(lr)){
				if(systems.contains(ans.getName())){
					ret.add(ans.getName());
				}
			}	
		}	
		
		return ret;
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		StopWatch s = new StopWatch();
		s.start();
		
		FMAOBO fmaobo = new FMAOBO();
		
		System.out.println("FMAOBO elapsed time=" + s.getElapsedTimeSecs());
		
		FMA fma = new FMA();
		
		System.out.println("FMA elapsed time=" + s.getElapsedTimeSecs());
		
		System.out.println(fma.toPlural(fmaobo.getByName("eyeball")).getName());
		System.out.println(fma.getLeftPlusRightWithoutIsA("left ventricle").getName());
		System.out.println(fma.hasLeft("clavicle"));
		System.out.println(fma.hasRight("clavicle"));
		System.out.println(fma.getLeft("clavicle").getName());
		System.out.println(fma.getRight("clavicle").getName());
		
		System.out.println("elapsed time=" + s.getElapsedTimeSecs());
	}

}
