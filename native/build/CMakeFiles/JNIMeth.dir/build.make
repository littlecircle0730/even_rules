# CMAKE generated file: DO NOT EDIT!
# Generated by "Unix Makefiles" Generator, CMake Version 3.16

# Delete rule output on recipe failure.
.DELETE_ON_ERROR:


#=============================================================================
# Special targets provided by cmake.

# Disable implicit rules so canonical targets will work.
.SUFFIXES:


# Remove some rules from gmake that .SUFFIXES does not remove.
SUFFIXES =

.SUFFIXES: .hpux_make_needs_suffix_list


# Suppress display of executed commands.
$(VERBOSE).SILENT:


# A target that is always out of date.
cmake_force:

.PHONY : cmake_force

#=============================================================================
# Set environment variables for the build.

# The shell in which to execute make rules.
SHELL = /bin/sh

# The CMake executable.
CMAKE_COMMAND = /usr/bin/cmake

# The command to remove a file.
RM = /usr/bin/cmake -E remove -f

# Escaping for special characters.
EQUALS = =

# The top-level source directory on which CMake was run.
CMAKE_SOURCE_DIR = /home/padecteam/padec_theone/native

# The top-level build directory on which CMake was run.
CMAKE_BINARY_DIR = /home/padecteam/padec_theone/native/build

# Include any dependencies generated for this target.
include CMakeFiles/JNIMeth.dir/depend.make

# Include the progress variables for this target.
include CMakeFiles/JNIMeth.dir/progress.make

# Include the compile flags for this target's objects.
include CMakeFiles/JNIMeth.dir/flags.make

CMakeFiles/JNIMeth.dir/padec_natpsi_NativeMeth.cpp.o: CMakeFiles/JNIMeth.dir/flags.make
CMakeFiles/JNIMeth.dir/padec_natpsi_NativeMeth.cpp.o: ../padec_natpsi_NativeMeth.cpp
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/padecteam/padec_theone/native/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_1) "Building CXX object CMakeFiles/JNIMeth.dir/padec_natpsi_NativeMeth.cpp.o"
	/usr/bin/c++  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -o CMakeFiles/JNIMeth.dir/padec_natpsi_NativeMeth.cpp.o -c /home/padecteam/padec_theone/native/padec_natpsi_NativeMeth.cpp

CMakeFiles/JNIMeth.dir/padec_natpsi_NativeMeth.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/JNIMeth.dir/padec_natpsi_NativeMeth.cpp.i"
	/usr/bin/c++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /home/padecteam/padec_theone/native/padec_natpsi_NativeMeth.cpp > CMakeFiles/JNIMeth.dir/padec_natpsi_NativeMeth.cpp.i

CMakeFiles/JNIMeth.dir/padec_natpsi_NativeMeth.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/JNIMeth.dir/padec_natpsi_NativeMeth.cpp.s"
	/usr/bin/c++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /home/padecteam/padec_theone/native/padec_natpsi_NativeMeth.cpp -o CMakeFiles/JNIMeth.dir/padec_natpsi_NativeMeth.cpp.s

CMakeFiles/JNIMeth.dir/extern/unified_circ_lib/circs.cpp.o: CMakeFiles/JNIMeth.dir/flags.make
CMakeFiles/JNIMeth.dir/extern/unified_circ_lib/circs.cpp.o: ../extern/unified_circ_lib/circs.cpp
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --progress-dir=/home/padecteam/padec_theone/native/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_2) "Building CXX object CMakeFiles/JNIMeth.dir/extern/unified_circ_lib/circs.cpp.o"
	/usr/bin/c++  $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -o CMakeFiles/JNIMeth.dir/extern/unified_circ_lib/circs.cpp.o -c /home/padecteam/padec_theone/native/extern/unified_circ_lib/circs.cpp

