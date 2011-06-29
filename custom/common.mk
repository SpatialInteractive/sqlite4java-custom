JNI_SOURCES := build/swig/sqlite_wrap.c \
		../native/sqlite3_wrap_manual.c \
		../native/intarray.c

SQLITE_OPTIONS ?= -DNDEBUG -DSQLITE_ENABLE_COLUMN_METADATA -DSQLITE_ENABLE_FTS3 \
			-DSQLITE_ENABLE_FTS3_PARENTHESIS -DSQLITE_ENABLE_MEMORY_MANAGEMENT \
			-DSQLITE_ENABLE_STAT2 -DHAVE_READLINE=0 -DSQLITE_THREADSAFE=1 \
			-DSQLITE_THREAD_OVERRIDE_LOCK=-1 -DTEMP_STORE=1 \
			-DSQLITE_OMIT_DEPRECATED -DSQLITE_OS_UNIX=1 \
			-DSQLITE_ENABLE_RTREE=1
 
		
ifdef SQLITE_USE_BUNDLED
	SQLITE_LIBS :=
	SQLITE_CFLAGS := \
			-I../sqlite \
			$(SQLITE_OPTIONS)
	SQLITE_SOURCES := ../sqlite/sqlite3.c
else
	SQLITE_LIBS := -lsqlite3
	SQLITE_CFLAGS := 
	SQLITE_SOURCES :=
endif

JAVA_CMD ?= java
JAVA_CFLAGS ?= $(shell $(JAVA_CMD) -jar java-config.jar --cflags)
CFLAGS ?= -O3
CC ?= gcc

clean:
	rm -f build/dist/*.so build/dist/*.jnilib

