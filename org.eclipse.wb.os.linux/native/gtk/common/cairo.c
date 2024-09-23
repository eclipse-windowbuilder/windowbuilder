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

JNIEXPORT jint JNICALL OS_NATIVE(_1CAIRO_1FORMAT_1ARGB32)
		(JNIEnv *envir, jobject that) {
	return (jint) CAIRO_FORMAT_ARGB32;
}

JNIEXPORT jint JNICALL OS_NATIVE(_1CAIRO_1OPERATOR_1SOURCE)
		(JNIEnv *envir, jobject that) {
	return (jint) CAIRO_OPERATOR_SOURCE;
}

JNIEXPORT JHANDLE JNICALL OS_NATIVE(_1cairo_1create)
		(JNIEnv *envir, jobject that, JHANDLE target) {
	return (JHANDLE) cairo_create((cairo_surface_t *)(CHANDLE) target);
}

JNIEXPORT JHANDLE JNICALL OS_NATIVE(_1cairo_1image_1surface_1create)
		(JNIEnv *envir, jobject that, jint format, jint width, jint height) {
	return (JHANDLE) cairo_image_surface_create((cairo_format_t)format, (int) width, (int) height);
}

JNIEXPORT void JNICALL OS_NATIVE(_1cairo_1clip)
		(JNIEnv *envir, jobject that, JHANDLE cr) {
	cairo_clip((cairo_t *)(CHANDLE) cr);
}

JNIEXPORT void JNICALL OS_NATIVE(_1cairo_1paint)
		(JNIEnv *envir, jobject that, JHANDLE cr) {
	cairo_paint((cairo_t *)(CHANDLE) cr);
}

JNIEXPORT void JNICALL OS_NATIVE(_1cairo_1set_1operator)
		(JNIEnv *envir, jobject that, JHANDLE cr, jint op) {
	cairo_set_operator((cairo_t *)(CHANDLE) cr, (cairo_operator_t) op);
}

JNIEXPORT void JNICALL OS_NATIVE(_1cairo_1destroy)
		(JNIEnv *envir, jobject that, JHANDLE cr) {
	cairo_destroy((cairo_t *)(CHANDLE) cr);
}

JNIEXPORT void JNICALL OS_NATIVE(_1cairo_1surface_1flush)
		(JNIEnv *envir, jobject that, JHANDLE surface) {
	cairo_surface_flush((cairo_surface_t *)(CHANDLE) surface);
}

JNIEXPORT void JNICALL OS_NATIVE(_1cairo_1region_1destroy)
		(JNIEnv *envir, jobject that, JHANDLE region) {
	cairo_region_destroy((cairo_region_t *)(CHANDLE) region);
}