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
package org.eclipse.wb.internal.core.model.nonvisual;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.generic.FlowContainerConfigurable;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Group item for {@link FlorContainer}s.
 *
 * @author sablin_aa
 * @coverage core.model.nonvisual
 */
public class FlowContainerGroupInfo extends CollectorObjectInfo {
	private final JavaInfo m_component;
	ArrayList<FlowContainerConfigurable> m_containers = new ArrayList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FlowContainerGroupInfo(AstEditor editor, JavaInfo javaInfo, String caption)
			throws Exception {
		super(editor, caption);
		m_component = javaInfo;
		m_component.addChild(this);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Items
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public List<ObjectInfo> getItems() {
		List<ObjectInfo> list = new ArrayList<>();
		List<JavaInfo> children = m_component.getChildrenJava();
		for (JavaInfo child : children) {
			for (FlowContainerConfigurable container : m_containers) {
				if (container.validateComponent(child)) {
					list.add(child);
					break;
				}
			}
		}
		return list;
	}

	@Override
	public void addItem(ObjectInfo item) throws Exception {
		error("addItem(ObjectInfo)");
	}

	@Override
	protected void addItem(int index, ObjectInfo item) throws Exception {
		error("addItem(int,ObjectInfo)");
	}

	@Override
	protected void removeItem(ObjectInfo item) throws Exception {
		error("removeItem(ObjectInfo)");
	}

	private void error(String operation) throws Exception {
		throw new Exception("Operation '" + operation + "' not allowed.");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public List<FlowContainerConfigurable> getContainers() {
		return Collections.unmodifiableList(m_containers);
	}

	public void addContainer(FlowContainerConfigurable container) {
		m_containers.add(container);
	}

	public boolean removeContainer(FlowContainerConfigurable container) {
		return m_containers.remove(container);
	}
}
