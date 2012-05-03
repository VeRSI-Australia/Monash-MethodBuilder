package monbulk.MethodBuilder.client.view;

import monbulk.shared.Architecture.ISearchController;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class SearchWidget extends Composite {

		private PushButton _btnSearch;
		private final TextBox _tbSearchText;
		private HTMLPanel _Filters;
		private HorizontalPanel _HeaderWidget;
		private final ISearchController _Controller;
		public SearchWidget(ISearchController tmpControl) {
		
			this._Controller = tmpControl;
		_btnSearch = new PushButton();
		_btnSearch.setStyleName("btnSearch");
		
		this._tbSearchText = new TextBox();
		this._tbSearchText.setStyleName("tbSearchBox");
		this._tbSearchText.addChangeHandler(new ChangeHandler()
		{

			@Override
			public void onChange(ChangeEvent event) {
				
				//Dispatch a SearchEvent
				_Controller.Filter(_tbSearchText.getValue());
				
			}
			
		});
		this._HeaderWidget.add(this._btnSearch);
		this._HeaderWidget.add(this._tbSearchText);
		
		
	}
	public Widget getHeaderWidget()
	{
		return this._HeaderWidget.asWidget();
	}
	/**
	 * Not Implemented - should return the filters widget
	 * 
	 * @return Widget 	Returns the widget for search filters
	 */
	public Widget getBodyWidget()
	{
		return null;
	}

}
