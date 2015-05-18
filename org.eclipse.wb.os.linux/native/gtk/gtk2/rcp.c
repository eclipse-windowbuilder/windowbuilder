/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
#include "../common/wbp.h"
#include <stdlib.h>
#include <string.h>

#include <X11/Xatom.h>

#define _NET_WM_STATE_TOGGLE        2    /* toggle property  */
#define MAX_PROPERTY_VALUE_LEN 4096

////////////////////////////////////////////////////////////////////////////
//
// Toggle "above" state for Eclipse, move preview window to another workspace
//
////////////////////////////////////////////////////////////////////////////
int m_currentDesktop = 0;
int m_desktopCount = 0;

// gets the property of the X Window
static gchar *get_property (Display *disp, Window win, Atom xa_prop_type, gchar *prop_name, unsigned long *size) {
    Atom xa_prop_name;
    Atom xa_ret_type;
    int ret_format;
    unsigned long ret_nitems;
    unsigned long ret_bytes_after;
    unsigned long tmp_size;
    unsigned char *ret_prop;
    gchar *ret;
    
    xa_prop_name = XInternAtom(disp, prop_name, False);
    
    /* MAX_PROPERTY_VALUE_LEN / 4 explanation (XGetWindowProperty manpage):
     *
     * long_length = Specifies the length in 32-bit multiples of the
     *               data to be retrieved.
     */
    if (XGetWindowProperty(disp, win, xa_prop_name, 0, MAX_PROPERTY_VALUE_LEN / 4, False,
            xa_prop_type, &xa_ret_type, &ret_format,     
            &ret_nitems, &ret_bytes_after, &ret_prop) != Success) {
        return NULL;
    }
  
    if (xa_ret_type != xa_prop_type) {
        XFree(ret_prop);
        return NULL;
    }

    /* null terminate the result to make string handling easier */
    tmp_size = (ret_format / 8) * ret_nitems;
    ret = g_malloc(tmp_size + 1);
    memcpy(ret, ret_prop, tmp_size);
    ret[tmp_size] = '\0';

    if (size) {
        *size = tmp_size;
    }
    
    XFree(ret_prop);
    return ret;
}
// sends the client message to the X Window
static int client_msg(Display *disp, Window win, char *msg,
        unsigned long data0, unsigned long data1, 
        unsigned long data2, unsigned long data3,
        unsigned long data4) {
    XEvent event;
    long mask = SubstructureRedirectMask | SubstructureNotifyMask;

    event.xclient.type = ClientMessage;
    event.xclient.serial = 0;
    event.xclient.send_event = True;
    event.xclient.message_type = XInternAtom(disp, msg, False);
    event.xclient.window = win;
    event.xclient.format = 32;
    event.xclient.data.l[0] = data0;
    event.xclient.data.l[1] = data1;
    event.xclient.data.l[2] = data2;
    event.xclient.data.l[3] = data3;
    event.xclient.data.l[4] = data4;
    
    if (XSendEvent(disp, DefaultRootWindow(disp), False, mask, &event)) {
        return EXIT_SUCCESS;
    } else {
        fprintf(stderr, "Cannot send %s event.\n", msg);fflush(stderr);
        return EXIT_FAILURE;
    }
}
// toggles the "above" state of the X Window
static int toggle_window_state_above(Display *disp, Window win) {
    unsigned long action = _NET_WM_STATE_TOGGLE;
    Atom prop1 = XInternAtom(disp, "_NET_WM_STATE_ABOVE", False);
	int result = client_msg(disp, win, "_NET_WM_STATE", action, (unsigned long)prop1, 0, 0, 0);
	return result;
}
// determines is the X Window has "above" state
static Bool isWindowAbove (Display *disp, Window win) {
    Atom	  actual;
    int		  format;
    unsigned long n, left;
    unsigned char *data;

    int result = XGetWindowProperty(disp, win, XInternAtom(disp, "_NET_WM_STATE", False),
				 0L, 1024L, FALSE, XA_ATOM, &actual, &format, &n, &left, &data);

    if (result == Success && n && data) {
		Atom *a = (Atom *) data;
		while (n--) {
			if (XInternAtom (disp, "_NET_WM_STATE_ABOVE", 0) == *a++) {
				XFree ((void *) data);
				return True;
			}
		}
		XFree ((void *) data);
    }

    return False;
}

