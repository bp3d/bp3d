package jp.dbcls.bp3d.calcproperty;

/**
 * OrganSystemを計算します。
 * 
 * @author mituhasi
 * 
 */

import java.util.*;

import jp.dbcls.bp3d.*;
import jp.dbcls.bp3d.util.Bp3dUtility;

public class CalcOrganSystem {
	Bp3d bp3d;
	TraverseBp3d bp3dTraverse;
	Set<String> organSystems;
	
	/**
	 * invoked in MakeBp3d1
	 * 
	 * @throws Exception
	 */
	public CalcOrganSystem(Bp3d bp3d) throws Exception {
		this.bp3d = bp3d;
		this.bp3dTraverse = bp3d.getBp3dTraverse();
		this.organSystems = new HashSet<String>();	
	}
	
	private void makeOrganSystemsList(){
		organSystems.add("non-physical anatomical entity");	 // A01:一般解剖学		
		organSystems.add("cardinal body part");	 // A01.01:人体の体部		
		organSystems.add("skeletal system"); 	  // A02:骨格系
		organSystems.add("articular system"); 	// A03:関節系 
		organSystems.add("muscular system");	  // A04:筋肉系
		organSystems.add("alimentary system");	// A05:消化器系 
		organSystems.add("respiratory system");	// A06:呼吸器系 
		organSystems.add("thorax");	  // A07:胸腔；胸郭 
		organSystems.add("urinary system");	    // A08:泌尿器系 
		organSystems.add("genital system");	    // A09:生殖系 
		organSystems.add("abdominopelvic cavity");	// A10:腹骨盤腔 
		organSystems.add("endocrine system"); // A11:内分泌系 
		organSystems.add("cardiovascular system"); // A12:循環器系 
		organSystems.add("lymphoid system");	// A13:リンパ系 
		organSystems.add("nervous system");	 // A14:神経系 
		organSystems.add("sense organ system");	 //A15:感覚器系 
		organSystems.add("integumentary system");	// A16:外皮系
	}
		
	/**
	 * OrganSystemを取得する
	 * @param ent
	 * @return
	 * @throws Exception
	 */
	public String calcOrganSystem(Bp3dEntry ent) throws Exception {				
		Set<String> hits = new TreeSet<String>();
		
		/**
		 * organSystem名そのものの場合
		 */
		if(organSystems.contains(ent.getEn())){
			return ent.getEn();
		}
		
		for (String aId : bp3dTraverse.getAncestors(ent.getId())){			
			Bp3dEntry aEnt = bp3d.getEntry(aId);
			if(organSystems.contains(aEnt.getEn())){
				hits.add(aEnt.getEn());
			}			
		}

		/**
		 * non-physical anatomical entity以外にヒットした場合はそれを優先する
		 */
		if(hits.contains("non-physical anatomical entity")&& hits.size() > 1){
			hits.remove("non-physical anatomical entity");
		}
		
		/**
		 * cardinal body part以外にヒットした場合はそれを優先する
		 */
		if(hits.contains("cardinal body part")&& hits.size() > 1){
			hits.remove("cardinal body part");
		}
		
		return Bp3dUtility.join(hits, "/");
	}
	
	public void run() throws Exception {
		makeOrganSystemsList();
		
		for (Bp3dEntry bp3dEnt : bp3d.getAllEntries()){
			bp3dEnt.setOrganSystem(calcOrganSystem(bp3dEnt));
		}
	}
}
