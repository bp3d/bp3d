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
package jp.dbcls.bp3d.fma.junit;

import jp.dbcls.bp3d.fma.*;
import junit.framework.TestCase;

/**
 * @author ag
 *
 */
public class FMAOBOTest extends TestCase {
	FMAOBO fmaobo;
	
  protected void setUp() throws Exception {
  	fmaobo = new FMAOBO();  
  }

	/**
	 * HasPartに対応するPartOfがちゃんと定義されているかチェックする
	 */
	public void testHasPartEqualsPartOf() {
		boolean result = true;
		
		for (FMAOBOEntry ent : fmaobo.getAllEntries()) {
			for (FMAOBOEntry po : ent.getPartOf()) {
				if (!po.getHasPart().contains(ent)) {
					result = false;
					System.out.println(po.getId() + " does not contain " + ent.getId()
							+ " as hasPart.");
				}
			}
		}
		assertTrue(result);
	}
  
	/**
	 * 1語にleft/rightの両方が含まれている用語がないことを確認する
	 */
	public void testFindTermsThatContainsBothLeftAndRight(){
		boolean result = true;
		
		for(FMAOBOEntry ent : fmaobo.getAllEntries()){
			if(ent.getName().contains("left") && ent.getName().contains("right")){
				System.out.println("Term that contains both Left and Right=" + ent.getName());
				result = false;
			}
		}
		
		assertFalse(result);
	}	
}
