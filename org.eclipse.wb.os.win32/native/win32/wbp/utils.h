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
#ifndef __UTILS_H_
#define __UTILS_H_

void* unwrap_pointer(JNIEnv *env, jobject jptr);
jobject wrap_pointer(JNIEnv *env, const void* ptr);

#endif // __UTILS_H_