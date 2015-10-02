LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := JniBitmapOperationsLibrary
LOCAL_SRC_FILES := JniBitmapOperationsLibrary/JniBitmapOperationsLibrary.cpp
LOCAL_LDLIBS := -llog
LOCAL_LDFLAGS += -ljnigraphics

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE                  := DeflatingDecompressor-v3
LOCAL_SRC_FILES               := DeflatingDecompressor/DeflatingDecompressor.cpp
LOCAL_LDLIBS                  := -lz

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE                  := LineBreak-v2
LOCAL_SRC_FILES               := LineBreak/LineBreaker.cpp LineBreak/liblinebreak-2.0/linebreak.c LineBreak/liblinebreak-2.0/linebreakdata.c LineBreak/liblinebreak-2.0/linebreakdef.c

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

EXPAT_DIR                     := expat-2.0.1

LOCAL_MODULE                  := expat
LOCAL_SRC_FILES               := $(EXPAT_DIR)/lib/xmlparse.c $(EXPAT_DIR)/lib/xmlrole.c $(EXPAT_DIR)/lib/xmltok.c
LOCAL_CFLAGS                  := -DHAVE_EXPAT_CONFIG_H -ggdb
LOCAL_C_INCLUDES              := $(LOCAL_PATH)/$(EXPAT_DIR)
LOCAL_EXPORT_C_INCLUDES       := $(LOCAL_PATH)/$(EXPAT_DIR)/lib

include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := djvudroid
LOCAL_CFLAGS    := -DHAVE_CONFIG_H -DTHREADMODEL=NOTHREADS -DDEBUGLVL=0 -fpermissive
LOCAL_LDLIBS 	:= -Wl,-llog -Wl
LOCAL_SRC_FILES := djvudroid/Arrays.cpp \
	djvudroid/BSByteStream.cpp \
	djvudroid/BSEncodeByteStream.cpp \
	djvudroid/ByteStream.cpp \
	djvudroid/DataPool.cpp \
	djvudroid/DjVmDir.cpp \
	djvudroid/DjVmDir0.cpp \
	djvudroid/DjVmDoc.cpp \
	djvudroid/DjVmNav.cpp \
	djvudroid/DjVuAnno.cpp \
	djvudroid/DjVuDocEditor.cpp \
	djvudroid/DjVuDocument.cpp \
	djvudroid/DjVuDumpHelper.cpp \
	djvudroid/DjVuErrorList.cpp \
	djvudroid/DjVuFile.cpp \
	djvudroid/DjVuFileCache.cpp \
	djvudroid/DjVuGlobal.cpp \
	djvudroid/DjVuGlobalMemory.cpp \
	djvudroid/DjVuImage.cpp \
	djvudroid/DjVuInfo.cpp \
	djvudroid/DjVuMessage.cpp \
	djvudroid/DjVuMessageLite.cpp \
	djvudroid/DjVuNavDir.cpp \
	djvudroid/DjVuPalette.cpp \
	djvudroid/DjVuPort.cpp \
	djvudroid/DjVuText.cpp \
	djvudroid/GBitmap.cpp \
	djvudroid/GContainer.cpp \
	djvudroid/GException.cpp \
	djvudroid/GIFFManager.cpp \
	djvudroid/GMapAreas.cpp \
	djvudroid/GOS.cpp \
	djvudroid/GPixmap.cpp \
	djvudroid/GRect.cpp \
	djvudroid/GScaler.cpp \
	djvudroid/GSmartPointer.cpp \
	djvudroid/GString.cpp \
	djvudroid/GThreads.cpp \
	djvudroid/GURL.cpp \
	djvudroid/GUnicode.cpp \
	djvudroid/IFFByteStream.cpp \
	djvudroid/IW44EncodeCodec.cpp \
	djvudroid/IW44Image.cpp \
	djvudroid/JB2EncodeCodec.cpp \
	djvudroid/JB2Image.cpp \
	djvudroid/JPEGDecoder.cpp \
	djvudroid/MMRDecoder.cpp \
	djvudroid/MMX.cpp \
	djvudroid/UnicodeByteStream.cpp \
	djvudroid/XMLParser.cpp \
	djvudroid/XMLTags.cpp \
	djvudroid/ZPCodec.cpp \
	djvudroid/atomic.cpp \
	djvudroid/debug.cpp \
	djvudroid/DjVuToPS.cpp \
	djvudroid/ddjvuapi.cpp \
	djvudroid/miniexp.cpp

