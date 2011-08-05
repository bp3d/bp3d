package jp.dbcls.bp3d.util;

import java.io.*;
import java.util.*;
import java.util.zip.*;

public class ZipCompression {
	// 圧縮ファイル名(コンストラクタ指定時のみ)
	private String zipFilePath;
	// 圧縮ディレクトリパス(ディレクトリ処理時のみ使用)
	private String targetDirPath;
	// ZIP形式拡張子
	private static final String EXTENSION_ZIP = ".zip";

	/**
	 * <pre>
	 * 圧縮時のファイル名はデフォルト設定になります。
	 * 
	 * ディレクトリ指定時
	 *     c:/hoge → c:/hoge.zip
	 * 
	 * ファイル指定時
	 *     c:/hoge/hoge.txt → c:/hoge/hoge.zip
	 * </pre>
	 */
	public ZipCompression() {
	}

	/**
	 * <pre>
	 * 圧縮ファイル名を指定します。
	 * 親ディレクトリが存在しない場合はエラーになります。
	 * </pre>
	 * 
	 * @param zipFilePath
	 *          圧縮ファイル名(フルパス指定)
	 * @throws FileNotFoundException
	 *           親ディレクトリが存在しない場合
	 */
	public ZipCompression(String zipFilePath) throws FileNotFoundException {
		if (!(new File(zipFilePath)).getParentFile().isDirectory()) {
			throw new FileNotFoundException("親ディレクトリが存在しません。" + zipFilePath);
		}
		this.zipFilePath = zipFilePath;
	}

