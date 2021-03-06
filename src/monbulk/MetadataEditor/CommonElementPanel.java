package monbulk.MetadataEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.dom.client.Style;

import monbulk.shared.Services.*;

public class CommonElementPanel extends ElementPanel
{
	public interface ChangeTypeHandler
	{
		public void onChangeType(Element element, String newType);
	}

	private static ElementPanelUiBinder uiBinder = GWT.create(ElementPanelUiBinder.class);

	@UiField TextBox m_name;
	@UiField ListBox m_type;
	@UiField TextBox m_description;
	@UiField TextBox m_minOccurs;
	@UiField TextBox m_maxOccurs;
	@UiField Label m_maxOccursLabel;
	@UiField Label m_minOccursLabel;
	@UiField LayoutPanel m_layout;
	@UiField HTMLPanel m_maxOccursPanel;
	@UiField Label m_typeLabel;
	@UiField HTMLPanel m_typePanel;
	CheckBox m_required = new CheckBox("Required");
	
	ChangeTypeHandler m_changeTypeHandler = null;

	interface ElementPanelUiBinder extends UiBinder<Widget, CommonElementPanel> { }

	public CommonElementPanel()
	{
		Widget w = uiBinder.createAndBindUi(this);
		initWidget(w);
	}
	
	public void setNameFocus()
	{
		m_name.setFocus(true);
		m_name.selectAll();
	}
	
	public void setMinOccursFocus()
	{
		m_minOccurs.setFocus(true);
		m_minOccurs.selectAll();
	}
	
	public void addNameKeyUpHandler(KeyUpHandler handler)
	{
		m_name.addKeyUpHandler(handler);
	}
	
	public String getName()
	{
		return m_name.getText();
	}
	
	public void setChangeTypeHandler(ChangeTypeHandler handler)
	{
		m_changeTypeHandler = handler;
	}
	
	public void update(Element element)
	{
		element.setSetting("name", m_name.getText());
		element.setDescription(m_description.getText());
		
		if (element.getIsAttribute())
		{
			// On attributes, min-occurs is either 0 or 1.
			element.setSetting("min-occurs", m_required.getValue() ? "1" : "0");
		}
		else
		{
			String minOccurs = m_minOccurs.getValue();
			element.setSetting("min-occurs", minOccurs);

			// max-occurs doesn't exist on attributes.
			String maxOccurs = m_maxOccurs.getValue();
			element.setSetting("max-occurs", maxOccurs);
		}
	}

	public void set(Element element)
	{
		super.set(element);

		// If element is an attribute is true, don't allow selection of
		// certain metadata element types that shouldn't be used in
		// attributes.
		boolean isAttribute = element.getIsAttribute();
		m_type.clear();
		for (Element.ElementTypes e : Element.ElementTypes.values())
		{
			if (e.isVisible() && (!isAttribute || e.isUseInAttributes()))
			{
				// Element type is valid, so add it to the combo.
				m_type.addItem(e.toString());
			}
		}

		// Set name and type for the element from the values we are editing.
		m_name.setText(element.getName());
		m_description.setText(element.getDescription());

		String type = element.getType().toString();
		
		for (int i = 0; i < m_type.getItemCount(); i++)
		{
			if (m_type.getItemText(i).equals(type))
			{
				m_type.setSelectedIndex(i);
				break;
			}
		}
		
		String minOccurs = element.getSetting("min-occurs", "1");

		if (element.getIsAttribute())
		{
			// HACK: Remove the max occurs panel and label, and move
			// everything else up.  This is a bit messy but this is
			// the quickest and simplest way to do it.  max-occurs
			// doesn't exist for attributes.
			// NOTE: If this same panel is re-used for an element
			// that is not an attribute, these will not be re-added!
			m_maxOccursPanel.removeFromParent();
			m_maxOccursLabel.removeFromParent();
			m_layout.setWidgetTopHeight(m_typeLabel, 92, Style.Unit.PX, 24, Style.Unit.PX);
			m_layout.setWidgetTopHeight(m_typePanel, 90, Style.Unit.PX, 22, Style.Unit.PX);
			m_layout.setHeight("126px");
			
			// Remove min-occurrences and replace with a "required"
			// checkbox because min-occurs is limited to 0..1 for
			// attributes.
			m_minOccursLabel.removeFromParent();
			m_minOccurs.removeFromParent();
			if (m_required.getParent() == null)
			{
				m_layout.add(m_required);
			}

			m_layout.setWidgetTopHeight(m_required, 62, Style.Unit.PX, 24, Style.Unit.PX);
			m_layout.setWidgetLeftWidth(m_required, 0, Style.Unit.PX, 250, Style.Unit.PX);
			
			assert(minOccurs != null);
			m_required.setValue(minOccurs.equals("1"));
		}
		else
		{
			m_minOccurs.setValue(minOccurs);

			String maxOccurs = element.getSetting("max-occurs", "");
			m_maxOccurs.setValue(maxOccurs);
		}
	}
	
	public void setReadOnly(boolean readOnly)
	{
		super.setReadOnly(readOnly);
		m_name.setEnabled(!readOnly);
		m_description.setEnabled(!readOnly);
		m_minOccurs.setEnabled(!readOnly);
		m_maxOccurs.setEnabled(!readOnly);
		m_type.setEnabled(!readOnly);
		m_required.setEnabled(!readOnly);
	}
	
	@UiHandler("m_type")
	void onTypeSelected(ChangeEvent event)
	{
		if (m_changeTypeHandler != null)
		{
			int index = m_type.getSelectedIndex();
			String newType = m_type.getItemText(index);
			m_changeTypeHandler.onChangeType(m_element, newType);
		}
	}
	
	public Element.ElementTypes getType()
	{
		return Element.ElementTypes.All;
	}
}