static Bool isOverrideRedirect(Display* disp, Window win) {
	XWindowAttributes attrs;
	XGetWindowAttributes(disp, win, &attrs);
	return attrs.override_redirect;
}
static void gtk_widget_show_map_callback (GtkWidget *widget, GdkEvent *event, gint *flag) {
	*flag = TRUE;
	g_signal_handlers_disconnect_by_func (widget,gtk_widget_show_map_callback, flag);
}

// moves the preview window onto another workspace (desktop).
static int moveWindowToDesktop(GtkWidget *shellWidget) {
	if (GTK_WIDGET_VISIBLE(shellWidget)) {
		// can't do anything, because it's already visible
		return JNI_FALSE;
	}
	// get underlying X resources
	GdkWindow *window = shellWidget->window;
	Window x11window = GDK_WINDOW_XID(window);
	Display* disp = GDK_DRAWABLE_XDISPLAY(window);
	// remove "override_redirect" flag
	// https://fogbugz.instantiations.com:443/default.php?41586
	Bool wasOverride = isOverrideRedirect(disp, x11window);
	if (wasOverride) {
		gdk_window_set_override_redirect(window, FALSE);
	}
	// disable showing preview window by any helpers
	gtk_window_set_focus_on_map((GtkWindow*)shellWidget, FALSE);
	gtk_window_set_skip_taskbar_hint((GtkWindow*)shellWidget, TRUE);
	gtk_window_set_skip_pager_hint((GtkWindow*)shellWidget, TRUE);
	// get current desktop/desktop count
    unsigned long *desktopCountPtr = NULL;
    unsigned long *currentDesktopPtr = NULL;
    Window root = DefaultRootWindow(disp);
	if (!(desktopCountPtr = (unsigned long *)get_property(disp, root,
		XA_CARDINAL, "_NET_NUMBER_OF_DESKTOPS", NULL))) {
		if (!(desktopCountPtr = (unsigned long *)get_property(disp, root,
			XA_CARDINAL, "_WIN_WORKSPACE_COUNT", NULL))) {
			return JNI_FALSE;
		}
	}
    if (!(currentDesktopPtr = (unsigned long *)get_property(disp, root,
            XA_CARDINAL, "_NET_CURRENT_DESKTOP", NULL))) {
        if (!(currentDesktopPtr = (unsigned long *)get_property(disp, root,
                XA_CARDINAL, "_WIN_WORKSPACE", NULL))) {
			return JNI_FALSE;        
		}
    }
	m_currentDesktop = *currentDesktopPtr;
	m_desktopCount = *desktopCountPtr;
	g_free(currentDesktopPtr);
	g_free(desktopCountPtr);
	// determine the desktop number on which the preview window would be moved
	int desktop;
	int desktopCountActual = m_desktopCount;
	if (m_desktopCount == 1 ) { 
		// create new desktop if the only one exists, would be removed later
	    if (!client_msg(disp, DefaultRootWindow(disp), "_NET_NUMBER_OF_DESKTOPS", 2, 0, 0, 0, 0)) {
			// success
			desktopCountActual++;
			XSync(disp, False);
		}
	}
	if (m_currentDesktop == desktopCountActual - 1) {
		desktop = desktopCountActual - 2;
	} else {
		desktop = m_currentDesktop + 1;
	}
	// show widget required, only mapped windows can be moved to another desktop.
	// this could cause flickering in rare cases (a few events in queue?)
	gint flag = FALSE;
	gtk_widget_show(shellWidget);
	// move window to another desktop
	client_msg(disp, x11window, "_NET_WM_DESKTOP", (unsigned long)desktop, 0, 0, 0, 0);
	// wait for window to be completely shown: wait for ConfigureNotify which has no above window.
    g_signal_connect (shellWidget, "configure-event", G_CALLBACK (gtk_widget_show_map_callback),&flag);
    while (!flag) {
		gtk_main_iteration();
	}
	if (wasOverride) {
		gdk_window_set_override_redirect(window, TRUE);
	}
	// success
	return JNI_TRUE;
}