	/**
	 * 指定ファイルをZIP形式に圧縮します。
	 * 
	 * @param filePath
	 *          圧縮対象ファイル(フルパス指定)
	 * @return 圧縮ファイル名(フルパス)
	 * @throws IOException
	 *           入出力関連エラーが発生した場合
	 */
	public String doFile(String filePath) throws IOException {
		System.out.println("圧縮開始");
		System.out.println("圧縮ファイル=" + filePath);

		// ファイル存在チェック
		File targetFile = new File(filePath);
		if (!targetFile.isFile()) {
			throw new FileNotFoundException("指定のファイルが存在しません。" + filePath);
		}

		// 圧縮先ファイルへのストリームを開く
		if (zipFilePath == null) {
			zipFilePath = getCompressFileName(filePath);
		}
		System.out.println("圧縮ファイル名=" + zipFilePath);
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFilePath));

		// ファイル圧縮処理
		putEntryFile(out, targetFile);

		// 出力ストリームを閉じる
		out.flush();
		out.close();

		System.out.println("圧縮終了");

		return zipFilePath;
	}

	/**
	 * 指定ディレクトリをZIP形式に圧縮します。
	 * 
	 * @param directoryPath
	 *          圧縮対象ディレクトリ(フルパス指定)
	 * @return 圧縮ファイル名(フルパス)
	 * @throws IOException
	 *           入出力関連エラーが発生した場合
	 */
	public String doDirectory(String directoryPath) throws IOException {
		System.out.println("圧縮開始");
		System.out.println("圧縮ディレクトリ=" + directoryPath);

		targetDirPath = directoryPath;

		// ディレクトリ存在チェック
		File targetDirectory = new File(directoryPath);
		if (!targetDirectory.isDirectory()) {
			throw new FileNotFoundException("指定のディレクトリが存在しません。" + directoryPath);
		}

		// 指定ディレクトリ直下のファイル一覧を取得
		List<File> rootFiles = Arrays.asList(targetDirectory.listFiles());

		// 圧縮先ファイルへのストリームを開く
		if (zipFilePath == null) {
			zipFilePath = getCompressFileName(directoryPath);
		}
		System.out.println("圧縮ファイル名=" + zipFilePath);
		ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFilePath));

		// ディレクトリ自体を書き込む
		putEntryDirectory(out, targetDirectory);

		// ファイルリスト分の圧縮処理
		compress(out, rootFiles);

		// 出力ストリームを閉じる
		out.flush();
		out.close();

		System.out.println("圧縮終了");
		return zipFilePath;
	}

	/**
	 * <pre>
	 * ファイル一覧(ディレクトリ含む)をZipOutputStreamに登録します。
	 * ディレクトリが存在する場合は再帰的に本メソッドをコールし全てのファイルを登録します。
	 * </pre>
	 * 
	 * @param out
	 * @param fileList
	 * @throws IOException
	 */
	private void compress(ZipOutputStream out, List<File> fileList)
			throws IOException {
		// ファイルリスト分の圧縮処理
		for (File file : fileList) {
			if (file.isFile()) {
				System.out.println("file compress->" + file.getPath());
				// ファイル書き込み
				putEntryFile(out, file);
			} else {
				// ディレクトリ自体を書き込む
				putEntryDirectory(out, file);
				// ディレクトリ内のファイルについては再帰的に本メソッドを処理する
				List<File> inFiles = Arrays.asList(file.listFiles());
				compress(out, inFiles);
			}
		}
	}

	/**
	 * <pre>
	 * ZipOutputStreamに対してファイルを登録します。
	 * </pre>
	 * 
	 * @param out
	 * @param file
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void putEntryFile(ZipOutputStream out, File file)
			throws FileNotFoundException, IOException {
		byte[] buf = new byte[128];

		// 圧縮元ファイルへのストリームを開く
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));

		// エントリを作成する
		ZipEntry entry = new ZipEntry(getZipEntryName(file.getPath()));
		out.putNextEntry(entry);

		// データを圧縮して書き込む
		int size;
		while ((size = in.read(buf, 0, buf.length)) != -1) {
			out.write(buf, 0, size);
		}

		// エントリと入力ストリームを閉じる
		out.closeEntry();
		in.close();
	}

	/**
	 * <pre>
	 * ZipOutputStreamに対してディレクトリを登録します。
	 * </pre>
	 * 
	 * @param out
	 * @param file
	 * @throws IOException
	 */
	private void putEntryDirectory(ZipOutputStream out, File file)
			throws IOException {
		ZipEntry entry = new ZipEntry(getZipEntryName(file.getPath()) + "/");
		entry.setSize(0);
		out.putNextEntry(entry);
	}

	/**
	 * <pre>
	 * ZipEntryを生成する為のパスを取得します。
	 * 主に階層構造(ディレクトリ)に対応する為に作成しました。
	 * 
	 * 圧縮指定ディレクトリが&quot;C:/hoge&quot;と指定された場合に以下のように取得します。
	 *     C:/hoge/hogehoge.txt    → hogehoge.txt
	 *     C:/hoge/hoge2/hoge3.txt → hoge2/hoge3.txt
	 * </pre>
	 * 
	 * @param filePath
	 *          ファイルパス
	 * @return ファイル名
	 */
	private String getZipEntryName(String filePath) {
		if (targetDirPath == null) {
			// ファイル圧縮時
			return (new File(filePath)).getName();
		}
		String parantPath = (new File(targetDirPath)).getParent();
		parantPath = removeLastSeparator(parantPath);
		return filePath.substring(parantPath.length() + 1);
	}

	/**
	 * <pre>
	 * 圧縮ファイル名を求めます。
	 * fileNameが拡張子なし(ディレクトリ扱い)の場合、&quot;.zip&quot;が付加されます。
	 * fileNameが拡張子あり(ファイル扱い)の場合、既存の拡張子が取り除かれ&quot;.zip&quot;が付加されます。
	 * 
	 *     hogeDirectory → hogeDirectory.zip
	 *     hogeFile.txt  → hogeFile.zip
	 * 
	 * fileNameにはフルパスを指定することも可能です。
	 * 
	 *     c:/hogeDirectory → c:/hogeDirectory.zip
	 *     c:/hogeFile.txt  → c:/hogeFile.zip
	 * </pre>
	 * 
	 * @param directoryPath
	 *          圧縮ディレクトリ名
	 * @return
	 */
	private String getCompressFileName(String fileName) {
		int tmp1 = fileName.lastIndexOf("\\");
		int tmp2 = fileName.lastIndexOf("/");

		int sepIndex = tmp1 > tmp2 ? tmp1 : tmp2;
		int extIndex = fileName.lastIndexOf(".");

		String zipName;
		if (sepIndex >= extIndex) {
			zipName = fileName + EXTENSION_ZIP;
		} else {
			// zipName = fileName.substring(0, extIndex) + EXTENSION_ZIP;
			zipName = fileName + EXTENSION_ZIP;
		}
		return zipName;
	}

	/**
	 * <pre>
	 * パスの最後にセパレータがある場合のみそれを除去して返却します。
	 * それ以外はそのまま返却します。
	 * </pre>
	 * 
	 * @param path
	 *          c:/hoge/
	 * @return c:/hoge
	 */
	private String removeLastSeparator(String path) {
		String separator = System.getProperty("file.separator");
		if (!path.endsWith(separator)) {
			return path;
		}
		return path.substring(0, path.length() - 1);
	}

	/**
	 * test code
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		ZipCompression comp = new ZipCompression();
		comp.doDirectory("C:/Users/mituhasi/ag/tmp/A");
	}

}
