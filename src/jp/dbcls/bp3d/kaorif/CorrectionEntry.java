package jp.dbcls.bp3d.kaorif;

/**
 * 正誤表の１エントリ
 * 
 * @author ag
 * 
 */
public class CorrectionEntry {
	String piece;
	String correction;
	String cly;
	String comment;
	boolean isUsed = false; // 使われたか？

	public CorrectionEntry() throws Exception {
	}

	public String getPiece() {
		return piece;
	}

	public void setPiece(String piece) {
		this.piece = piece;
	}

	public String getCorrection() {
		return correction;
	}

	public void setCorrection(String correction) {
		this.correction = correction;
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

	public boolean isUsed() {
		return isUsed;
	}

	public void setUsed(boolean isUsed) {
		this.isUsed = isUsed;
	}

}
