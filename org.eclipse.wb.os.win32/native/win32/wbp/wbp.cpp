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
#include "stdafx.h"
#include "utils.h"

BOOL m_init = FALSE;

GETLAYEREDWINDOWATTRIBUTES GetLayeredWindowAttributesPtr = NULL;
SETLAYEREDWINDOWATTRIBUTES SetLayeredWindowAttributesPtr = NULL;

void init() {
	if (!m_init) {
		(FARPROC&)GetLayeredWindowAttributesPtr = GetProcAddress(GetModuleHandle(_T("user32")), "GetLayeredWindowAttributes");
		(FARPROC&)SetLayeredWindowAttributesPtr = GetProcAddress(GetModuleHandle(_T("user32")), "SetLayeredWindowAttributes");
		m_init = TRUE;
	}
}
extern "C" {

	JNIEXPORT void JNICALL OS_NATIVE(_1makeShot)(JNIEnv *env, jclass that, JHANDLE jwnd, JHANDLE jdc) {
		HWND hwnd = (HWND)unwrap_pointer(env, jwnd);
		HDC hdc = (HDC)unwrap_pointer(env, jdc);
		SendMessage(hwnd, WM_PRINT, (WPARAM)hdc, PRF_CLIENT | PRF_NONCLIENT | PRF_ERASEBKGND | PRF_CHILDREN);
	}
	JNIEXPORT jboolean JNICALL OS_NATIVE(_1isPlusMinusTreeClick)(JNIEnv *env, jclass that, JHANDLE jwnd, jint jx, jint jy) {
		HWND hwnd = (HWND)unwrap_pointer(env, jwnd);
		TVHITTESTINFO ht;
		ht.pt.x = jx;
		ht.pt.y = jy;
		SendMessage(hwnd, TVM_HITTEST, 0, (LPARAM)&ht);
		if (ht.flags == TVHT_ONITEMBUTTON) {
			return JNI_TRUE;
		}
		return JNI_FALSE;
	}
	JNIEXPORT void JNICALL OS_NATIVE(_1setAlpha)(JNIEnv *env, jclass that, JHANDLE jwnd, jint jalpha) {
		HWND hwnd = (HWND)unwrap_pointer(env, jwnd);
		BYTE alpha = (BYTE)jalpha;
		LONG_PTR bits = GetWindowLongPtr(hwnd, GWL_EXSTYLE);
		if (SetLayeredWindowAttributesPtr != NULL) {
			if (alpha == 0xFF) {
				SetWindowLongPtr(hwnd, GWL_EXSTYLE, bits & ~WS_EX_LAYERED);
				UINT flags = RDW_ERASE | RDW_INVALIDATE | RDW_FRAME | RDW_ALLCHILDREN;
				RedrawWindow(hwnd, NULL, NULL, flags);
			} else {
				SetWindowLongPtr(hwnd, GWL_EXSTYLE, bits | WS_EX_LAYERED);
				SetLayeredWindowAttributesPtr(hwnd, NULL, alpha, LWA_ALPHA);
			}
		}
	}
	JNIEXPORT jint JNICALL OS_NATIVE(_1getAlpha)(JNIEnv *env, jclass that, JHANDLE jwnd) {
		HWND hwnd = (HWND)unwrap_pointer(env, jwnd);
		BYTE alpha;
		if ((GetLayeredWindowAttributesPtr != NULL) && (GetLayeredWindowAttributesPtr(hwnd, NULL, &alpha, NULL))) {
			return alpha & 0xFF;
		}
		return 0xFF;
	}
	JNIEXPORT void JNICALL OS_NATIVE(_1getTabItemBounds)(JNIEnv *env, jclass that, JHANDLE jwnd, jint jitemIndex, jintArray jbounds) {
		HWND hwnd = (HWND)unwrap_pointer(env, jwnd);
		// prepare buffer
		jsize boundsSize = env->GetArrayLength(jbounds);
		jint *bounds = new jint [boundsSize * sizeof(jint)];

		RECT itemRect;
		SendMessage(hwnd, TCM_GETITEMRECT, (WPARAM)jitemIndex, (LPARAM)&itemRect);

		*(bounds + 0) = itemRect.left;
		*(bounds + 1) = itemRect.right;
		*(bounds + 2) = itemRect.top;
		*(bounds + 3) = itemRect.bottom;
		// copy dimensions into java array
		env->SetIntArrayRegion(jbounds, 0, boundsSize, bounds);
		delete []bounds;
	}
	JNIEXPORT void JNICALL OS_NATIVE(_1DeleteObject)(JNIEnv *env, jclass that, JHANDLE jhandle) {
		HGDIOBJ obj = (HGDIOBJ)unwrap_pointer(env, jhandle);
		DeleteObject(obj);
	}
	JNIEXPORT void JNICALL OS_NATIVE(_1scroll)(JNIEnv *env, jclass that, JHANDLE jwnd, jint jcount) {
		HWND hwnd = (HWND)unwrap_pointer(env, jwnd);
		int count;
		WPARAM wParam;
		if (jcount < 0) {
			count = -jcount;
			wParam = SB_LINEDOWN;
		} else {
			count = jcount;
			wParam = SB_LINEUP;
		}
		// send events
		for (int i = 0; i < count; i++) {
			SendMessage(hwnd, WM_VSCROLL, wParam, 0);
		}
	}
}

