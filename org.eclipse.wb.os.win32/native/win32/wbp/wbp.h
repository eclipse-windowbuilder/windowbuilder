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
#ifndef __WBP_H_
#define __WBP_H

#define JHANDLE jobject
#define OS_NATIVE(func) Java_org_eclipse_wb_internal_os_win32_OSSupportWin32_##func

typedef BOOL (WINAPI *GETLAYEREDWINDOWATTRIBUTES)(HWND hwnd, COLORREF *pcrKey, BYTE *pbAlpha, DWORD *pdwFlags);
typedef BOOL (WINAPI *SETLAYEREDWINDOWATTRIBUTES)(HWND hwnd, COLORREF crKey, BYTE bAlpha, DWORD dwFlags);
void init();

#endif