package jp.dbcls.bp3d.fma;

import java.util.*;

import jp.dbcls.bp3d.*;
//import jp.dbcls.bp3d.tree.*;

/**
 * FMA OBOの各エントリ
 * 
 */
public class FMAOBOEntry {
	private String id = null;
	private String name = null;
	private String def = null;
	private Set<String> exactSynonym = new HashSet<String>();
	private FMAOBOEntry isA = null;
	private Set<FMAOBOEntry> reverseIsA = new HashSet<FMAOBOEntry>();
	private Set<FMAOBOEntry> hasPart = new HashSet<FMAOBOEntry>();
	private Set<FMAOBOEntry> partOf = new HashSet<FMAOBOEntry>();

	public FMAOBOEntry() {}

	/**
	 * @return the def
	 */
	public String getDef() {
		return def;
	}

	/**
	 * @param def
	 *          the def to set
	 */
	public void setDef(String def) {
		this.def = def;
	}

	/**
	 * @return the exactSynonym
	 */
	public Set<String> getExactSynonym() {
		return exactSynonym;
	}

	public String getExactSynonym(String del) {
		String ret = "";
		for (String synonym : getExactSynonym()) {
			ret += synonym + del;
		}
		if (ret.length() > 0) {
			ret = ret.substring(0, ret.length() - 1);
		}
		return ret;
	}

	/**
	 * @param exactSynonym
	 *          the exactSynonym to set
	 */
	public void setExactSynonym(Set<String> exactSynonym) {		
		this.exactSynonym = exactSynonym;
	}

	public void addExactSynonym(String id) {
		this.exactSynonym.add(id);
	}

	public void setIsA(FMAOBOEntry ent) {
		this.isA = ent;
	}

	/**
	 * @return the hasPart
	 */
	public Set<FMAOBOEntry> getHasPart() {
		return hasPart;
	}

	/**
	 * @param hasPart
	 *          the hasPart to set
	 */
	public void setHasPart(Set<FMAOBOEntry> hasPart) {
		this.hasPart = hasPart;
	}

	public void addHasPart(FMAOBOEntry ent) {
		this.hasPart.add(ent);
	}

	/**
	 * @return the hasPart
	 */
	public Set<FMAOBOEntry> getPartOf() {
		return partOf;
	}

	/**
	 * @param hasPart
	 *          the hasPart to set
	 */
	public void setPartOf(Set<FMAOBOEntry> partOf) {
		this.partOf = partOf;
	}

	public void addPartOf(FMAOBOEntry partOf) {
		this.partOf.add(partOf);
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *          the id to set
	 */
	public void setId(String id) {
		id = id.replace("FMA:", "FMA");
		this.id = id;
	}

	/**
	 * @return the isA
	 */
	public FMAOBOEntry getIsA() {
		return isA;
	}

	/**
	 * @return the reverseIsA
	 */
	public Set<FMAOBOEntry> getReverseIsA() {
		return reverseIsA;
	}

	/**
	 * @param isA
	 *          the isA to set
	 */
	public void addReverseIsA(FMAOBOEntry ent) {
		this.reverseIsA.add(ent);
	}

	public void removeReverseIsA(FMAOBOEntry ent) {
		this.reverseIsA.remove(ent);
	}

	public void removeReverseIsAAll(Collection<FMAOBOEntry> ents) {
		this.reverseIsA.removeAll(ents);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	public Set<String> getEn() {
		Set<String> ens = new HashSet<String>();
		ens.add(name);
		for (String synonym : exactSynonym) {
			ens.add(synonym);
		}
		return ens;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	protected void parseId(String st) {
		st = st.substring(st.indexOf(":") + 2);
		setId(st);
	}

	protected void parseName(String st) {
		st = st.substring(st.indexOf(":") + 2).toLowerCase();
		setName(st);
	}

	protected void parseDef(String st) {
		st = st.substring(st.indexOf("\"") + 1);
		st = st.substring(0, st.indexOf("\" []"));
		setDef(st);
	}

	protected void parseExactSynonym(String st) {
		st = st.substring(st.indexOf("\"") + 1);
		st = st.substring(0, st.indexOf("\" EXACT []"));
		addExactSynonym(st);
	}

	public void display() {
		System.out.println("id=" + getId());
		System.out.println("name=" + getName());
		System.out.println("def=" + getDef());
		System.out.println("exact_synonym=" + getExactSynonym().toString());
		System.out.println("is_a=" + getIsA().toString());
		System.out.println("reverse_is_a=" + getReverseIsA().toString());
		System.out.println("has_part=" + getHasPart().toString());
		System.out.println("part_of=" + getPartOf().toString());
	}
}