static int restoreWindow(GtkWidget *shellWidget) {
	if (m_desktopCount == 0) {
		// nothing to do?
		return JNI_FALSE;
	}
	// get underlying X resources
	GdkWindow *window = shellWidget->window;
	Display* disp = GDK_DRAWABLE_XDISPLAY(window);
	// remove the extra desktop maybe created above
	if (m_desktopCount == 1) { 
	    client_msg(disp, DefaultRootWindow(disp), "_NET_NUMBER_OF_DESKTOPS", 1, 0, 0, 0, 0);
	}
	// cleanup
	m_currentDesktop = 0;
	m_desktopCount = 0;
	// success
	return JNI_TRUE;
}

////////////////////////////////////////////////////////////////////////////
//
// Shell screen shot
//
////////////////////////////////////////////////////////////////////////////
static GdkPixmap* copyPixmap(GdkPixmap *source, gint width, gint height) {
	if (source) {	
		GdkPixmap* pixmap = gdk_pixmap_new(source, width, height, -1);
		GdkGC *gc = gdk_gc_new(source);
		gdk_draw_drawable(pixmap, gc, source, 0, 0, 0, 0, -1, -1);
		g_object_unref(gc);
		g_object_unref(source);
		return pixmap;
	}
	return NULL;
}
/*
not used because produces screenshot with errors, but may be useful in future when gtk fixes their bugs.
static GdkPixmap* makeShot2(GtkWidget *widget) {
	GdkPixmap* source = gtk_widget_get_snapshot(widget, NULL);
	if (source == NULL) {
		return NULL;	
	}
	// determine snapshot rectangle
	int x = widget->allocation.x;
	int y = widget->allocation.y;
	int width = widget->allocation.width;
	int height = widget->allocation.height;
	// grow snapshot rectangle to cover all widget windows
	if (widget->parent && !GTK_WIDGET_NO_WINDOW (widget)){
		GdkWindow *parent_window = gtk_widget_get_parent_window (widget);
		GList *windows = NULL, *list;
		for (list = gdk_window_peek_children (parent_window); list; list = list->next) {
			GdkWindow *subwin = list->data;
			gpointer windata;
			int wx, wy, ww, wh;
			gdk_window_get_user_data (subwin, &windata);
			if (windata != widget) {
				continue;
			}
			windows = g_list_prepend (windows, subwin);
			gdk_window_get_position (subwin, &wx, &wy);
			gdk_drawable_get_size (subwin, &ww, &wh);
			// grow snapshot rectangle by extra widget sub window
			if (wx < x) {
				width += x - wx;
				x = wx;
			}
			if (wy < y) {
				height += y - wy;
				y = wy;
			}
			if (x + width < wx + ww) {
				width += wx + ww - (x + width);
			}
			if (y + height < wy + wh) {
				height += wy + wh - (y + height);
			}
		}
	} else if (!widget->parent) {
    	x = y = 0; // toplevel
	}
	// return it copied to avoid incompatibility with SWT.
	return copyPixmap(source, width, height);
}*/

JNIEnv *m_envir;
jobject m_callback;
jmethodID m_IScreenshotCallback_storeImage;
//
typedef struct _GdkWindowPaint GdkWindowPaint;
struct _GdkWindowPaint {
	GdkRegion *region;
	GdkPixmap *pixmap;
	gint x_offset;
	gint y_offset;
};
static void exposeAllWidgetsCallback(GtkWidget *widget, gpointer data);
//
#define PREPARE_EVENT \
		GdkEventExpose ev;\
		ev.type = GDK_EXPOSE;\
		ev.send_event = TRUE;\
		ev.area.x = 0;\
		ev.area.y = 0;\
		ev.count = 0;

#define UPDATE_EVENT \
		gdk_window_get_geometry(ev.window, NULL, NULL, &ev.area.width, &ev.area.height, NULL);\
		ev.region = gdk_region_rectangle(&ev.area);