include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE                  := mupdf

# jpeg
# uses pristine source tree

# Homepage: http://www.ijg.org/
# Original Licence: see jpeg/README
# Original Copyright (C) 1991-2009, Thomas G. Lane, Guido Vollbeding

MY_JPEG_SRC_FILES := \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jcapimin.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jcapistd.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jcarith.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jctrans.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jcparam.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jdatadst.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jcinit.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jcmaster.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jcmarker.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jcmainct.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jcprepct.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jccoefct.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jccolor.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jcsample.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jchuff.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jcdctmgr.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jfdctfst.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jfdctflt.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jfdctint.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jdapimin.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jdapistd.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jdarith.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jdtrans.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jdatasrc.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jdmaster.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jdinput.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jdmarker.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jdhuff.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jdmainct.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jdcoefct.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jdpostct.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jddctmgr.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jidctfst.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jidctflt.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jidctint.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jdsample.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jdcolor.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jquant1.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jquant2.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jdmerge.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jaricom.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jcomapi.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jutils.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jerror.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jmemmgr.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg/jmemnobs.c

# freetype
# (flat file hierarchy, use 
# "cp .../freetype-.../src/*/*.[ch] freetype/"
#  and copy over the full include/ subdirectory)

# Homepage: http://freetype.org/
# Original Licence: GPL 2 (or its own, but for the purposes
#                   of this project, GPL is fine)
# 

MY_FREETYPE_C_INCLUDES := \
	$(LOCAL_PATH)/NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/include

MY_FREETYPE_CFLAGS := -DFT2_BUILD_LIBRARY

# libz provided by the Android-3 Stable Native API:
MY_FREETYPE_LDLIBS := -lz

# see freetype/doc/INSTALL.ANY for further customization,
# currently, all sources are being built
MY_FREETYPE_SRC_FILES := \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftsystem.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftinit.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftdebug.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftbase.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftbbox.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftglyph.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftbdf.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftbitmap.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftcid.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftfstype.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftgasp.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftgxval.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftlcdfil.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftmm.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftotval.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftpatent.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftpfr.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftstroke.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftsynth.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/fttype1.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftwinfnt.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/base/ftxf86.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/bdf/bdf.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/cff/cff.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/cid/type1cid.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/pcf/pcf.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/pfr/pfr.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/sfnt/sfnt.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/truetype/truetype.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/type1/type1.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/type42/type42.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/winfonts/winfnt.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/raster/raster.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/smooth/smooth.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/autofit/autofit.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/cache/ftcache.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/gzip/ftgzip.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/lzw/ftlzw.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/gxvalid/gxvalid.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/otvalid/otvalid.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/psaux/psaux.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/pshinter/pshinter.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/src/psnames/psnames.c

# mupdf
# pristine source tree

# Homepage: http://ccxvii.net/mupdf/
# Licence: GPL 3
# MuPDF is Copyright 2006-2009 Artifex Software, Inc. 

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH)/NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/include \
	$(LOCAL_PATH)/NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg \
	$(LOCAL_PATH)/NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitzdraw \
	$(LOCAL_PATH)/NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz \
	$(LOCAL_PATH)/NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf \
	$(LOCAL_PATH)/NativeFormats 	

# use this to build w/o a CJK font built-in:
#MY_MUPDF_CFLAGS := -Drestrict= -DNOCJK
# but see caveat below, unexpected breakage may occur.
# ATM, the irony is that CJK compiles in a bit-wise copy
# of Androids own droid.ttf ... Maybe resort to pointing
# to it in the filesystem? But this would violate proper
# API use. Bleh.
LOCAL_CFLAGS := -Drestrict= -DNOCJK $(MY_FREETYPE_CFLAGS)
LOCAL_LDLIBS := -lz

