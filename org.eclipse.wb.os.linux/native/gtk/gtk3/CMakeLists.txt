project(wbp-libs C)
cmake_minimum_required(VERSION 2.6)

if (CMAKE_SIZEOF_VOID_P EQUAL 8)
	set(WBPLIBS_BITNESS_DEF "-DWBP_ARCH64")
else()
	set(WBPLIBS_BITNESS_DEF "-DWBP_ARCH32")
endif()

find_package(JNI REQUIRED)
if (NOT JNI_FOUND)
    message (FATAL_ERROR "Cannot found JNI dev files")
endif()
include_directories(${JNI_INCLUDE_DIRS})

find_package(PkgConfig REQUIRED)
pkg_check_modules(GTK3 REQUIRED "gtk+-3.0")
if (GTK3_FOUND)
	include_directories(${GTK3_INCLUDE_DIRS})
	link_directories(${GTK3_LIBRARY_DIRS})
	add_definitions(${GTK3_CFLAGS_OTHER})
	add_definitions(${WBPLIBS_BITNESS_DEF})
	add_library(wbp3 SHARED ../common/utils.c rcp.c)
	target_link_libraries(wbp3 ${GTK3_LIBRARIES})
else()
	message (FATAL_ERROR "Cannot found dev libs for Gtk3")
endif()



