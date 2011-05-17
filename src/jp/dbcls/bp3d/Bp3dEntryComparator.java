package jp.dbcls.bp3d;

import java.util.*;

/**
 * Bp3dEntryをTAIDでソートする
 * 
 * @author mituhasi
 * 
 */
public class Bp3dEntryComparator implements Comparator<Bp3dEntry> {

	/**
	 * Bp3dEntryをソートするためのIDを返す
	 * TAIDがあればTAID(e.g. A01....)、なければFMAID(FMA1324)or Bp3dId(BP12)
	 * 
	 * @param bp3dEnt
	 * @return
	 */
	private String getIdForSorting(Bp3dEntry bp3dEnt){
		String id = bp3dEnt.getTaId();
		if(id == null){
			id = bp3dEnt.getId();
		}
		
		return id;
	}
		
	public int compare(Bp3dEntry ent1, Bp3dEntry ent2) {
		String id1 = getIdForSorting(ent1);
		String id2 = getIdForSorting(ent2);

		return id1.compareTo(id2);
	}
}
