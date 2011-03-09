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
#include "UGLTypes.h"
#include <jni.h>
//=========================================================================================
typedef struct UGL_Widget {
	int display;
	int callback_target;
	UGL_Int ugl_style;
	int widget_dispose;
} UGL_Widget;
//
typedef struct UGL_Control {
	UGL_Widget widget;
	HWND hWnd;
} UGL_Control;
//
typedef struct UGL_Image {
	HANDLE image_handle;
	HDC selected_DC;
	HBITMAP null_bitmap;
	BOOL is_direct;
	UCHAR transparent_pixel;
	INT32 transparent_color;
	CRITICAL_SECTION dcSection;
} UGL_Image;
//
typedef struct UGL_Menu {
	UGL_Widget widget;
	struct UGL_Control* parent;
	int cascadeItem;
	HMENU hMenu;
} UGL_Menu;
//
typedef struct UGL_DC UGL_DC;
typedef void (*LockFunction)(UGL_DC* ugl_dc);
//
typedef struct UGL_DC {
	HDC hdc;
	HGDIOBJ originalPen;
	HGDIOBJ originalBrush;
	int ugl_image;
	RECT minClip;
	LOGPEN penData;
	LockFunction lock;
	LockFunction unlock;
} UGL_DC;
//
#define LOCK_DC(ugl_dc) ugl_dc->lock(ugl_dc)
#define UNLOCK_DC(ugl_dc) ugl_dc->unlock(ugl_dc)
//=========================================================================================
extern "C" {
	JNIEXPORT void JNICALL Java_org_eclipse_wb_internal_ercp_eswt_EmbeddedScreenShotMaker_embeddedMakeShot(JNIEnv*, jclass, jint controlHandle, jint imageHandle) {
		UGL_Control *control = (UGL_Control*)controlHandle;
		UGL_DC *image = (UGL_DC*)imageHandle;
		//
		LOCK_DC(image);
		SendMessageW((HWND)control->hWnd, WM_PRINT, (WPARAM)image->hdc, PRF_CLIENT | PRF_NONCLIENT | PRF_ERASEBKGND | PRF_CHILDREN);
		UNLOCK_DC(image);
	}
	//
	JNIEXPORT jint JNICALL Java_org_eclipse_wb_internal_ercp_eswt_EmbeddedScreenShotMaker_embeddedControlToSwt(JNIEnv*, jclass, jint controlHandle) {
		UGL_Control *control = (UGL_Control*)controlHandle;
		return (jint)control->hWnd;
	}
	//
	JNIEXPORT jint JNICALL Java_org_eclipse_wb_internal_ercp_eswt_EmbeddedScreenShotMaker_embeddedMenuToSwt(JNIEnv*, jclass, jint menuHandle) {
		UGL_Menu *menu = (UGL_Menu*)menuHandle;
		return (jint)menu->hMenu;
	}
	//
	JNIEXPORT jint JNICALL Java_org_eclipse_wb_internal_ercp_eswt_EmbeddedScreenShotMaker_embeddedImageToSwt(JNIEnv*, jclass, jint imageHandle) {
		UGL_Image *image = (UGL_Image*)imageHandle;
		return (jint)image->image_handle;
	}
	//
}