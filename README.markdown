libsqlite4java-custom
---------------------
This is a fork of the sqlite3 java wrappers from here:

* http://code.google.com/p/sqlite4java/

I recommend you go to that project for anything real.  I created this project because the default build of sqlite was not sufficient and I found the build system to be a little too brittle to do what I wanted.  In the custom/ directory is a custom and build file that builds a custom jar and binary.

Again, this is for my own uses.  Others are recommended to see the original project.

Extension Loading
-----------------
This version enables the SQLite RTree extension and adds a couple of entry points to enable extension loading.  This is necessary in order to load SpatiaLite.

In order to load SpatiaLite (assuming you have it built and lying around somewhere - ie. 'brew install spatialite'):

	SQLiteConnection db=new SQLiteConnection(new File("test.db"));
	db.open(true);
	db.enableLoadExtension(true);

The key is to call the enableLoadExtension method on the database to enable the extension loading mechanism.  Then you have two options:

	db.exec("select load_extension('/usr/local/lib/libspatialite.dylib')");
	
or

	db.loadExtension("libspatialite.dylib", null);
	
Building
--------
The build system was stripped down substantially so that I could hack on the options.  Currently, there is only a makefile for osx, but adding others should be trivially easy and mainly involves getting the paths setup right.  You are expected to bring your own swig to the party.

	cd custom
	ant dist
	
The jar and native library will be put into custom/build/dist.  Test that it loads properly by running:
	java -jar build/dist/sqlite4java-custom.jar
	
