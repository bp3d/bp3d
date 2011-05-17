package jp.dbcls.bp3d.kaorif.visiblebody.nerves;

public class VisibleBodyManuallyMapped {
	private String original = null; /** visible bodyのオリジナルの名称 **/
	private String renamed = null; /** visible bodyのrename後の名称 **/
	private String av = "";    /** artery/vein **/
	private String remark = "";    /** remark**/
	
	public String getOriginal() {
		return original;
	}
	public void setOriginal(String original) {
		this.original = original;
	}
	public String getRenamed() {
		return renamed;
	}
	public void setRenamed(String renamed) {
		this.renamed = renamed;
	}	
	public String getAv() {
		return av;
	}
	public void setAv(String av) {
		this.av = av;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
}
