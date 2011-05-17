package jp.dbcls.bp3d.ta;

import java.util.*;
import jp.dbcls.bp3d.*;
import jp.dbcls.bp3d.fma.*;

public class TAEntry implements Cloneable {
	private String taId = null;
	private double taTab = 0;
	private String taKanji = null;
	private String taEn = null;
	private String classification = null;
	private boolean existsPoly = false;
	private List<TAEntry> containedPoly = new ArrayList<TAEntry>();
	private boolean isCovered = false;

	public TAEntry(){}

	@Override
	public TAEntry clone() {	//throwsを無くす
		try {
			return (TAEntry)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError(e.toString());
		}
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

	public String getClassification() {
		return classification;
	}

	public void setClassification(String classification) {
		this.classification = classification;
	}

	public boolean isExistsPoly() {
		return existsPoly;
	}

	public void setExistsPoly(boolean existsPoly) {
		this.existsPoly = existsPoly;
	}

	public List<TAEntry> getContainedPoly() {
		return containedPoly;
	}

	public void setContainedPoly(List<TAEntry> containedPoly) {
		this.containedPoly = containedPoly;
	}

	public boolean isCovered() {
		return isCovered;
	}

	public void setCovered(boolean isCovered) {
		this.isCovered = isCovered;
	}
}
