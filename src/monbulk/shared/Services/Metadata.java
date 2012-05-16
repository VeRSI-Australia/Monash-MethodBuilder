package monbulk.shared.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

import monbulk.shared.util.XmlHelper;
import arc.mf.client.xml.*;
import com.google.gwt.core.client.GWT;

public class Metadata
{
	public enum ElementTypes
	{
		// These types are unique classes.
		Document("document", true, false, false),
		Enumeration("enumeration", true, false, true),

		// These types are generic.
		String("string", true, false, true),
		Integer("integer", true, true, true),
		Date("date", true, false, true),
		Float("float", true, true, true),
		Double("double", true, true, true),
		Long("long", true, true, true),
		Boolean("boolean", true, false, false),
		
		// Special types that aren't visible or selectable by the user.
		Keyword("keyword", false, false, false),
		AssetId("asset-id", false, false, false),
		Asset("asset", false, false, false),
		CiteableId("citeable-id", false, false, false),
		EmailAddress("email-address", false, false, false),

		// Special types that aren't real MF types.
		Number("number", false, true, true),
		Attribute("attribute", false, false, false),
		All("all", false, false, false);
		
		private String m_metaName;
		private boolean m_isVisible;
		private boolean m_isNumber;
		private boolean m_useInAttributes;

		ElementTypes(String typeName, boolean isVisible, boolean isNumber, boolean useInAttributes)
		{
			m_metaName = typeName;
			m_isVisible = isVisible;
			m_isNumber = isNumber;
			m_useInAttributes = useInAttributes;
		}
		
		public String getMetaName()
		{
			return m_metaName;
		}
		
		public boolean isVisible()
		{
			return m_isVisible;
		}
		
		public boolean isNumber()
		{
			return m_isNumber;
		}
		
		public boolean isUseInAttributes()
		{
			return m_useInAttributes;
		}
		
		// Returns true if this type is the same as otherType, or if this type
		// is a number type and the other type is also a number type, and vice
		// versa.
		public boolean isSame(ElementTypes otherType)
		{
			return (this == otherType) || (isNumber() && otherType.isNumber());
		}
		
		static ElementTypes fromMetaName(String typeName) throws Exception
		{
			for (ElementTypes t : ElementTypes.values())
			{
				if (t.getMetaName().equalsIgnoreCase(typeName))
				{
					return t;
				}
			}
			
			throw new Exception("Unknown metadata type '" + typeName + "'");
		}
	}
	
	public static Metadata.Element createElement(XmlElement e) throws Exception
	{
		String typeName = e.value("@type");
		String name = e.value("@name");
		String description = e.value("description");
		Metadata.Element element = createElement(typeName, name, description, false);
		element.setFromXmlElement(e);
		return element;
	}
	
	public static Metadata.Element createElement(String typeName, String name, String description, boolean isAttribute) throws Exception
	{
		ElementTypes type = ElementTypes.fromMetaName(typeName);

		if (type == ElementTypes.Document)
		{
			return new DocumentElement(name, description);
		}
		else if (type == ElementTypes.Enumeration)
		{
			return new EnumerationElement(name, description, isAttribute);
		}
		else
		{
			return new Element(type, name, description, isAttribute);
		}
	}
	
	public static class Element
	{
		protected DocumentElement m_parent = null;
		protected ElementTypes m_type;
		protected boolean m_isAttribute = false;
		protected HashMap<String, String> m_settings = new HashMap<String, String>();
		protected HashMap<String, String> m_restrictions = new HashMap<String, String>();

		// Attributes on an element are stored as a list of elements,
		// since they are almost exactly the same.
		protected ArrayList<Element> m_attributes = new ArrayList<Element>();

