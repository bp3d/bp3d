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
			hits.add("A01.1");
		}else if(!ent.getTaId().equals("null")){  // TAIDをもつFMAエントリの場合
			String taId = ent.getTaId();
			hits.add(getOrganSystemFromTaId(taId));
		}else{																		// TAIDをもたないFMAエントリの場合は、親のTAIDをとる
			String fmaId = ent.getId().replace("nsn", "");
			for(String pId : bp3d.getParents(fmaId)){
				Bp3dEntry pEnt = bp3d.getEntry(pId);
				if(!pEnt.getTaId().equals("null")){
					hits.add(getOrganSystemFromTaId(pEnt.getTaId()));
				}
			}
			
			if(hits.size() == 0){  // 親のTAIDをとれない場合は、祖先のTAIDをとる
				for(String pId : bp3dTraverse.getAncestors(fmaId)){
					Bp3dEntry pEnt = bp3d.getEntry(pId);
					if(!pEnt.getTaId().equals("null")){
						hits.add(getOrganSystemFromTaId(pEnt.getTaId()));
					}
				}
			}

		}
						
		/**
		 * 	A00を除き、A01.01とA01.02以外にヒットした場合はそれを優先する
		 */
		hits.remove("A00");
		if(hits.contains("A01.2") && hits.size() > 1){
			hits.remove("A01.2");
		}
		if(hits.contains("A01.1") && hits.size() > 1){
			hits.remove("A01.1");
		}
		
		return hits;
	}
	
	private String getOrganSystemFromTaId(String taId){
		if(taId.startsWith("A01")){
			taId = taId.substring(0, 5);
		}else{
			taId = taId.substring(0, 3);
		}
		
		return taId;
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


	private void writeEn(String outFile, String code) throws Exception {
		FileOutputStream fos = new FileOutputStream(outFile, false);
		OutputStreamWriter osw = new OutputStreamWriter(fos, code);
		BufferedWriter bw = new BufferedWriter(osw);
		
		bw.write("<html>\n");			
		bw.write("<body>\n");
		bw.write("<table border=1>\n");
		bw.write("<tr>");			
		bw.write("<th>Organ system</th>");	
		bw.write("<th># of parts</th>");
		bw.write("</tr>\n");

		int total = 0;
		
		for(String taIds : statsByOrganSystem.keySet()){
			bw.write("<tr>");
			bw.write("<td>" + organSystem.getId2En(taIds) + "</td>");
			bw.write("<td align=right>" + statsByOrganSystem.get(taIds) + "</td>");
			bw.write("</tr>\n");
			total += statsByOrganSystem.get(taIds);
		}
		
		bw.write("<tr>");			
		bw.write("<td align=center>Total</td>");
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

		String outFileEn = this.DATADIR + "/releaseNote/stats_e.html";
		writeEn(outFileEn, "UTF-8");				
	}
		
}
