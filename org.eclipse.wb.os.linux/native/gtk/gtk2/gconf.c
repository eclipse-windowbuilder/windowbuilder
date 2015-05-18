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
#include <gconf/gconf.h>
#include <jni.h>

#define OS_NATIVE(func) Java_org_eclipse_wb_internal_os_linux_Activator_##func

#define KEY_POSITION_MATCHES "/apps/compiz/plugins/place/screen0/options/position_matches"
#define KEY_POSITION_WA "/apps/compiz/plugins/place/screen0/options/position_constrain_workarea"
#define KEY_POSITION_X "/apps/compiz/plugins/place/screen0/options/position_x_values"
#define KEY_POSITION_Y "/apps/compiz/plugins/place/screen0/options/position_y_values"

JNIEXPORT jboolean JNICALL OS_NATIVE(_1isCompizSet)(
			JNIEnv *envir) {
	GConfEngine* engine = gconf_engine_get_default();
	if (engine == NULL) {
		// don't mess with errors
		return JNI_TRUE;
	}
	GError *err = NULL;
	GSList* lst = gconf_engine_get_list(engine, KEY_POSITION_MATCHES, GCONF_VALUE_STRING, &err);
	if (err != NULL) {
		gconf_engine_unref(engine);
		return JNI_TRUE;
	}
	gboolean found = FALSE;
	guint length = g_slist_length(lst);
	guint i;
    for (i = 0; i < length; i++) {
		gchar *value = g_slist_nth_data(lst, i);
		if (g_strrstr(value, "__wbp_preview_window") != NULL) {
			found = TRUE;
			break;		
		}
	}
	g_slist_free(lst);
	gconf_engine_unref(engine);
	return found ? JNI_TRUE : JNI_FALSE;
}

static gboolean append_to_list(GConfEngine* engine, const gchar* key, GConfValueType type, gpointer value) {
	GError *err = NULL;
	GSList* lst = gconf_engine_get_list(engine, key, type, &err);
	if (err != NULL) {
		return FALSE;
	}
	GSList* newList = g_slist_copy(lst);
	g_slist_free(lst);
	newList = g_slist_append(newList, value);
	return gconf_engine_set_list(engine, key, type, newList, &err);
}

JNIEXPORT jboolean JNICALL OS_NATIVE(_1setupCompiz)(
			JNIEnv *envir) {

	GConfEngine* engine = gconf_engine_get_default();
	if (engine == NULL) {
		return JNI_FALSE;
	}
	// add key window
	gboolean result = append_to_list(engine, KEY_POSITION_MATCHES, GCONF_VALUE_STRING, "title=__wbp_preview_window");
	result &= append_to_list(engine, KEY_POSITION_X, GCONF_VALUE_INT, GINT_TO_POINTER(-10000));
	result &= append_to_list(engine, KEY_POSITION_Y, GCONF_VALUE_INT, GINT_TO_POINTER(-10000));
	result &= append_to_list(engine, KEY_POSITION_WA, GCONF_VALUE_BOOL, GINT_TO_POINTER(FALSE));

	gconf_engine_unref(engine);

	return result ? JNI_TRUE : JNI_FALSE;
}