		/**
		 * Copy constructor.  Creates a new element from an existing element.
		 * @param element
		 */
		public Element(Element element)
		{
			m_parent = element.m_parent;
			m_type = element.m_type;

			@SuppressWarnings("unchecked")
			HashMap<String, String> settings = (HashMap<String, String>)element.m_settings.clone();
			m_settings = settings;
			
			@SuppressWarnings("unchecked")
			HashMap<String, String> restrictions = (HashMap<String, String>)element.m_restrictions.clone();
			m_restrictions = restrictions;

			m_isAttribute = element.m_isAttribute;
			
			for (Element e : element.m_attributes)
			{
				m_attributes.add(e.clone());
			}
		}

		public Element(ElementTypes type, String name, String description, boolean isAttribute)
		{
			setSetting("name", name);
			setSetting("description", description);
			m_type = type;
			m_isAttribute = isAttribute;
		}
		
		public Element clone()
		{
			return new Element(this);
		}

		public void setRestriction(String name, String value)
		{
			if (value.length() > 0)
			{
				m_restrictions.put(name, value);
			}
			else
			{
				m_restrictions.remove(name);
			}
		}
		
		public String getRestriction(String name, String defaultValue)
		{
			String value = m_restrictions.get(name);
			if (value == null)
			{
				value = defaultValue;
			}
			
			return value;
		}

		public void setSetting(String name, String value)
		{
			if (value != null && value.length() > 0)
			{
				m_settings.put(name, value);
			}
			else
			{
				m_settings.remove(name);
			}
		}

		public String getSetting(String name, String defaultValue)
		{
			String value = m_settings.get(name);
			if (value == null)
			{
				value = defaultValue;
			}

			return value;
		}

		public void setFromXmlElement(XmlElement xmlElement)
		{
			if (xmlElement.hasAttributes())
			{
				List<XmlAttribute> attributes = xmlElement.attributes();
				for (XmlAttribute a : attributes)
				{
					m_settings.put(a.name(), a.value());
				}
			}

			if (xmlElement.hasElements())			
			{
				for (XmlElement e : xmlElement.elements())
				{
					if (e.name().equals("restriction"))
					{
						if (m_type != ElementTypes.Enumeration)
						{
							// Enumerations handle themselves.
							if (e.hasElements())
							{
								for (XmlElement r : e.elements())
								{
									m_restrictions.put(r.name(), r.value());
								}
							}
						}
					}
					else if (canHaveAttributes() && e.name().equals("attribute"))
					{
						// Process mf attributes.
						try
						{
							Element element = createElement(e);
							element.m_isAttribute = true;
							m_attributes.add(element);
						}
						catch (Exception exception)
						{
						}
					}
				}
			}
		}

		// TODO: Should parent be the owner Element for attributes?
		public DocumentElement getParent()
		{
			return m_parent;
		}
		
		public void setParent(DocumentElement element)
		{
			m_parent = element;
		}
		
		public void setDescription(String description)
		{
			setSetting("description", description);
		}
		
		public String getDescription()
		{
			return getSetting("description", "");
		}
		
		public String getName()
		{
			return getSetting("name", "");
		}

		public ElementTypes getType()
		{
			return m_type;
		}
		
		public ArrayList<Element> getAttributes()
		{
			return m_attributes;
		}
		
		public boolean canHaveAttributes()
		{
			return m_type != ElementTypes.Date && !m_isAttribute;
		}
		
		public boolean getIsAttribute()
		{
			return m_isAttribute;
		}
		
		public String toString()
		{
			String name = getSetting("name", "");
			String desc = getSetting("description", "");
			return m_type.toString() + ", " + name + ", " + desc;
		}

		/**
		 * Used by enum element type so it can add its own restriction elements.
		 * @param w
		 */
		protected void addRestrictionsXml(XmlStringWriter w)
		{
		}

