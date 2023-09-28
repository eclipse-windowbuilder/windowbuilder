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
package org.eclipse.wb.internal.core.xml.model.clipboard;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.xml.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectClipboardCopy;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for copy/paste information about {@link XmlObjectInfo}.
 *
 * <p>
 * During "copy" operation method {@link #createMemento(XmlObjectInfo)} should be used to create
 * instance of {@link XmlObjectMemento} with all information about {@link XmlObjectInfo}.
 *
 * <p>
 * Later, during "paste", following methods should be invoked (only one time and only in this
 * sequence):
 * <ul>
 * <li> {@link #create(XmlObjectInfo)} to create {@link XmlObjectInfo} that can be used to bind to
 * the {@link XmlObjectInfo} hierarchy.</li>
 * <li> {@link #apply()}, after adding {@link XmlObjectInfo} to the hierarchy, to apply all
 * {@link ClipboardCommand}'s and do other things for configuring created {@link XmlObjectInfo}.</li>
 * </ul>
 *
 * @author scheglov_ke
 * @coverage XML.model.clipboard
 */
public class XmlObjectMemento implements Serializable {
	private static final long serialVersionUID = 0L;
	/**
	 * The key under which {@link XmlObjectMemento} registers itself in created {@link XmlObjectInfo}
	 * .
	 */
	public static final String KEY_MEMENTO = "KEY_MEMENTO";

	////////////////////////////////////////////////////////////////////////////
	//
	// Creation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link XmlObjectMemento} for given {@link XmlObjectInfo}.
	 */
	public static XmlObjectMemento createMemento(XmlObjectInfo javaInfo) throws Exception {
		if (javaInfo instanceof AbstractComponentInfo) {
			return new ComponentInfoMemento((AbstractComponentInfo) javaInfo);
		}
		return new XmlObjectMemento(javaInfo);
	}

	/**
	 * Checks that component can be copy/paste, i.e. {@link XmlObjectMemento} can be created, but
	 * don't really create it. We use this to enable/disable copy/cut actions on selection change.
	 *
	 * @return <code>true</code> if {@link XmlObjectMemento} can be created for given
	 *         {@link XmlObjectInfo}.
	 */
	public static boolean hasMemento(XmlObjectInfo javaInfo) {
		if (javaInfo.isRoot()) {
			return false;
		}
		return javaInfo.getCreationSupport().getClipboard() != null;
	}

	/**
	 * If given {@link XmlObjectInfo} was create from some {@link XmlObjectMemento}, then applies this
	 * memento.
	 */
	public static void apply(XmlObjectInfo object) throws Exception {
		XmlObjectMemento memento = (XmlObjectMemento) object.getArbitraryValue(KEY_MEMENTO);
		memento.apply();
	}

	/**
	 * @return <code>true</code> if given {@link XmlObjectInfo} is in process for pasting.
	 */
	public static boolean isApplying(XmlObjectInfo object) {
		return (XmlObjectMemento) object.getArbitraryValue(KEY_MEMENTO) != null;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	private final String m_componentClassName;
	private final IClipboardCreationSupport m_creationSupport;
	private final List<ClipboardCommand> m_commands = new ArrayList<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	protected XmlObjectMemento(XmlObjectInfo javaInfo) throws Exception {
		m_componentClassName = javaInfo.getDescription().getComponentClass().getName();
		// creation
		{
			m_creationSupport = javaInfo.getCreationSupport().getClipboard();
			Assert.isNotNull(m_creationSupport, "No clipboard CreationSupport for %s", javaInfo);
			cleanUpAnonymous(m_creationSupport);
		}
		// prepare commands
		addCommands(javaInfo, m_commands);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Copy utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds {@link ClipboardCommand}'s for restoring properties, children, etc for given
	 * {@link XmlObjectInfo}.
	 *
	 * @param javaInfo
	 *          the {@link XmlObjectInfo} to add commands for.
	 * @param commands
	 *          the container to add commands to.
	 */
	static void addCommands(XmlObjectInfo javaInfo, List<ClipboardCommand> commands) throws Exception {
		// properties command
		commands.add(new PropertiesClipboardCommand(javaInfo));
		// broadcast commands
		javaInfo.getBroadcast(XmlObjectClipboardCopy.class).invoke(javaInfo, commands);
		// clean up anonymous commands
		for (ClipboardCommand command : commands) {
			cleanUpAnonymous(command);
		}
	}

	/**
	 * Clears "this$0", "this$1", etc, because it is convenient to use clipboard anonymous classes,
	 * and we sure that they don't reference enclosing classes, even if Java thinks that they do this.
	 */
	static void cleanUpAnonymous(Object o) throws Exception {
		for (int i = 0; i < 10; i++) {
			Field field = ReflectionUtils.getFieldByName(o.getClass(), "this$" + i);
			if (field != null) {
				field.set(o, null);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Creation/paste
	//
	////////////////////////////////////////////////////////////////////////////
	private transient XmlObjectInfo m_javaInfo;
	private transient boolean m_applied;

	/**
	 * @return the class of component represented by this {@link XmlObjectMemento}. This can be used
	 *         to check component class/model in more light way than creating real
	 *         {@link XmlObjectInfo}.
	 */
	public String getComponentClassName() {
		return m_componentClassName;
	}

	/**
	 * Creates new {@link XmlObjectInfo} using remembered values.
	 *
	 * @param existingHierarchyObject
	 *          some {@link XmlObjectInfo} in model hierarchy.
	 *
	 * @return the new {@link XmlObjectInfo}.
	 */
	public XmlObjectInfo create(XmlObjectInfo existingHierarchyObject) throws Exception {
		Assert.isLegal(!m_applied, "This memento already applied.");
		if (m_javaInfo == null) {
			XmlObjectInfo rootObject = existingHierarchyObject.getRootXML();
			EditorContext context = rootObject.getContext();
			// prepare creation
			CreationSupport creationSupport = m_creationSupport.create(rootObject);
			// create XMLObject_Info
			m_javaInfo = XmlObjectUtils.createObject(context, m_componentClassName, creationSupport);
			m_javaInfo.putArbitraryValue(KEY_MEMENTO, this);
		}
		return m_javaInfo;
	}

	/**
	 * Performs configuring for this {@link XmlObjectInfo}.
	 */
	public void apply() throws Exception {
		Assert.isNotNull(m_javaInfo, "XMLObject_Info should be already created using create().");
		Assert.isLegal(
				m_javaInfo.getParent() != null,
				"XMLObject_Info should be already bounds to the hierarchy.");
		Assert.isLegal(!m_applied, "This memento already applied.");
		m_applied = true;
		// do apply
		if (XmlObjectUtils.hasTrueParameter(m_javaInfo, "clipboard.lightRefresh")) {
			m_javaInfo.getRootXML().refreshLight();
		}
		m_creationSupport.apply(m_javaInfo);
		// execute commands
		executeCommands();
		// done
		m_javaInfo.removeArbitraryValue(KEY_MEMENTO);
	}

	/**
	 * Executes remembered {@link ClipboardCommand}'s for this {@link XmlObjectInfo}.
	 */
	private void executeCommands() throws Exception {
		for (ClipboardCommand command : m_commands) {
			command.execute(m_javaInfo);
		}
	}
}
