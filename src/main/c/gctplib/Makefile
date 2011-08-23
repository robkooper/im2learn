#------------------------------------------------------
# Makefile for MRTInfertace.
# Cygwin version (Windows).
#------------------------------------------------------

JAVADIR = /etc/java
MODISDIR = /etc/Modis
CLASSDIR = /etc/im2learn/classes

CC = gcc
WCFLAGS = -mno-cygwin -Wall -Wl,--add-stdcall-alias
WINCS = -I$(JAVADIR)/include -I$(JAVADIR)/include/win32 -I$(MODISDIR)/include
LCFLAGS = -Wall  
LINCS = -I$(JAVADIR)/include -I$(JAVADIR)/include/linux -I$(MODISDIR)/include

#-----------------------------------------------------------
# Define the source code for MRTInterface:
#-----------------------------------------------------------
SRC = ncsa_im2learn_core_geo_projection_MRTInterface.c 
ICLASS = ncsa_im2learn_core_geo_projection_MRTInterface

#-----------------------------
# Define the object libraries:
#-----------------------------
# GCTPLIB = -lgctp
GCTPLIB = $(MODISDIR)/gctp/libgctp.a

#---------------------------------
# Define MRTInterface Library:
#---------------------------------
DLL = MRTInterface.dll
SO = MRTInterface.so 

#-----------------------------
# Targets for each executable:
#-----------------------------

javah: 
	$(JAVADIR)/bin/javah -d . -classpath $(CLASSDIR) $(ICLASS)

windows: 
	$(CC) $(WCFLAGS) $(WINCS) -shared -o $(DLL) $(SRC) $(GCTPLIB) -lm -s

linux:
	$(CC) $(LCFLAGS) $(LINCS) -shared -Wl,-soname,$(SO) $(SRC) $(GCTPLIB) -lm -s 

