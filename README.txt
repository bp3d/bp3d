README

■ Anatomography用データの作成手順

1. パラメータ設定ファイルsrc/jp.dbcls.bp3d.bp3d.propertiesファイルを確認し、
bp3d.datadir/bp3d.dataversionディレクトリの下に必要なファイルが存在することを確認します。

bp3d.dataディレクトリ（例：c:/bp3d/data)の内容については、bp3d.datadir/bp3d.dataversion/README.txtを参照のこと。

2.src/jp.dbcls.bp3d.make以下のスクリプトを起動します。

2-1. MakeBp3d0.java
  
  
 
■ データアーカイブ用データの作成手順

1. Anatomography用の





■ ピース名のcorrectionのルール

空白の有無、大文字、小文字の違い含めて完全一致しなければいけない。

例：
ルール：l.thalamusをl. thalamusが定義されている場合。
L. Thanamusには、このルールは適用されない。



■bp3d.propertiesの各パラメータの意味

bp3d.dataversion=3.0
bp3d.datadir=C:/bp3d/data/
bp3d.calcnormal=true
bp3d.calcvolume=true
bp3d.testdatadir=C:/bp3d/data/test
bp3d.tmpvtkfile=C:/bp3d/data/test/tmp.vtk
bp3d.fmafile=C:/bp3d/data/dictionary/FMAOBO/fma2_obo.obo
#bp3d.fmafile=C:/bp3d/data/dictionary/FMAOBO/fma3.obo
bp3d.pathseparator=\\
bp3d.claydir=Y:
bp3d.credit=BodyParts3D, Copyright(C) 2010-2011 The Database Center for Life Science licensed under CC Attribution-Share Alike 2.1 Japan.



