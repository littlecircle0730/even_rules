cmake_minimum_required(VERSION 3.16)
project(TinkeringZone)

find_package(Threads REQUIRED)
find_package(Boost 1.66.0 REQUIRED COMPONENTS thread system)
find_package(ABY)

#looks dirty, but since the relic library is poorly exported let's treat it as an external library
set(RELIC_LIB_DIR "/usr/local/lib/librelic_s.a")

add_library(RELIC::relic STATIC IMPORTED)
set_target_properties(RELIC::relic PROPERTIES
	IMPORTED_LOCATION ${RELIC_LIB_DIR}
	INTERFACE_INCLUDE_DIRECTORIES "/usr/local/include/relic;/usr/local/include/relic/low"
)

if (ABY_FOUND)
    message(STATUS "Found ABY")
elseif(NOT ABY_FOUND AND NOT TARGET ABY::aby)
    message("ABY was not found: add ABY subdirectory")
    add_subdirectory(extern/ABY)
endif()

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

#set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fPIC")

set(NATIVE_FILES padec_natpsi_NativeMeth.cpp extern/unified_circ_lib/circs.cpp)

add_library(JNIMeth SHARED ${NATIVE_FILES})

target_link_libraries(JNIMeth ABY::aby ENCRYPTO_utils::encrypto_utils)

#set_property(TARGET JNIClient PROPERTY POSITION_INDEPENDENT_CODE ON)

#add_executable(tinkering_server tinkering_server.cpp)
#add_executable(tinkering_client tinkering_client.cpp)

#add_library(psiclient STATIC circ_lib/circs_client.cpp)
#add_library(psiserver STATIC circ_lib/circs_server.cpp)

#target_link_libraries(psiclient ABY::aby ENCRYPTO_utils::encrypto_utils)
#target_link_libraries(psiserver ABY::aby ENCRYPTO_utils::encrypto_utils)
#target_link_libraries(tinkering_server ABY::aby ENCRYPTO_utils::encrypto_utils)
#target_link_libraries(tinkering_client ABY::aby ENCRYPTO_utils::encrypto_utils)