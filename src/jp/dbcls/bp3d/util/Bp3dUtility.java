package jp.dbcls.bp3d.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.dbcls.bp3d.Bp3dProperties;

/**
 * ユーティリティ関数群です。<br>
 * Ver.1.1 2009/02/03 <br>
 * 
 * @author hfujihas@bits.cc (original by mitsuhashi)
 * @version 1.2, 2009/03/20
 */
public class Bp3dUtility {
		
	public static String join(Collection<String> list, String del){
		String ret = "";
		for(String element : list){
			if(ret.equals("")){
				ret = element;
			}	else {
				ret += del + element;
			}
		}
		return ret;
	}

	/**
	 * パスからディレクトリ名を取得します。
	 * 
	 * @param path
	 *          フルパス名。
	 * @return
	 */
	public static String getDirName(String path) {
		return path.substring(0, path.lastIndexOf("/") + 1);
	}

	/**
	 * パスからファイル名を取得します。
	 * 
	 * @param path
	 *          フルパス名
	 * @return
	 */
	public static String getFileName(String path) {
		return path.substring(path.lastIndexOf("/") + 1);
	}

	/**
	 * target中にあるfrom文字列をto文字列に置換（最初に出現するものだけ）
	 * 
	 * @param target
	 * @param from
	 * @param to
	 * @return
	 */
	public static String trace(String target, String from, String to) {
		int i = from.indexOf(target);
		return to.substring(i, i + 1);
	}

	/**
	 * 全角数字を半角に変換します。
	 * 
	 * @param s
	 *          変換元文字列
	 * @return 変換後文字列
	 */
	public static String zenkakuNumToHankaku(String s) {
		StringBuffer sb = new StringBuffer(s);
		for (int i = 0; i < sb.length(); i++) {
			char c = sb.charAt(i);
			if (c >= '０' && c <= '９') {
				sb.setCharAt(i, (char) (c - '０' + '0'));
			}
		}
		return sb.toString();
	}

	/**
	 * 全角アルファベットを半角に変換します。
	 * 
	 * @param s
	 * @return
	 */
	public static String zenkakuAlphabetToHankaku(String s) {
		StringBuffer sb = new StringBuffer(s);
		for (int i = 0; i < sb.length(); i++) {
			char c = sb.charAt(i);
			if (c >= 'ａ' && c <= 'ｚ') {
				sb.setCharAt(i, (char) (c - 'ａ' + 'a'));
			} else if (c >= 'Ａ' && c <= 'Ｚ') {
				sb.setCharAt(i, (char) (c - 'Ａ' + 'A'));
			}
		}
		return sb.toString();
	}

	/**
	 * バイナリファイルをコピーする
	 * 
	 * @param infile
	 * @param outfile
	 * @throws Exception
	 */
	public static void copy(String infile, String outfile) throws Exception {
		FileInputStream input = new FileInputStream(infile);
		FileOutputStream output = new FileOutputStream(outfile);
		byte buf[] = new byte[256];
		int len;
		while ((len = input.read(buf)) != -1) {
			output.write(buf, 0, len);
		}
		output.flush();
		output.close();
		input.close();
	}

	public static void copy(File infile, File outfile) throws Exception {
		FileInputStream input = new FileInputStream(infile);
		FileOutputStream output = new FileOutputStream(outfile);
		byte buf[] = new byte[256];
		int len;
		while ((len = input.read(buf)) != -1) {
			output.write(buf, 0, len);
		}
		output.flush();
		output.close();
		input.close();
	}

	/**
	 * ディレクトリをコピーする
	 * 
	 * @param inDirStr
	 * @param outDirStr
	 * @throws Exception
	 */
	public static void copyDir(String inDirStr, String outDirStr)
			throws Exception {
		File inDir = new File(inDirStr);
		if (inDir == null || !inDir.exists()) {
			return;
		}
		if (inDir.isFile()) {
			copy(inDirStr, outDirStr + "/" + inDir.getName());
		} else {
			File[] list = inDir.listFiles();
			for (int i = 0; i < list.length; i++) {
				copyDir(list[i].getAbsolutePath(), outDirStr);
			}
		}
	}

	/**
	 * ファイル/ディレクトリを削除する。
	 * 
	 * @param root
	 *          削除対象
	 */
	public static final void clean(File root) {
		if (root == null || !root.exists()) {
			return;
		}
		if (root.isFile()) {
			// ファイル削除
			if (root.exists() && !root.delete()) {
				root.deleteOnExit();
			}
		} else {
			// ディレクトリの場合、再帰する
			File[] list = root.listFiles();
			for (int i = 0; i < list.length; i++) {
				clean(list[i]);
			}
			if (root.exists() && !root.delete()) {
				root.deleteOnExit();
			}
		}
	}

	/**
	 * ファイルパスを結合する(/usr, local -> /usr/local)
	 * 
	 * @param parent
	 * @param child
	 * @return
	 */
	public static String concatPath(String parent, String child) {
		if (parent.endsWith("/")) {
			parent = parent.substring(0, parent.length() - 1);
		}
		if (child.startsWith("/")) {
			child = child.substring(1);
		}

		return parent + "/" + child;
	}