LOCAL_SRC_FILES := \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_crypt.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_debug.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_lex.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_nametree.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_open.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_parse.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_repair.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_stream.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_xref.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_annot.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_outline.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_cmap.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_cmap_parse.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_cmap_load.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_cmap_table.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_fontagl.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_fontenc.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_unicode.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_font.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_type3.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_fontmtx.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_fontfile.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_function.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_colorspace1.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_colorspace2.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_image.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_pattern.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_shade.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_shade1.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_shade4.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_xobject.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_build.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_interpret.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_page.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_pagetree.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/pdf_store.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitzdraw/glyphcache.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf-overlay/fitzdraw/pixmap.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitzdraw/porterduff.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitzdraw/meshdraw.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitzdraw/imagedraw.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitzdraw/imageunpack.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitzdraw/imagescale.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitzdraw/pathscan.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitzdraw/pathfill.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitzdraw/pathstroke.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf-overlay/fitzdraw/render.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitzdraw/blendmodes.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/base_cpudep.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/base_error.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/base_hash.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/base_matrix.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/base_memory.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/base_rect.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/base_string.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/base_unicode.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/util_getopt.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/crypt_aes.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/crypt_arc4.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/crypt_crc32.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/crypt_md5.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/obj_array.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/obj_dict.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/obj_parse.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/obj_print.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/obj_simple.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/stm_buffer.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/stm_filter.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/stm_open.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/stm_read.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/stm_misc.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/filt_pipeline.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/filt_basic.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/filt_arc4.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/filt_aesd.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/filt_dctd.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/filt_faxd.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/filt_faxdtab.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/filt_flate.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/filt_lzwd.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/filt_predict.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/node_toxml.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/node_misc1.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/node_misc2.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/node_path.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/node_text.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/node_tree.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/res_colorspace.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/res_font.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/res_image.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz/res_shade.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/font_mono.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/font_serif.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/font_sans.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/font_misc.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/cmap_cns.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/cmap_korea.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/cmap_tounicode.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/cmap_japan.c \
	NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf/cmap_gb.c \
	$(MY_FREETYPE_SRC_FILES) \
	$(MY_JPEG_SRC_FILES) 


include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE                  := NativeFormats-v4
LOCAL_CFLAGS                  := -Wall -ggdb 
LOCAL_LDLIBS                  := -lz -llog
LOCAL_STATIC_LIBRARIES        := expat mupdf djvudroid


