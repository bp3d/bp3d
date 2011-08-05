/**
The MIT License

Copyright (c) 2010, Database Center for Life Science (DBCLS)

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

import java.io.File;

import jp.dbcls.bp3d.Bp3dProperties;
import jp.dbcls.bp3d.kaorif.Abbrev;
import jp.dbcls.bp3d.fma.FMAOBO;

/**
 * SPLの名前のついたobjファイル名をFMAのそれに変換する
 * SPL2FMA(大久保先生作成対応表）利用
 * 
 * @author ag
 *
 */
public class TranslateSPL2FMA {	
	private static final String DATADIR = Bp3dProperties
		.getString("bp3d.datadir")
		+ "/" + Bp3dProperties.getString("bp3d.dataversion");
	private static final String clayFile = "101224-brain";
	private static final String reductionRate = "0.05";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String objDirStr = DATADIR + "/FFMP/obj/" + clayFile + "/" + reductionRate; 
		SPL2FMA spl2fma = new SPL2FMA();
		FMAOBO fmaobo = new FMAOBO();
		Abbrev abbrev = new Abbrev();
		
		File objDir = new File(objDirStr);
		if (!objDir.exists()) {
			System.err.println("ReplaceUnderscoreWithBlank: objDir not found " + objDirStr);
			System.exit(1);
		}

		for (File objFile : objDir.listFiles()) {
			if (objFile.isDirectory()) {
				continue;
			}
			String objFileName = objFile.getName();
			String organName = objFileName.replace(clayFile + "_", "");		
			organName = organName.replaceFirst("[.]obj$", "");
			
			/** 
			 * SPL名の場合は、大久保先生作成の対応表で変換する
			 */
			if(spl2fma.isSPLName(organName)){				
				if(spl2fma.isCorrespondingMulitpleFMAId(organName)){
					String fmaId = spl2fma.getFMAId(organName);
					System.out.println("MULTIPLE FMA:" + fmaId + "<-" + organName);
				}else if(spl2fma.isCorrespondingSingleFMAId(organName)){					
					String fmaId = spl2fma.getFMAId(organName);
					organName = fmaobo.get(fmaId).getName();
//					System.out.println(fmaId + "->" + organName);
				}								
			}
							
			String renameStr = objDirStr + "/" + clayFile + "_" + abbrev.getAbbrev(organName) + ".obj";
									
			File renamed = new File(renameStr);
			objFile.renameTo(renamed);
		}
	}
}
