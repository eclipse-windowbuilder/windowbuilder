/*******************************************************************************
 * Copyright (c) 2024 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 ******************************************************************************/
#include "wbp.h"
#include <jni.h>

////////////////////////////////////////////////////////////////////////////
//
// JNI
//
////////////////////////////////////////////////////////////////////////////

JNIEXPORT jboolean JNICALL OS_NATIVE(_1gdk_1window_1is_1visible)
		(JNIEnv *envir, jobject that, JHANDLE windowHandle) {
	return gdk_window_is_visible((GdkWindow*)(CHANDLE) windowHandle);
}

JNIEXPORT void JNICALL OS_NATIVE(_1gdk_1window_1get_1geometry)
		(JNIEnv *envir, jobject that, JHANDLE windowHandle, jintArray x, jintArray y, jintArray width, jintArray height) {
	jint x1;
	jint y1;
	jint width1;
	jint height1;
	gdk_window_get_geometry((GdkWindow*)(CHANDLE) windowHandle, &x1, &y1, &width1, &height1);
	if (x != NULL) {
		(*envir) -> SetIntArrayRegion(envir, x, 0, 1, &x1);
	}
	if (y != NULL) {
		(*envir) -> SetIntArrayRegion(envir, y, 0, 1, &y1);
	}
	if (width != NULL) {
		(*envir) -> SetIntArrayRegion(envir, width, 0, 1, &width1);
	}
	if (height != NULL) {
		(*envir) -> SetIntArrayRegion(envir, height, 0, 1, &height1);
	}
}

JNIEXPORT void JNICALL OS_NATIVE(_1gdk_1window_1process_1updates)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle, jboolean update_children) {
	gdk_window_process_updates((GdkWindow*)(CHANDLE) widgetHandle, (gboolean) update_children);
}

JNIEXPORT JHANDLE JNICALL OS_NATIVE(_1gdk_1window_1get_1visible_1region)
		(JNIEnv *envir, jobject that, JHANDLE window) {
	return (JHANDLE) gdk_window_get_visible_region((GdkWindow *)(CHANDLE) window);
}

JNIEXPORT void JNICALL OS_NATIVE(_1gdk_1cairo_1region)
		(JNIEnv *envir, jobject that, JHANDLE cr, JHANDLE region) {
	gdk_cairo_region((cairo_t *)(CHANDLE) cr, (cairo_region_t *)(CHANDLE) region);
}

JNIEXPORT void JNICALL OS_NATIVE(_1gdk_1cairo_1set_1source_1window)
		(JNIEnv *envir, jobject that, JHANDLE cr, JHANDLE window, jdouble x, jdouble y) {
	gdk_cairo_set_source_window((cairo_t *)(CHANDLE) cr, (GdkWindow *)(CHANDLE) window, (gdouble)x, (gdouble)y);
}