static void exposeWidget(GtkWidget *widget) {
	GdkWindow *window = widget->window;
	if (!GTK_WIDGET_REALIZED(widget)) {
		return;
	}
// g_warning ("type = %s", G_OBJECT_TYPE_NAME (widget));
	if (GTK_IS_SPIN_BUTTON(widget)) {
		// spin button
		GtkWidgetClass *clazz = (GtkWidgetClass *)GTK_SPIN_BUTTON_GET_CLASS(widget);
		GtkSpinButton *spin = GTK_SPIN_BUTTON (widget);
		{
			PREPARE_EVENT
			ev.window = spin->panel;
			UPDATE_EVENT
			clazz->expose_event(widget, &ev);
		}
		// spin button also contains GtkEntry, so give a chance to expose it too, so no 'else' statement
	}
	if (GTK_IS_ENTRY(widget)) {
		// single text
		GtkWidgetClass *clazz = (GtkWidgetClass *)GTK_ENTRY_GET_CLASS(widget);
		{
			PREPARE_EVENT
			ev.window = window;
			UPDATE_EVENT
			clazz->expose_event(widget, &ev);
		}
		//
		{
			PREPARE_EVENT
			ev.window = ((GtkEntry*)widget)->text_area;
			UPDATE_EVENT
			clazz->expose_event(widget, &ev);
		}			
	} else if (GTK_IS_TEXT_VIEW(widget)) {
		// multi-line text
		{
			PREPARE_EVENT
			ev.window = gtk_text_view_get_window((GtkTextView *)widget, GTK_TEXT_WINDOW_TEXT);
			UPDATE_EVENT
			gtk_widget_send_expose(widget, (GdkEvent*)&ev);
		}			
	} else if (GTK_IS_TREE_VIEW(widget)) {
		// tree
		GtkWidgetClass *clazz = (GtkWidgetClass *)GTK_TREE_VIEW_GET_CLASS(widget);
		GtkTreeView *tree_view = GTK_TREE_VIEW (widget);
		{
			PREPARE_EVENT
			ev.window = gtk_tree_view_get_bin_window(tree_view);
			UPDATE_EVENT
			clazz->expose_event(widget, &ev);
		}
	} else {
		// everything else
		{
			PREPARE_EVENT
			ev.window = window;
			UPDATE_EVENT
			gtk_widget_send_expose(widget, (GdkEvent*)&ev);
		}			
	}
}

static void exposeAllWidgets(GtkWidget *widget) {
	if (!GTK_IS_WIDGET(widget)) {
		return;
	}
	exposeWidget(widget);
	if (!GTK_IS_CONTAINER(widget)) {
		return;
	}
	GtkContainer *container = GTK_CONTAINER(widget);
	gtk_container_forall(container, exposeAllWidgetsCallback, 0);
}

static GdkPixmap* getPixmap(GdkWindow *window, int shouldCallback) {
	if (!gdk_window_is_visible(window)) {
		// don't deal with unmapped windows
		return NULL;
	}
	gint width, height;
	gdk_window_get_geometry(window, NULL, NULL, &width, &height, NULL);
	//
	GdkRectangle rect;
	rect.x = 0;	rect.y = 0;	rect.width = width;	rect.height = height;
	//
	GdkRegion *region = gdk_region_rectangle(&rect);
	gdk_window_begin_paint_region(window, region);
	//
	region = gdk_region_rectangle(&rect);
	gdk_window_invalidate_region(window, region, TRUE);
	//
	gpointer widget = NULL;
	gdk_window_get_user_data(window, &widget);
	if (widget != NULL) {
		exposeAllWidgets((GtkWidget*)widget);
	}
	//
	gdk_window_process_updates(window, TRUE);
	//
	GdkWindowObject *private = (GdkWindowObject *)(window);
	GdkPixmap *internalPixmap = ((GdkWindowPaint *)private->paint_stack->data)->pixmap;
	if (internalPixmap == NULL) {
		return NULL;
	}
	//
	g_object_ref(internalPixmap);
	GdkPixmap *pixmap = copyPixmap(internalPixmap, width, height);
	gdk_window_end_paint(window);
	//
	if (shouldCallback) {
		(*m_envir)->CallVoidMethod(m_envir, m_callback, m_IScreenshotCallback_storeImage, wrap_pointer(m_envir, widget), wrap_pointer(m_envir, pixmap));
	}
	//
	return pixmap;
}

