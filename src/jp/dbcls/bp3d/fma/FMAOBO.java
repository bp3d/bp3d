package jp.dbcls.bp3d.fma;

import java.io.*;
import java.util.*;

import jp.dbcls.bp3d.Bp3dProperties;

/**
 * FMAOBO ver.2(FMALite)をパースする
 * 
 * @author mituhasi
 * 
 */
public class FMAOBO {
	/** デバッグ出力フラグ **/
	private final boolean isDebug = false;
	
	/** FMAIDとFMAOBOEntryオブジェクトとの対応表 **/
	public Map<String, FMAOBOEntry> id2entry = new HashMap<String, FMAOBOEntry>();
	/** term(name, exact synonym)とFMAOBOEntryオブジェクトとの対応表 **/
	public SortedMap<String, FMAOBOEntry> term2entry = new TreeMap<String, FMAOBOEntry>();
	/** FMAIDだけ存在して、エントリのデータがないもの **/
	public SortedMap<String, String> idOnly= new TreeMap<String, String>();
	
	public FMAOBO() throws IOException {
		readFile(Bp3dProperties.getString("bp3d.fmafile"));
	}

	public FMAOBO(String fmafile) throws IOException {
		readFile(fmafile);
		addEntry();
	}

	public FMAOBOEntry createFMAOBOEntry() {
		FMAOBOEntry entry = new FMAOBOEntry();
		return entry;
	}

	/**
	 * FMAOBOEntryを作成
	 * 
	 * @param filename
	 * @throws IOException
	 */
	private void readFile(String filename) throws IOException {
		FileInputStream is = new FileInputStream(filename);
		InputStreamReader in = new InputStreamReader(is, "UTF-8");
		BufferedReader br = new BufferedReader(in);
		String oboEntryStr = "";

		/** read header **/
		while (!br.readLine().equals("")) {
		}

		/** read [Term] **/
		String line = null;
		while ((line = br.readLine()) != null) {
			if (line.equals("[Term]")) {
				while ((line = br.readLine()) != null) {
					if (line.equals("")) {
						FMAOBOEntry entry = createFMAOBOEntry();
						parseOBOEntry(entry, oboEntryStr);
						this.id2entry.put(entry.getId(), entry);
						this.term2entry.put(entry.getName().toLowerCase(), entry);
						for (String s : entry.getExactSynonym()) {
							if(term2entry.containsKey(s.toLowerCase())){
								if(isDebug){
									System.out.println("FMAOBO: duplicated term=" + s.toLowerCase());
								}
							}
							this.term2entry.put(s.toLowerCase(), entry);
						}
						oboEntryStr = "";
						break;
					} else {
						oboEntryStr += line + "\n";
					}
				}
			}
		}

		br.close();
		is.close();

		/**
		 * part-of, is-aの部分のFMAOBOEntryを作成する
		 **/
		is = new FileInputStream(filename);
		in = new InputStreamReader(is, "UTF-8");
		br = new BufferedReader(in);
		oboEntryStr = "";

		/** read header **/
		while (!br.readLine().equals("")) {
		}

		/** read [Term] **/
		line = null;
		while ((line = br.readLine()) != null) {
			if (line.equals("[Term]")) {
				while ((line = br.readLine()) != null) {
					if (line.equals("")) {
						parseRelationship(oboEntryStr);
						oboEntryStr = "";
						break;
					} else {
						oboEntryStr += line + "\n";
					}
				}
			}
		}
	}

