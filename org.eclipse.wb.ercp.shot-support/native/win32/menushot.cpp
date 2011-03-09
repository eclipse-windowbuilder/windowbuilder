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
// Shot.cpp.
// Menu Screenshoting algorithm:
// 1. Initialize variables and set hook to shell window
//		in hook proc:
//		1. catch creation of menu window by intercept WM_CREATE message 
//			to get a menu window handle (by checking window class "#32768")
//		2. Subclass the menu window by obtained handle.
//		3. In new menu window proc:
//			1. catch menu window size and create DC and Bitmap to draw
//			2. catch window position changing and modify it to be 
//			shown outside of screen.
//			3. post WM_PRINT message to force the menu 	window do draw 
//			itself onto prepared bitmap. 
//			4. After all post WM_CLOSE message to force the menu window to quit modal loop
// 2. do TrackPopupMenu
// 3. remove hook from shell window and do some clean ups

#include "stdafx.h"
#define WM_GETMENUDIMENSIONS (WM_APP+478)

// handle for shell window
HWND hWnd = NULL;
// handle for our menu
HMENU hMenu = NULL;
// old hook address
HHOOK hCallWndHook = NULL;
// popup menu window handle
HWND hwndPopupMenu = NULL;
// handle for default DC
HDC hdefaultDC = NULL;
// handle for DC on which we will draw
HDC hDC = NULL;
// handle for hBitmap which we return. It would be released by SWT
HBITMAP hBitmap;
// Original menu window proc
LONG_PTR wpOrigEditProc = NULL;
// enable or not WM_PRINT
bool calcSizeDone = false;
// array of item dimensions
jint *sizes = NULL;
// Subclass procedure 
LRESULT APIENTRY MenuSubclassProc(HWND hwnd, UINT uMsg, WPARAM wParam, LPARAM lParam) { 
	switch(uMsg) {
		case WM_NCCALCSIZE:
			{
				// get calculated size for menu window
				int width, height;
				// allow the menu window to calculate it's sizes (possibly it is already done, but i'm not sure)
				LRESULT callResult = CallWindowProc((WNDPROC)wpOrigEditProc, hwnd, uMsg, wParam, lParam);
				// get dimensions depending on returned param types
				if (wParam == TRUE) { 
					NCCALCSIZE_PARAMS *pParams = (NCCALCSIZE_PARAMS *)lParam;
					width = pParams->lppos->cx;
					height = pParams->lppos->cy;
				} else {
					RECT *pSize = (RECT*)lParam;
					width = pSize->right - pSize->left;
					height = pSize->bottom - pSize->top;
				}
				// got dimensions, prepare DC and Bitmap
				hdefaultDC = GetDC(NULL);
				if (hdefaultDC != NULL) {
					hDC = CreateCompatibleDC(hdefaultDC);
					if (hDC != NULL) {
						hBitmap = CreateCompatibleBitmap(hdefaultDC, width, height);
						if (hBitmap != NULL) {
							SelectObject(hDC, hBitmap);
						}
					}
				}
				// the menu window initialized, allow to make shot
				calcSizeDone = true;
				// don't call the default menu window proc again
				return callResult;
			}
			break;
		case WM_WINDOWPOSCHANGING:
			if (calcSizeDone) {
				// move menu window deep out of the screen
				WINDOWPOS* pPos = (WINDOWPOS*)lParam;
				pPos->x += 10000;
				// if initialized successfully then make the window to draw onto our DC
				if (hDC != NULL && hBitmap != NULL) {
					PostMessage(hwnd, WM_PRINT, (WPARAM)hDC, PRF_CLIENT | PRF_NONCLIENT | PRF_ERASEBKGND | PRF_CHILDREN);
				}
				// Post message to get items dimensions
				PostMessage(hwnd, WM_GETMENUDIMENSIONS, NULL, NULL);
				// the menu window is no longer needed
				PostMessage(hwnd, WM_CLOSE, NULL, NULL);
				calcSizeDone = false;
			}
			break;
		case WM_GETMENUDIMENSIONS:
			{	
				// get menu item dimensions
				int itemCount = GetMenuItemCount(hMenu);
				// get starting x,y offsets
				RECT rect1;
				if (!GetMenuItemRect (hWnd, hMenu, 0, &rect1)) {
					break;
				}
				// get all items dimensions relative to menu (a little overhead is here :))
				for (int index = 0; index < itemCount; ++index) {
					RECT rect2;
					if (!GetMenuItemRect (hWnd, hMenu, index, &rect2)) {
						break;
					}
					sizes[index * 4 + 0] = rect2.left - rect1.left + 2;
					sizes[index * 4 + 1] = rect2.top - rect1.top + 2;
					sizes[index * 4 + 2] = rect2.right - rect2.left;
					sizes[index * 4 + 3] = rect2.bottom - rect2.top;
				}
			}
			break;
	}
	// call default menu window proc
	return CallWindowProc((WNDPROC)wpOrigEditProc, hwnd, uMsg, wParam, lParam); 
} 
// Hook procedure
LRESULT CALLBACK CallWndProc(int nCode, WPARAM wParam, LPARAM lParam) {
	if (nCode == HC_ACTION) {
		// we can process messages only when it is permitted by Windows
		CWPSTRUCT *pCwp = (CWPSTRUCT *)lParam;
		if (pCwp == NULL) {
			return CallNextHookEx(hCallWndHook, nCode, wParam, lParam);
		}
		// intercept WM_CREATE
		if (pCwp->message == WM_CREATE) {
			// get class name of the window
			_TCHAR className[4096];
			memset(className, 0, sizeof(className));
			CREATESTRUCT *pCreateStruct = (CREATESTRUCT *)pCwp->lParam;
			if (GetClassName(pCwp->hwnd, className, (sizeof(className) / sizeof(_TCHAR)) - 1) == 0) {
				return CallNextHookEx(hCallWndHook, nCode, wParam, lParam);
			}
			// is it menu window class?
			if (_tcscmp(className, _T("#32768")) == 0) {
				// yes! we've found menu window handle
				hwndPopupMenu = pCwp->hwnd;
				// subclass menu window
				wpOrigEditProc = SetWindowLongPtr(hwndPopupMenu, GWLP_WNDPROC, (LONG_PTR) MenuSubclassProc); 
			}
			// let Window to call next hook
			return CallNextHookEx(hCallWndHook, nCode, wParam, lParam);
		}
		// remove subclassing when menu window is about to destroy
		if (pCwp->message == WM_DESTROY && pCwp->hwnd == hwndPopupMenu && wpOrigEditProc != NULL) {
			SetWindowLongPtr(hwndPopupMenu, GWLP_WNDPROC, (LONG_PTR) wpOrigEditProc); 
			wpOrigEditProc = NULL;
		}
	}
	// Call the next handler in the chain
	return CallNextHookEx(hCallWndHook, nCode, wParam, lParam);
}