static GdkPixmap* traverse(GdkWindow *window, int shouldCallback){
	gint depth;
	gdk_window_get_geometry(window, NULL, NULL, NULL, NULL, &depth);
	// strange window
	if (depth == 0) {
		return NULL;
	}
	//
	GdkPixmap *pixmap = getPixmap(window, shouldCallback);
	if (pixmap == NULL) {
		return NULL;
	}
	//
	GdkGC *gc = gdk_gc_new(pixmap);
	GList *children = gdk_window_get_children(window);
	guint length = g_list_length(children);
	//
	guint i;
    for (i = 0; i < length; i++) {
		GdkWindow *win = g_list_nth_data(children, i);
		GdkPixmap* pix = traverse(win, shouldCallback);
		if (pix == NULL) {
			continue;
		}
		gint x, y, width, height;
		gdk_window_get_geometry(win, &x, &y, &width, &height, NULL);
		gdk_draw_drawable(pixmap, gc, pix, 0, 0, x, y, width, height);
		if (!shouldCallback) {
			g_object_unref(pix);
		}
    }
 	g_object_unref(gc);
	return pixmap;
}

static void exposeAllWidgetsCallback(GtkWidget *widget, gpointer data) {
	exposeAllWidgets(widget);
}
static GdkPixmap* makeShot(GtkWidget* shellWidget) {
	GdkWindow *window = shellWidget->window;
	return traverse(window, m_callback != NULL);
}
////////////////////////////////////////////////////////////////////////////
//
// Widget bounds
//
////////////////////////////////////////////////////////////////////////////
static void getWidgetBounds(GtkWidget* widget, JNIEnv *envir, jintArray jsizes) {
	// prepare buffer
	jsize sizesSize = (*envir)->GetArrayLength(envir, jsizes);
	jint *sizes = malloc(sizesSize * sizeof(jint));
	*(sizes + 0) = GTK_WIDGET_X(widget);
	*(sizes + 1) = GTK_WIDGET_Y(widget);
	*(sizes + 2) = GTK_WIDGET_WIDTH(widget);
	*(sizes + 3) = GTK_WIDGET_HEIGHT(widget);
	// copy dimensions into java array
	(*envir)->SetIntArrayRegion(envir, jsizes, 0, sizesSize, sizes);
	free(sizes);
}
////////////////////////////////////////////////////////////////////////////
//
// Menu
//
////////////////////////////////////////////////////////////////////////////
static GdkPixmap* fetchMenuVisualData(GtkMenu *menu, JNIEnv *envir, jintArray jsizes) {
	GtkWidget *menuWidget = GTK_WIDGET (menu);
	// try to move menu window outside ot the screen 
	gtk_window_move ((GtkWindow*)menu->toplevel, -100, -100);
	// display menu window
 	gtk_widget_show (menuWidget);
    gtk_widget_show (menu->toplevel);
	// get menu items sizes
	// prepare buffer
	jsize sizesSize = (*envir)->GetArrayLength(envir, jsizes);
	jint *sizes = malloc(sizesSize * sizeof(jint));
	// get border thickness
	gint border_x = GTK_CONTAINER (menuWidget)->border_width + menuWidget->style->xthickness;
	gint border_y = GTK_CONTAINER (menuWidget)->border_width + menuWidget->style->ythickness;
	// traverse thru children
	GList* children = gtk_container_get_children(GTK_CONTAINER(menu));
	gint count = g_list_length (children);
	if (count > 0) {
		GtkWidget *menuItem;
		gint i;
		for (i = 0; i < count; ++i) {
			menuItem = GTK_WIDGET(g_list_nth_data(children, i));
			*(sizes + i * 4 + 0) = menuItem->allocation.x + border_x;
			*(sizes + i * 4 + 1) = menuItem->allocation.y + border_y;
			*(sizes + i * 4 + 2) = menuItem->allocation.width;
			*(sizes + i * 4 + 3) = menuItem->allocation.height;
		}
	}
	g_list_free(children);
	// copy dimensions into java array
	(*envir)->SetIntArrayRegion(envir, jsizes, 0, sizesSize, sizes);
	free(sizes);
	// make screenshot
	GdkPixmap *pixmap = NULL;
	GdkWindow *window = menu->toplevel->window;
	pixmap = traverse(window, 0);
	// hide menu 
	gtk_widget_hide (menu->toplevel);
    gtk_widget_hide (GTK_WIDGET (menu));
	// all done
	return pixmap;
}

