# How to make a proper Makefile!

# 0. Make a default rule! It should let you recompile your libs.

.PHONY: all
all: libJNIMeth.so

# 1. Create a way to get headers!

padec_natpsi_NativeMeth.h: ../src/padec/natpsi/NativeMeth.java
	javac -h . ../src/padec/natpsi/NativeMeth.java

.PHONY: headers
headers: padec_natpsi_NativeMeth.h

# 2. Compile your libs!

libJNIMeth.so: padec_natpsi_NativeMeth.cpp padec_natpsi_NativeMeth.h
	mkdir build;cd build && cmake .. && make && cp libJNIMeth.so .. && cd ..

.PHONY: libs
libs: libJNIMeth.so

# 3. Install your libs!
/usr/lib/libJNIMeth.so: libJNIMeth.so
	cp libJNIMeth.so /usr/lib/

.PHONY: install
install: /usr/lib/libJNIMeth.so

# 4. Clean!
.PHONY: clean

clean:
	rm -rf build;rm *.class;rm libJNIMeth.so;rm *.h