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
#ifndef __UTILS_H_
#define __UTILS_H_

void* unwrap_pointer(JNIEnv *env, jobject jptr);
jobject wrap_pointer(JNIEnv *env, const void* ptr);

#endif // __UTILS_H_