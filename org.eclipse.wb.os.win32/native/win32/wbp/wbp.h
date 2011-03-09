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
#ifndef __WBP_H_
#define __WBP_H

#define JHANDLE jobject
#define OS_NATIVE(func) Java_org_eclipse_wb_internal_os_win32_OSSupportWin32_##func

typedef BOOL (WINAPI *GETLAYEREDWINDOWATTRIBUTES)(HWND hwnd, COLORREF *pcrKey, BYTE *pbAlpha, DWORD *pdwFlags);
typedef BOOL (WINAPI *SETLAYEREDWINDOWATTRIBUTES)(HWND hwnd, COLORREF crKey, BYTE bAlpha, DWORD dwFlags);
void init();

#endif