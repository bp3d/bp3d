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
package jp.dbcls.bp3d.obj.brain;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

import jp.dbcls.bp3d.Bp3dProperties;

/**
 * 大久保先生が作成したSPL_Talairach_MAI_to_FMA.txtを読み込む
 * @author ag
 *
 */
public class SPL2FMA {
	final String INFILE = Bp3dProperties.getString("bp3d.datadir") + 
		"/dictionary/bits/SPL_Talairach_MAI_to_FMA/SPL_Talairach_MAI_to_FMA.txt";
	Map<String, String> spl2fmaId = new HashMap<String, String>();

	public SPL2FMA() throws Exception {
		readFile();
	}
	
	/**
	 * SPL_Talairach_MAI_to_FMA.txtをハッシュに詰め込む
	 * @throws Exception
	 */
	public void readFile() throws Exception {
		FileInputStream is = new FileInputStream(this.INFILE);
		InputStreamReader in = new InputStreamReader(is, "MS932");
		BufferedReader br = new BufferedReader(in);
		String line;

		while ((line = br.readLine()) != null) {
			if(line.startsWith("\"##") || line.trim().isEmpty()){ continue; }
			String[] data = Pattern.compile("[|]").split(line);
			String spl = data[0];
			String fmaId = data[1];
			if(!spl.isEmpty()){
				spl2fmaId.put(spl.toLowerCase(), fmaId);
			}
		}
		
		br.close();
		in.close();
		is.close();		
	}			
	
	/**
	 * SPL名からFMAIDを取り出す
	 * @param spl
	 * @return
	 */
	public String getFMAId(String spl){	
		return this.spl2fmaId.get(spl.toLowerCase());
	}
	
	/**
	 * SPL->FMAの対応表に含まれているSPL名か判定する
	 * @param spl
	 * @return
	 */
	public boolean isSPLName(String spl){
		return this.spl2fmaId.containsKey(spl.toLowerCase());
	}
	
	/**
	 * FMAIDに１：１に対応しているかを判定する
	 * 
	 * @param sql
	 * @return
	 */
	public boolean isCorrespondingSingleFMAId(String spl){
		String fmaId = getFMAId(spl);
		if(fmaId == null  || fmaId.equals("data assignment error")){
			return false;
		}
		if(fmaId.contains("[")){
			return false;
		}
		return true;
	}
	
	
	/**
	 * 以下の例のように複数のFMAエントリの和になっているかを判定する
	 * orbital_gyri_gyrus_rectus_L|[FMA256200+FMA80186+FMA72757+FMA72759+FMA80188+FMA72660]
	 * @param sql
	 * @return
	 */
	public boolean isCorrespondingMulitpleFMAId(String spl){
		String fmaId = getFMAId(spl);
		if(fmaId == null  || fmaId.equals("data assignment error")){
			return false;
		}
		if(fmaId.contains("[")){
			return true;
		}
		return false;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		SPL2FMA splfma = new SPL2FMA();
		
		String spl = "angular_gyrus_Inferior_parietal_lobule_L";
//		String spl = "anterior_substantia_perforata_R";
				
		if(splfma.isSPLName(spl)){
			System.out.println(spl + "->" + splfma.getFMAId(spl));
		}
	}

}
