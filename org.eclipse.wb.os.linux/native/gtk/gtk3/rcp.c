/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
JNIEnv *m_envir;
jobject m_callback;
jmethodID m_IScreenshotCallback_storeImage;

/* for debug purposes uncomment this and add void log(String msg) method to callback object class
jmethodID m_IScreenshotCallback_log;
static void logJava(char *str, void *p) {
	char buf[1024];
	memset(buf, 0, sizeof(buf));
	sprintf(buf, "%s = %p", str, p);
	jobject jstr = (*m_envir)->NewStringUTF(m_envir, buf);
	(*m_envir)->CallVoidMethod(m_envir, m_callback, m_IScreenshotCallback_log, jstr);
	(*m_envir)->DeleteLocalRef(m_envir, jstr);
} */

static cairo_surface_t* copyImageSurface(GdkWindow *sourceWindow, gint width, gint height) {
	cairo_surface_t *targetSurface = cairo_image_surface_create(CAIRO_FORMAT_ARGB32, width, height);
	cairo_t *cr = cairo_create(targetSurface);
	gdk_cairo_set_source_window(cr, sourceWindow, 0, 0);
	cairo_set_operator(cr, CAIRO_OPERATOR_SOURCE);
	cairo_paint(cr);
	cairo_destroy(cr);
	cairo_surface_flush(targetSurface);
	return targetSurface;
}

static cairo_surface_t* getImageSurface(GdkWindow *window) {
	if (!gdk_window_is_visible(window)) {
		// don't deal with unmapped windows
		return NULL;
	}
	gint width, height;
	gdk_window_get_geometry(window, NULL, NULL, &width, &height);
	// force paint. Note, not all widgets do this completely, known so far is GtkTreeViewer.
	GdkRectangle rect;
	rect.x = 0;	rect.y = 0;	rect.width = width;	rect.height = height;
	gdk_window_begin_paint_rect(window, &rect);
	gdk_window_invalidate_rect(window, &rect, TRUE);
	// access a widget registered with the window
	gpointer widget = NULL;
	gdk_window_get_user_data(window, &widget);
	// end force paint and copy image
	gdk_window_process_updates(window, TRUE);
	gdk_window_end_paint(window);
	cairo_surface_t *surface = copyImageSurface(window, width, height);
	// get Java code notified
	if (m_callback) {
		(*m_envir)->CallVoidMethod(m_envir, m_callback, m_IScreenshotCallback_storeImage, wrap_pointer(m_envir, widget), wrap_pointer(m_envir, surface));
	}
	// done
	return surface;
}

static cairo_surface_t* traverse(GdkWindow *window) {
	cairo_surface_t *surface = getImageSurface(window);
	if (surface == NULL) {
		return NULL;
	}
	GList *children = gdk_window_get_children(window);
	guint length = g_list_length(children);
	guint i;
    for (i = 0; i < length; i++) {
		GdkWindow *win = g_list_nth_data(children, i);
		cairo_surface_t* sur = traverse(win);
		if (sur == NULL) {
			continue;
		}
		if (!m_callback) {
			cairo_surface_destroy(sur);
		}
    }
	return surface;
}

static cairo_surface_t *makeShot(GtkWidget* shellWidget) {
	GdkWindow *window = gtk_widget_get_window(shellWidget);
	return traverse(window);
}

////////////////////////////////////////////////////////////////////////////
//
// Menu
//
////////////////////////////////////////////////////////////////////////////

// A (partial) copy of GtkMenuPrivate struct to get an access to toplevel widget.
// Careful, this struct could be changed in Gtk in future, so this must be kept 
// in sync to avoid crashing.
typedef struct _GtkMenuPrivateCopy
{
  GtkWidget *parent_menu_item;
  GtkWidget *old_active_menu_item;

  GtkAccelGroup *accel_group;
  gchar         *accel_path;

  GtkMenuPositionFunc position_func;
  gpointer            position_func_data;
  GDestroyNotify      position_func_data_destroy;
  gint                position_x;
  gint                position_y;

  guint toggle_size;
  guint accel_size;

  GtkWidget *toplevel;
} GtkMenuPrivateCopy;

static cairo_surface_t* fetchMenuVisualData(GtkMenu *menu, JNIEnv *envir, jintArray jsizes) {
	m_callback = NULL;
	GtkWidget *menuWidget = GTK_WIDGET (menu);
	GtkMenuPrivateCopy *priv = (GtkMenuPrivateCopy*)menu->priv;
	// try to move menu window outside of the screen 
	gtk_window_move((GtkWindow*)priv->toplevel, -1000, -1000);
	// display menu window
 	gtk_widget_show_now(menuWidget);
    gtk_widget_show_now(priv->toplevel);
	// get menu items sizes
	// prepare buffer
	jsize sizesSize = (*envir)->GetArrayLength(envir, jsizes);
	jint *sizes = malloc(sizesSize * sizeof(jint));
	// traverse thru children
	GList* children = gtk_container_get_children(GTK_CONTAINER(menu));
	gint count = g_list_length (children);
	if (count > 0) {
		GtkWidget *menuItem;
		gint i;
		for (i = 0; i < count; ++i) {
			GtkAllocation a;
			menuItem = GTK_WIDGET(g_list_nth_data(children, i));
			gtk_widget_get_allocation(menuItem, &a);
			*(sizes + i * 4 + 0) = a.x;
			*(sizes + i * 4 + 1) = a.y;
			*(sizes + i * 4 + 2) = a.width;
			*(sizes + i * 4 + 3) = a.height;
		}
	}
	g_list_free(children);
	// copy dimensions into java array
	(*envir)->SetIntArrayRegion(envir, jsizes, 0, sizesSize, sizes);
	free(sizes);
	// make screenshot
	cairo_surface_t *surface = NULL;
	GdkWindow *window = gtk_widget_get_window(menuWidget);
	surface = traverse(window);
	// hide menu 
	gtk_widget_hide(priv->toplevel);
    gtk_widget_hide(GTK_WIDGET (menu));
	// all done
	return surface;
}

