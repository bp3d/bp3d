package jp.dbcls.bp3d.kaorif.visiblebody.nerves;

import jp.dbcls.bp3d.fma.*;
import jp.dbcls.bp3d.ta.*;
import java.util.*;

public class VisibleBodyEntry {
	private String name = null;  /** visible bodyのオリジナルの名称 **/
	private String coreName = null; /** left/right, branch artery/veinなどを削ぎ落とした名称 **/
	private int indent = 0;
	private FMAOBOEntry fma;
	private Set<TAEntry> ta = new HashSet<TAEntry>();
	private String leftRight;	/** left, rightの区別 **/
	private String modifier; /** branch, superior/inferior などの修飾語 **/
	private boolean isExactMatch = false; /** exact matchでFMAにマッピングできたか */
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
	public String getModifier() {
		return modifier;
	}
	public void setModifier(String modifier) {
		this.modifier = modifier;
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
}
