package jp.dbcls.bp3d;

This is incomplete.

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.regex.Pattern;

import jp.dbcls.bp3d.fma.FMA;
import jp.dbcls.bp3d.fma.FMAOBO;
import jp.dbcls.bp3d.fma.FMAOBOEntry;
import jp.dbcls.bp3d.fma.tree.IsATree;
import jp.dbcls.bp3d.fma.tree.MemberOfTreeForConventionalTree;
import jp.dbcls.bp3d.Bp3dEntry;
import jp.dbcls.bp3d.appendpoly.AppendPoly;
import jp.dbcls.bp3d.appendpoly.AppendPolyInfo;
import jp.dbcls.bp3d.kaorif.Kaorif;
import jp.dbcls.bp3d.kaorif.visiblebody.VisibleBody;
import jp.dbcls.bp3d.obj.OBJInfoEntry;
import jp.dbcls.bp3d.obj.OBJInfo;
import jp.dbcls.bp3d.obj.ParseOBJName;
import jp.dbcls.bp3d.ta.*;
import jp.dbcls.bp3d.util.StopWatch;

/**
 * FMAOBO/TA/kaorif.xlsのデータから、Bp3d Conventional Treeを作成する
 * 開発者向け
 * 
 * @author mituhasi
 * 
 */

public class ConstructBp3dForDeveloper extends ConstructBp3d {	
	private VisibleBody visibleBody;
	private FMA fma;
	
	public ConstructBp3dForDeveloper() throws Exception {}

	@Override
	protected void run() throws Exception {		
		this.visibleBody = new VisibleBody();
		this.fma = new FMA();
		
		/** TAのエントリを対応するFMAエントリに変更してパーツリストに追加する **/
		addTAEntries();		
		/** TAのインデント情報をmember-of階層として取り込む **/
		makeMemberOfBasedOnTA();
		System.out.println("addTAEntries():NumOfEntries=" + id2Entry.size());
		
		/** kaorif.xlsのエントリとmember-ofを追加 **/
		addKaorifEntries();
		addKaorifMemberOf();
		System.out.println("addKaorifEntries():NumOfEntries=" + id2Entry.size());			

		/** OBJファイルディレクトリをスキャンして、OBJファイル１つからなる
		 * パーツ(primitive parts)リストを作成する **/
		addOBJAsPrimitive();
		System.out.println("addOBJAsPrimitive():NumOfEntries=" + id2Entry.size());					
		
		addVisibleBodyAsPrimitive();
		System.out.println("addVisibleBodyAsPrimitive():NumOfEntries=" + id2Entry.size());					
		
		/** FMAのmember-ofとTAのインデント情報を使って、member-of関係を作成する **/
		makeMemberOf();
		
		this.bp3dTraverse = new TraverseBp3d(bp3dTree);
		
		/** 冗長なmember-ofを削除する **/
		trimRedundantMemberOf();
					
		/**childrenが存在するOBJファイルはパーツの一部分であると判断し、childrenのOBJファイルと
		 * combineして、完全なパーツ(Bp3dEntryType.COMPOSITE_PRIMITIVE)にするために、
		 * AppendPolyInfoに追加する  **/
		materializeCompositeParts();

		appendPolyInfo.export();
		
		/** 複数のprimitiveを合併し、ひとつのprimitiveにする **/
		appendPoly = new AppendPoly(appendPolyInfo);
		appendPoly.run();
		appendPoly.write();

		/** Bp3dEntryの分類を割り当てる **/
		assignBp3dEntryType();
				
		/** bp3d.txt, memberOf.txtを出力する**/
		export();

		System.out.println("End of run():NumOfEntries=" + id2Entry.size());						
	}

	/**
	 * VisibleBodyのエントリを追加する
	 * @throws Exception
	 */
	public void addVisibleBodyAsPrimitive() throws Exception {		
		for(String fmaId : visibleBody.getAllFMAIDs()){
			this.addEntry(fmaId);
		}
	}
	
