package jp.dbcls.bp3d.fma;

import java.util.*;

/**
 * FMAOBOのリストをソートするためのcomparator orderが定義されていれば、それに応じてソート。定義されていない場合は、英語名でソート
 * 
 * @author mituhasi
 * 
 */
public class FMAEnComparator implements Comparator<FMAOBOEntry> {

	public int compare(FMAOBOEntry o1, FMAOBOEntry o2) {
		String en1 = o1.getName();
		String en2 = o2.getName();

		return en1.compareTo(en2);
	}
}
