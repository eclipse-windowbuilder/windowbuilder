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
package org.eclipse.wb.tests.designer.core.annotations;

import org.eclipse.core.resources.IProject;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * Indicates that after running test {@link Method} with this annotation {@link IProject} should be
 * disposed, so it will be recreated later. Usually we need this because test performs modifications
 * which can not be tracked by test class, such as adding new files (not using test-local methods)
 * or changing classpath.
 * 
 * @author scheglov_ke
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DisposeProjectAfter {
}