////////////////////////////////////////////////////////////////////////////
//
// JNI
//
////////////////////////////////////////////////////////////////////////////
JNIEXPORT jboolean JNICALL 
	OS_NATIVE(_1toggle_1above)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle, jboolean forceToggle) {
	// NOT IMPLEMENTED
	return JNI_TRUE;
}
JNIEXPORT jboolean JNICALL 
	OS_NATIVE(_1begin_1shot)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle) {
	// just show it
	gtk_widget_show_now((GtkWidget*)unwrap_pointer(envir, widgetHandle));
	return JNI_TRUE;
}
JNIEXPORT jboolean JNICALL 
	OS_NATIVE(_1end_1shot)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle) {
	// hide then
	gtk_widget_hide((GtkWidget*)unwrap_pointer(envir, widgetHandle));
	return JNI_TRUE;
}
// shot
JNIEXPORT JHANDLE JNICALL OS_NATIVE(_1makeShot)(
			JNIEnv *envir, jobject that, JHANDLE widgetHandle, jobject callback) {
	m_envir = envir;
	if (callback != NULL) {
		m_callback = (*envir)->NewGlobalRef(envir, callback);
		jclass clazz = (*envir)->GetObjectClass(envir, m_callback);
		m_IScreenshotCallback_storeImage = (*envir)->GetMethodID(envir, clazz, "storeImage", CALLBACK_SIG);
		/* uncomment this for debug purposes
		m_IScreenshotCallback_log = (*envir)->GetMethodID(envir, clazz, "log", "(Ljava/lang/String;)V"); */
	}
	// make shot
	cairo_surface_t* surface = makeShot((GtkWidget*)unwrap_pointer(envir, widgetHandle));
	// clean up
	if (callback != NULL) {
		(*envir)->DeleteGlobalRef(envir, m_callback);
	}
	m_callback = NULL;
	return (JHANDLE)wrap_pointer(envir, surface);
}
// menu
JNIEXPORT JHANDLE JNICALL OS_NATIVE(_1fetchMenuVisualData)(
			JNIEnv *envir, jobject that, JHANDLE jmenuHandle, jintArray jsizes) {
	GtkWidget* menu = (GtkWidget*)unwrap_pointer(envir, jmenuHandle);
	cairo_surface_t* surface = fetchMenuVisualData(GTK_MENU(menu), envir, jsizes);
	return wrap_pointer(envir, surface);
}
// tab item bounds
JNIEXPORT void JNICALL OS_NATIVE(_1getWidgetBounds)(
			JNIEnv *envir, jobject that, JHANDLE jhandle, jintArray jsizes) {
	getWidgetBounds((GtkWidget*)unwrap_pointer(envir, jhandle), envir, jsizes);
}
// unref
JNIEXPORT void JNICALL OS_NATIVE(_1disposeImageHandle)(
			JNIEnv *envir, jobject that, JHANDLE jhandle) {
	cairo_surface_destroy((cairo_surface_t*)unwrap_pointer(envir, jhandle));
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
JNIEXPORT void JNICALL OS_NATIVE(_1setAlpha)(
			JNIEnv *envir, jobject that, JHANDLE jshellHandle, jint jalpha) {
	if (isValidVersion()) {
		GtkWidget *shell = (GtkWidget*)unwrap_pointer(envir, jshellHandle);
		if (gtk_widget_is_composited(shell)) {
			int alpha = (int)jalpha;
			alpha &= 0xFF;
			gtk_widget_set_opacity(shell, alpha / 255.0);
		}
	}
}

JNIEXPORT jint JNICALL OS_NATIVE(_1getAlpha)(
			JNIEnv *envir, jobject that, JHANDLE jshellHandle) {
	if (isValidVersion()) {
		GtkWidget *shell = (GtkWidget*)unwrap_pointer(envir, jshellHandle);
		if (gtk_widget_is_composited(shell)) {
			return (jint) (gtk_widget_get_opacity(shell) * 255);
		}
	}
    return 255;
}
JNIEXPORT jboolean JNICALL OS_NATIVE(_1isPlusMinusTreeClick)(
			JNIEnv *envir, jobject that, JHANDLE jhandle, jint jx, jint jy) {
	return isPlusMinusTreeClick((GtkTreeView*)unwrap_pointer(envir, jhandle), (gint)jx, (gint)jy);
}

