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
public class ExtendedMemberOfTreeTest extends TestCase {
	FMAOBO fmaobo;
	ExtendedMemberOfTree memberOf;
  FMAOBOEntry forAncestors;
	FMAOBOEntry forOffsprings;  
  Set<String> offsprings;
	Set<String> ancestors;
	
	private void setUpAnswer(){		
		forOffsprings = fmaobo.getByName("third ventricle");
		forAncestors = fmaobo.getByName("head");
		
		offsprings = new HashSet<String>();
		offsprings.add("wall of third ventricle");
		offsprings.add("ependyma of third ventricle");
		offsprings.add("ependyma proper of third ventricle");
		offsprings.add("epithelium of choroid plexus of third ventricle");
		offsprings.add("choroid plexus of third ventricle");
		offsprings.add("cavity of third ventricle");		
		
		ancestors = new HashSet<String>();
		ancestors.add("body");
		ancestors.add("physical anatomical entity");
		ancestors.add("anatomical entity");
		ancestors.add("anatomical structure");
		ancestors.add("female human body");
		ancestors.add("cardinal body part");
		ancestors.add("male human body");
		ancestors.add("material anatomical entity");
		ancestors.add("body of vertebrate");
		ancestors.add("human body");				
	}
	
  protected void setUp() throws Exception {
  	fmaobo = new FMAOBO();
  	memberOf = new ExtendedMemberOfTree(fmaobo);
  	
  	setUpAnswer();
  }
  
  public void testChildren(){
  	
  }
  
  
  public void testOffsprings(){
  	Set<String> poOffsprings = new HashSet<String>();
  	for(FMAOBOEntry offspring : memberOf.getOffsprings(forOffsprings)){
  		poOffsprings.add(offspring.getName());
  	}
  	
  	assertEquals(offsprings, poOffsprings);
  }

  public void testAncestors(){
  	Set<String> poAncestors = new HashSet<String>();
  	for(FMAOBOEntry ancestor : memberOf.getAncestors(forAncestors)){
  		poAncestors.add(ancestor.getName());
  	}
  	
  	assertEquals(ancestors, poAncestors);
  }  
}