	/**
	 * OBOのプレフィックスを削除する(e.g: FMA:0001->0001)
	 * 
	 * @param fmaId
	 * @return
	 */
	public static String removeOBOPrefix(String fmaId) {
		Pattern pattern = Pattern.compile(":");
		String[] s = pattern.split(fmaId);

		if (s.length < 2) {
			return "";
		} else {
			return s[1];
		}
	}

	/**
	 * fmaIdのコロンをとる(e.g: FMA:0001->FMA0001)
	 * 
	 * @param fmaId
	 * @return
	 */
	public static String removeOBOColon(String fmaId) {
		fmaId = fmaId.replace(":", "");
		if (fmaId == null || fmaId.length() < 1) {
			return "";
		} else {
			return fmaId;
		}
	}

	/**
	 * ベクトルの内積
	 * 
	 * @param vup
	 * @param vn
	 * @return
	 */
	public static double Dot(double[] vup, double[] vn) {
		int dot = 0;
		for (int i = 0; i < 3; i++) {
			dot += vup[0] * vn[0];
		}
		return dot;
	}

	/**
	 * OBJファイル（ポリゴン座標）が存在するかどうか判定する
	 * 
	 * @param id
	 * @return
	 */
	public static boolean existsOBJ(String id) {
		String objDir = Bp3dProperties.getString("bp3d.datadir") + "/"
				+ Bp3dProperties.getString("bp3d.dataversion");
		String objFileName = objDir + "/" + id + ".obj";
		File objFile = new File(objFileName);

		return objFile.exists();
	}

	/**
	 * 現在の時刻を返す
	 * 
	 * @return
	 */
	public static String getCurrentTime() {
		Calendar cal1 = Calendar.getInstance(); // (1)オブジェクトの生成

		int year = cal1.get(Calendar.YEAR); // (2)現在の年を取得
		int month = cal1.get(Calendar.MONTH) + 1; // (3)現在の月を取得
		int day = cal1.get(Calendar.DATE); // (4)現在の日を取得
		int hour = cal1.get(Calendar.HOUR_OF_DAY); // (5)現在の時を取得
		int minute = cal1.get(Calendar.MINUTE); // (6)現在の分を取得
		int second = cal1.get(Calendar.SECOND); // (7)現在の秒を取得

		// (9)現在の年、月、日、時、分、秒を表示
		String time = year + "/" + month + "/" + day + " " + hour + ":" + minute
				+ ":" + second;

		return time;
	}

	/**
	 * カラー値をHSVからRGBに変換する
	 * 
	 * @param h
	 * @param s
	 * @param v
	 * @return
	 */
	public static double[] hsvToRgb(double h, double s, double v) {
		int i = (int) Math.floor(h / 60);
		double f = h / 60 - i;
		double m = v * (1 - s);
		double n = v * (1 - s * f);
		double k = v * (1 - s * (1 - f));
		double rgb[] = new double[3];

		switch (i) {
		case 0:
			rgb[0] = v;
			rgb[1] = k;
			rgb[2] = m;
			break;
		case 1:
			rgb[0] = n;
			rgb[1] = v;
			rgb[2] = m;
			break;
		case 2:
			rgb[0] = m;
			rgb[1] = v;
			rgb[2] = k;
			break;
		case 3:
			rgb[0] = m;
			rgb[1] = n;
			rgb[2] = v;
			break;
		case 4:
			rgb[0] = k;
			rgb[1] = m;
			rgb[2] = v;
			break;
		case 5:
			rgb[0] = v;
			rgb[1] = m;
			rgb[2] = n;
			break;
		default:
			rgb[0] = 0;
			rgb[1] = 0;
			rgb[2] = 0;
			break;
		}

		return rgb;
	}

	/**
	 * スペースを大文字始まりに変更(e.g. get value->getValue)
	 * 
	 * @param inStr
	 * @return
	 */
	public static String space2Capitalize(String inStr) {
		String outStr = "";
		char[] inStrArray = inStr.toCharArray();

		for (int i = 0; i < inStrArray.length; i++) {
			if (inStrArray[i] == ' ') {
				outStr += String.valueOf(inStrArray[++i]).toUpperCase();
			} else {
				outStr += String.valueOf(inStrArray[i]);
			}
		}

		return outStr;
	}	


	/**
	 * 丸かっこに囲まれた文字列を取り除く
	 * @param term
	 * @return
	 */
	public static String truncateParenthesis(String term){				
		// ?で最短一致(最小一致・最小マッチ)
    Pattern pattern = Pattern.compile("[(].*?[)][ ]?");
    Matcher matcher = pattern.matcher(term);
    term = matcher.replaceAll("");
    term = term.replaceAll(" ,", ",");
    term = term.trim();
    
    return term;
	}
	
	public static void main(String args[]){
		System.out.println(Bp3dUtility.truncateParenthesis("CN 11 (XI) Accessory (spinal accessory), L"));				
	}
}
