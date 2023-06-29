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
package org.eclipse.wb.internal.core.xml.model.utils;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.IRootProcessor;
import org.eclipse.wb.internal.core.xml.model.IWrapperInfo;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.association.Association;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAdd;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectMove;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectResolveTag;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.xml.model.description.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.xml.model.generic.FlowContainerFactory;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Control;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utils for {@link XmlObjectInfo} manipulation.
 *
 * @author scheglov_ke
 * @coverage XML.model.utils
 */
public final class XmlObjectUtils {
	////////////////////////////////////////////////////////////////////////////
	//
	// Checks
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return <code>true</code> if given {@link XmlObjectInfo} is implicit, i.e. has not real
	 *         element.
	 */
	public static boolean isImplicit(XmlObjectInfo object) {
		return object.getCreationSupport() instanceof IImplicitCreationSupport;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parameters
	//
	////////////////////////////////////////////////////////////////////////////
	private static final String KEY_PARAMETER_PREFIX = "Instance-level parameter: ";

	/**
	 * Sets the {@link XmlObjectInfo} instance level parameter value, can be accesses later using
	 * {@link #getParameter(XmlObjectInfo, String)}.
	 */
	public static void setParameter(XmlObjectInfo object, String name, String value) {
		String key = KEY_PARAMETER_PREFIX + name;
		object.putArbitraryValue(key, value);
	}

	/**
	 * Returns the value of {@link XmlObjectInfo} parameter.
	 * <p>
	 * Usually returns just {@link ComponentDescription#getParameter(String)}, but checks also
	 * instance level parameters.
	 *
	 * @return the value of {@link XmlObjectInfo} parameter.
	 */
	public static String getParameter(XmlObjectInfo object, String name) {
		// try to key from XMLObject_Info instance
		{
			String key = KEY_PARAMETER_PREFIX + name;
			String value = (String) object.getArbitraryValue(key);
			if (value != null) {
				return value;
			}
		}
		// get from ComponentDescription
		return object.getDescription().getParameter(name);
	}

	/**
	 * Checks if {@link XmlObjectInfo} has parameter with value <code>"true"</code>.
	 */
	public static boolean hasTrueParameter(XmlObjectInfo object, String name) {
		String parameter = getParameter(object, name);
		return "true".equals(parameter);
	}

	/**
	 * @return mapped {@link XmlObjectInfo} parameters.
	 */
	public static Map<String, String> getParameters(XmlObjectInfo object) {
		Map<String, String> parameters = Maps.newHashMap();
		parameters.putAll(extractArbitraryParameters(object));
		parameters.putAll(object.getDescription().getParameters());
		return parameters;
	}

	/**
	 * @return the {@link Map} of parameters set using
	 *         {@link #setParameter(XmlObjectInfo, String, String)}.
	 */
	private static Map<String, String> extractArbitraryParameters(XmlObjectInfo object) {
		Map<String, String> parameters = Maps.newHashMap();
		for (Entry<Object, Object> arbitrary : object.getArbitraries().entrySet()) {
			Object key = arbitrary.getKey();
			Object value = arbitrary.getValue();
			if (key instanceof String && value instanceof String) {
				String stringKey = (String) key;
				if (stringKey.startsWith(KEY_PARAMETER_PREFIX)) {
					parameters.put(stringKey.substring(KEY_PARAMETER_PREFIX.length()), (String) value);
				}
			}
		}
		return parameters;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Script
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * If given {@link XmlObjectInfo} has parameter with script, execute it with "model" and "object"
	 * variables.
	 */
	public static Object executeScriptParameter(XmlObjectInfo model, String scriptName)
			throws Exception {
		String script = getParameter(model, scriptName);
		if (script == null) {
			return null;
		}
		return executeScript(model, script);
	}

	/**
	 * Execute script with "model" and "object" variables.
	 */
	public static Object executeScript(XmlObjectInfo model, String script) throws Exception {
		ClassLoader classLoader = model.getContext().getClassLoader();
		Map<String, Object> variables = Maps.newHashMap();
		variables.put("model", model);
		variables.put("object", model.getObject());
		return ScriptUtils.evaluate(classLoader, script, variables);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Calls {@link IRootProcessor} for given {@link XmlObjectInfo}.
	 */
	public static void callRootProcessors(XmlObjectInfo root) throws Exception {
		List<IRootProcessor> processors =
				ExternalFactoriesHelper.getElementsInstances(
						IRootProcessor.class,
						"org.eclipse.wb.core.xml.rootProcessors",
						"processor");
		for (IRootProcessor processor : processors) {
			processor.process(root);
		}
	}

	/**
	 * Sometimes real component is created using some wrapper, for example as {@link Viewer} creates
	 * {@link Control}. We should drop {@link Viewer} in code, but layout policies should see that we
	 * drop {@link Control}. So we should "extract" {@link Control} {@link XmlObjectInfo} from
	 * {@link Viewer} {@link XmlObjectInfo}.
	 *
	 * @return the wrapped {@link XmlObjectInfo} or original {@link XmlObjectInfo} if there are no
	 *         wrapping.
	 */
	public static XmlObjectInfo getWrapped(XmlObjectInfo original) throws Exception {
		if (original instanceof IWrapperInfo) {
			IWrapperInfo wrapperInfo = (IWrapperInfo) original;
			return wrapperInfo.getWrapped();
		}
		return original;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Model creation
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return new {@link XmlObjectInfo} for given component {@link Class} name and
	 *         {@link CreationSupport}.
	 */
	public static XmlObjectInfo createObject(EditorContext context,
			String componentClassName,
			CreationSupport creationSupport) throws Exception {
		Class<?> componentClass = context.getClassLoader().loadClass(componentClassName);
		return createObject(context, componentClass, creationSupport);
	}

	/**
	 * @return new {@link XmlObjectInfo} for given component {@link Class} and {@link CreationSupport}
	 *         .
	 */
	public static XmlObjectInfo createObject(EditorContext context,
			Class<?> componentClass,
			CreationSupport creationSupport) throws Exception {
		// prepare description
		ComponentDescription componentDescription =
				ComponentDescriptionHelper.getDescription(context, componentClass);
		// create model
		return createObject(context, componentDescription, creationSupport);
	}

	/**
	 * @return new {@link XmlObjectInfo} for given {@link ComponentDescription} and
	 *         {@link CreationSupport}.
	 */
	public static XmlObjectInfo createObject(EditorContext context,
			ComponentDescription componentDescription,
			CreationSupport creationSupport) throws Exception {
		// prepare constructor of model
		Constructor<?> modelConstructor;
		{
			Class<?> modelClass = componentDescription.getModelClass();
			modelConstructor =
					modelClass.getConstructor(new Class[]{
							EditorContext.class,
							ComponentDescription.class,
							CreationSupport.class});
		}
		// create model
		return (XmlObjectInfo) modelConstructor.newInstance(new Object[]{
				context,
				componentDescription,
				creationSupport});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates new {@link XmlObjectInfo} as first child.
	 */
	public static void addFirst(XmlObjectInfo component,
			Association association,
			XmlObjectInfo container) throws Exception {
		// add model
		container.addChildFirst(component);
		// add element
		{
			ElementTarget target = new ElementTarget(container.getCreationSupport().getElement(), 0);
			association.add(component, target);
		}
	}

	/**
	 * Creates new {@link XmlObjectInfo}.
	 */
	public static void add(XmlObjectInfo component,
			Association association,
			XmlObjectInfo container,
			XmlObjectInfo nextComponent) throws Exception {
		container.getBroadcast(XmlObjectAdd.class).before(container, component);
		// add model
		container.addChild(component, nextComponent);
		// add element
		{
			ElementTarget target = getTarget(container, nextComponent);
			association.add(component, target);
		}
		// done
		container.getBroadcast(XmlObjectAdd.class).after(container, component);
	}

	/**
	 * Move existing {@link XmlObjectInfo}.
	 */
	public static void move(XmlObjectInfo component,
			Association association,
			XmlObjectInfo container,
			XmlObjectInfo nextComponent) throws Exception {
		XmlObjectInfo oldParent = component.getParentXML();
		container.getBroadcast(XmlObjectMove.class).before(component, oldParent, container);
		// move model
		if (oldParent == container) {
			container.moveChild(component, nextComponent);
		} else {
			component.getParent().removeChild(component);
			container.addChild(component, nextComponent);
		}
		// move element
		if (association != null) {
			ElementTarget target = getTarget(container, nextComponent);
			association.move(component, target, oldParent, container);
		} else {
			Assert.isTrue(oldParent == container, "Without association only reorder is possible");
			DocumentElement containerElement = container.getElement();
			DocumentElement componentElement = getElementInParent(container, component.getElement());
			int targetIndex = getTargetIndex(containerElement, nextComponent);
			containerElement.moveChild(componentElement, targetIndex);
		}
		// end move operation
		container.getBroadcast(XmlObjectMove.class).after(component, oldParent, container);
	}

	/**
	 * @return the {@link ElementTarget} to add new {@link XmlObjectInfo} before given one.
	 */
	private static ElementTarget getTarget(XmlObjectInfo container, XmlObjectInfo nextComponent) {
		DocumentElement containerElement = container.getCreationSupport().getElement();
		int index = getTargetIndex(containerElement, nextComponent);
		return new ElementTarget(containerElement, index);
	}

	/**
	 * @return the index of <code>component</code> element in <code>container</code> element.
	 */
	private static int getTargetIndex(DocumentElement containerElement, XmlObjectInfo component) {
		List<DocumentElement> children = containerElement.getChildren();
		if (component == null) {
			return children.size();
		} else {
			DocumentElement componentElement = component.getCreationSupport().getElement();
			componentElement = containerElement.getDirectChild(componentElement);
			return children.indexOf(componentElement);
		}
	}

	/**
	 * @return the {@link DocumentElement} which is "element" or its parent and is direct child of
	 *         {@link DocumentElement} of "parent".
	 */
	public static DocumentElement getElementInParent(XmlObjectInfo parent, DocumentElement element) {
		DocumentElement parentElement = parent.getCreationSupport().getElement();
		return parentElement.getDirectChild(element);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Class
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Asks every {@link TagResolverProvider} to register {@link XmlObjectResolveTag} broadcast.
	 */
	public static void registerTagResolvers(XmlObjectInfo rootObject) throws Exception {
		List<TagResolverProvider> providers =
				ExternalFactoriesHelper.getElementsInstances(
						TagResolverProvider.class,
						"org.eclipse.wb.core.xml.tagResolverProviders",
						"provider");
		for (TagResolverProvider provider : providers) {
			provider.register(rootObject);
		}
	}

	/**
	 * @param object
	 *          some {@link XmlObjectInfo} in hierarchy.
	 * @param clazz
	 *          the {@link Class} to use for tag.
	 * @return the name of tag to use, required namespaces already added to the root
	 *         {@link DocumentElement}.
	 */
	public static String getTagForClass(XmlObjectInfo object, Class<?> clazz) throws Exception {
		// prepare namespace and tag
		String namespace;
		String tag;
		{
			String[] namespaceArray = new String[1];
			String[] tagArray = new String[1];
			object.getBroadcast(XmlObjectResolveTag.class).invoke(object, clazz, namespaceArray, tagArray);
			namespace = namespaceArray[0];
			tag = tagArray[0];
			Assert.isTrue2(namespace != null && tag != null, "Can not resolve tag for {0}", clazz);
		}
		// prepare full tag
		if (StringUtils.isEmpty(namespace)) {
			return tag;
		} else {
			return namespace + ":" + tag;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FlowContainer
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Uses first {@link FlowContainer} which accepts given component for creation.
	 */
	public static void flowContainerCreate(XmlObjectInfo container,
			XmlObjectInfo component,
			XmlObjectInfo nextComponent) throws Exception {
		List<FlowContainer> flowContainers = new FlowContainerFactory(container, false).get();
		for (FlowContainer flowContainer : flowContainers) {
			if (flowContainer.validateComponent(component)) {
				flowContainer.command_CREATE(component, nextComponent);
				ExecutionUtils.refresh(container);
				break;
			}
		}
	}

	/**
	 * Uses first {@link FlowContainer} which accepts given component for move.
	 */
	public static void flowContainerMove(XmlObjectInfo container,
			XmlObjectInfo component,
			XmlObjectInfo nextComponent) throws Exception {
		List<FlowContainer> flowContainers = new FlowContainerFactory(container, false).get();
		for (FlowContainer flowContainer : flowContainers) {
			if (flowContainer.validateComponent(component)) {
				flowContainer.command_MOVE(component, nextComponent);
				ExecutionUtils.refresh(container);
				break;
			}
		}
	}
}
