package jp.dbcls.bp3d.kaorif;

/**
 * Ignoreテーブルの１エントリ
 * 
 * @author ag
 * 
 */
public class IgnoreEntry {
	String piece;
	String cly;
	String comment;
	boolean isIgnored = false; // 無視するか？
	boolean isApplied = false; // 適用されたか？

	public IgnoreEntry() throws Exception {
	}

	public String getPiece() {
		return piece;
	}

	public void setPiece(String piece) {
		this.piece = piece;
	}

	public String getCly() {
		return cly;
	}

	public void setCly(String cly) {
		this.cly = cly;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isApplied() {
		return isApplied;
	}

	public void setApplied(boolean isApplied) {
		this.isApplied = isApplied;
	}

	public boolean isIgnored() {
		return isIgnored;
	}

	public void setIgnored(boolean isIgnored) {
		this.isIgnored = isIgnored;
	}
}