		public void addXml(XmlStringWriter w)
		{
			// Make a copy of the settings.
			@SuppressWarnings("unchecked")
			HashMap<String, String> attributes = (HashMap<String, String>)m_settings.clone();
			
			// Remove the "description" entry.  All settings except the
			// description are saved as attributes on the xml element.
			String description = attributes.remove("description");

			// Add the type.
			attributes.put("type", m_type.getMetaName());

			// Add the root element.
			String root = getIsAttribute() ? "attribute" : "element";
			w.push(root, XmlHelper.getAttributesArray(attributes));
			
			if (m_restrictions != null && m_restrictions.size() > 0)
			{
				// Add all restrictions.
				w.push("restriction", new String[] { "base", m_type.getMetaName() });
				{
					Set<Entry<String, String>> r = m_restrictions.entrySet();
					for (Entry<String, String> e : r) 
					{
						String key = e.getKey();
						String value = e.getValue();

						// Ignore the dummy value.
						if (!key.equals("dummy") && !value.equals("dummy"))
						{
							w.add(key, value);
						}
					}
					addRestrictionsXml(w);
				}
				w.pop();
			}
			
			if (description != null && description.length() > 0)
			{
				// Add the description.
				w.add("description", description);
			}
			
			if (m_attributes != null && m_attributes.size() > 0)
			{
				// Add any mf attributes.
				for (Element e : m_attributes)
				{
					e.addXml(w);
					w.pop();
				}
			}
		}
	}
	
	// A document has a number of child elements.
	public static class DocumentElement extends Element
	{
		public enum ReferenceType
		{
			Document,
			Element;
			
			public static ReferenceType parse(String value) throws Exception
			{
				if (value != null)
				{
					for (ReferenceType r : ReferenceType.values())
					{
						if (r.toString().equalsIgnoreCase(value))
						{
							return r;
						}
					}
				}
				
				throw new Exception("Unknown reference type '" + value + "'");
			}
		};

		private ArrayList<Element> m_children = new ArrayList<Element>();
		private boolean m_isReference = false;
		private ReferenceType m_referenceType;
		private String m_referenceName;
		private String m_referenceValue;

		/**
		 * Copy constructor.  Creates a new DocumentElement from an existing element.
		 * @param element
		 */
		public DocumentElement(DocumentElement element)
		{
			super(element);
			
			for (Element e : element.m_children)
			{
				m_children.add(e.clone());
			}

			m_isReference = element.m_isReference;
			m_referenceType = element.m_referenceType;
			m_referenceName = element.m_referenceName;
			m_referenceValue = element.m_referenceValue;
		}
		
		public DocumentElement(String name, String description)
		{
			super(ElementTypes.Document, name, description, false);
		}
		
		public ArrayList<Element> getChildren()
		{
			return m_children;
		}
		
		public boolean canHaveAttributes()
		{
			return false;
		}

		public void setFromXmlElement(XmlElement xmlElement)
		{
			super.setFromXmlElement(xmlElement);
			
			XmlElement reference = xmlElement.element("reference");
			if (reference != null)
			{
				try
				{
					m_referenceType = ReferenceType.parse(reference.value("@type"));
					m_referenceName = reference.value("@name");
					m_referenceValue = reference.value("value");
					m_isReference = true;
				}
				catch (Exception e)
				{
					GWT.log("Bad reference: " + e.toString());
				}
			}
		}
		
		public boolean getIsReference()
		{
			return m_isReference;
		}
		
		public void setIsReference(boolean isReference)
		{
			m_isReference = isReference;
		}
		
		public String getReferenceName()
		{
			return m_referenceName;
		}
		
		public void setReferenceName(String referenceName)
		{
			m_referenceName = referenceName;
		}
		
		public ReferenceType getReferenceType()
		{
			return m_referenceType;
		}
		
		public void setReferenceType(ReferenceType referenceType)
		{
			m_referenceType = referenceType;
		}
		
		public String getReferenceValue()
		{
			return m_referenceValue;
		}
		
		public void setReferenceValue(String referenceValue)
		{
			m_referenceValue = referenceValue;
		}
		
		public Element clone()
		{
			return new DocumentElement(this);
		}
		
		public void addXml(XmlStringWriter w)
		{
			if (getIsReference())
			{
				// Add reference info.
				String[] attributes = new String[4];
				int index = 0;
				attributes[index++] = "name";
				attributes[index++] = getReferenceName();
				attributes[index++] = "type";
				attributes[index++] = getReferenceType().toString().toLowerCase();
				w.add("reference", attributes, getReferenceValue());
			}
			
			if (m_children != null && m_children.size() > 0)
			{
				// Add children.
				for (Element e : m_children)
				{
					e.addXml(w);
					w.pop();
				}
			}
		}
	}
	
