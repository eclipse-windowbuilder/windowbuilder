/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
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
package org.eclipse.wb.internal.core.model.description.rules;

import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.function.FailableBiConsumer;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * The {@link FailableBiConsumer} for including/excluding methods. For example
 * sometimes we want prevent execution of some methods.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class MethodsOperationRule implements FailableBiConsumer<ComponentDescription, String, Exception> {
	private final boolean m_include;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MethodsOperationRule(boolean include) {
		m_include = include;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Rule
	//
	////////////////////////////////////////////////////////////////////////////
	private ComponentDescription m_componentDescription;

	@Override
	public void accept(ComponentDescription componentDescription, String signature) throws Exception {
		m_componentDescription = componentDescription;
		if (isRegexpSignature(signature)) {
			processRegexp(signature);
		} else {
			processSingleSignature(signature);
		}
	}

	private boolean isRegexpSignature(String signature) {
		return signature.startsWith("/") && signature.endsWith("/");
	}

	private void processRegexp(final String signature) throws Exception {
		final Pattern pattern = Pattern.compile(StringUtils.substring(signature, 1, -1));
		process(t -> pattern.matcher(t).matches());
	}

	private void processSingleSignature(final String signature) throws Exception {
		process(t -> signature.equals(t));
	}

	private void process(Predicate<String> signaturePredicate) throws Exception {
		if (m_include) {
			processInclude(signaturePredicate);
		} else {
			processExclude(signaturePredicate);
		}
	}

	private void processInclude(Predicate<String> signaturePredicate) throws Exception {
		Method[] methods = m_componentDescription.getComponentClass().getMethods();
		for (Method method : methods) {
			String methodSignature = ReflectionUtils.getMethodSignature(method);
			if (signaturePredicate.test(methodSignature)) {
				m_componentDescription.addMethod(method);
			}
		}
	}

	private void processExclude(Predicate<String> signaturePredicate) {
		for (Iterator<MethodDescription> I = m_componentDescription.getMethods().iterator(); I.hasNext();) {
			MethodDescription methodDescription = I.next();
			String methodSignature = methodDescription.getSignature();
			if (signaturePredicate.test(methodSignature)) {
				I.remove();
			}
		}
	}
}