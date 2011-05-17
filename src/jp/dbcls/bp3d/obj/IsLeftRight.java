package jp.dbcls.bp3d.obj;
/**
 * 
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
*
 */
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import jp.dbcls.bp3d.kaorif.*;
import jp.dbcls.bp3d.util.StopWatch;
import jp.dbcls.bp3d.fma.*;

/**
 * ポリゴンが左半分・右半分・正中をまたいでいるかを判定する
 * @author ag
 *
 */
public class IsLeftRight {		
	double ratio = 0.0; // ratio = left/right
	static double RATIO_LEFT = 1.25; // left part if ratio > RATIO_LEFT
	static double RATIO_RIGHT = 0.8; // right part if ratio < RATIO_RIGHT
	int countL = 0;
	int countR = 0;
	boolean isProperName = false;
	
	FMA fma;
	OBJInfo infoSet;
							
	public IsLeftRight(FMA fma, OBJInfo infoSet) throws Exception {
		this.fma = fma;
		this.infoSet = infoSet;
	}

	public int getCountLeft(){
		return countL;
	}

	public int getCountRight(){
		return countR;
	}

	public double getRatio(){
		return ratio;
	}
	
	public boolean isProperName(){
		return isProperName;
	}

	/**
	 * Left/Rightのpoint数を数えてRATIOを計算する
	 * @param inFile
	 * @return
	 * @throws Exception
	 */
	public void countLR(String inFile) throws Exception {
		FileInputStream is = new FileInputStream(inFile);
		InputStreamReader in = new InputStreamReader(is, "UTF-8");
		BufferedReader br = new BufferedReader(in);
		
		String line;
		
		countL = 0;
		countR = 0;
		
		while ((line = br.readLine()) != null) {
			if (line.startsWith("v")) {
				String[] vertices = line.split(" ");
				double x = Double.parseDouble(vertices[1]);
				if(x < 0){
					countR++;
				}else if(x > 0){
					countL++;
				}			
			}
		}
		
		br.close();
		in.close();
		is.close();
		
		if(countL == 0){
			ratio = 0.0;
		}else if(countR == 0){
			ratio = 100;
		}else{
			ratio = (countL + 0.0)/ (countR + 0.0);
		}		
	}
	
	/**
	 * ratioから判断した左、右、左右両方に対してFMAの適当なpreferred nameを割り当てる
	 * left/rightのエントリがFMAにないときは、,nsnとする。
	 * @param organ
	 * @return
	 */
	public String getProperName(String organName){			
		String ret = "";
		
		if(!fma.hasLeftPlusRight(organName)){
			return ret;
		}
		
		FMAOBOEntry leftPlusRight = fma.getLeftPlusRight(organName);
		String LeftPlusRightName = leftPlusRight.getName();
			
		this.isProperName = false;
		
		if(ratio < RATIO_RIGHT){ // right partと判定
			if(!fma.hasRight(organName)){
				ret = LeftPlusRightName + ", nsn";
			}else{
				ret = fma.getRight(LeftPlusRightName).getName();
				this.isProperName = true;
			}
		} else if(ratio > RATIO_LEFT){ // left partと判定
			if(!fma.hasLeft(organName)){
				ret = LeftPlusRightName + ", nsn";
			}else{
				ret = fma.getLeft(LeftPlusRightName).getName();
				this.isProperName = true;
			}
		}else{  // left+right partと判定
			ret = LeftPlusRightName;
			this.isProperName = true;
		}
		
		return ret;		
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		StopWatch sw = new StopWatch();
		sw.start();

		String dir = "D:/ag/data/test/100826/";
		String objFile = dir + "100820-brain01_optic nerve 1.obj";

		FMA fma = new FMA();
		OBJInfo infoSet = new OBJInfo();
		
		String organName = "optic nerve";
		IsLeftRight isLR = new IsLeftRight(fma, infoSet);
		isLR.countLR(objFile);
		
		System.out.print(organName + " should be ");
		System.out.print(isLR.getProperName(organName));
		System.out.print(". ratio=" + isLR.getRatio());
		System.out.println(", isProperName=" + isLR.isProperName());
		
		sw.stop();

		System.out.println(isLR.getClass().getName() + " completed: elapsed time="
				+ sw.getElapsedTimeSecs() + "sec");
	}
}
