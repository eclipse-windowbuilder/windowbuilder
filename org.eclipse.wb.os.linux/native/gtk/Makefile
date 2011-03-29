# Makefile for creating WBP library for Linux GTK

# determine arch bits
ARCH_BITS = $(shell getconf LONG_BIT)

WBP_LIB = libwbp.so
WBP_COMPIZ_LIB = libwbp-compiz.so

OUTPUT_DIR = ../../os/linux/x86$(shell ./arch_dir.sh)

# Do not use pkg-config to get libs because it includes unnecessary dependencies (i.e. pangoxft-1.0)
GTKCFLAGS = `pkg-config --cflags gtk+-2.0`
GTKLIBS = `pkg-config --libs-only-L gtk+-2.0 gthread-2.0` -lgtk-x11-2.0 -L/usr/X11R6/lib $(XLIB64)
GCONFFLAGS = `pkg-config --cflags gconf-2.0`
GCONFLIBS = `pkg-config --libs gconf-2.0`

DEFINES = -DWBP_ARCH$(ARCH_BITS)

CFLAGS = -O2 \
	-Wall \
	-fPIC \
	-fno-stack-protector \
	-g \
	-I. \
	-I$(JAVA_HOME)/include \
	-I$(JAVA_HOME)/include/linux
	
LIBS = -shared -Wl,--as-needed
	
WBP_OBJECTS = utils.o core.o rcp.o
WBP_COMPIZ_OBJECTS = utils.o gconf.o

ifndef NO_STRIP
	LIBS := $(LIBS) -s
endif

all: make_wbp make_wbp_compiz

#
# WBP lib
#
make_wbp:$(WBP_LIB)
make_wbp_compiz:$(WBP_COMPIZ_LIB)

$(WBP_LIB): $(WBP_OBJECTS)
	$(CC) -o $(WBP_LIB) $(GTKCFLAGS) $(CFLAGS) $(WBP_OBJECTS) $(GTKLIBS) $(LIBS)

$(WBP_COMPIZ_LIB): $(WBP_COMPIZ_OBJECTS)
	$(CC) -o $(WBP_COMPIZ_LIB) $(GTKCFLAGS) $(CFLAGS) $(WBP_COMPIZ_OBJECTS) $(GTKLIBS) $(GCONFLIBS) $(LIBS)

utils.o: utils.c
	$(CC) $(GTKCFLAGS) $(CFLAGS) $(DEFINES) -c utils.c
core.o: core.c
	$(CC) $(GTKCFLAGS) $(CFLAGS) $(DEFINES) -c core.c
rcp.o: rcp.c
	$(CC) $(GTKCFLAGS) $(CFLAGS) $(DEFINES) -c rcp.c
gconf.o: gconf.c
	$(CC) $(GTKCFLAGS) $(CFLAGS) $(GCONFFLAGS) $(DEFINES) -c gconf.c



#
# Install
#
install: all
	mkdir -p $(OUTPUT_DIR)
	cp *.so $(OUTPUT_DIR)

#
# Clean
#
clean:
	rm -f *.o *.so
