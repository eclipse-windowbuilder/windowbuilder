/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

#ifndef __UGLTYPES_H__
#define __UGLTYPES_H__

#include <windows.h>
//========================================================================================

/*
 * Platform-specific typedefs.
 */
//========================================================================================


typedef BOOLEAN				UGL_Boolean;		/* 1 for true, 0 for false */

typedef LPTSTR				UGL_String;			/* null-terminated array of
												   16-bit Unicode characters */												

typedef INT					UGL_Int;			/* 32-bit two's-complement */

typedef UGL_String*			UGL_StringArray;	/* an array of null-terminated
												   16-bit Unicode character
												   arrays */

typedef UGL_Int*			UGL_IntArray;		/* pointer to 32-bit two's-complement */

typedef LPBYTE				UGL_ByteArray;		/* pointer to 8-bit two's-complement */

typedef WCHAR				UGL_Char;			/* 16-bit Unicode */

#define	UGL_TRUE	1
#define	UGL_FALSE	0

#endif /*__UGLTYPES_H__*/