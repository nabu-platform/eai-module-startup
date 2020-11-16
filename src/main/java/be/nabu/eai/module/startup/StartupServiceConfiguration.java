package be.nabu.eai.module.startup;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import be.nabu.eai.repository.jaxb.ArtifactXMLAdapter;
import be.nabu.eai.repository.util.KeyValueMapAdapter;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.types.api.annotation.Field;

@XmlRootElement(name = "startupService")
@XmlType(propOrder = { "service", "asynchronous", "runPostDeployment", "properties", "runPreDeployment", "runDuringDeployment", "runAtStartup" })
public class StartupServiceConfiguration {
	
	private DefinedService service;
	private Map<String, String> properties;
	private boolean asynchronous;
	// the hooks
	private boolean runAtStartup = true, runDuringDeployment, runPostDeployment, runPreDeployment;
	
	@XmlJavaTypeAdapter(value = ArtifactXMLAdapter.class)
	public DefinedService getService() {
		return service;
	}
	public void setService(DefinedService service) {
		this.service = service;
	}
	
	@XmlJavaTypeAdapter(value = KeyValueMapAdapter.class)
	public Map<String, String> getProperties() {
		if (properties == null) {
			properties = new LinkedHashMap<String, String>();
		}
		return properties;
	}
	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	@Field(hide = "runPostDeployment = true || runPreDeployment = true || runDuringDeployment = true", comment = "By default all startup services are run synchronously. You can however run them in a separate thread if necessary (e.g. for service daemons).")
	public boolean isAsynchronous() {
		return asynchronous;
	}
	public void setAsynchronous(boolean asynchronous) {
		this.asynchronous = asynchronous;
	}
	
	// this is for example interesting to synchronize ddl _before_ you run additional dml in the deployment actions
	// most things however should only occur after the deployment is fully done
	@Field(hide = "asynchronous = true", comment = "If you enable this, the service will be run after every deployment is fully completed.")
	public boolean isRunPostDeployment() {
		return runPostDeployment;
	}
	public void setRunPostDeployment(boolean runPostDeployment) {
		this.runPostDeployment = runPostDeployment;
	}
	
	@Field(comment = "If you enable this, the service will be run every time the server starts up or is brought back online after it has been taken offline.")
	public boolean isRunAtStartup() {
		return runAtStartup;
	}
	public void setRunAtStartup(boolean runAtStartup) {
		this.runAtStartup = runAtStartup;
	}
	
	@Field(comment = "If you enable this, the service will be run after the initial deployment is done but before any deployment actions are run. This can be a good time to for instance synchronize DDLs if you are inserting brand new DMLs during the deployment actions.")
	public boolean isRunDuringDeployment() {
		return runDuringDeployment;
	}
	public void setRunDuringDeployment(boolean runDuringDeployment) {
		this.runDuringDeployment = runDuringDeployment;
	}

	@Field(comment = "If you enable this, the service will be run before a deployment starts.")
	public boolean isRunPreDeployment() {
		return runPreDeployment;
	}
	public void setRunPreDeployment(boolean runPreDeployment) {
		this.runPreDeployment = runPreDeployment;
	}

}
