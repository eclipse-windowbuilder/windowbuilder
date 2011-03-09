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
#include "wbp.h"

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

