package monbulk.client;

import arc.mf.client.RemoteServer;
import arc.mf.session.DefaultLoginDialog;
import arc.mf.session.ErrorDialog;
import arc.mf.session.LoginDialog;
import arc.mf.session.Session;
import arc.mf.session.SessionHandler;
import arc.gui.gwt.widget.ContainerWidget;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

import monbulk.MediaFlux.Services.MediaFluxServices;
import monbulk.client.desktop.*;
import monbulk.MetadataEditor.MetadataEditor;
import monbulk.MethodBuilder.client.MethodBuilder;

/**
 * Monbulk entry point.
 */
public class Monbulk implements EntryPoint
{
	public void onModuleLoad()
	{
		String hostName = Window.Location.getHostName();
		if (hostName.equals("127.0.0.1"))
		{
			debugLogon();
		}
		else
		{
			showLogin();
		}
	}
	
	private void showLogin()
	{
		Session.setLoginTitle("Monbulk Logon");
		LoginDialog dlg = new DefaultLoginDialog();
		dlg.setVersion(Version.VERSION);
		dlg.setTitle("Monbulk");
		Session.setLoginDialog(dlg);
		initialise();
	}
	
	private void debugLogon()
	{
		// HACK: If we're running from the localhost, connect automatically
		// to medimage so we have a live system to get data from.
		
		RemoteServer.SVC_URL = "http://localhost:81" + RemoteServer.SVC_URL;
		
		//RemoteServer.SVC_URL = "http://medimage.versi.edu.au:443" + RemoteServer.SVC_URL;
		Session.setAutoLogonCredentials("system", "manager", "change_me");
		initialise();
	}
	
	private void initialise()
	{
		Session.initialize(new SessionHandler()
		{
			@Override
			public void sessionCreated(boolean initial)
			{
				MediaFluxServices.registerMediaFluxServices();
				// HACK: Remove the arc root panel because it gets in the way
				// of our own panels.
				ContainerWidget c = arc.gui.gwt.widget.panel.RootPanel.container();
				RootPanel.get().remove(c);
				Desktop d = new Desktop(RootPanel.get());
				
				try
				{
					MetadataEditor me = new MetadataEditor();
					me.setPixelSize(1000, 678);
					d.registerWindow("MetadataEditor", "Metadata Editor", me);
					MethodBuilder mb = new MethodBuilder(d.getEventBus());
					d.registerWindow("MethodBuilder", "Method Builder", mb);
					mb.setPixelSize(1200, 800);
				}
				catch (Exception e)
				{
					String msg = e.toString();
					if (e.getCause() != null)
					{
						msg = e.getCause().toString();
					}
					
					Window.alert("Monbulk desktop: " + msg);
				}
			}

			@Override
			public void sessionExpired()
			{
			}

			@Override
			public void sessionTerminated()
			{
			}
		},
		null,
		new ErrorDialog()
		{
			public void setVersionHTML(String version)
			{
			}
			
			public void show(String context,String service,String args,int nbOutputs,Throwable se)
			{
				String msg = se.toString();
				if (se.getCause() != null)
				{
					msg = se.getCause().toString();
				}
				
				Window.alert(context + ": " + msg);
			}
		},
		null);
	}
}
