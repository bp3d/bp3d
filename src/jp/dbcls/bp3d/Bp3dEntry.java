package jp.dbcls.bp3d;

import java.text.*;
import java.util.*;

public class Bp3dEntry {
	protected String id = null;
	protected String en = null;
	protected String kanji = null;
	protected String kana = null;
	protected String taId = null;
	protected Date lastUpdate = null;
	protected Double zmin = -1.0; // zvalue > 0
	protected Double zmax = -1.0; // zvalue > 0
	protected Double volume = -1.0; // volume >= 0
	protected String organSystem = null;
	protected Bp3dEntryType type = null;
	protected String objPath  = null;
	protected Set<Bp3dEntry> toDivide = new HashSet<Bp3dEntry>();
	
	public Bp3dEntry() {}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setEn(String en) {
		this.en = en;
	}

	public String getEn() {
		return en;
	}

	public void setKanji(String kanji) {
		this.kanji = kanji;
	}

	public String getKanji() {
		return kanji;
	}

	public void setKana(String kana) {
		this.kana = kana;
	}

	public String getKana() {
		return kana;
	}
	
	public String getTaId() {
		return taId;
	}

	public void setTaId(String taId) {
		this.taId = taId;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public String getLastUpdateString() {
		if (lastUpdate == null) {
			return null;
		} else {
			return new SimpleDateFormat("yyyy/MM/dd").format(lastUpdate);
		}
	}

	public void setLastUpdate(String lastUpdate) throws Exception {
		this.lastUpdate = DateFormat.getDateInstance().parse(lastUpdate);
	}

	public void setLastUpdate(Date lastUpdate) throws Exception {
		this.lastUpdate = lastUpdate;
	}
	
	public void setOrganSystem(String organSystem) {
		this.organSystem = organSystem;
	}

	public String getOrganSystem() {
		return organSystem;
	}

	/**
	 * 
	 * @return
	 */
	public Double getVolume() {
		return volume;
	}

	/**
	 * @param volume
	 *          the volume to set
	 */
	public void setVolume(Double volume) {
		this.volume = volume;
	}
	
	public Bp3dEntryType getType() {
		return type;
	}

	public void setType(Bp3dEntryType type) {
		this.type = type;
	}

	public String getObjPath() {
		return objPath;
	}

	public void setObjPath(String objPath) {
		this.objPath = objPath;
	}

	public Set<Bp3dEntry> getToDivide() {
		return toDivide;
	}

	public void addToDivide(Bp3dEntry toDivide) {
		this.toDivide.add(toDivide);
	}

	public boolean isCompositePrimitive(){
		return getType().equals(Bp3dEntryType.COMPOSITE_PRIMITIVE);
	}

	public boolean isComposite(){
		return getType().equals(Bp3dEntryType.COMPOSITE);
	}

	public boolean isCompositeAnonymous(){
		return getType().equals(Bp3dEntryType.COMPOSITE_ANONYMOUS);
	}
	
	public boolean isCompleted(){
		return getType().equals(Bp3dEntryType.COMPLETED);
	}

	public boolean isCompletedAnonymous(){
		return getType().equals(Bp3dEntryType.COMPLETED_ANONYMOUS);
	}
	
	public void display() {
		System.out.print("id=" + getId());
		System.out.print(",en=" + getEn());
		System.out.print(",kanji=" + getKanji());
		System.out.print(",kana=" + getKana());
		System.out.print(",TAID=" + this.getTaId());
		System.out.println(",volume=" + getVolume());
	}

	/**
	 * test code
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Bp3dEntry ent = new Bp3dEntry();
		String line = "FMA62004" + "\t" + "medulla oblongata" + "\t" + "延髄" + "\t"
				+ "えんずい" + "\t" + "1" + "\t" + "2007/10/05" + "\t" + "nervous system"
				+ "\t" + "brain" + "\t" + "5";
		ent.display();
	}

}
