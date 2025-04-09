/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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

#include <pango/pangocairo.h>
#include <gtk/gtk.h>
#include <gdk/gdkx.h>

#include <jni.h>

gint baselineValue = -1;

static void checkAllWidgetsCallback(GtkWidget *widget, gpointer data);
//
static gint getBaselineFromLayout(PangoLayout *layout) {
	if (layout == NULL) {
		return -1;
	}
	PangoLayoutIter *iter = pango_layout_get_iter(layout);
	if (iter == NULL) {
		return -1;
	}
	gint baseline = pango_layout_iter_get_baseline(iter);
	pango_layout_iter_free(iter);
	
	return baseline;
}
//
static void checkWidget(GtkWidget *widget, GtkWidget *topWidget){
	if (baselineValue != -1) {
		return;
	}
	PangoLayout *layout = NULL;
	gint x, y;
	gint baseline = -1;
	if (GTK_IS_LABEL(widget)) {
		GtkLabel *label = (GtkLabel*)widget;
		layout = gtk_label_get_layout(label);
		baseline = getBaselineFromLayout(layout);
		gtk_label_get_layout_offsets(label, &x, &y);
	} else if (GTK_IS_ENTRY(widget)) {
		GtkEntry *entry = (GtkEntry*)widget;
		layout = gtk_entry_get_layout(entry);
		baseline = getBaselineFromLayout(layout);
		gtk_entry_get_layout_offsets(entry, &x, &y);
	} else {
		return;
	}
	if (baseline == -1) {
		return;
	}
	baselineValue = PANGO_PIXELS(baseline) + y;
}

static void checkAllWidgets(GtkWidget *widget, GtkWidget *topWidget){
    // search through widgets tree until GtkLabel or GtkEntry have found
	if(!GTK_IS_WIDGET(widget))
		return;
	checkWidget(widget, topWidget);
	if (baselineValue != -1) {
		return;
	}
	if(!GTK_IS_CONTAINER(widget))
		return;
	GtkContainer *container = GTK_CONTAINER(widget);
	gtk_container_forall(container, checkAllWidgetsCallback, topWidget);
}

JNIEXPORT jint JNICALL Java_org_eclipse_wb_swt_widgets_baseline_GtkBaseline_fetchBaseline(
			JNIEnv *envir, jobject that, jlong widgetHandle) {
	if (widgetHandle == 0) {
		return -1;
	}
	GtkWidget *widget = (GtkWidget*)widgetHandle;
	baselineValue = -1;
	checkAllWidgets(widget, widget);
	return (jint)baselineValue;
}
JNIEXPORT jint JNICALL Java_org_eclipse_wb_swt_widgets_baseline_GtkBaseline_fetchBaselineFromLayout(
			JNIEnv *envir, jobject that, jlong layoutHandle) {
	if (layoutHandle == 0) {
		return -1;
	}
	return PANGO_PIXELS(getBaselineFromLayout((PangoLayout*)layoutHandle));
}

static void checkAllWidgetsCallback(GtkWidget *widget, gpointer data){
	checkAllWidgets(widget, (GtkWidget*)data);
}
