package jp.dbcls.bp3d.ta.bits;

import jp.dbcls.bp3d.fma.*;

public class TABitsEntry implements Cloneable {
	private String taId = null;
	private double taTab = 0.0;
	private String taKanji = null;
	private String taEn = null;
	private FMAOBOEntry fma = new FMAOBOEntry();
	private FMAOBOEntry fmaFromEn = new FMAOBOEntry();
	private String classification = null;
	private String edit = null;

	static final String IDENTICAL = "IDENTICAL";
	static final String NOTIDENTICAL = "NOTIDENTICAL";
	static final String NOFMA = "NOFMA";
	static final String ORIGINAL = "ORIGINAL"; // オリジナルデータのIDがfmaobo2にも存在した
	static final String NOFMAOBO2 = "NOFMAOBO2"; // オリジナルデータのIDがfmaobo2には存在しない
	/** for supplement list **/
	static final String DELETE = "DELETE"; // 削除
	static final String ADD = "ADD"; // 追加
		
	public TABitsEntry(){}

	@Override
	public Object clone() {	//throwsを無くす
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
	}
		
	public String getEdit() {
		return edit;
	}

	public void setEdit(String edit) {
		this.edit = edit;
	}

	public String getTaId() {
		return taId;
	}

	public void setTaId(String taId) {
		this.taId = taId;
	}

	public double getTaTab() {
		return taTab;
	}

	public void setTaTab(double taTab) {
		this.taTab = taTab;
	}

	public String getTaKanji() {
		return taKanji;
	}

	public void setTaKanji(String taKanji) {
		this.taKanji = taKanji;
	}

	public String getTaEn() {
		return taEn;
	}

	public void setTaEn(String taEn) {
		this.taEn = taEn;
	}

	public FMAOBOEntry getFma() {
		return fma;
	}

	public void setFma(FMAOBOEntry fma) {
		this.fma = fma;
	}

	public FMAOBOEntry getFmaFromEn() {
		return fmaFromEn;
	}

	public void setFmaFromEn(FMAOBOEntry fmaFromEn) {
		this.fmaFromEn = fmaFromEn;
	}

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}
}
