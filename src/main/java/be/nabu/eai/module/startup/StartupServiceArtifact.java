package be.nabu.eai.module.startup;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.eai.repository.util.SystemPrincipal;
import be.nabu.libs.artifacts.api.StartableArtifact;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.types.api.ComplexContent;

public class StartupServiceArtifact extends JAXBArtifact<StartupServiceConfiguration> implements StartableArtifact {

	private boolean started;
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public StartupServiceArtifact(String id, ResourceContainer<?> directory, Repository repository) {
		super(id, directory, repository, "startup-service.xml", StartupServiceConfiguration.class);
	}

	@Override
	public void start() throws IOException {
		ComplexContent content = getConfiguration().getService().getServiceInterface().getInputDefinition().newInstance();
		for (String key : getConfiguration().getProperties().keySet()) {
			content.set(key, getConfiguration().getProperties().get(key));
		}
		ServiceRuntime runtime = new ServiceRuntime(getConfiguration().getService(), getRepository().newExecutionContext(SystemPrincipal.ROOT));
		try {
			runtime.run(content);
			started = true;
		}
		catch (Exception e) {
			logger.error("Could not run startup service: " + getConfiguration().getService().getId(), e);
		}
	}

	@Override
	public boolean isStarted() {
		return started;
	}

}
