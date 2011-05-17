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
package jp.dbcls.bp3d.kaorif.visiblebody;

import java.util.*;
import jp.dbcls.bp3d.fma.*;

import jp.dbcls.bp3d.kaorif.visiblebody.nerves.*;
import jp.dbcls.bp3d.kaorif.visiblebody.vessels.*;

/**
 * @author ag
 *
 */
public class VisibleBody {
	VisibleBodyVessels vv = new VisibleBodyVessels();
	VisibleBodyNerves vn = new VisibleBodyNerves();
	FMA fma = new FMA();			
	
	public VisibleBody() throws Exception {}
		
	/**
	 * 全FMAIDの集合を返す
	 * @return
	 */
	public Set<String> getAllFMAIDs(){
		Set<String> ret = new HashSet<String>();
		ret.addAll(vv.getAllFMAIds());
		ret.addAll(vn.getAllFMAIds());
		
		return ret;
	}
	
	/**
	 * fmaIdを持つエントリがvisibleBodyに含まれるかチェックする
	 * @param fmaId
	 * @return
	 */
	public boolean contains(String fmaId){
		if(vv.contains(fmaId) || vn.contains(fmaId)){
			return true;
		}else if(fma.hasLeft(fmaId) && fma.hasRight(fmaId)){
			return (vv.contains(fma.getLeft(fmaId).getId()) || 
					vn.contains(fma.getLeft(fmaId).getId())) &&
					(vv.contains(fma.getRight(fmaId).getId()) || 
					 vn.contains(fma.getRight(fmaId).getId()));				
		}else{
			return false;
		}
	}

	/**
	 * Left/Rightの両方を指す概念にマッチしてもtrueにする
	 * @param fmaId
	 * @return
	 */
	public boolean containsLeftRight(String fmaId){
		if(contains(fmaId)){
			return true;
		}
		FMAOBOEntry lr = fma.getLeftPlusRight(fmaId);
		if(lr != null){
			return contains(lr.getId());
		}
		
		return false;
	}	

	
	public static void main(String[] args) throws Exception {
		VisibleBody vb = new VisibleBody();
		/**
		 * See if FMA4950	left ascending lumbar vein is contained.
		 */
		System.out.println(vb.contains("FMA4950"));
		System.out.println(vb.contains("FMA50896"));
		System.out.println(vb.contains("left angular vein"));		
		System.out.println(vb.contains("angular vein"));
	}
	
}
