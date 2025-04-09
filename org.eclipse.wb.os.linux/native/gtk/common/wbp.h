/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
#include <stdio.h>
#include <stdlib.h>

#include <gtk/gtk.h>
#include <gdk/gdkx.h>
#include <jni.h>

#if (defined(__LP64__) && (!defined(WBP_ARCH64))) || defined(_WIN64)
	#if !defined(WBP_ARCH64)
		#define WBP_ARCH64
	#endif
#endif /*__LP64__*/

#define JHANDLE jlong
#define JHANDLEARRAY jlongArray
#ifdef WBP_ARCH64
	#define CHANDLE jlong
#else
	#define CHANDLE jint
#endif

#define GTK_WIDGET_X(arg0) (arg0)->allocation.x
#define GTK_WIDGET_Y(arg0) (arg0)->allocation.y
#define GTK_WIDGET_WIDTH(arg0) (arg0)->allocation.width
#define GTK_WIDGET_HEIGHT(arg0) (arg0)->allocation.height

#define CALLBACK_SIG "(Ljava/lang/Number;Ljava/lang/Number;)V"
#define OS_NATIVE(func) Java_org_eclipse_wb_internal_os_linux_OSSupportLinux_##func

////////////////////////////////////////////////////////////////////////////
//
// GdkRectangle
//
////////////////////////////////////////////////////////////////////////////

typedef struct JGdkRectangle {
	jclass clazz;
	jfieldID x;
	jfieldID y;
	jfieldID width;
	jfieldID height;
} JGdkRectangle;
static JGdkRectangle GDK_RECTANGLE = { .clazz = NULL};

static void init_gdk_rectangle(JNIEnv *envir, jobject jrectangle) {
	if (GDK_RECTANGLE.clazz != NULL) {
		return;
	}
	GDK_RECTANGLE.clazz = (*envir)->GetObjectClass(envir, jrectangle);
	GDK_RECTANGLE.x = (*envir)->GetFieldID(envir, GDK_RECTANGLE.clazz, "x", "I");
	GDK_RECTANGLE.y = (*envir)->GetFieldID(envir, GDK_RECTANGLE.clazz, "y", "I");
	GDK_RECTANGLE.width = (*envir)->GetFieldID(envir, GDK_RECTANGLE.clazz, "width", "I");
	GDK_RECTANGLE.height = (*envir)->GetFieldID(envir, GDK_RECTANGLE.clazz, "height", "I");
}

static void get_gdk_rectangle(JNIEnv *envir, jobject jrectangle, GdkRectangle *rectangle) {
	init_gdk_rectangle(envir, jrectangle);
	rectangle->x = (*envir)->GetIntField(envir, jrectangle, GDK_RECTANGLE.x);
	rectangle->y = (*envir)->GetIntField(envir, jrectangle, GDK_RECTANGLE.y);
	rectangle->width = (*envir)->GetIntField(envir, jrectangle, GDK_RECTANGLE.width);
	rectangle->height = (*envir)->GetIntField(envir, jrectangle, GDK_RECTANGLE.height);
}

static void set_gdk_rectangle(JNIEnv *envir, jobject jrectangle, GdkRectangle *rectangle) {
	init_gdk_rectangle(envir, jrectangle);
	(*envir)->SetIntField(envir, jrectangle, GDK_RECTANGLE.x, (jint)rectangle->x);
	(*envir)->SetIntField(envir, jrectangle, GDK_RECTANGLE.y, (jint)rectangle->y);
	(*envir)->SetIntField(envir, jrectangle, GDK_RECTANGLE.width, (jint)rectangle->width);
	(*envir)->SetIntField(envir, jrectangle, GDK_RECTANGLE.height, (jint)rectangle->height);
}