	/**
	 * NowMakingの
	 * @throws Exception
	 */
	public void addNowMaking() throws Exception {		
		final String INFILE = Bp3dProperties.getString("bp3d.datadir") + 
			Bp3dProperties.getString("bp3d.dataversion") 
			+ "/conf/now_making/now_making_110331.txt";
		
		FileInputStream is = new FileInputStream(INFILE);
		InputStreamReader in = new InputStreamReader(is, "MS932");
		BufferedReader br = new BufferedReader(in);
		String line;
		
		while ((line = br.readLine()) != null) {
			if(line.startsWith("#")){ continue; }
			String[] data = Pattern.compile("\t").split(line);
			String en = data[0].trim();
			this.addEntry(en);			
		}
		
		br.close();
		in.close();		
		is.close();
	}
	
	
	/**
	 * 各パーツの分類を返す。
	 * 
	 * primitive: OBJファイル単独でそのパーツを表現している
	 * composite: 複数のprimitiveの和で定義される
	 * composite+primitive: 複数のprimitiveとそのパーツ自身を表すOBJ（部分データ, non-specified-name)の和で定義される
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public void assignBp3dEntryType(){		
		for(Bp3dEntry ent : this.getAllEntries()){
			String id = ent.getId();
			String en = ent.getEn();
			if(isCompleted(ent)){
				ent.setType(Bp3dEntryType.COMPLETED);
			}else if(hasChild(id) && objInfo.containsKey(en)){			
				ent.setType(Bp3dEntryType.COMPOSITE_PRIMITIVE);
			}else if(visibleBody.contains(id)){
				ent.setType(Bp3dEntryType.VISIBLE_BODY);
			}else if(hasChild(id) == false){
				ent.setType(Bp3dEntryType.NEED_TO_MAKE);
			}else{
				ent.setType(Bp3dEntryType.COMPOSITE);
			}
		}

		/** 
		 * COMPOSITE_PRIMITIVEの一部である、NEED_TO_MAKEをNEED_TO_DIVIDEに変更する
		 */
		for(Bp3dEntry ent : this.getAllEntries()){
			String id = ent.getId();
			if(ent.getType().equals(Bp3dEntryType.NEED_TO_MAKE)){
				for(String aId : bp3dTraverse.getAncestors(id)){
					Bp3dEntry aEnt = this.getEntry(aId);
					if(aEnt.getType().equals(Bp3dEntryType.COMPOSITE_PRIMITIVE)){
						ent.setType(Bp3dEntryType.MADE_BY_DIVISION);
						ent.addToDivide(aEnt);
					}
				}
			}
		}
	}

	/**
	 * 
	 * @param bp3dEnt
	 * @return
	 * @throws Exception
	 */
	public boolean isCompleted(Bp3dEntry bp3dEnt){
		String en = bp3dEnt.getEn();
		
		if(objInfo.containsKey(en)){
			return true;
		}
		
		if(!bp3dTree.hasChild(bp3dEnt.getId())){
			return false;
		}

		if(en.equals("clavicle")){
			System.out.println("aaa");
		}
		
		if(fma.hasLeft(en) && fma.hasRight(en)
				&& objInfo.containsKey(fma.getLeft(en))
				&& objInfo.containsKey(fma.getRight(en))){
					return true;
		}
				
		for(String cId : bp3dTree.getChildren(bp3dEnt.getId())){
			if(!isCompleted(getEntry(cId))){
				return false;
			}
		}
		
		return true;
	}
	
	
	/**
	 * parts listとmember ofリストを出力する
	 * @throws Exception
	 */
	public void export() throws Exception {		
		FileOutputStream fos;
		OutputStreamWriter out;
		BufferedWriter bw;

		String logFile = DATADIR + "/logs/MakeBp3d0/bp3dDevel.txt";

		fos = new FileOutputStream(logFile, false);
		out = new OutputStreamWriter(fos, "MS932");
		bw = new BufferedWriter(out);
					
		bw.write("id" + "\t" + "en" + "\t" + "kanji" + "\t" + "type" + "\n");

		for (Bp3dEntry ent : this.getAllEntries()){
			bw.write(ent.getId() + "\t" + ent.getEn() + "\t" 
					+  ent.getKanji() + "\t" + ent.getType() + "\n");
		}
				
		bw.close();
		out.close();
		fos.close();

		logFile = DATADIR + "/logs/MakeBp3d0/bp3dToDivide.txt";
		
		fos = new FileOutputStream(logFile, false);
		out = new OutputStreamWriter(fos, "MS932");
		bw = new BufferedWriter(out);
					
		bw.write("id" + "\t" + "en" + "\t" + "to_divide" + "\t" 
				+ "to_divide_cly" + "\t" + "to_divide_piece" + "\n");

		for (Bp3dEntry ent : this.getAllEntries()){
			if(ent.getType().equals(Bp3dEntryType.MADE_BY_DIVISION)){
				for(Bp3dEntry entToDivide : ent.getToDivide()){
					String en = entToDivide.getEn();
					String kanji = entToDivide.getKanji();
					String cly = objInfo.get(en).getLatestClyName();
					String piece = objInfo.get(en).getLatestPieceName();	
					bw.write(ent.getId() + "\t" + ent.getEn() + "\t" + ent.getKanji() + "\t"							
							+ en + "\t" + kanji + "\t" + cly + "\t" + piece + "\n");
				}
			}
		}

		bw.close();
		out.close();
		fos.close();
				
		logFile = DATADIR + "/logs/MakeBp3d0/bp3dMemberOfDevel.txt";

		fos = new FileOutputStream(logFile, false);
		out = new OutputStreamWriter(fos, "MS932");
		bw = new BufferedWriter(out);
					
		bw.write("child\tparent\ttype\n");

		for (String child : this.getMemberOfs().keySet()) {
			for (String parent : this.getMemberOfs().get(child)) {
				String type = "";
				if(bp3dTree.isTAMemberOf(child, parent)){
					type = "TA";
				}else if(bp3dTree.isKaorifMemberOf(child, parent)){
					type = "kaorif";
				}else{
					type = "FMA";
				}
				
				bw.write(getName(child) + "\t" + getName(parent) + "\t"
						+ type + "\n");
			}
		}

		bw.close();
		out.close();
		fos.close();	
	}
		
	/**
	 * テストコード
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		StopWatch sw = new StopWatch();
		sw.start();

		ConstructBp3dForDeveloper bp3d = new ConstructBp3dForDeveloper();
		if(bp3d.contains("medulla oblongata")){
			System.out.println(bp3d.isCompleted(bp3d.getEntry("medulla oblongata")));
		}else{
			System.out.println("BBB");
		}

		
		System.out.println("Number Of BodyParts=" + bp3d.getAllEntries().size());
		
		sw.stop();

		System.out.println("Bp3d completed: elapsed time="
				+ sw.getElapsedTimeSecs() + "sec");
	}	
}
