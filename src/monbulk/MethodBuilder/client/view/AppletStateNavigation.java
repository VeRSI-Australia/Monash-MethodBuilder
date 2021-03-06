package monbulk.MethodBuilder.client.view;

import java.util.ArrayList;



import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;

import monbulk.client.Monbulk;
import monbulk.client.Roles;
import monbulk.shared.Architecture.IPresenter;
import monbulk.shared.Architecture.IPresenter.FormPresenter;
import monbulk.shared.Architecture.IView;
import monbulk.shared.Services.User;


public class AppletStateNavigation extends Composite implements IView {

	private static AppletStateNavigationUiBinder uiBinder = GWT
			.create(AppletStateNavigationUiBinder.class);

	interface AppletStateNavigationUiBinder extends
			UiBinder<Widget, AppletStateNavigation> {
	}
	FormPresenter Presenter;
	
	@UiField PushButton btnSave;
	@UiField PushButton btnPublish;
	@UiField PushButton btnCancel;
	@UiField PushButton btnClone;
	@UiField PushButton btnDelete;
	@UiField Label lblInUse;
	
	public AppletStateNavigation() {
		initWidget(uiBinder.createAndBindUi(this));
		User _user = Monbulk.getUser();
		this.btnCancel.setVisible(false);
		if(_user.hasRole(Roles.MethodBuilder.READONLY))
		{
			
			this.btnSave.setVisible(false);
			this.btnClone.setVisible(false);
			this.btnDelete.setVisible(false);
			lblInUse.setVisible(false);
		}
		else if(_user.hasRole(Roles.MethodBuilder.CREATE))
		{
			this.btnClone.setVisible(true);
			this.btnSave.setVisible(false);
			this.btnDelete.setVisible(false);
			lblInUse.setVisible(false);
		}
		else
		{
			this.btnSave.setVisible(true);
			this.btnClone.setVisible(true);
			this.btnDelete.setVisible(true);
			lblInUse.setVisible(false);
		}
	
	}

	@Override
	public void setPresenter(IPresenter presenter) {
		this.Presenter = (FormPresenter)presenter;
		
	}

	@Override
	public void setData(ArrayList<String> someList) {
		if(someList.size()>0)
		{
			if(someList.get(0)=="InUse")
			{
				this.btnSave.setEnabled(false);
				
				this.btnDelete.setEnabled(false);
				lblInUse.setVisible(true);
				lblInUse.setText("Method is already in use.");
			}
			else
			{
				lblInUse.setVisible(false);
			}
			if(someList.get(0)=="Loaded")
			{
				this.btnClone.setEnabled(true);
				this.btnDelete.setEnabled(true);
			}
			if(someList.get(0)=="New")
			{
				this.btnClone.setEnabled(false);
				this.btnDelete.setEnabled(false);
			}
			
		}
		
	}
	public void setStates(String Role)
	{
		
	}
	@UiHandler("btnSave")
	public void onClick(ClickEvent e)
	{
		this.Presenter.FormComplete("Navigation", "Save");
	}
	@UiHandler("btnCancel")
	public void onClick1(ClickEvent e)
	{
		this.Presenter.FormComplete("Navigation", "Cancel");
	}
	@UiHandler("btnPublish")
	public void onClick2(ClickEvent e)
	{
		this.Presenter.FormComplete("Navigation", "Publish");
	}
	@UiHandler("btnClone")
	public void onClick3(ClickEvent e)
	{
		this.Presenter.FormComplete("Navigation", "Clone");
	}
	@UiHandler("btnDelete")
	public void onClick4(ClickEvent e)
	{
		this.Presenter.FormComplete("Navigation", "DeleteMethod");
	}
	
	

}
