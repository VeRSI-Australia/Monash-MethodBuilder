package monbulk.MethodBuilder.client.view;

/*Java Util Imports */
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/*GWT Miscellaneous Imports */
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.HandlerManager;

/*GWT Binder Imports */
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;

/*GWT User Imports */
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.StackLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

/*Monbulk Imports */
import monbulk.shared.Architecture.IPresenter;
import monbulk.shared.Architecture.ISearchController;
import monbulk.shared.Events.DragEvent;
import monbulk.shared.Model.IPojo;
import monbulk.shared.Model.pojo.pojoMethod;
import monbulk.shared.Model.pojo.pojoMethodComplete;
import monbulk.shared.Services.MethodService;
import monbulk.shared.Services.ServiceRegistry;
import monbulk.shared.util.MonbulkEnums;
import monbulk.shared.view.iMenuWidget;

/**
 * This Class instantiates the MethodList Service and converts the corresponding 
 * list of POJO data classes (pojoMethod) into stacks in a stack panel.
 * NB: Each Stack has a MethodMenuItem widget which allows for Editing and Cloning of Methods
 * NB2: A DragEvent is used to alert a presenter that data has been selected
 * 
 * <p>This class is a Member of the MethodBuilder package which in included in the Monbulk project
 *  </p>
 *  
 *  <pThis class implements Interfaces for iMenuWidget and MethodServiceHandler which are both
 * 	found in the Monbulk Shared package 
 * </p>
 * 
 *  @author Andrew Glenn
 *  @version 183
 *  @since 183
 *  @see MethodMenuItem
 *  @see monbulk.shared.Model.pojo.pojoMethod
 *  @see monbulk.shared.Services.MethodService
 *  @see monbulk.shared.view.iMenuWidget;
 *  @see monbulk.shared.Architecture.IPresenter
 *  @category View
 *  
 */
public class MethodList extends Composite implements iMenuWidget, MethodService.MethodServiceHandler,ISearchController {

	private static MethodListUiBinder uiBinder = GWT
			.create(MethodListUiBinder.class);

	interface MethodListUiBinder extends UiBinder<Widget, MethodList> {
	}
	
	private final HandlerManager eventBus;
	private String ActiveClassName;
	private String PassiveClassName;
	
	@UiField
	MenuBar _MethodList;
	
	@UiField
	StackLayoutPanel _MenuStack;
	
	@UiField
	PushButton _Newbutton;
	
	 
	private IPresenter _presenter;
	/**
	 * Contsructor for MethodList 
	 * @param eBus	requires the singleton eventBus from GWT.event.shared
	 * @throws ServiceNotFoundException		If the service has not been registered
	 */
	public MethodList(HandlerManager eBus) {
		initWidget(uiBinder.createAndBindUi(this));
		this.eventBus = eBus;
		
		try
		{
			MethodService service = (MethodService)ServiceRegistry.getService(MonbulkEnums.ServiceNames.Methods);
			service.getMethodList(this);
		}
		catch (ServiceRegistry.ServiceNotFoundException e)
		{
			GWT.log("Couldn't find Method service");
		}
		_Newbutton.setText("Add New Method");
	}
	/**
	 * In case you want to set the StylenNames for the menu - you can construct as follows
	 * 
	 * @param tmpEvent
	 * @param menuClassName
	 * @param activeClassName
	 * @param pClassName
	 * @deprecated We no longer use the Menu class but a stack instead
	 */
	public MethodList(HandlerManager tmpEvent, String menuClassName, String activeClassName, String pClassName)
	{
		initWidget(uiBinder.createAndBindUi(this));
		this.eventBus = tmpEvent;
		this._MethodList.setStyleName(menuClassName);
		this.ActiveClassName = activeClassName;
		this.PassiveClassName = pClassName;
		try
		{
			MethodService service = (MethodService)ServiceRegistry.getService(MonbulkEnums.ServiceNames.Methods);
			service.getMethodList(this);
		}
		catch (ServiceRegistry.ServiceNotFoundException e)
		{
			GWT.log("Couldn't find metadata service");
		}
		this._MethodList.setSize("137px", "400px");
		
		
	}
	/**
	 * If we Click "Add New" We fire a drag Event in the presenter with no attached data 
	 * 
	 * @param e If you add a new button
	 */
	@UiHandler("_Newbutton")
	void onClick(ClickEvent e) {
		this._presenter.FireDragEvent(new DragEvent("","NewMethod",0,null));
	}

	
	public void setText(String text) {
		_Newbutton.setText(text);
	}

	public String getText() {
		return _Newbutton.getText();
	}
	@Override
	public void Filter(String Name)
	{
		int count = this._MenuStack.getWidgetCount();
		int i=0;
		while(i<count)
		{
			MethodMenuItem tmpItem = (MethodMenuItem)this._MenuStack.getHeaderWidget(i);
			if(!tmpItem.getText().contains(Name))
			{
				tmpItem.setVisible(false);
				//this._MenuStack.getHeaderWidget(i).setVisible(false);
			}
			i++;
		}
	}
	/*
	 * (non-Javadoc)
	 * @see monbulk.shared.view.iMenuWidget#getBaseWidget()
	 */
	@Override
	public HasWidgets getBaseWidget() {
		// TODO Auto-generated method stub
		return this._MenuStack;
	}

	/*
	 * (non-Javadoc)
	 * @see monbulk.shared.view.iMenuWidget#setActiveMenu(java.lang.String)
	 * 
	 * 
	 */
	@Override
	public void setActiveMenu(String activeItem) {
		int count = this._MenuStack.getWidgetCount();
		int i=1;
		while(i<count)
		{
			MethodMenuItem tmpItem = (MethodMenuItem)this._MenuStack.getHeaderWidget(i);
			tmpItem.setActive(activeItem);
			i++;
		}
		
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see monbulk.shared.Services.MethodService.MethodServiceHandler#onReadMethodList(java.util.ArrayList)
	 */
	@Override
	public void onReadMethodList(ArrayList<pojoMethod> arrMethods) {
		Iterator<pojoMethod> it = arrMethods.iterator();
		SearchWidget _SearchWidget = new SearchWidget(this);
		this._MenuStack.add(null,_SearchWidget.getHeaderWidget(),30);
		int i=1;
		while(it.hasNext())
		{
			//What this should be is a 2d array with a string for the text and a string for the command to call
			//this.addItem(new MenuItem());
			pojoMethod tmpMethod = it.next();
			//MenuCommand tmpCommand = new MenuCommand("Edit:" + tmpMethod.getMethodID(), eventBus);
			//MenuItem tmpItem= new MenuItem(tmpMethod.getFieldVale(pojoMethod.MethodNameField),tmpCommand);
			
			//tmpItem.setStyleName(this.PassiveClassName);
			MethodMenuItem tmpItem = new MethodMenuItem(tmpMethod, i);
			i++;
			//this._MethodList.addItem(tmpItem);
			this._MenuStack.add(null, tmpItem, 30);
			
		}
		
	}
	@Override
	public void onReadMethod(pojoMethodComplete method) {
		return;
		
	}
	/*
	 * (non-Javadoc)
	 * @see monbulk.shared.view.iMenuWidget#setPresenter(monbulk.shared.Architecture.IPresenter)
	 */
	@Override
	public void setPresenter(IPresenter tmpPresenter) {
		this._presenter = tmpPresenter;
		
	}
	@Override
	public void populateItems(List<String> tmpArray) {
		return;
		
	}


}
