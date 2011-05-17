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
package jp.dbcls.bp3d.fma.tree.junit;

import java.util.*;

import jp.dbcls.bp3d.fma.*;
import jp.dbcls.bp3d.fma.tree.*;
import junit.framework.TestCase;

/**
 * @author ag
 *
 */
public class IsATreeTest extends TestCase {
	FMAOBO fmaobo;
	IsATree isA;
	FMAOBOEntry root;	
  FMAOBOEntry forAncestors;
	FMAOBOEntry forOffsprings;  
  Set<String> offsprings;
	Set<String> ancestors;
	
	private void setUpAnswer(){		
		forOffsprings = fmaobo.getByName("lung");
		forAncestors = fmaobo.getByName("lung");
		
		offsprings = new HashSet<String>();
		offsprings.add("left lung");
		offsprings.add("right lung");		
		
		ancestors = new HashSet<String>();
		ancestors.add("anatomical entity");
		ancestors.add("physical anatomical entity");
		ancestors.add("material anatomical entity");
		ancestors.add("anatomical structure");
		ancestors.add("organ");
		ancestors.add("solid organ");
		ancestors.add("parenchymatous organ");
		ancestors.add("lobular organ");		
	}
	
  protected void setUp() throws Exception {
  	fmaobo = new FMAOBO();  	  	
  	isA = new IsATree(fmaobo);
  	
  	setUpAnswer();
  }
  
  public void testOffsprings(){
  	Set<String> isAOffsprings = new HashSet<String>();
  	for(FMAOBOEntry offspring : isA.getOffsprings(forOffsprings)){
  		isAOffsprings.add(offspring.getName());
  	}
  	
  	assertEquals(offsprings, isAOffsprings);
  }

  public void testAncestors(){
  	Set<String> isAAncestors = new HashSet<String>();
  	for(FMAOBOEntry ancestor : isA.getAncestors(forAncestors)){
  		isAAncestors.add(ancestor.getName());
  	}
  	
  	assertEquals(ancestors, isAAncestors);
  }    
}
