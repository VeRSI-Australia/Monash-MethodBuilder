package monbulk.MethodBuilder.client.model;

import java.util.Iterator;

import monbulk.MethodBuilder.shared.iModelAllowMetaData.iModelHasHelpExtendsMetaData;
import monbulk.shared.Architecture.IPresenter.FormPresenter;
import monbulk.shared.Architecture.iModel;
import monbulk.shared.Architecture.iModel.iFormModel;
import monbulk.shared.Form.FormBuilder;
import monbulk.shared.Form.FormField;
import monbulk.shared.Form.iFormField;
import monbulk.shared.Model.IPojo;

/*
 * Class baseModel
 * Author: Andrew Glenn
 * Date: 28/3/12
 * 
 * Purpose: All models effectively perform a round tripe of the following
 * 
 * 1. Getting any related data For a form (from a POJO)
 * 2. Saving details from a form to pojo
 * 3. Calling a Service to write and read data
 * 3. Write output in various formats
 */

public abstract class baseModel implements iFormModel{

	protected IPojo dataObject;
	protected FormBuilder formData;
	protected FormPresenter Presenter;
	public void convertPojoToForm(IPojo dataObject)
	{
		this.formData = dataObject.getFormStructure();
	}
	public void savePojo()
	{
		//this.dataObject. Should take formData and Save
	}
	public String getStringRpresentation(String Format) {
		
		return this.dataObject.writeOutput(Format);
	}
	@Override
	public String ValidateForm() {
		
		Iterator<iFormField> i = this.formData.getFormDetails().iterator();
		//Assumedly we should validate as well
		
		while(i.hasNext())
		{
			iFormField tmpItem = i.next();
			if(tmpItem.getFieldTypeName()=="String" || tmpItem.getFieldTypeName()=="Description")
			{
				if(!tmpItem.hasValue())
				{
					return "Validation Fails: No Value specified for Field:" + tmpItem.GetFieldName();
				}
			}
		}
		return "";
	}
	@Override
	public String ValidateForm(String FormName) {
		return this.ValidateForm();
	}
	@Override
	public FormBuilder getFormData() {	
		return this.formData;
	}
	public iFormField getFormItem(String FieldName)
	{
		if(this.formData != null)
		{
			iFormField thisField = this.formData.getFieldItemForName(FieldName);
			if(thisField!=null)
			{
				return thisField;
			}
		}
		return null;
	}
	
	@Override
	public void setPresenter(FormPresenter presenter) {
		// TODO Auto-generated method stub
		this.Presenter = presenter;
	}
}
