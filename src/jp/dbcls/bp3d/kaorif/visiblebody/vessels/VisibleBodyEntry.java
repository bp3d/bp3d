package jp.dbcls.bp3d.kaorif.visiblebody.vessels;

import java.util.HashSet;
import java.util.Set;

import jp.dbcls.bp3d.fma.*;
import jp.dbcls.bp3d.ta.TAEntry;

public class VisibleBodyEntry {
	private String name = null;
	/** visible bodyのオリジナルの名称 **/
	private String coreName = null;
	/** left/right, branch artery/veinなどをそぎ落とした名称 **/
	private int indent = 0;
	private FMAOBOEntry fma;
	private Set<TAEntry> ta = new HashSet<TAEntry>();
	private VisibleBodyEntry upper;
	/** tree上の親、DAGになっていることに注意 **/
	private String leftRight;
	/** left, rightの区別 **/
	private String modifier;
	/** branch, superior/inferior などの修飾語 **/
	private String arteryVein;
	/** arteries, veinsの下に入っているかのフラグ **/
	private boolean isOrderedByKaorif = false;
	/** 藤枝さんがモデリングするパーツか **/
	private boolean isExactMatch = false;
	/** exact matchでFMAにマッピングできたか */
	private VisibleBodyManuallyMapped manuallyMapped = null;
	
	public Set<TAEntry> getTa() {
		return ta;
	}

	public void setTa(Set<TAEntry> ta) {
		this.ta = ta;
	}

	public VisibleBodyManuallyMapped getManuallyMapped() {
		return manuallyMapped;
	}

	public void setManuallyMapped(VisibleBodyManuallyMapped manuallyMapped) {
		this.manuallyMapped = manuallyMapped;
	}

	public boolean isExactMatch() {
		return isExactMatch;
	}

	public void setExactMatch(boolean isExactMatch) {
		this.isExactMatch = isExactMatch;
	}

	public boolean isOrderedByKaorif() {
		return isOrderedByKaorif;
	}

	public void setOrderedByKaorif(boolean isOrderedByKaorif) {
		this.isOrderedByKaorif = isOrderedByKaorif;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	public String getArteryVein() {
		return arteryVein;
	}

	public void setArteryVein(String arteryVein) {
		this.arteryVein = arteryVein;
	}

	public String getCoreName() {
		return coreName;
	}

	public void setCoreName(String coreName) {
		this.coreName = coreName;
	}

	public String getLeftRight() {
		return leftRight;
	}

	public void setLeftRight(String leftRight) {
		this.leftRight = leftRight;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getIndent() {
		return indent;
	}

	public void setIndent(int indent) {
		this.indent = indent;
	}

	public FMAOBOEntry getFma() {
		return fma;
	}

	public void setFma(FMAOBOEntry fma) {
		this.fma = fma;
	}

	public VisibleBodyEntry getUpper() {
		return upper;
	}

	public void addUpper(VisibleBodyEntry upper) {
		this.upper = upper;
	}
}
