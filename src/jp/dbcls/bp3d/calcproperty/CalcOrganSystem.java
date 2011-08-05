package jp.dbcls.bp3d.calcproperty;

/**
 * OrganSystemを計算します。
 * 
 * @author mituhasi
 * 
 */

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

import jp.dbcls.bp3d.*;
import jp.dbcls.bp3d.util.Bp3dUtility;

public class CalcOrganSystem {
	private final String DATADIR = Bp3dProperties.getString("bp3d.datadir") + "/" 
		+ Bp3dProperties.getString("bp3d.dataversion");
		
	Bp3d bp3d;
	TraverseBp3d bp3dTraverse;
	OrganSystem organSystem;
	Map<String, Integer> statsByOrganSystem;
	
	/*
	 * invoked in MakeBp3d1
	 * 
	 * @throws Exception
	 */
	public CalcOrganSystem(Bp3d bp3d) throws Exception {
		this.bp3d = bp3d;
		this.bp3dTraverse = bp3d.getBp3dTraverse();
		this.organSystem = new OrganSystem(bp3d);
		this.statsByOrganSystem = new TreeMap<String, Integer>();
	}
		
	/**
	 * OrganSystem(TAIDのリスト）を取得する
	 * @param ent
	 * @return
	 * @throws Exception
	 */
	public SortedSet<String> calcOrganSystem(Bp3dEntry ent) throws Exception {				
		SortedSet<String> hits = new TreeSet<String>();
		
		/**
		 * entがorganSystem名そのものの場合
		 */
		if(ent.getEn().equals("human body")){
			hits.add("A01");
		}else if(organSystem.contains(ent.getEn())){
			hits.add(organSystem.getEn2Id(ent.getEn()));
		}else{		
			/**
			 * entがorganSystemでない場合
			 */
			for (String aId : bp3dTraverse.getAncestors(ent.getId())){			
				Bp3dEntry aEnt = bp3d.getEntry(aId);
				if(organSystem.contains(aEnt.getEn())){
					hits.add(organSystem.getEn2Id(aEnt.getEn()));
				}			
			}

			/**
			 * 	A01:non-physical anatomical entity以外にヒットした場合はそれを優先する
			 */
			if(hits.contains("A01")&& hits.size() > 1){
				hits.remove("A01");
			}
		}
				
		return hits;
	}
	
	private void write(String outFile, String code) throws Exception {
		FileOutputStream fos = new FileOutputStream(outFile, false);
		OutputStreamWriter osw = new OutputStreamWriter(fos, code);
		BufferedWriter bw = new BufferedWriter(osw);
		
		bw.write("<html>\n");			
		bw.write("<body>\n");
		bw.write("<table border=1>\n");
		bw.write("<tr>");			
		bw.write("<th>Organ system</th>");	
		bw.write("<th>器官系</th>");
		bw.write("<th>パーツ数</th>");
		bw.write("</tr>\n");

		int total = 0;
		
		for(String taIds : statsByOrganSystem.keySet()){		
			bw.write("<tr>");			
			bw.write("<td>" + organSystem.getId2En(taIds) + "</td>");
			bw.write("<td>" + organSystem.getId2Kanji(taIds) + "</td>");
			bw.write("<td align=right>" + statsByOrganSystem.get(taIds) + "</td>");
			bw.write("</tr>\n");
			total += statsByOrganSystem.get(taIds);
		}
		
		bw.write("<tr>");			
		bw.write("<td align=center colspan=2>合計</td>");
		bw.write("<td align=right>" + total + "</td>");
		bw.write("</tr>\n");		
		bw.write("</table>\n");
		bw.write("</body>\n");
		bw.write("</html>\n");			
		
		bw.close();
		osw.close();
		fos.close();
	}
	
	/**
	 * 1. 各臓器の器官系を求める
	 * 2. 器官系毎にパーツ数の合計を求める
	 * 
	 * @throws Exception
	 */
	public void run() throws Exception {
		for (Bp3dEntry bp3dEnt : bp3d.getAllEntries()){
			SortedSet<String> taIds = calcOrganSystem(bp3dEnt);
			bp3dEnt.setOrganSystem(Bp3dUtility.join(organSystem.getId2En(taIds), "/"));
			
			String taIdsStr = Bp3dUtility.join(taIds, "/");
			if(statsByOrganSystem.containsKey(taIdsStr)){
				int count = statsByOrganSystem.get(taIdsStr);	
				statsByOrganSystem.put(taIdsStr, count + 1);	
			}else{
				statsByOrganSystem.put(taIdsStr, 1);
			}
		}
		
		String outFile = this.DATADIR + "/releaseNote/stats.html";
		write(outFile, "UTF-8");
	}
		
}