////////////////////////////////////////////////////////////////////////////
//
// JNI
//
////////////////////////////////////////////////////////////////////////////
JNIEXPORT jboolean JNICALL 
	OS_NATIVE(_1toggle_1above)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle, jboolean forceToggle) {

	GtkWidget* shellWidget = (GtkWidget*)unwrap_pointer(envir, widgetHandle);
	GdkWindow *window = shellWidget->window;

	Window x11window = GDK_WINDOW_XWINDOW(window);
	Display* display = GDK_DRAWABLE_XDISPLAY(window);
	if (!isWindowAbove(display, x11window) || forceToggle == JNI_TRUE) {
		toggle_window_state_above(display, x11window);
		return JNI_TRUE;
	}
	return JNI_FALSE;
}
JNIEXPORT jboolean JNICALL 
	OS_NATIVE(_1begin_1shot)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle) {

	GtkWidget* shellWidget = (GtkWidget*)unwrap_pointer(envir, widgetHandle);
	return moveWindowToDesktop(shellWidget);
}
JNIEXPORT jboolean JNICALL 
	OS_NATIVE(_1end_1shot)
		(JNIEnv *envir, jobject that, JHANDLE widgetHandle) {

	GtkWidget* shellWidget = (GtkWidget*)unwrap_pointer(envir, widgetHandle);
	return restoreWindow(shellWidget);
}
// shot
JNIEXPORT JHANDLE JNICALL OS_NATIVE(_1makeShot)(
			JNIEnv *envir, jobject that, JHANDLE widgetHandle, jobject callback) {
	m_envir = envir;
	if (callback != NULL) {
		m_callback = (*envir)->NewGlobalRef(envir, callback);
		jclass clazz = (*envir)->GetObjectClass(envir, m_callback);
		m_IScreenshotCallback_storeImage = (*envir)->GetMethodID(envir, clazz, "storeImage", CALLBACK_SIG);
	}
	// make shot
	GdkPixmap* pixmap = makeShot((GtkWidget*)unwrap_pointer(envir, widgetHandle));
	// clean up
	if (callback != NULL) {
		(*envir)->DeleteGlobalRef(envir, m_callback);
	}
	m_callback = NULL;
	return (JHANDLE)wrap_pointer(envir, pixmap);
}
// menu
JNIEXPORT JHANDLE JNICALL OS_NATIVE(_1fetchMenuVisualData)(
			JNIEnv *envir, jobject that, JHANDLE jmenuHandle, jintArray jsizes) {
	GtkWidget* menu = (GtkWidget*)unwrap_pointer(envir, jmenuHandle);
	GdkPixmap* pixmap = fetchMenuVisualData(GTK_MENU(menu), envir, jsizes);
	return wrap_pointer(envir, pixmap);
}
// tab item bounds
JNIEXPORT void JNICALL OS_NATIVE(_1getWidgetBounds)(
			JNIEnv *envir, jobject that, JHANDLE jhandle, jintArray jsizes) {
	getWidgetBounds((GtkWidget*)unwrap_pointer(envir, jhandle), envir, jsizes);
}
// unref
JNIEXPORT void JNICALL OS_NATIVE(_1disposeImageHandle)(
			JNIEnv *envir, jobject that, JHANDLE jhandle) {
	g_object_unref((GObject*)unwrap_pointer(envir, jhandle));
}
// other
static int isValidVersion() {
	return gtk_major_version == 2 && gtk_minor_version >= 12;
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
			gtk_window_set_opacity((GtkWindow*)shell, alpha / 255.0);
		}
	}
}

JNIEXPORT jint JNICALL OS_NATIVE(_1getAlpha)(
			JNIEnv *envir, jobject that, JHANDLE jshellHandle) {
	if (isValidVersion()) {
		GtkWidget *shell = (GtkWidget*)unwrap_pointer(envir, jshellHandle);
		if (gtk_widget_is_composited(shell)) {
			return (jint) (gtk_window_get_opacity((GtkWindow*)shell) * 255);
		}
	}
    return 255;
}
JNIEXPORT jboolean JNICALL OS_NATIVE(_1isPlusMinusTreeClick)(
			JNIEnv *envir, jobject that, JHANDLE jhandle, jint jx, jint jy) {
	return isPlusMinusTreeClick((GtkTreeView*)unwrap_pointer(envir, jhandle), (gint)jx, (gint)jy);
}

