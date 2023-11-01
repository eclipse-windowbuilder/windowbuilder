/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.xml.model.generic;

import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Association;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Configurable {@link FlowContainer} for {@link XmlObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage XML.model.generic
 */
public final class FlowContainerConfigurable implements FlowContainer {
	private final XmlObjectInfo m_container;
	private final FlowContainerConfiguration m_configuration;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FlowContainerConfigurable(XmlObjectInfo container, FlowContainerConfiguration configuration) {
		m_container = container;
		m_configuration = configuration;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isHorizontal() {
		return m_configuration.getHorizontalPredicate().test(m_container);
	}

	@Override
	public boolean isRtl() {
		return m_configuration.getRtlPredicate().test(m_container);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean validateComponent(Object component) {
		return m_configuration.getComponentValidator().validate(m_container, component);
	}

	@Override
	public boolean validateReference(Object reference) {
		return m_configuration.getReferenceValidator().validate(m_container, reference);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void command_CREATE(Object newObject, Object referenceObject) throws Exception {
		if (!tryDuckTyping("command_CREATE", newObject, referenceObject)) {
			command_CREATE_default(newObject, referenceObject);
		}
		tryDuckTyping("command_CREATE_after", newObject, referenceObject);
		tryDuckTyping("command_APPEND_after", newObject, referenceObject);
		tryDuckTyping("command_TARGET_after", newObject, referenceObject);
	}

	@Override
	public void command_MOVE(Object moveObject, Object referenceObject) throws Exception {
		if (!tryDuckTyping("command_MOVE", moveObject, referenceObject)) {
			command_MOVE_default(moveObject, referenceObject);
		}
		tryDuckTyping("command_MOVE_after", moveObject, referenceObject);
		tryDuckTyping("command_TARGET_after", moveObject, referenceObject);
	}

	private void command_CREATE_default(Object newObject, Object referenceObject) throws Exception {
		XmlObjectInfo component = (XmlObjectInfo) newObject;
		XmlObjectInfo nextComponent = (XmlObjectInfo) referenceObject;
		Association association = m_configuration.getAssociation();
		XmlObjectUtils.add(component, association, m_container, nextComponent);
	}

	private void command_MOVE_default(Object moveObject, Object referenceObject) throws Exception {
		XmlObjectInfo component = (XmlObjectInfo) moveObject;
		XmlObjectInfo oldParent = (XmlObjectInfo) component.getParent();
		XmlObjectInfo nextComponent = (XmlObjectInfo) referenceObject;
		if (System.getProperty("flowContainer.simulateMove") == null) {
			Association association = m_configuration.getAssociation();
			XmlObjectUtils.move(component, association, m_container, nextComponent);
		}
		if (oldParent != m_container) {
			tryDuckTyping("command_ADD_after", component, referenceObject);
			tryDuckTyping("command_APPEND_after", component, referenceObject);
		}
	}

	private boolean tryDuckTyping(String methodName, Object object, Object referenceObject)
			throws Exception {
		Method method = getCommandMethod(methodName, object, referenceObject);
		if (method != null) {
			method.invoke(m_container, object, referenceObject);
			return true;
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Method} with given name that can be invoked with object/referenceObject.
	 */
	private Method getCommandMethod(String methodName, Object object, Object referenceObject) {
		List<Method> methods = new ArrayList<>();
		for (Method method : m_container.getClass().getMethods()) {
			if (method.getName().equals(methodName)) {
				Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes.length == 2) {
					if (ReflectionUtils.isAssignableFrom(parameterTypes[0], object)
							&& ReflectionUtils.isAssignableFrom(parameterTypes[1], referenceObject)) {
						methods.add(method);
					}
				}
			}
		}
		return ReflectionUtils.getMostSpecific(methods);
	}
}
