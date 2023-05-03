/*******************************************************************************
 * Copyright (c) 2023 Patrick Ziegler and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model;

import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition;
import net.bytebuddy.dynamic.DynamicType.Builder.MethodDefinition.ImplementationDefinition;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Method;

/**
 * General-purpose Byte-Buddy aware method interceptor. Instances of this class
 * may be used as an argument for
 * {@link ImplementationDefinition#intercept(Implementation)}, in order to
 * instrument the method invocations of the generated class.
 */
public interface MethodInterceptor {
  /**
   * This method is invoked by all methods that match against
   * {@link MethodDefinition#method(ElementMatcher)}, instead of the original
   * method. The original method may be invoked by normal reflection using the
   * Method object.
   *
   * @param method intercepted Method
   * @param args   argument array; primitive types are wrapped
   * @throws Throwable any exception may be thrown; if so, super method will not
   *                   be invoked
   * @return any value compatible with the signature of the proxied method. Method
   *         returning void will ignore this value.
   */
  @RuntimeType
  Object intercept(@Origin Method method, @AllArguments Object[] args) throws Throwable;
}