	public static class EnumerationElement extends Element
	{
		private ArrayList<String> m_values = new ArrayList<String>();
		
		/**
		 * Copy constructor.  Creates a new EnumerationElement from an existing element.
		 * @param element
		 */
		public EnumerationElement(EnumerationElement element)
		{
			super(element);
			
			@SuppressWarnings("unchecked")
			ArrayList<String> values = (ArrayList<String>)element.m_values.clone();
			m_values = values;
		}

		public EnumerationElement(String name, String description, boolean isAttribute)
		{
			super(ElementTypes.Enumeration, name, description, isAttribute);

			// Dummy entry so there is always a restriction for enumerations.
			setRestriction("dummy", "dummy");
		}
		
		// Returns the values of the enum.  Note that this could
		// be empty if the values come from a dictionary.
		public ArrayList<String> getValues()
		{
			return m_values;
		}
		
		public void addValue(String value)
		{
			m_values.add(value);
		}
		
		public void removeValue(String value)
		{
			m_values.remove(value);
		}

		public void setDictionaryName(String name)
		{
			setRestriction("dictionary", name);
		}
		
		public String getDictionaryName()
		{
			return getRestriction("dictionary", "");
		}
		
		public boolean isUsingDictionary()
		{
			String dictionaryName = getDictionaryName();
			return dictionaryName != null && dictionaryName.length() > 0;
		}
		
		public void setFromXmlElement(XmlElement element)
		{
			super.setFromXmlElement(element);
			
			XmlElement restriction = element.element("restriction");
			if (restriction != null)
			{
				String dictionary = restriction.value("dictionary");
				if (dictionary != null && dictionary.length() > 0)
				{
					// TODO: Not needed.  This should be parsed by the parent
					// class when it parses all restrictions.
					setDictionaryName(dictionary);
				}
				else
				{
					List<String> values = restriction.values("value");
					m_values.addAll(values);
				}
			}
		}
		
		public boolean canHaveAttributes()
		{
			return false;
		}
		
		public Element clone()
		{
			return new EnumerationElement(this);
		}
		
		public void addRestrictionsXml(XmlStringWriter w)
		{
			if (!isUsingDictionary() && m_values != null && m_values.size() > 0)
			{
				for (String v : m_values)
				{
					w.add("value", v);
				}
			}
		}
	}
	
	private ArrayList<Element> m_elements = new ArrayList<Element>();
	private String m_name;
	private String m_description;
	private String m_label;
	
	public Metadata(String name, String description, String label)
	{
		m_name = name;
		m_description = description;
		m_label = label;
	}
	
	public void setName(String name)
	{
		m_name = name;
	}
	
	public String getName()
	{
		return m_name;
	}
	
	public void setDescription(String description)
	{
		m_description = description;
	}
	
	public String getDescription()
	{
		return m_description;
	}
	
	public void setLabel(String label)
	{
		m_label = label;
	}
	
	public String getLabel()
	{
		return m_label;
	}
	
	public ArrayList<Element> getElements()
	{
		return m_elements;
	}
	
	public String getXml()
	{
		XmlStringWriter x = new XmlStringWriter();
		x.setAddCarriageReturnAfterElement(true);
		x.add("type", m_name);

		if (m_label != null && m_label.length() > 0)
		{
			x.add("label", m_label);
		}
		
		if (m_description != null && m_description.length() > 0)
		{
			x.add("description", m_description);
		}
		
		x.push("definition");
		for (Element e : m_elements)
		{
			e.addXml(x);

			// This feels a bit messy.  Pop should probably be in addXml()
			// but derived metadata elements need to call up to the parent
			// class to add common xml/attributes, so the parent can't pop.
			x.pop(); 
		}
		x.pop();

		return x.document();
	}
}