LOCAL_SRC_FILES               := \
	NativeFormats/main.cpp \
	NativeFormats/JavaNativeFormatPlugin.cpp \
	NativeFormats/JavaPluginCollection.cpp \
	NativeFormats/DjvuDroidBridge.cpp \
	NativeFormats/util/AndroidUtil.cpp \
	NativeFormats/util/JniEnvelope.cpp \
	NativeFormats/zlibrary/core/src/constants/ZLXMLNamespace.cpp \
	NativeFormats/zlibrary/core/src/drm/FileEncryptionInfo.cpp \
	NativeFormats/zlibrary/core/src/encoding/DummyEncodingConverter.cpp \
	NativeFormats/zlibrary/core/src/encoding/Utf16EncodingConverters.cpp \
	NativeFormats/zlibrary/core/src/encoding/Utf8EncodingConverter.cpp \
	NativeFormats/zlibrary/core/src/encoding/JavaEncodingConverter.cpp \
	NativeFormats/zlibrary/core/src/encoding/ZLEncodingCollection.cpp \
	NativeFormats/zlibrary/core/src/encoding/ZLEncodingConverter.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLDir.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLFSManager.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLFile.cpp \
	NativeFormats/zlibrary/core/src/filesystem/ZLInputStreamDecorator.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLGzipInputStream.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZDecompressor.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipDir.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipEntryCache.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipHeader.cpp \
	NativeFormats/zlibrary/core/src/filesystem/zip/ZLZipInputStream.cpp \
	NativeFormats/zlibrary/core/src/language/ZLCharSequence.cpp \
	NativeFormats/zlibrary/core/src/language/ZLLanguageDetector.cpp \
	NativeFormats/zlibrary/core/src/language/ZLLanguageList.cpp \
	NativeFormats/zlibrary/core/src/language/ZLLanguageMatcher.cpp \
	NativeFormats/zlibrary/core/src/language/ZLStatistics.cpp \
	NativeFormats/zlibrary/core/src/language/ZLStatisticsGenerator.cpp \
	NativeFormats/zlibrary/core/src/language/ZLStatisticsItem.cpp \
	NativeFormats/zlibrary/core/src/language/ZLStatisticsXMLReader.cpp \
	NativeFormats/zlibrary/core/src/library/ZLibrary.cpp \
	NativeFormats/zlibrary/core/src/logger/ZLLogger.cpp \
	NativeFormats/zlibrary/core/src/util/ZLFileUtil.cpp \
	NativeFormats/zlibrary/core/src/util/ZLLanguageUtil.cpp \
	NativeFormats/zlibrary/core/src/util/ZLStringUtil.cpp \
	NativeFormats/zlibrary/core/src/util/ZLUnicodeUtil.cpp \
	NativeFormats/zlibrary/core/src/xml/ZLAsynchronousInputStream.cpp \
	NativeFormats/zlibrary/core/src/xml/ZLPlainAsynchronousInputStream.cpp \
	NativeFormats/zlibrary/core/src/xml/ZLXMLReader.cpp \
	NativeFormats/zlibrary/core/src/xml/expat/ZLXMLReaderInternal.cpp \
	NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFSDir.cpp \
	NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFSManager.cpp \
	NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFileInputStream.cpp \
	NativeFormats/zlibrary/core/src/unix/filesystem/ZLUnixFileOutputStream.cpp \
	NativeFormats/zlibrary/core/src/unix/library/ZLUnixLibrary.cpp \
	NativeFormats/zlibrary/text/src/model/ZLCachedMemoryAllocator.cpp \
	NativeFormats/zlibrary/text/src/model/ZLTextModel.cpp \
	NativeFormats/zlibrary/text/src/model/ZLTextParagraph.cpp \
	NativeFormats/zlibrary/text/src/model/ZLTextStyleEntry.cpp \
	NativeFormats/zlibrary/text/src/model/ZLVideoEntry.cpp \
	NativeFormats/zlibrary/text/src/fonts/FontManager.cpp \
	NativeFormats/zlibrary/text/src/fonts/FontMap.cpp \
	NativeFormats/zlibrary/ui/src/android/filesystem/JavaFSDir.cpp \
	NativeFormats/zlibrary/ui/src/android/filesystem/JavaInputStream.cpp \
	NativeFormats/zlibrary/ui/src/android/filesystem/ZLAndroidFSManager.cpp \
	NativeFormats/zlibrary/ui/src/android/library/ZLAndroidLibraryImplementation.cpp \
	NativeFormats/fbreader/src/bookmodel/BookModel.cpp \
	NativeFormats/fbreader/src/bookmodel/BookReader.cpp \
	NativeFormats/fbreader/src/formats/EncodedTextReader.cpp \
	NativeFormats/fbreader/src/formats/FormatPlugin.cpp \
	NativeFormats/fbreader/src/formats/PluginCollection.cpp \
	NativeFormats/fbreader/src/formats/fb2/FB2BookReader.cpp \
	NativeFormats/fbreader/src/formats/fb2/FB2CoverReader.cpp \
	NativeFormats/fbreader/src/formats/fb2/FB2MetaInfoReader.cpp \
	NativeFormats/fbreader/src/formats/fb2/FB2Plugin.cpp \
	NativeFormats/fbreader/src/formats/fb2/FB2Reader.cpp \
	NativeFormats/fbreader/src/formats/fb2/FB2TagManager.cpp \
	NativeFormats/fbreader/src/formats/fb2/FB2UidReader.cpp \
	NativeFormats/fbreader/src/formats/css/CSSInputStream.cpp \
	NativeFormats/fbreader/src/formats/css/CSSSelector.cpp \
	NativeFormats/fbreader/src/formats/css/StringInputStream.cpp \
	NativeFormats/fbreader/src/formats/css/StyleSheetParser.cpp \
	NativeFormats/fbreader/src/formats/css/StyleSheetTable.cpp \
	NativeFormats/fbreader/src/formats/css/StyleSheetUtil.cpp \
	NativeFormats/fbreader/src/formats/html/HtmlBookReader.cpp \
	NativeFormats/fbreader/src/formats/html/HtmlDescriptionReader.cpp \
	NativeFormats/fbreader/src/formats/html/HtmlEntityCollection.cpp \
	NativeFormats/fbreader/src/formats/html/HtmlPlugin.cpp \
	NativeFormats/fbreader/src/formats/html/HtmlReader.cpp \
	NativeFormats/fbreader/src/formats/html/HtmlReaderStream.cpp \
	NativeFormats/fbreader/src/formats/oeb/NCXReader.cpp \
	NativeFormats/fbreader/src/formats/oeb/OEBBookReader.cpp \
	NativeFormats/fbreader/src/formats/oeb/OEBCoverReader.cpp \
	NativeFormats/fbreader/src/formats/oeb/OEBEncryptionReader.cpp \
	NativeFormats/fbreader/src/formats/oeb/OEBMetaInfoReader.cpp \
	NativeFormats/fbreader/src/formats/oeb/OEBPlugin.cpp \
	NativeFormats/fbreader/src/formats/oeb/OEBSimpleIdReader.cpp \
	NativeFormats/fbreader/src/formats/oeb/OEBTextStream.cpp \
	NativeFormats/fbreader/src/formats/oeb/OEBUidReader.cpp \
	NativeFormats/fbreader/src/formats/oeb/OPFReader.cpp \
	NativeFormats/fbreader/src/formats/oeb/XHTMLImageFinder.cpp \
	NativeFormats/fbreader/src/formats/pdb/BitReader.cpp \
	NativeFormats/fbreader/src/formats/pdb/DocDecompressor.cpp \
	NativeFormats/fbreader/src/formats/pdb/HtmlMetainfoReader.cpp \
	NativeFormats/fbreader/src/formats/pdb/HuffDecompressor.cpp \
	NativeFormats/fbreader/src/formats/pdb/MobipocketHtmlBookReader.cpp \
	NativeFormats/fbreader/src/formats/pdb/MobipocketPlugin.cpp \
	NativeFormats/fbreader/src/formats/pdb/PalmDocLikePlugin.cpp \
	NativeFormats/fbreader/src/formats/pdb/PalmDocLikeStream.cpp \
	NativeFormats/fbreader/src/formats/pdb/PalmDocStream.cpp \
	NativeFormats/fbreader/src/formats/pdb/PdbPlugin.cpp \
	NativeFormats/fbreader/src/formats/pdb/PdbReader.cpp \
	NativeFormats/fbreader/src/formats/pdb/PdbStream.cpp \
	NativeFormats/fbreader/src/formats/pdb/SimplePdbPlugin.cpp \
	NativeFormats/fbreader/src/formats/rtf/RtfBookReader.cpp \
	NativeFormats/fbreader/src/formats/rtf/RtfDescriptionReader.cpp \
	NativeFormats/fbreader/src/formats/rtf/RtfPlugin.cpp \
	NativeFormats/fbreader/src/formats/rtf/RtfReader.cpp \
	NativeFormats/fbreader/src/formats/rtf/RtfReaderStream.cpp \
	NativeFormats/fbreader/src/formats/txt/PlainTextFormat.cpp \
	NativeFormats/fbreader/src/formats/txt/TxtBookReader.cpp \
	NativeFormats/fbreader/src/formats/txt/TxtPlugin.cpp \
	NativeFormats/fbreader/src/formats/txt/TxtReader.cpp \
	NativeFormats/fbreader/src/formats/guji/GujiTextFormat.cpp \
	NativeFormats/fbreader/src/formats/guji/GujiBookReader.cpp \
	NativeFormats/fbreader/src/formats/guji/GujiPlugin.cpp \
	NativeFormats/fbreader/src/formats/guji/GujiReader.cpp \
	NativeFormats/fbreader/src/formats/pdf/PdfPlugin.cpp \
	NativeFormats/fbreader/src/formats/pdf/PdfBridge.cpp \
	NativeFormats/fbreader/src/formats/pdf/PdfDescriptionReader.cpp \
	NativeFormats/fbreader/src/formats/pdf/PdfBookReader.cpp \
	NativeFormats/fbreader/src/formats/util/EntityFilesCollector.cpp \
	NativeFormats/fbreader/src/formats/util/MergedStream.cpp \
	NativeFormats/fbreader/src/formats/util/MiscUtil.cpp \
	NativeFormats/fbreader/src/formats/util/XMLTextStream.cpp \
	NativeFormats/fbreader/src/formats/xhtml/XHTMLReader.cpp \
	NativeFormats/fbreader/src/formats/xhtml/XHTMLTagInfo.cpp \
	NativeFormats/fbreader/src/formats/doc/DocBookReader.cpp \
	NativeFormats/fbreader/src/formats/doc/DocMetaInfoReader.cpp \
	NativeFormats/fbreader/src/formats/doc/DocPlugin.cpp \
	NativeFormats/fbreader/src/formats/doc/DocStreams.cpp \
	NativeFormats/fbreader/src/formats/doc/OleMainStream.cpp \
	NativeFormats/fbreader/src/formats/doc/OleStorage.cpp \
	NativeFormats/fbreader/src/formats/doc/OleStream.cpp \
	NativeFormats/fbreader/src/formats/doc/OleStreamParser.cpp \
	NativeFormats/fbreader/src/formats/doc/OleStreamReader.cpp \
	NativeFormats/fbreader/src/formats/doc/OleUtil.cpp \
	NativeFormats/fbreader/src/formats/doc/DocInlineImageReader.cpp \
	NativeFormats/fbreader/src/formats/doc/DocFloatImageReader.cpp \
	NativeFormats/fbreader/src/formats/doc/DocAnsiConverter.cpp \
	NativeFormats/fbreader/src/library/Author.cpp \
	NativeFormats/fbreader/src/library/Book.cpp \
	NativeFormats/fbreader/src/library/Comparators.cpp \
	NativeFormats/fbreader/src/library/Tag.cpp \
	NativeFormats/fbreader/src/library/UID.cpp

