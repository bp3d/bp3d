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
package jp.dbcls.bp3d.ta;

import java.io.*;
import java.util.*;

import jp.dbcls.bp3d.*;
import jp.dbcls.bp3d.fma.*;
import jp.dbcls.bp3d.fma.tree.*;
import jp.dbcls.bp3d.util.Bp3dUtility;

/**
 * 
 * @author ag
 *
 */
public class TagTA  {
	FMAOBO fmaobo;
	ConstructBp3d bp3d;
	TA ta;	
	FMA fma;
	
	IsATree isA;
	
	public TagTA() throws Exception{		
		this.fmaobo = new FMAOBO();
		this.isA = new IsATree();
	}
		
	public TA getTa() {
		return ta;
	}

	private Set<String> getChildren(String fmaId){
		Set<String> ret = new HashSet<String>();
		for(FMAOBOEntry ent : isA.getChildren(fmaobo.getByName(fmaId))){
			ret.add(ent.getName());
		}
		return ret;
	}	
	
	/**
	 * FMAのis-aを使ってTAに分類タグをつける
	 */
	public Set<String> getTag(String fmaId){
		Set<String> tags = new HashSet<String>();
		
		SortedSet<String> classes = new TreeSet<String>();
		for(String id : isA.getAncestors(fmaId)){
			classes.add(fmaobo.getById(id).getName());
		}
		
		if(classes.size() == 0){
			return tags;
		}

		/** all->non physical/phyisical **/
		String tag = "non-physical anatomical entity";
		if(classes.contains(tag)){
			tags.add(tag);
			return tags;		
		}
		tag = "attribute entity";
		if(classes.contains(tag)){
			tags.add(tag);
			return tags;		
		}
		
		/** physical->immmaterial/material **/
		Set<String> immaterials = new HashSet<String>();
		immaterials.add("anatomical space");		
		immaterials.add("set of anatomical space");
		immaterials.add("anatomical boundary entity");	
		immaterials.retainAll(classes);
		if(immaterials.size() > 0){
			tag = "space and boundary entity";
			tags.add(tag);
			return tags;
		}
		
		tag = "immaterial anatomical entity";
		if(classes.contains(tag)){
			tag = "immaterial anatomical entity but not space";
			tags.add(tag);
			return tags;
		}

		
		/** material->portion of body substance/anatomical structure, anatomical set **/
		tag = "portion of body substance";
		if(classes.contains(tag)){
			tags.add(tag);
			return tags;			
		}			

		tag = "anatomical set";
		if(classes.contains(tag)){
			if(classes.contains("set of organs")){
				tag = "set of organs";
			}else if(classes.contains("set of organ parts")){
				tag = "set of organ parts";
			}else{
				tag = "anatomical set but not set of organs";
			}
			
			tags.add(tag);
			return tags;			
		}			
		
		tag = "anatomical structure";
		if(classes.contains(tag)){			
			Set<String> subcategories = getChildren(tag);
			subcategories.retainAll(classes);
			
			if(subcategories.contains("cardinal organ part")){
				subcategories = getChildren("cardinal organ part");
				subcategories.retainAll(classes);
			}
						
			tags.addAll(subcategories);
		}
				
		return tags;
	}

	private SortedSet<String> id2en(Set<String> ids){
		SortedSet<String> ens = new TreeSet<String>();
		for(String id : ids){
			ens.add(fmaobo.getById(id).getName());
		}
		return ens;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		TagTA tag = new TagTA();
		FMAOBO fmaobo = new FMAOBO();
		IsATree isA = new IsATree();
		
		List<String> names = new ArrayList<String>();
/**
		names.add("radial tuberosity");		// 橈骨粗面	FMA23489	radial tuberosity			
		names.add("hyoid bone"); // 舌骨	FMA52749	hyoid bone
		names.add("set of cervical vertebrae"); // 頚椎[C1-C7]	FMA72063	set of cervical vertebrae
		names.add("right");
		names.add("medial");
		names.add("simple joint"); // 単関節	FMA75291	simple joint
		names.add("cardinal body part"); 
**/		
		names.add("set of ligaments"); 
//		names.add("set of neural cells");
		
		for(String name : names){
			String fmaId = fmaobo.getByName(name).getId();
			Set<String> classes = isA.getAncestors(fmaId);
			System.out.println(name + "\t" + tag.getTag(fmaId) + "\t" + tag.id2en(classes));			
		}		
	}

}
