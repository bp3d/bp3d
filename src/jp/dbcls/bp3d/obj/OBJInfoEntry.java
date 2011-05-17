package jp.dbcls.bp3d.obj;

import java.text.*;
import java.util.*;

import jp.dbcls.bp3d.Bp3dProperties;

/**
 * 同一FMAIDに対応するOBJファイルの情報(複数のファイル存在する場合も含めて）を保持するクラス
 * 
 * @author ag
 * 
 */
public class OBJInfoEntry {
	private final String PATHSEPARATOR = Bp3dProperties
			.getString("bp3d.pathseparator"); // パスの区切り文字
	String en = null;
	String enCorrected = null;
	String enLong = null;
	String fmaId = null;
	String leftRight = null;  // left/rightの判定
	List<String> dirs = null; // OBJファイルのフルパス
	List<Date> dates = null; // 更新日
	Boolean isAppended = false; // AppendPolyで作られている場合は、true

	public OBJInfoEntry() {
		this.dirs = new ArrayList<String>();
		this.dates = new ArrayList<Date>();
	}
	
	/**
	 * 
	 * @return
	 */
	public String getLeftRight() {
		return leftRight;
	}

	/**
	 * 
	 * @param leftRight
	 */
	public void setLeftRight(String leftRight) {
		this.leftRight = leftRight;
	}

	/**
	 * @return the en
	 */
	public String getEn() {
		return en;
	}

	/**
	 * @param en
	 *          the en to set
	 */
	public void setEn(String en) {
		this.en = en;
	}

	/**
	 * 
	 * @return
	 */
	public List<String> getDirs() {
		return dirs;
	}

	/**
	 * 
	 * @param dir
	 */
	public void addDir(String dir) {
		this.dirs.add(dir);
	}

	/**
	 * 最新のOBJファイルのフルパスを返す
	 * 
	 * @return
	 */
	public String getLatestFile() {
		int size = this.dirs.size();
		if (size > 0) {
			return this.dirs.get(size - 1);
		} else {
			return null;
		}
	}

	/**
	 * クレイファイル名のリスト(.clyはなし）を返す
	 * 
	 * @return
	 */
	public List<String> getClyNames() {
		List<String> ret = new ArrayList<String>();

		for (int i = 0; i < this.dirs.size(); i++) {
			ret.add(getClyName(i));
		}

		return ret;
	}

	/**
	 * リストの先頭からi-1番目のOBJファイルのクレイファイル名(.clyはなし）を返す
	 * 
	 * @param i
	 * @return
	 */
	public String getClyName(int i) {
		String path = getDirs().get(i);
		path = path.replace(PATHSEPARATOR, "/");
		String[] tkns = path.split("/");
		String clyName = tkns[tkns.length - 3];

		return clyName;
	}

	/**
	 * すべてのOBJファイルのピース名を返す
	 * 
	 * @return
	 */
	public List<String> getPieceNames() {
		List<String> ret = new ArrayList<String>();

		for (int i = 0; i < this.dirs.size(); i++) {
			ret.add(getPieceName(i));
		}

		return ret;
	}

	/**
	 * リストの先頭からi-1番目のOBJファイルのピース名を返す
	 * 
	 * @param i
	 * @return
	 */
	public String getPieceName(int i) {
		String path = getDirs().get(i);
		path = path.replace(PATHSEPARATOR, "/");
		String[] tkns = path.split("/");

		String clyName = getClyName(i);

		String pieceName = tkns[tkns.length - 1];
		String separator = "";
		if (pieceName.contains(clyName + "_")) {
			separator = "_";
		} else if (pieceName.contains(clyName + "-")) {
			separator = "-";
		} else {
			System.err
					.println("OBJInfo.getPieceName:invalid piece name=" + pieceName);
		}

		pieceName = pieceName.replace(clyName + separator, "").replace(".obj", "");

		// String pieceName = tkns[tkns.length - 1].replace(clyName + "-",
		// "").replace(".obj", "");

		return pieceName;
	}

	/**
	 * リストの最後（最新）のクレイファイル名を返す
	 * 
	 * @return
	 */
	public String getLatestClyName() {
		return getClyName(getDirs().size() - 1);
	}

	/**
	 * リストの最後（最新）のピース名を返す
	 * 
	 * @return
	 */
	public String getLatestPieceName() {
		return getPieceName(getDirs().size() - 1);
	}

	public List<Date> getDates() {
		return dates;
	}

	public Date getLastUpdate() {
		int size = this.dates.size();
		if (size > 0) {
			return this.dates.get(size - 1);
		} else {
			return null;
		}
	}

	public String getLastUpdateStr() {
		Date date = getLastUpdate();
		SimpleDateFormat sdf = new SimpleDateFormat("yy/mm/dd");
		return sdf.format(date);
	}

	/**
	 * @param lastUpdate
	 *          the lastUpdate to set
	 */
	public void addDate(String date) throws Exception {
		this.dates.add(DateFormat.getDateInstance().parse(date));
	}

	public void addDate(Date date) throws Exception {
		this.dates.add(date);
	}

	public Boolean isAppended() {
		return isAppended;
	}

	public void setIsAppended(Boolean isAppended) {
		this.isAppended = isAppended;
	}

	/**
	 * @return the enLong
	 */
	public String getEnLong() {
		return enLong;
	}

	/**
	 * @param enLong
	 *          the enLong to set
	 */
	public void setEnLong(String enLong) {
		this.enLong = enLong;
	}

	/**
	 * @return the enCorrected
	 */
	public String getEnCorrected() {
		return enCorrected;
	}

	/**
	 * @param enCorrected
	 *          the enCorrected to set
	 */
	public void setEnCorrected(String enCorrected) {
		this.enCorrected = enCorrected;
	}

	/**
	 * @return the fmaId
	 */

	public String getFmaId() {
		return fmaId;
	}
	
	/**
	 * @param fmaId
	 *          the fmaId to set
	 */
/**
	public void setFmaId(String fmaId) {
		this.fmaId = fmaId;
	}
**/
	
	public void display() {
		System.out.println("en=" + getEn());
		System.out.println("enCorrected=" + getEnCorrected());
		System.out.println("longName=" + this.getEnLong());
		System.out.println("updateDate=" + getDates());
		System.out.println("obj=" + getDirs());
	}
}
