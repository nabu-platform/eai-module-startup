package be.nabu.eai.module.startup;

import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.managers.base.JAXBArtifactManager;
import be.nabu.libs.resources.api.ResourceContainer;

public class StartupServiceManager extends JAXBArtifactManager<StartupServiceConfiguration, StartupServiceArtifact> {

	public StartupServiceManager() {
		super(StartupServiceArtifact.class);
	}

	@Override
	protected StartupServiceArtifact newInstance(String id, ResourceContainer<?> container, Repository repository) {
		return new StartupServiceArtifact(id, container, repository);
	}

}
