/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    Alexander Mitin <Alexander.Mitin@gmail.com> Gtk3 support
 *******************************************************************************/
#include "../common/wbp.h"
#include <stdlib.h>
#include <string.h>

////////////////////////////////////////////////////////////////////////////
//
// Widget bounds
//
////////////////////////////////////////////////////////////////////////////
static void getWidgetBounds(GtkWidget* widget, JNIEnv *envir, jintArray jsizes) {
	GtkAllocation a;
	// prepare buffer
	jsize sizesSize = (*envir)->GetArrayLength(envir, jsizes);
	jint *sizes = malloc(sizesSize * sizeof(jint));
	memset(&a, 0, sizeof(GtkAllocation));
	gtk_widget_get_allocation(widget, &a);
	*(sizes + 0) = a.x;
	*(sizes + 1) = a.y;
	*(sizes + 2) = a.width;
	*(sizes + 3) = a.height;
	// copy dimensions into java array
	(*envir)->SetIntArrayRegion(envir, jsizes, 0, sizesSize, sizes);
	free(sizes);
}

////////////////////////////////////////////////////////////////////////////
//
// Screenshot
//
////////////////////////////////////////////////////////////////////////////

static cairo_surface_t* copyImageSurface(GdkWindow *sourceWindow, gint width, gint height) {
	// Create the Cairo surface on which the snapshot is drawn on
	cairo_surface_t *targetSurface = cairo_image_surface_create(CAIRO_FORMAT_ARGB32, width, height);
	cairo_t *cr = cairo_create(targetSurface);
	// Get the visible region of the window
	// Wayland: Trying to take a screenshot of a partially unmapped widget
	// results in a SIGFAULT.
	cairo_region_t *visibleRegion = gdk_window_get_visible_region(sourceWindow);
	// Set the visible region as the clip for the Cairo context
	gdk_cairo_region(cr, visibleRegion);
	cairo_clip(cr);
	// Paint the surface
	gdk_cairo_set_source_window(cr, sourceWindow, 0, 0);
	cairo_set_operator(cr, CAIRO_OPERATOR_SOURCE);
	cairo_paint(cr);
	// Cleanup
	cairo_destroy(cr);
	cairo_surface_flush(targetSurface);
	cairo_region_destroy(visibleRegion);
	return targetSurface;
}

////////////////////////////////////////////////////////////////////////////
//
// JNI
//
////////////////////////////////////////////////////////////////////////////
JNIEXPORT jboolean JNICALL OS_NATIVE(_1gdk_1window_1is_1visible)
		(JNIEnv *envir, jobject that, JHANDLE windowHandle) {
	return gdk_window_is_visible((GdkWindow*)unwrap_pointer(envir, windowHandle));
}
JNIEXPORT void JNICALL OS_NATIVE(_1gdk_1window_1get_1geometry)
		(JNIEnv *envir, jobject that, JHANDLE windowHandle, jintArray x, jintArray y, jintArray width, jintArray height) {
	jint x1;
	jint y1;
	jint width1;
	jint height1;
	gdk_window_get_geometry((GdkWindow*)unwrap_pointer(envir, windowHandle), &x1, &y1, &width1, &height1);
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
JNIEXPORT JHANDLE JNICALL OS_NATIVE(_1gtk_1widget_1get_1window)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle) {
	return (JHANDLE)wrap_pointer(envir, gtk_widget_get_window((GtkWidget*)unwrap_pointer(envir, widgetHandle)));
}
JNIEXPORT JHANDLE JNICALL OS_NATIVE(_1gdk_1window_1process_1updates)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle, jboolean update_children) {
	gdk_window_process_updates((GdkWindow*)unwrap_pointer(envir, widgetHandle), update_children);
}
JNIEXPORT jboolean JNICALL OS_NATIVE(_1toggle_1above)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle, jboolean forceToggle) {
	// NOT IMPLEMENTED
	return JNI_TRUE;
}
JNIEXPORT jboolean JNICALL OS_NATIVE(_1begin_1shot)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle) {
	// just show it
	gtk_widget_show_now((GtkWidget*)unwrap_pointer(envir, widgetHandle));
	return JNI_TRUE;
}
JNIEXPORT jboolean JNICALL OS_NATIVE(_1end_1shot)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle) {
	// hide then
	gtk_widget_hide((GtkWidget*)unwrap_pointer(envir, widgetHandle));
	return JNI_TRUE;
}
JNIEXPORT JHANDLE JNICALL OS_NATIVE(_1getImageSurface)
		(JNIEnv *envir, jobject that, JHANDLE windowHandle, jint width, jint height) {
	return (JHANDLE)wrap_pointer(envir, copyImageSurface((GdkWindow*)unwrap_pointer(envir, windowHandle), width, height));
}
// tab item bounds
JNIEXPORT void JNICALL OS_NATIVE(_1getWidgetBounds)
		(JNIEnv *envir, jobject that, JHANDLE jhandle, jintArray jsizes) {
	getWidgetBounds((GtkWidget*)unwrap_pointer(envir, jhandle), envir, jsizes);
}
// other
static int isValidVersion() {
	return gtk_major_version == 3 && gtk_minor_version >= 0;
}
static jboolean isPlusMinusTreeClick(GtkTreeView *tree, gint x, gint y) {
	gint cell_x;
	gint cell_y;
	GtkTreePath *path;
	GtkTreeViewColumn *column;
	//
	if (gtk_tree_view_get_path_at_pos(tree, x, y, &path, &column, &cell_x, &cell_y)) {
		GtkTreeViewColumn *expanderColumn = gtk_tree_view_get_expander_column(tree);
		if (expanderColumn == column) {
			GdkRectangle rect;
			gtk_tree_view_get_cell_area(tree, path, column, &rect);
			if (x < rect.x) {
				return JNI_TRUE;
			}
		}
	}
	return JNI_FALSE;

}
JNIEXPORT void JNICALL OS_NATIVE(_1setAlpha)
		(JNIEnv *envir, jobject that, JHANDLE jshellHandle, jint jalpha) {
	if (isValidVersion()) {
		GtkWidget *shell = (GtkWidget*)unwrap_pointer(envir, jshellHandle);
		if (gtk_widget_is_composited(shell)) {
			int alpha = (int)jalpha;
			alpha &= 0xFF;
			gtk_widget_set_opacity(shell, alpha / 255.0);
		}
	}
}

JNIEXPORT jint JNICALL OS_NATIVE(_1getAlpha)
		(JNIEnv *envir, jobject that, JHANDLE jshellHandle) {
	if (isValidVersion()) {
		GtkWidget *shell = (GtkWidget*)unwrap_pointer(envir, jshellHandle);
		if (gtk_widget_is_composited(shell)) {
			return (jint) (gtk_widget_get_opacity(shell) * 255);
		}
	}
    return 255;
}
JNIEXPORT jboolean JNICALL OS_NATIVE(_1isPlusMinusTreeClick)
		(JNIEnv *envir, jobject that, JHANDLE jhandle, jint jx, jint jy) {
	return isPlusMinusTreeClick((GtkTreeView*)unwrap_pointer(envir, jhandle), (gint)jx, (gint)jy);
}