LOCAL_C_INCLUDES              := \
	$(LOCAL_PATH)/NativeFormats/util \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/constants \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/drm \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/encoding \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/filesystem \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/image \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/language \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/library \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/logger \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/util \
	$(LOCAL_PATH)/NativeFormats/zlibrary/core/src/xml \
	$(LOCAL_PATH)/NativeFormats/zlibrary/text/src/model \
	$(LOCAL_PATH)/NativeFormats/zlibrary/text/src/fonts \
	$(LOCAL_PATH)/NativeFormats/fbreader/src/formats/pdf/mupdf/freetype/include \
	$(LOCAL_PATH)/NativeFormats/fbreader/src/formats/pdf/mupdf/jpeg \
	$(LOCAL_PATH)/NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitzdraw \
	$(LOCAL_PATH)/NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/fitz \
	$(LOCAL_PATH)/NativeFormats/fbreader/src/formats/pdf/mupdf/mupdf/mupdf \
	$(LOCAL_PATH)/NativeFormats \
	$(LOCAL_PATH)/djvudroid
	
LOCAL_CXX_INCLUDES := \
	$(LOCAL_PATH)/djvudroid	

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_MODULE    := hunspell-jni
LOCAL_SRC_FILES := hunspell/hunspell-jni.cpp hunspell/affentry.cxx hunspell/affixmgr.cxx hunspell/csutil.cxx \
                   hunspell/dictmgr.cxx hunspell/hashmgr.cxx hunspell/hunspell.cxx \
                   hunspell/suggestmgr.cxx hunspell/phonet.cxx hunspell/filemgr.cxx \
                   hunspell/hunzip.cxx hunspell/replist.cxx

LOCAL_CPP_EXTENSION := .cxx .cpp .cc

include $(BUILD_SHARED_LIBRARY)
