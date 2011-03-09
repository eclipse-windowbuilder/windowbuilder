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

#ifdef _WIN64
	#define JHANDLE jlong
	#define OS_NATIVE(func) Java_org_eclipse_wb_internal_ercp_eswt_EmbeddedScreenShotMaker_##func
#else
	#define JHANDLE jint
	#define OS_NATIVE(func) Java_org_eclipse_wb_internal_ercp_eswt_EmbeddedScreenShotMaker_##func
#endif

#endif