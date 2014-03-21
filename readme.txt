CA326 BitTorrent Visualiser
Authors: Stephan McLean 11400862
	 Kevin Sweeney  11352861

This file describes how to run our system and the tests for our system.

To view descriptions of our BitTorrent API classes we have also included JavaDocs in the javadocs folder.

To compile:
You must compile from the BitTorrentVisualiser directory. Our system depends on some libraries which we have included in the libs folder.

OS X
javac -cp “.:libs/jfxrt.jar” btv/client/gui/BTVUI.java

Windows
javac -classpath “.;libs/jfxrt.jar” btv/client/gui/BTVUI.java

To run:
We have included the torrent files we used to test our system. test.torrent and test2.torrent

OS X
java -cp “”.:libs/jfxrt.jar” btv/client/gui/BTVUI

Windows
java -classpath “”.;libs/jfxrt.jar” btv/client/gui/BTVUI


To run tests:
We have written tests for our different packages and they are stored in the btv/tests folder. Every package has a test runner which runs all the test cases.

For example to test our bencoding package

OS X
javac -cp “.:libs/junit-4.11.jar:hamcrest-core-1.3.jar” btv/tests/bencoding/*.java

java -cp “.:libs/junit-4.11.jar:hamcrest-core-1.3.jar” btv/tests/bencoding/BEncodingTestRunner

Windows
javac -classpath “.;libs/junit-4.11.jar:hamcrest-core-1.3.jar” btv/tests/bencoding/*.java

java -classpath “.;libs/junit-4.11.jar:hamcrest-core-1.3.jar” btv/tests/bencoding/BEncodingTestRunner