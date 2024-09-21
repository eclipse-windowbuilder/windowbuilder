/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
#include <jni.h>

////////////////////////////////////////////////////////////////////////////
//
// GtkAllocation
//
////////////////////////////////////////////////////////////////////////////

typedef struct JGtkAllocation {
	jclass clazz;
	jfieldID x;
	jfieldID y;
	jfieldID width;
	jfieldID height;
} JGtkAllocation;
JGtkAllocation GTK_ALLOCATION = { .clazz = NULL};

static void init_gtk_allocation(JNIEnv *envir, jobject jallocation) {
	if (GTK_ALLOCATION.clazz != NULL) {
		return;
	}
	GTK_ALLOCATION.clazz = (*envir)->GetObjectClass(envir, jallocation);
	GTK_ALLOCATION.x = (*envir)->GetFieldID(envir, GTK_ALLOCATION.clazz, "x", "I");
	GTK_ALLOCATION.y = (*envir)->GetFieldID(envir, GTK_ALLOCATION.clazz, "y", "I");
	GTK_ALLOCATION.width = (*envir)->GetFieldID(envir, GTK_ALLOCATION.clazz, "width", "I");
	GTK_ALLOCATION.height = (*envir)->GetFieldID(envir, GTK_ALLOCATION.clazz, "height", "I");
}

static void get_gtk_allocation(JNIEnv *envir, jobject jallocation, GtkAllocation *allocation) {
	init_gtk_allocation(envir, jallocation);
	allocation->x = (*envir)->GetIntField(envir, jallocation, GTK_ALLOCATION.x);
	allocation->y = (*envir)->GetIntField(envir, jallocation, GTK_ALLOCATION.y);
	allocation->width = (*envir)->GetIntField(envir, jallocation, GTK_ALLOCATION.width);
	allocation->height = (*envir)->GetIntField(envir, jallocation, GTK_ALLOCATION.height);
}

