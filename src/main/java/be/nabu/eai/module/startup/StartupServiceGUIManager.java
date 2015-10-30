package be.nabu.eai.module.startup;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseJAXBGUIManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.Element;

public class StartupServiceGUIManager extends BaseJAXBGUIManager<StartupServiceConfiguration, StartupServiceArtifact> {


	public StartupServiceGUIManager() {
		super("Startup Service", StartupServiceArtifact.class, new StartupServiceManager(), StartupServiceConfiguration.class);
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return null;
	}

	public <V> void setValue(StartupServiceArtifact instance, Property<V> property, V value) {
		if ("service".equals(property.getName())) {
			Map<String, String> properties = getConfiguration(instance).getProperties();
			if (properties == null) {
				properties = new LinkedHashMap<String, String>();
			}
			else {
				properties.clear();
			}
			if (value != null) {
				DefinedService service = (DefinedService) value;
				for (Element<?> element : TypeUtils.getAllChildren(service.getServiceInterface().getInputDefinition())) {
					properties.put(element.getName(), properties.get(element.getName()));
				}
			}
			getConfiguration(instance).setProperties(properties);
		}
		if (!"properties".equals(property.getName())) {
			super.setValue(instance, property, value);
		}
	}

	@Override
	protected StartupServiceArtifact newInstance(MainController controller, RepositoryEntry entry, Value<?>... values) throws IOException {
		return new StartupServiceArtifact(entry.getId(), entry.getContainer(), entry.getRepository());
	}

}
