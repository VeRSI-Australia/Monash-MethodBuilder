package monbulk.MetadataEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import monbulk.shared.Services.*;
import monbulk.shared.widgets.TextBoxEx;

public class NumberElementPanel extends ElementPanel
{
	private static FloatDoubleElementPanelUiBinder uiBinder = GWT.create(FloatDoubleElementPanelUiBinder.class);
	interface FloatDoubleElementPanelUiBinder extends UiBinder<Widget, NumberElementPanel> { }

	@UiField TextBoxEx m_minimum;
	@UiField TextBoxEx m_maximum;

	public NumberElementPanel()
	{
		initWidget(uiBinder.createAndBindUi(this));
	}

	public void update(Element element)
	{
		String min = m_minimum.getText();
		element.setRestriction("minimum", min);
		String max = m_maximum.getText();
		element.setRestriction("maximum", max);
	}
	
	public void set(Element element)
	{
		Element.ElementTypes t = element.getType();
		boolean floatType = (t == Element.ElementTypes.Float);
		m_minimum.setAllowDecimalPoint(floatType);
		m_maximum.setAllowDecimalPoint(floatType);

		String min = element.getRestriction("minimum", "");
		m_minimum.setText(min);
		String max = element.getRestriction("maximum", "");
		m_maximum.setText(max);
	}
	
	public Element.ElementTypes getType()
	{
		return Element.ElementTypes.Number;
	}
	
	public void setReadOnly(boolean readOnly)
	{
		super.setReadOnly(readOnly);
		m_minimum.setEnabled(!readOnly);
		m_maximum.setEnabled(!readOnly);
	}
}