CMakeFiles/JNIMeth.dir/extern/unified_circ_lib/circs.cpp.i: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Preprocessing CXX source to CMakeFiles/JNIMeth.dir/extern/unified_circ_lib/circs.cpp.i"
	/usr/bin/c++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -E /home/padecteam/padec_theone/native/extern/unified_circ_lib/circs.cpp > CMakeFiles/JNIMeth.dir/extern/unified_circ_lib/circs.cpp.i

CMakeFiles/JNIMeth.dir/extern/unified_circ_lib/circs.cpp.s: cmake_force
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green "Compiling CXX source to assembly CMakeFiles/JNIMeth.dir/extern/unified_circ_lib/circs.cpp.s"
	/usr/bin/c++ $(CXX_DEFINES) $(CXX_INCLUDES) $(CXX_FLAGS) -S /home/padecteam/padec_theone/native/extern/unified_circ_lib/circs.cpp -o CMakeFiles/JNIMeth.dir/extern/unified_circ_lib/circs.cpp.s

# Object files for target JNIMeth
JNIMeth_OBJECTS = \
"CMakeFiles/JNIMeth.dir/padec_natpsi_NativeMeth.cpp.o" \
"CMakeFiles/JNIMeth.dir/extern/unified_circ_lib/circs.cpp.o"

# External object files for target JNIMeth
JNIMeth_EXTERNAL_OBJECTS =

libJNIMeth.so: CMakeFiles/JNIMeth.dir/padec_natpsi_NativeMeth.cpp.o
libJNIMeth.so: CMakeFiles/JNIMeth.dir/extern/unified_circ_lib/circs.cpp.o
libJNIMeth.so: CMakeFiles/JNIMeth.dir/build.make
libJNIMeth.so: /usr/local/lib/libaby.a
libJNIMeth.so: /usr/local/lib/libencrypto_utils.a
libJNIMeth.so: /usr/local/lib/libotextension.a
libJNIMeth.so: /usr/local/lib/libencrypto_utils.a
libJNIMeth.so: /usr/lib/x86_64-linux-gnu/libboost_system.so.1.71.0
libJNIMeth.so: /usr/lib/x86_64-linux-gnu/libboost_thread.so.1.71.0
libJNIMeth.so: /usr/lib/x86_64-linux-gnu/libboost_atomic.so.1.71.0
libJNIMeth.so: /usr/lib/x86_64-linux-gnu/libgmpxx.so
libJNIMeth.so: /usr/lib/x86_64-linux-gnu/libcrypto.so
libJNIMeth.so: /usr/local/lib/librelic_s.a
libJNIMeth.so: /usr/lib/x86_64-linux-gnu/libgmp.so
libJNIMeth.so: CMakeFiles/JNIMeth.dir/link.txt
	@$(CMAKE_COMMAND) -E cmake_echo_color --switch=$(COLOR) --green --bold --progress-dir=/home/padecteam/padec_theone/native/build/CMakeFiles --progress-num=$(CMAKE_PROGRESS_3) "Linking CXX shared library libJNIMeth.so"
	$(CMAKE_COMMAND) -E cmake_link_script CMakeFiles/JNIMeth.dir/link.txt --verbose=$(VERBOSE)

# Rule to build all files generated by this target.
CMakeFiles/JNIMeth.dir/build: libJNIMeth.so

.PHONY : CMakeFiles/JNIMeth.dir/build

CMakeFiles/JNIMeth.dir/clean:
	$(CMAKE_COMMAND) -P CMakeFiles/JNIMeth.dir/cmake_clean.cmake
.PHONY : CMakeFiles/JNIMeth.dir/clean

CMakeFiles/JNIMeth.dir/depend:
	cd /home/padecteam/padec_theone/native/build && $(CMAKE_COMMAND) -E cmake_depends "Unix Makefiles" /home/padecteam/padec_theone/native /home/padecteam/padec_theone/native /home/padecteam/padec_theone/native/build /home/padecteam/padec_theone/native/build /home/padecteam/padec_theone/native/build/CMakeFiles/JNIMeth.dir/DependInfo.cmake --color=$(COLOR)
.PHONY : CMakeFiles/JNIMeth.dir/depend

