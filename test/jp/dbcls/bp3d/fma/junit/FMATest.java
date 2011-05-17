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

import java.util.*;

import jp.dbcls.bp3d.fma.*;
import junit.framework.TestCase;

/**
 * @author ag
 *
 */
public class FMATest extends TestCase {
	FMAOBO fmaobo;
	FMA fma;
	
  protected void setUp() throws Exception {
  	fmaobo = new FMAOBO();  
  	fma = new FMA();
  }

  /**
   * Left+Rightの名称を持っているか否かを判定
   */
  public void testHasLeftPlusRight(){  	
  	/** left/rightともにFMAに存在する場合は、left/right/left+rightどれが引数でもtrue**/
  	assertTrue(fma.hasLeftPlusRight("fornix of forebrain"));
  	assertTrue(fma.hasLeftPlusRight("left fornix of forebrain"));
  	assertTrue(fma.hasLeftPlusRight("right fornix of forebrain"));

  	/** posterior commissureは存在するが、left posterior commissureはFMAにない**/
  	assertFalse(fma.hasLeftPlusRight("posterior commissure"));
  	assertFalse(fma.hasLeftPlusRight("left posterior commissure")); 
  	
  	/** left ventricleはFMAに存在し、そのis-aが cardiac ventricle →よくない**/
  	assertTrue(fma.hasLeftPlusRight("left ventricle"));
  }

 
  public void testHasLeftPlusRightWithoutIsA(){  	
  	/** left/rightともにFMAに存在する場合は、left/right/left+rightどれが引数でもtrue**/
  	assertTrue(fma.hasLeftPlusRightWithoutIsA("fornix of forebrain"));
  	assertTrue(fma.hasLeftPlusRightWithoutIsA("left fornix of forebrain"));
  	assertTrue(fma.hasLeftPlusRightWithoutIsA("right fornix of forebrain"));

  	/** left posterior commissureはFMAにないが、posterior commissureは存在する **/
  	assertTrue(fma.hasLeftPlusRightWithoutIsA("posterior commissure"));
  	assertTrue(fma.hasLeftPlusRightWithoutIsA("left posterior commissure")); 
  	
  	/** left ventricleはFMAに存在し、そのis-aが cardiac ventricle **/
  	assertTrue(fma.hasLeftPlusRightWithoutIsA("left ventricle"));
  }

  
  public void testGetLeftPlusRight(){  	
  	/** left/rightともにFMAに存在する場合は、left/right/left+rightどれが引数でもtrue**/
  	assertEquals("fornix of forebrain", fma.getLeftPlusRight("fornix of forebrain").getName());
  	assertEquals("fornix of forebrain", fma.getLeftPlusRight("left fornix of forebrain").getName());
  	assertEquals("fornix of forebrain", fma.getLeftPlusRight("right fornix of forebrain").getName());

  	/** left posterior commissureはFMAにないが、posterior commissureは存在する **/
  	assertNull(fma.getLeftPlusRight("posterior commissure"));
  	assertNull(fma.getLeftPlusRight("left posterior commissure"));
  	
  	/** left ventricleはFMAに存在し、そのis-aがcardiac ventricle **/
  	assertEquals("cardiac ventricle", fma.getLeftPlusRight("left ventricle").getName());
  }

  public void testGetLeftPlusRightWithoutIsA(){  	
  	/** left/rightともにFMAに存在する場合は、left/right/left+rightどれが引数でもtrue**/
  	assertEquals("fornix of forebrain", fma.getLeftPlusRightWithoutIsA("fornix of forebrain").getName());
  	assertEquals("fornix of forebrain", fma.getLeftPlusRightWithoutIsA("left fornix of forebrain").getName());
  	assertEquals("fornix of forebrain", fma.getLeftPlusRightWithoutIsA("right fornix of forebrain").getName());

  	/** left posterior commissureはFMAにないが、posterior commissureは存在する **/
  	assertEquals("posterior commissure", fma.getLeftPlusRightWithoutIsA("posterior commissure").getName());
  	assertEquals("posterior commissure", fma.getLeftPlusRightWithoutIsA("left posterior commissure").getName());
  	
  	/** left ventricleはFMAに存在し、そのis-aがcardiac ventricle **/
  	assertEquals("ventricle", fma.getLeftPlusRightWithoutIsA("left ventricle").getName());
  }
  
  public void testHasLeft(){ 
  	/** left/rightともにFMAに存在する場合は、left+rightのときtrue **/
  	assertTrue(fma.hasLeft("fornix of forebrain"));
  	assertFalse(fma.hasLeft("left fornix of forebrain"));
  	assertFalse(fma.hasLeft("right fornix of forebrain"));

  	/** left posterior commissureはFMAに存在しないので false**/
  	assertFalse(fma.hasLeft("posterior commissure"));
  	assertFalse(fma.hasLeft("left posterior commissure"));
  	  	
  	/** left ventricleはFMAに存在し、そのis-aがcardiac ventricleなのでtrue**/
  	assertFalse(fma.hasLeft("left ventricle"));
  } 

  public void testGetLeft(){  	
  	/** left/rightともにFMAに存在する場合は、left/right/left+rightどれが引数でもleftを返す**/
  	assertEquals("left fornix of forebrain", fma.getLeft("fornix of forebrain").getName());
  	assertNull(fma.getLeft("left fornix of forebrain"));
  	assertNull(fma.getLeft("right fornix of forebrain"));

  	/** left posterior commissureはFMAに存在しないので nullを返す **/
  	assertNull(fma.getLeft("posterior commissure"));
  	assertNull(fma.getLeft("left posterior commissure"));
  	  	
  	/** left ventricleはFMAに存在し、そのis-aがventricleではないので、なし**/
  	assertNull(fma.getLeft("left ventricle"));
  } 
  
  public void testIsLeft(){  	
  	/** left/rightともにFMAに存在する場合は、leftが引数のときtrue **/
  	assertFalse(fma.isLeft("fornix of forebrain"));
  	assertTrue(fma.isLeft("left fornix of forebrain"));
  	assertFalse(fma.isLeft("right fornix of forebrain"));

  	/** left posterior commissureはFMAに存在しないので false**/
  	assertFalse(fma.isLeft("posterior commissure"));
  	assertFalse(fma.isLeft("left posterior commissure"));
  	  	
  	/** left ventricleはFMAに存在し、そのis-aが存在するので**/
  	assertTrue(fma.isLeft("left ventricle"));
  } 

  public void testConvertSingularPlural(){  	
  	/** renal vein -> set of renal veins **/
  	FMAOBOEntry ent = fmaobo.getByName("renal vein");
  	assertEquals("set of renal veins", fma.convertSingularPlural(ent).getName());

  	/** set of eyeballs -> eyeball **/
  	ent = fmaobo.getByName("set of eyeballs");
  	assertEquals("eyeball", fma.convertSingularPlural(ent).getName()); 
  }
  	
  
  /**
   * 引数の臓器名のorgan systemが正しく出力されるか
   */
  public void testGetOrganSystem(){
  	String organName = "lung";

  	Set<String> answer = new HashSet<String>();
  	answer.add("respiratory system");
  	  	
  	assertEquals(answer, fma.getOrganSystem(organName));  	
  }	
}
