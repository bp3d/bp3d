/**
The MIT License

Copyright (c) 2011, Database Center for Life Science (DBCLS)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
**/
package jp.dbcls.bp3d.ta;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import java.util.*;

import jp.dbcls.bp3d.*;
import jp.dbcls.bp3d.fma.FMAOBO;
import jp.dbcls.bp3d.fma.FMAOBOEntry;
import jp.dbcls.bp3d.obj.OBJInfo;
import jp.dbcls.bp3d.util.Bp3dUtility;

import jp.dbcls.bp3d.kaorif.visiblebody.VisibleBody;

/**
 * TAの各エントリに対しBp3dがカバーしているかを示す表を出力する
 * @author ag
 *
 */
public class TAReport {
	private final String LOGDIR = Bp3dProperties.getString("bp3d.datadir")
		+ "/" + Bp3dProperties.getString("bp3d.dataversion") + "/logs/";
	
	FMAOBO fmaobo;
	Bp3d bp3d;
	TA ta;
	OBJInfo objInfo;
	VisibleBody vb;
	TagTA tag = new TagTA();
	
	SortedMap<String, TAEntry> taId2TA;
		
	public TAReport() throws Exception {
		fmaobo = new FMAOBO();
		boolean forDeveloper = true;
		bp3d = new Bp3d(forDeveloper);
		ta = new TA(fmaobo);
		taId2TA = new TreeMap<String, TAEntry>();
		vb = new VisibleBody();
		tag = new TagTA();
	}	
	
	private boolean isNoNeed(String tag){
		List<String> names = new ArrayList<String>();		
		names.add("non-physical anatomical entity");
		names.add("attribute entity");
		names.add("space and boundary entity");
		names.add("portion of tissue");
		names.add("anatomical junction");
		names.add("portion of body substance");
		names.add("cardinal cell part");
		names.add("cardinal tissue part");
		names.add("cell");
	  names.add("acellular anatomical structure");
	  names.add("vestigial embryonic structure");
	  names.add("gestational structure");
	  names.add("subdivision of cardinal body part");
	  names.add("organ region");
	  names.add("anatomical set but not set of organs");
	  
	  return (names.contains(tag) ? true : false);
	}
		
	public void run() throws Exception {		
		String logFile = LOGDIR + "/TAReport.txt";

		FileOutputStream fos = new FileOutputStream(logFile, false);
		OutputStreamWriter out = new OutputStreamWriter(fos, "MS932");
		BufferedWriter bw = new BufferedWriter(out);

		bw.write("TAID\tTAEn\tTAKanji\tBp3dId\tBp3dEn\tTag\tType\n");
		
		for(TAEntry taEnt : ta.getEntries()){
			taId2TA.put(taEnt.getTaId(), taEnt);			
		}
		
		for(TAEntry taEnt : taId2TA.values()){
			String taId = taEnt.getTaId();
			String taEn = taEnt.getTaEn();
			String taKanji = taEnt.getTaKanji();
			
			if(ta.getFMAByTAId(taId) != null){								
				for(FMAOBOEntry fma : ta.getFMAByTAId(taId)){
					String id = fma.getId();
					String en = fma.getName();
					String tags = Bp3dUtility.join(tag.getTag(id), "/");
					Bp3dEntryType type = null;
										
					if(bp3d.contains(id)){
						type = bp3d.getEntry(id).getType();
						if(type.equals(Bp3dEntryType.VISIBLE_BODY)){
							type = Bp3dEntryType.VISIBLE_BODY;
						}else if(type.equals(Bp3dEntryType.COMPOSITE) && isNoNeed(tags)){
							type = Bp3dEntryType.NO_NEED;
						}
						bw.write(taId + "\t" + taEn + "\t" + taKanji + "\t" + id + "\t" + en + "\t" + tags + "\t" + type+ "\n");
					}else{
						bw.write(taId + "\t" + taEn + "\t" + taKanji + "\t" + id + "\t" + en + "\t" + tags + "\t\n");
					}
				}
			}else{
				bw.write(taId + "\t" + taEn + "\t" + taKanji + "\t\t\t\n");
			}
		}
		
		bw.close();
		fos.close();
		out.close();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		TAReport taReport = new TAReport();
		taReport.run();
	}

}