	/**
	 * parse OBO Text entry relationship:"(is-a, hasPart, part-of)を除く
	 * 
	 * @param obo
	 */
	private void parseOBOEntry(FMAOBOEntry entry, String obo) {
		StringTokenizer st = new StringTokenizer(obo, "\n");
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			if (line.startsWith("relationship:") || line.startsWith("is_a:")) {
			} else if (line.startsWith("synonym:")) {
				entry.parseExactSynonym(line);
			} else if (line.startsWith("id:")) {
				entry.parseId(line);
			} else if (line.startsWith("name:")) {
				entry.parseName(line);
			} else if (line.startsWith("def:")) {
				entry.parseDef(line);
			} else if (line.startsWith("[Term]") || line.startsWith("namespace")
					|| line.startsWith("is_transitive")) {
			} else if (line.startsWith("[Typedef]")) {
				return;
			} else {
				if(isDebug){
					System.out.println("Unknown attribute:" + line);
				}
			}
		}
	}

	/**
	 * "relationship:"(is-a, hasPart, part-of)をパースする
	 * 
	 * @param obo
	 */
	private void parseRelationship(String obo) {
		String id1;
		FMAOBOEntry ent1 = null;
		StringTokenizer st1 = new StringTokenizer(obo, "\n");

		while (st1.hasMoreTokens()) {
			String line = st1.nextToken();
			if (line.startsWith("id:")) {
				id1 = line.substring(line.indexOf(":") + 2).replace(":", "");
				ent1 = id2entry.get(id1);
			} else if (line.startsWith("is_a:")) {
				StringTokenizer st2 = new StringTokenizer(line);
				st2.nextToken();
				String id2 = st2.nextToken().replace(":", "");
				String name2 = "";
				if(line.contains("!")){
					name2 = line.substring(line.indexOf("!"));
				}
				FMAOBOEntry ent2 = id2entry.get(id2);
				if(ent2 == null){
					this.idOnly.put(id2, name2);
					continue;
				}
				ent1.setIsA(ent2);
				ent2.addReverseIsA(ent1);
			} else if (line.startsWith("relationship:")) {
				StringTokenizer st2 = new StringTokenizer(line);
				st2.nextToken();
				String type = st2.nextToken();
				String id2 = st2.nextToken().replace(":", "");
				FMAOBOEntry ent2 = id2entry.get(id2);
				if (type.equals("constitutional_part_of") ||
						type.equals("regional_part_of") ||
						type.equals("systemic_part_of")) {
					ent1.addPartOf(ent2);
					ent2.addHasPart(ent1);
				} else {
					if(isDebug){
						System.err.println("Unknown Relationships:" + line);
					}
				}
			}
		}
	}


	public FMAOBOEntry get(String term) {
		if(term.startsWith("FMA") || term.startsWith("fma")){
			return getById(term);
		}else{
			return getByName(term);
		}
	}
	
	public FMAOBOEntry getById(String id) {
		return id2entry.get(id);
	}

	public FMAOBOEntry getByName(String name) {
		return term2entry.get(name.toLowerCase().trim());
	}

	public FMAOBOEntry getByName(Collection<String> names) {
		FMAOBOEntry hit = null;
		for (String name : names) {
			hit = this.term2entry.get(name.toLowerCase().trim());
			if (hit != null) {
				break;
			}
		}
		return hit;
	}
	
	public FMAOBOEntry getByName(String[] names) {
		FMAOBOEntry hit = null;
		for (String name : names) {
			hit = term2entry.get(name.toLowerCase().trim());
			if (hit != null) {
				break;
			}
		}
		return hit;
	}

	public Collection<String> getAllIds() {
		return id2entry.keySet();
	}

	public Collection<FMAOBOEntry> getAllEntries() {
		return id2entry.values();
	}

	/**
	 * term/FMAIDが含まれているかを判定する
	 * 
	 * @param term
	 * @return
	 */
	public boolean contains(String term) {
		if(id2entry.containsKey(term)){
			return true;
		}		
		return term2entry.containsKey(term.toLowerCase());		
	}
	
	/**
	 * term(synonymを含む), exact name, FMAIDの対応表を出力
	 * 
	 * @param filename
	 * @throws Exception
	 */
	private void writeTerm2Id(String filename) throws Exception {
		FileOutputStream os = new FileOutputStream(filename);
		OutputStreamWriter out = new OutputStreamWriter(os, "UTF-8");
		BufferedWriter bw = new BufferedWriter(out);

		for (String term : term2entry.keySet()) {
			String id = term2entry.get(term).getId();
			String name = id2entry.get(id).getName();
			String def = id2entry.get(id).getDef();
			bw.write(term + "\t" + id + "\t" + name + "\t" + def + "\n");
		}

		bw.close();
	}

	/**
	 * Entryの追加
	 */
	public void addEntry() {
		String id = "FMA242787";
		String name = "ventricular system of brain";
		FMAOBOEntry ent = new FMAOBOEntry();
		ent.setId(id);
		ent.setName(name);

		id2entry.put(id, ent);
		term2entry.put(name, ent);
	}

	/**
	 * enがpreferred nameか判定する
	 * @param en
	 * @return
	 */
	public boolean isPreferredName(String en){
		if(!contains(en)){
			return false;
		}
		
		return this.get(en).getName().equals(en);				
	}

	/**
	 * enをpreferred nameに変換する
	 * @param en
	 * @return
	 */
	public String toPreferredName(String en){
		if(contains(en)){
			return get(en).getName();
		}else{
			return en;
		}		
	}
	
	/**
	 * エントリ数を数える
	 * @throws Exception
	 */
	private void calcTermFrequency() throws Exception {
		Map<String, Integer> tf = new HashMap<String, Integer>();

		for (FMAOBOEntry ent : this.id2entry.values()) {
			String name = ent.getName();
			String[] terms = name.split("[ ]+");
			for (String term : terms) {
				if (tf.containsKey(term)) {
					tf.put(term, tf.get(term) + 1);
				} else {
					tf.put(term, 1);
				}
			}
		}

		ArrayList<Map.Entry<String, Integer>> tfList = new ArrayList<Map.Entry<String, Integer>>(
				tf.entrySet());

		Collections.sort(tfList, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> obj1,
					Map.Entry<String, Integer> obj2) {
				Map.Entry<String, Integer> ent1 = obj1;
				Map.Entry<String, Integer> ent2 = obj2;
				int val1 = ent1.getValue();
				int val2 = ent2.getValue();

				return (val1 < val2 ? 1 : -1);
			}
		});

		String filename = Bp3dProperties.getString("bp3d.dictionarydir")
				+ "/FMAOBO/tf.txt";
		FileOutputStream os = new FileOutputStream(filename);
		OutputStreamWriter out = new OutputStreamWriter(os, "UTF-8");
		BufferedWriter bw = new BufferedWriter(out);

		for (Map.Entry<String, Integer> tfEntry : tfList) {
			bw.write(tfEntry.getKey() + "\t" + tfEntry.getValue() + "\n");
		}

		bw.close();
		out.close();
		os.close();
	}

	/**
	 * FMAIDのみのエントリを表示する
	 */
	public void displayIdOnly(){
		for(String id : this.idOnly.keySet()){
			System.out.println("No Entry id=" + id + ", name=" + idOnly.get(id));
		}
	}

	/**
	 * FMAOBOEntryのCollectionの中身を表示する
	 * @param ents
	 */
	public static void display(Collection<FMAOBOEntry> ents){
		for(FMAOBOEntry ent : ents){
			ent.display();
		}
	}
	
	public static void main(String[] args) throws Exception {
		FMAOBO fmaobo = new FMAOBO();
		System.out.println(fmaobo.getByName("cerebrum"));
	}
}
