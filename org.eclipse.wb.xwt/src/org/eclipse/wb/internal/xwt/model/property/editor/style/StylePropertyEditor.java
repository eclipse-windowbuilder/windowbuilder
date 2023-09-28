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
package org.eclipse.wb.internal.xwt.model.property.editor.style;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDisplayPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.complex.IComplexPropertyEditor;
import org.eclipse.wb.internal.core.utils.state.EditorWarning;
import org.eclipse.wb.internal.core.xml.editor.DesignContextMenuProvider;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAddProperties;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectClipboardCopy;
import org.eclipse.wb.internal.core.xml.model.clipboard.ClipboardCommand;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.property.EmptyXmlProperty;
import org.eclipse.wb.internal.core.xml.model.property.GenericProperty;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.xml.model.property.IConfigurablePropertyObject;
import org.eclipse.wb.internal.core.xml.model.property.XmlProperty;
import org.eclipse.wb.internal.core.xml.model.property.accessor.ExpressionAccessor;
import org.eclipse.wb.internal.xwt.model.property.editor.style.impl.BooleanStylePropertyImpl;
import org.eclipse.wb.internal.xwt.model.property.editor.style.impl.MacroStylePropertyImpl;
import org.eclipse.wb.internal.xwt.model.property.editor.style.impl.SelectionStylePropertyImpl;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;

import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The {@link PropertyEditor} for configure SWT styles.
 *
 * @author lobas_av
 * @coverage XWT.model.property.editor
 */
