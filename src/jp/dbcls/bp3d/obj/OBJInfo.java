package jp.dbcls.bp3d.obj;

import java.util.*;

import jp.dbcls.bp3d.Bp3dProperties;

public class OBJInfo extends HashMap<String, OBJInfoEntry> {
	
	public void add(String key, OBJInfoEntry objInfoIn) throws Exception {
		String en = objInfoIn.getEn();
		String enCorrected = objInfoIn.getEnCorrected();
		String enLong = objInfoIn.getEnLong();
		String leftRight = objInfoIn.getLeftRight();
		Date updateDate = objInfoIn.getLastUpdate();
		String filename = objInfoIn.getLatestFile();
		boolean isAppended = objInfoIn.isAppended();

		if (this.containsKey(key)) {
			OBJInfoEntry obj = get(key);
			obj.setEn(en);
			obj.setEnCorrected(enCorrected);
			obj.setEnLong(enLong);
			obj.setLeftRight(leftRight);
			obj.addDate(updateDate);
			obj.addDir(filename);
			obj.setIsAppended(isAppended);
		} else {
			OBJInfoEntry obj = new OBJInfoEntry();
			obj.setEn(en);
			obj.setEnCorrected(enCorrected);
			obj.setEnLong(enLong);
			obj.setLeftRight(leftRight);			
			obj.addDate(updateDate);
			obj.addDir(filename);
			obj.setIsAppended(isAppended);
			put(key, obj);
		}
	}

	public List<Date> getDates(String en) {
		if (this.containsKey(en)) {
			OBJInfoEntry obj = get(en);
			return obj.getDates();
		} else {
			return new ArrayList<Date>();
		}
	}

	public Date getLastUpdate(String en) {
		if (this.containsKey(en)) {
			return get(en).getLastUpdate();
		} else {
			return null;
		}
	}

	public List<String> getDirs(String en) {
		if (this.containsKey(en)) {
			OBJInfoEntry obj = get(en);
			return obj.getDirs();
		} else {
			return new ArrayList<String>();
		}
	}

	public String getLatestFile(String en) {
		if (this.containsKey(en)) {
			return get(en).getLatestFile();
		} else {
			return null;
		}
	}

	/**
	 * test code
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String en = "distal phalanx of left thumb";
		String enCorrected = "distal phalanx of left big toe";
		String longForm = "distal phalanx of left big toe";
		String updateDate = "2008/10/21";
		String absolutePath = "C:/ag/data/stl/0.01/081021-bone04";

		OBJInfoEntry objInfo = new OBJInfoEntry();
		objInfo.setEn(en);
		objInfo.setEnCorrected(enCorrected);
		objInfo.setEnLong(longForm);
		objInfo.addDate(updateDate);
		objInfo.addDir(absolutePath);

		/** OBJInfoSetに追加 **/
		OBJInfo objInfoSet = new OBJInfo();
		objInfoSet.add(en, objInfo);

		objInfoSet.get(en).display();
	}

}
