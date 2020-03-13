package be.nabu.eai.module.startup;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import be.nabu.eai.repository.jaxb.ArtifactXMLAdapter;
import be.nabu.eai.repository.util.KeyValueMapAdapter;
import be.nabu.libs.services.api.DefinedService;

@XmlRootElement(name = "startupService")
@XmlType(propOrder = { "service", "asynchronous", "runPostDeploy", "properties" })
public class StartupServiceConfiguration {
	
	private DefinedService service;
	private Map<String, String> properties;
	private boolean asynchronous, runPostDeploy;
	
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
	public boolean isAsynchronous() {
		return asynchronous;
	}
	public void setAsynchronous(boolean asynchronous) {
		this.asynchronous = asynchronous;
	}
	public boolean isRunPostDeploy() {
		return runPostDeploy;
	}
	public void setRunPostDeploy(boolean runPostDeploy) {
		this.runPostDeploy = runPostDeploy;
	}
}