static void set_gtk_allocation(JNIEnv *envir, jobject jallocation, GtkAllocation *allocation) {
	init_gtk_allocation(envir, jallocation);
	(*envir)->SetIntField(envir, jallocation, GTK_ALLOCATION.x, (jint)allocation->x);
	(*envir)->SetIntField(envir, jallocation, GTK_ALLOCATION.y, (jint)allocation->y);
	(*envir)->SetIntField(envir, jallocation, GTK_ALLOCATION.width, (jint)allocation->width);
	(*envir)->SetIntField(envir, jallocation, GTK_ALLOCATION.height, (jint)allocation->height);
}

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
JGdkRectangle GDK_RECTANGLE = { .clazz = NULL};

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
JNIEXPORT JHANDLE JNICALL OS_NATIVE(_1gtk_1widget_1get_1window)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle) {
	return (JHANDLE) gtk_widget_get_window((GtkWidget*)(CHANDLE) widgetHandle);
}
JNIEXPORT jboolean JNICALL OS_NATIVE(_1gtk_1widget_1is_1composited)
		(JNIEnv *envir, jobject that, JHANDLE jhandle) {
	return gtk_widget_is_composited((GtkWidget*)(CHANDLE) jhandle);
}
JNIEXPORT jdouble JNICALL OS_NATIVE(_1gtk_1widget_1get_1opacity)
		(JNIEnv *envir, jobject that, JHANDLE jhandle) {
	return (jdouble) (gtk_widget_get_opacity((GtkWidget*)(CHANDLE) jhandle));
}
JNIEXPORT void JNICALL OS_NATIVE(_1gtk_1widget_1set_1opacity)
		(JNIEnv *envir, jobject that, JHANDLE jhandle, jdouble jalpha) {
	gtk_widget_set_opacity((GtkWidget*)(CHANDLE) jhandle, (double)jalpha);
}
JNIEXPORT void JNICALL OS_NATIVE(_1gdk_1window_1process_1updates)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle, jboolean update_children) {
	gdk_window_process_updates((GdkWindow*)(CHANDLE) widgetHandle, update_children);
}
JNIEXPORT jboolean JNICALL OS_NATIVE(_1toggle_1above)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle, jboolean forceToggle) {
	// NOT IMPLEMENTED
	return JNI_TRUE;
}
JNIEXPORT void JNICALL OS_NATIVE(_1gtk_1widget_1show_1now)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle) {
	// just show it
	gtk_widget_show_now((GtkWidget*)(CHANDLE) widgetHandle);
}
JNIEXPORT void JNICALL OS_NATIVE(_1gtk_1widget_1hide)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle) {
	// hide then
	gtk_widget_hide((GtkWidget*)(CHANDLE) widgetHandle);
}
JNIEXPORT JHANDLE JNICALL OS_NATIVE(_1getImageSurface)
		(JNIEnv *envir, jobject that, JHANDLE windowHandle, jint width, jint height) {
	return (JHANDLE) copyImageSurface((GdkWindow*)(CHANDLE) windowHandle, width, height);
}
// tree items
JNIEXPORT JHANDLE JNICALL OS_NATIVE(_1gtk_1tree_1view_1get_1expander_1column)
		(JNIEnv *envir, jobject that, JHANDLE tree_view) {
	return (JHANDLE) gtk_tree_view_get_expander_column((GtkTreeView*)(CHANDLE) tree_view);
}
JNIEXPORT void JNICALL OS_NATIVE(_1gtk_1tree_1view_1get_1cell_1area)
		(JNIEnv *envir, jobject that, JHANDLE tree_view, JHANDLE path, JHANDLE column, jobject jrect) {
	GdkRectangle rect;
	gtk_tree_view_get_cell_area((GtkTreeView*)(CHANDLE) tree_view, (GtkTreePath*)(CHANDLE) path, (GtkTreeViewColumn*)(CHANDLE) column, &rect);
	set_gdk_rectangle(envir, jrect, &rect);
}
JNIEXPORT jboolean JNICALL OS_NATIVE(_1gtk_1tree_1view_1get_1path_1at_1pos)
		(JNIEnv *envir, jobject that, JHANDLE tree_view, jint x, jint y, JHANDLEARRAY path, JHANDLEARRAY column, jintArray cell_x, jintArray cell_y) {
	jint cell_x1;
	jint cell_y1;
	JHANDLE path1;
	JHANDLE column1;

	jboolean result = gtk_tree_view_get_path_at_pos((GtkTreeView*)(CHANDLE) tree_view, x, y, (GtkTreePath**) &path1, (GtkTreeViewColumn**) &column1, &cell_x1, &cell_y1);
	
	if (path != NULL) {
		(*envir) -> SetLongArrayRegion(envir, path, 0, 1, &path1);
	}
	if (column != NULL) {
		(*envir) -> SetLongArrayRegion(envir, column, 0, 1, &column1);
	}
	if (cell_x != NULL) {
		(*envir) -> SetIntArrayRegion(envir, cell_x, 0, 1, &cell_x1);
	}
	if (cell_x != NULL) {
		(*envir) -> SetIntArrayRegion(envir, cell_y, 0, 1, &cell_y1);
	}

	return result;
}
JNIEXPORT void JNICALL OS_NATIVE(_1gtk_1tree_1path_1free)
		(JNIEnv *envir, jobject that, JHANDLE path) {
	gtk_tree_path_free((GtkTreePath*)(CHANDLE) path);
}
// tab item bounds
JNIEXPORT void JNICALL OS_NATIVE(_1gtk_1widget_1get_1allocation)
		(JNIEnv *envir, jobject that, JHANDLE jhandle, jobject jallocation) {
	GtkAllocation allocation;
	gtk_widget_get_allocation((GtkWidget*)(CHANDLE) jhandle, &allocation);
	set_gtk_allocation(envir, jallocation, &allocation);
}