extern "C" { 
	// Menu Screen Shot
	JNIEXPORT JHANDLE JNICALL OS_NATIVE(fetchPopupMenuVisualData)(JNIEnv *env, jclass that, JHANDLE jshell, JHANDLE jmenu, jintArray jsizes) {
		// initializing
		HWND oldFocused = GetFocus();
		hCallWndHook = NULL;
		hwndPopupMenu = NULL;
		hdefaultDC = NULL;
		hDC = NULL;
		hBitmap = NULL;
		wpOrigEditProc = NULL;
		calcSizeDone = false;
		hWnd = (HWND)jshell;
		hMenu = (HMENU)jmenu;
		// install hook
//		DWORD ourThreadID = GetWindowThreadProcessId(hWnd, NULL);
		DWORD ourThreadID = GetCurrentThreadId();
		hCallWndHook = SetWindowsHookEx(WH_CALLWNDPROC, (HOOKPROC)CallWndProc,	NULL, ourThreadID);
		if (hCallWndHook == NULL) {
			return NULL;
		}
		// prepare dimensions array elements
		jsize sizesSize = env->GetArrayLength(jsizes);
		sizes = new jint[sizesSize];
		// display menu window
		SetForegroundWindow(hWnd);
		TrackPopupMenuEx(hMenu, TPM_HORIZONTAL | TPM_LEFTALIGN | TPM_NOANIMATION, 100, 100, hWnd, NULL);
		// copy dimensions into java array
		env->SetIntArrayRegion(jsizes, 0, sizesSize, sizes);
		// remove hook and clean up
		UnhookWindowsHookEx(hCallWndHook);
		hCallWndHook = NULL;
		if (hdefaultDC) {
			ReleaseDC(NULL, hdefaultDC);
		}
		if (hDC) {
			DeleteDC(hDC);
		}
		delete []sizes;
		// restore focus
		if (oldFocused != NULL) {
			SetFocus(oldFocused);
		}
		// return bitmap (may be NULL)
		return (JHANDLE)hBitmap;
	}
}


