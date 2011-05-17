package jp.dbcls.bp3d.kaorif;

/**
 * preferred name -> kanji/kanaへの変換テーブルのエントリ
 * 
 * @author ag
 * 
 */
public class En2JaEntry {
	String en;
	String kanji;
	String kana;

	public En2JaEntry() throws Exception {}

	public String getEn() {
		return en;
	}

	public void setEn(String en) {
		this.en = en;
	}

	public String getKanji() {
		return kanji;
	}

	public void setKanji(String kanji) {
		this.kanji = kanji;
	}

	public String getKana() {
		return kana;
	}

	public void setKana(String kana) {
		this.kana = kana;
	}	
}
