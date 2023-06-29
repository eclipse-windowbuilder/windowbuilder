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
package org.eclipse.wb.internal.core.xml.model.property.accessor;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.accessor.AccessorUtils;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import java.lang.reflect.Method;

/**
 * {@link ExpressionAccessor} based on getter and setter {@link Method}.
 *
 * @author scheglov_ke
 * @coverage XML.model.property
 */
public final class MethodExpressionAccessor extends ExpressionAccessor {
	private final Method m_setter;
	private final Method m_getter;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public MethodExpressionAccessor(String attribute, Method setter, Method getter) {
		super(attribute);
		m_setter = setter;
		m_getter = getter;
		m_tooltipProvider = AccessorUtils.PropertyTooltipProvider_forMethod(setter);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Value
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Object getDefaultValue(XmlObjectInfo object) throws Exception {
		return object.getArbitraryValue(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void visit(XmlObjectInfo object, int state) throws Exception {
		super.visit(object, state);
		if (state == STATE_OBJECT_READY) {
			object.putArbitraryValue(this, askDefaultValue(object));
		}
	}

	/**
	 * @return the default value to remember, may be {@link Property#UNKNOWN_VALUE}.
	 */
	private Object askDefaultValue(final XmlObjectInfo object) {
		if (m_getter != null) {
			return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Object>() {
				@Override
				public Object runObject() throws Exception {
					Object toolkitObject = object.getObject();
					return m_getter.invoke(toolkitObject);
				}
			}, Property.UNKNOWN_VALUE);
		} else {
			return Property.UNKNOWN_VALUE;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IAdaptable
	//
	////////////////////////////////////////////////////////////////////////////
	private final PropertyTooltipProvider m_tooltipProvider;

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == PropertyTooltipProvider.class) {
			return adapter.cast(m_tooltipProvider);
		}
		// other
		return super.getAdapter(adapter);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the setter of the property.
	 */
	public Method getSetter() {
		return m_setter;
	}

	/**
	 * @return the getter of the property.
	 */
	public Method getGetter() {
		return m_getter;
	}
}
