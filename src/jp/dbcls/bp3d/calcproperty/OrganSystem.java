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

public class OrganSystem {
	private Bp3d bp3d;
	private Map<String, String> id2en;
	private Map<String, String> en2id;
	
	public OrganSystem(Bp3d bp3d) throws Exception {
		this.bp3d = bp3d;
		this.id2en = new TreeMap<String,String>();
		this.en2id = new TreeMap<String,String>();

		id2en.put("A01.1","cardinal body part");	 // A01.01:人体の体部	
		id2en.put("A01.2","set of immaterial anatomical entities");	 // A01.2.00: 平面、線
		id2en.put("A02","skeletal system"); 	  // A02:骨格系
		id2en.put("A03","articular system"); 	// A03:関節系 
		id2en.put("A04","muscular system");	  // A04:筋肉系
		id2en.put("A05","alimentary system");	// A05:消化器系 
		id2en.put("A06","respiratory system");	// A06:呼吸器系 
		id2en.put("A07","thorax");	  // A07:胸腔；胸郭 
		id2en.put("A08","urinary system");	    // A08:泌尿器系 
		id2en.put("A09","genital system");	    // A09:生殖系 
		id2en.put("A10","abdominopelvic cavity");	// A10:腹骨盤腔 
		id2en.put("A11","endocrine system"); // A11:内分泌系 
		id2en.put("A12","cardiovascular system"); // A12:循環器系 
		id2en.put("A13","lymphoid system");	// A13:リンパ系 
		id2en.put("A14","nervous system");	 // A14:神経系 
		id2en.put("A15","sense organ system");	 //A15:感覚器系 
		id2en.put("A16","integumentary system");	// A16:外皮系
		
		for(String id : id2en.keySet()){
			en2id.put(id2en.get(id), id);
		}		
	}

	public boolean contains(String en){
		return en2id.containsKey(en);
	}
	
	public String getId2En(String taIds){
		List<String> ret = new ArrayList<String>();
		for(String taId : taIds.split("/")){
			ret.add(id2en.get(taId));
		}
		return Bp3dUtility.join(ret, "/");
	}

	public String getId2Kanji(String taIds){
		List<String> ret = new ArrayList<String>();
		for(String taId : taIds.split("/")){	
			ret.add(bp3d.getEntry(id2en.get(taId)).getKanji());
		}
		return Bp3dUtility.join(ret, "/");
	}

	
	public String getEn2Id(String en){
		return en2id.get(en);		
	}

		
	public List<String> getId2En(SortedSet<String> taIds){
		List<String> ret = new ArrayList<String>();
		for(String taId : taIds){
			ret.add(id2en.get(taId));
		}
		return ret;
	}
	
	public List<String> orderByTAId(Set<String> ens){
		Set<String> taIds = new TreeSet<String>();
		for(String en : ens){
			taIds.add(getEn2Id(en));
		}
		List<String> orderedEns = new ArrayList<String>();
		for(String taId : taIds){
			orderedEns.add(getId2En(taId));
		}
		
		return orderedEns;
	}
}
