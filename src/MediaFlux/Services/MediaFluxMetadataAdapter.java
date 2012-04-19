package daris.Monbulk.MediaFlux.Services;

import java.util.ArrayList;
import java.util.List;
import arc.mf.client.xml.XmlElement;

import daris.Monbulk.shared.Services.*; 

public class MediaFluxMetadataAdapter
{
	public static Metadata createMetadata(XmlElement element) throws Exception
	{
		XmlElement type = element.element("type");
		Metadata metadata = new Metadata(type.value("@name"), type.value("description"), type.value("label"));
		parseMetadata(null, metadata.getElements(), type.elements("definition/element"));
		return metadata;
	}
	
	private static void parseMetadata(Metadata.Element parent, ArrayList<Metadata.Element> elements, List<XmlElement> xmlElements) throws Exception
	{
		if (xmlElements == null)
		{
			return;
		}

		for (XmlElement e : xmlElements)
		{
			Metadata.Element element = Metadata.createElement(e);
			if (element instanceof Metadata.DocumentElement)
			{
				Metadata.DocumentElement docElement = (Metadata.DocumentElement)element;
				parseMetadata(docElement, docElement.getElements(), e.elements("element"));
			}

			element.setParent(parent);
			elements.add(element);
		}
	}
}