public class StylePropertyEditor extends TextDisplayPropertyEditor
implements
IConfigurablePropertyObject,
IComplexPropertyEditor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance fields
	//
	////////////////////////////////////////////////////////////////////////////
	private IStyleClassResolver m_classResolver;
	private String m_className;
	private Class<?> m_class;
	private final List<SubStylePropertyImpl> m_macroProperties = new ArrayList<>();
	private final List<SubStylePropertyImpl> m_otherProperties = new ArrayList<>();
	private SubStylePropertyImpl[] m_properties;

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getText(Property property) throws Exception {
		return "[" + getSource(property, false, ", ") + "]";
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the current style value.
	 */
	long getStyle(Property property) throws Exception {
		Number value = (Number) property.getValue();
		return value != null ? value.longValue() : 0;
	}

	/**
	 * Sets the new value of given {@link SubStyleProperty}.
	 */
	void setStyleValue(Property property, long newValue) throws Exception {
		GenericProperty genericProperty = (GenericProperty) property;
		String source = getSource(getPropertyForValue(genericProperty.getObject(), newValue));
		genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
	}

	/**
	 * @return the {@link XmlProperty} that has given value.
	 */
	private static XmlProperty getPropertyForValue(XmlObjectInfo object, final Object value) {
		return new EmptyXmlProperty(object) {
			@Override
			public Object getValue() throws Exception {
				return value;
			}
		};
	}

	/**
	 * @return the source that represents updated value of style property.
	 */
	private String getSource(Property property) throws Exception {
		return getSource(property, true, " | ");
	}

	private String getSource(Property property, boolean addClassAndDefault, String separator)
			throws Exception {
		StringBuffer source = new StringBuffer();
		long macroFlag = 0;
		// handle macro properties
		for (SubStylePropertyImpl subProperty : m_macroProperties) {
			String sFlag = subProperty.getFlagValue(property);
			if (sFlag != null) {
				// add class prefix
				if (addClassAndDefault) {
					addClassPrefix(property, source);
				}
				// add flag
				source.append(sFlag);
				macroFlag = subProperty.getFlag(sFlag);
				break;
			}
		}
		// handle other (set, select) properties
		for (SubStylePropertyImpl subProperty : m_otherProperties) {
			String sFlag = subProperty.getFlagValue(property);
			if (sFlag != null) {
				// skip current flag if it part of macro flag
				if (macroFlag != 0 && (macroFlag & subProperty.getFlag(sFlag)) != 0) {
					continue;
				}
				// add separator if need
				if (source.length() != 0) {
					source.append(separator);
				}
				// add class prefix
				if (addClassAndDefault) {
					addClassPrefix(property, source);
				}
				// add flag
				source.append(sFlag);
			}
		}
		// use null (default), if no other flags
		if (addClassAndDefault && source.length() == 0) {
			return null;
		}
		return source.toString();
	}

	private void addClassPrefix(Property property, StringBuffer source) {
		String prefix = m_classResolver.resolve(property, m_className);
		if (!StringUtils.isEmpty(prefix)) {
			source.append(prefix);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	////////////////////////////////////////////////////////////////////////////
	public void setClassResolver(IStyleClassResolver classResolver) {
		m_classResolver = classResolver;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IConfigurablePropertyObject
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure(EditorContext context, Map<String, Object> parameters) throws Exception {
		// prepare class
		m_className = (String) parameters.get("class");
		m_class = context.getClassLoader().loadClass(m_className);
		// prepare sub properties
		List<SubStylePropertyImpl> properties = new ArrayList<>();
		configureSet(properties, context, parameters);
		configureMacro(properties, context, parameters);
		configureSelect(properties, context, parameters);
		m_properties = properties.toArray(new SubStylePropertyImpl[properties.size()]);
	}

	private void configureSet(List<SubStylePropertyImpl> properties,
			EditorContext context,
			Map<String, Object> parameters) throws Exception {
		if (parameters.containsKey("set")) {
			String[] setters = StringUtils.split((String) parameters.get("set"));
			// loop of all set's
			for (int i = 0; i < setters.length; i++) {
				// prepare flag name
				String[] names = StringUtils.split(setters[i], ':');
				String flagName = names[0];
				// prepare flag value
				Field field = getField(context, m_class, flagName);
				if (field == null) {
					continue;
				}
				long flag = field.getLong(null);
				// add property
				SubStylePropertyImpl property;
				if (names.length == 2) {
					property = new BooleanStylePropertyImpl(this, names[1], flagName, flag);
				} else {
					property = new BooleanStylePropertyImpl(this, flagName.toLowerCase(), flagName, flag);
				}
				properties.add(property);
				m_otherProperties.add(property);
			}
		}
	}

	private void configureMacro(List<SubStylePropertyImpl> properties,
			EditorContext context,
			Map<String, Object> parameters) throws Exception {
		int macroIndex = 0;
		while (true) {
			// prepare "macro" key
			String key = "macro" + Integer.toString(macroIndex++);
			if (!parameters.containsKey(key)) {
				break;
			}
			// prepare all part's
			String[] values = StringUtils.split((String) parameters.get(key));
			// title
			String title = values[0];
			// prepare flag string values
			int flagCount = 0;
			String[] flagValues = new String[values.length - 1];
			for (int i = 0; i < flagValues.length; i++) {
				String flag = values[i + 1];
				if (getField(context, m_class, flag) == null) {
					continue;
				}
				flagValues[flagCount++] = flag;
			}
			// flag values
			long[] flags = new long[flagCount];
			String[] sFlags = new String[flagCount + 1];
			sFlags[flagCount] = "";
			for (int i = 0; i < flagCount; i++) {
				String flag = flagValues[i];
				flags[i] = m_class.getField(flag).getLong(null);
				sFlags[i] = flag;
			}
			// add property
			SubStylePropertyImpl property = new MacroStylePropertyImpl(this, title, flags, sFlags);
			properties.add(property);
			m_macroProperties.add(property);
		}
	}

	private void configureSelect(List<SubStylePropertyImpl> properties,
			EditorContext context,
			Map<String, Object> parameters) throws Exception {
		int selectIndex = 0;
		while (true) {
			// prepare "select" key
			String key = "select" + Integer.toString(selectIndex++);
			if (!parameters.containsKey(key)) {
				break;
			}
			// prepare all part's
			String[] values = StringUtils.split((String) parameters.get(key));
			// title
			String title = values[0];
			// default value
			String defaultString = values[1];
			long defaultFlag;
			if (StringUtils.isNumeric(defaultString)) {
				defaultFlag = Long.parseLong(defaultString);
			} else {
				defaultFlag = m_class.getField(defaultString).getLong(null);
			}
			// prepare flag string values
			int flagCount = 0;
			String[] flagValues = new String[values.length - 2];
			for (int i = 0; i < flagValues.length; i++) {
				String flag = values[i + 2];
				if (!StringUtils.isNumeric(flag) && getField(context, m_class, flag) == null) {
					continue;
				}
				flagValues[flagCount++] = flag;
			}
			// flag values
			long[] flags = new long[flagCount];
			String[] sFlags = new String[flagCount];
			for (int i = 0; i < flagCount; i++) {
				String flag = flagValues[i];
				if (StringUtils.isNumeric(flag)) {
					flags[i] = Long.parseLong(flag);
				} else {
					flags[i] = m_class.getField(flag).getLong(null);
				}
				sFlags[i] = flag;
			}
			// add property
			SubStylePropertyImpl property =
					new SelectionStylePropertyImpl(this, title, flags, sFlags, defaultFlag);
			properties.add(property);
			m_otherProperties.add(property);
		}
	}

	private static Field getField(EditorContext context, Class<?> baseClass, String name) {
		try {
			return baseClass.getField(name);
		} catch (NoSuchFieldException e) {
			context.addWarning(new EditorWarning("StylePropertyEditor: can not find field "
					+ baseClass.getName()
					+ "."
					+ name, e));
			return null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Property[] getProperties(Property mainProperty) throws Exception {
		GenericProperty genericProperty = (GenericProperty) mainProperty;
		XmlObjectInfo xmlObject = genericProperty.getObject();
		Property[] properties = (Property[]) xmlObject.getArbitraryValue(this);
		if (properties == null) {
			int length = m_properties.length;
			properties = new Property[length];
			for (int i = 0; i < length; i++) {
				properties[i] = new SubStyleProperty(mainProperty, m_properties[i]);
			}
			xmlObject.putArbitraryValue(this, properties);
		}
		return properties;
	}

	/**
	 * Contributes actions into {@link Property} context menu.
	 */
	public void contributeActions(Property mainProperty,
			IMenuManager manager,
			String implementTitle,
			boolean isCascade) throws Exception {
		// prepare "implement" menu
		IMenuManager implementMenuManager = new MenuManager(implementTitle);
		if (isCascade) {
			// add all "boolean" properties
			for (SubStylePropertyImpl property : m_properties) {
				if (property instanceof BooleanStylePropertyImpl) {
					property.contributeActions(mainProperty, implementMenuManager);
				}
			}
			//
			implementMenuManager.add(new Separator());
			// add other properties
			for (SubStylePropertyImpl property : m_properties) {
				if (!(property instanceof BooleanStylePropertyImpl)) {
					IMenuManager subMenu = new MenuManager(property.getTitle());
					property.contributeActions(mainProperty, subMenu);
					implementMenuManager.add(subMenu);
				}
			}
		} else {
			for (SubStylePropertyImpl property : m_properties) {
				property.contributeActions(mainProperty, implementMenuManager);
			}
		}
		// add "implement" menu
		manager.appendToGroup(DesignContextMenuProvider.GROUP_LAYOUT, implementMenuManager);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds "Style" property for given {@link Object_Info}.
	 */
	public static void addStyleProperty(final XmlObjectInfo widget) {
		if (!(widget.getCreationSupport() instanceof ElementCreationSupport)) {
			return;
		}
		addClipboardSupport(widget);
		widget.addBroadcastListener(new XmlObjectAddProperties() {
			private boolean m_propertyReady;
			private Property m_property;

			@Override
			public void invoke(XmlObjectInfo object, List<Property> properties) throws Exception {
				if (object == widget) {
					addStyleProperty(object, properties);
				}
			}

			private void addStyleProperty(XmlObjectInfo object, List<Property> properties) {
				if (!m_propertyReady) {
					prepareProperty(object);
					m_propertyReady = true;
				}
				if (m_property != null) {
					properties.add(m_property);
				}
			}

			private void prepareProperty(XmlObjectInfo object) {
				StylePropertyEditor styleEditor =
						(StylePropertyEditor) object.getDescription().getArbitraryValue(
								StylePropertyEditor.class);
				if (styleEditor != null) {
					styleEditor.setClassResolver(XwtStyleClassResolver.INSTANCE);
					GenericPropertyDescription propertyDescription =
							new GenericPropertyDescription("style", "Style", int.class, new ExpressionAccessor(
									"x:Style") {
								@Override
								public Object getDefaultValue(XmlObjectInfo object) throws Exception {
									return SWT.NONE;
								}
							});
					propertyDescription.setEditor(styleEditor);
					m_property = new GenericPropertyImpl(object, propertyDescription);
					m_property.setCategory(PropertyCategory.system(4));
				}
			}
		});
	}

	/**
	 * Remembers and applies value of "Style" property.
	 */
	private static void addClipboardSupport(final XmlObjectInfo widget) {
		widget.addBroadcastListener(new XmlObjectClipboardCopy() {
			@Override
			public void invoke(XmlObjectInfo object, List<ClipboardCommand> commands) throws Exception {
				if (object == widget) {
					Property property = object.getPropertyByTitle("Style");
					if (property != null && property.isModified()) {
						final long style = ((StylePropertyEditor) property.getEditor()).getStyle(property);
						commands.add(new ClipboardCommand() {
							private static final long serialVersionUID = 0L;

							@Override
							public void execute(XmlObjectInfo object) throws Exception {
								Property property = object.getPropertyByTitle("Style");
								((StylePropertyEditor) property.getEditor()).setStyleValue(property, style);
							}
						});
					}
				}
			}
		});
	}
}