package org.everit.osgi.cache.infinispan.tests;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

@Component(immediate = true)
@Service(value = ConfigurationInitComponent.class)
public class ConfigurationInitComponent {

    @Reference(bind = "bindConfigAdmin")
    private ConfigurationAdmin configAdmin;

    @Activate
    public void activate(final BundleContext bundleContext) {
        try {
            Dictionary<String, Object> props = new Hashtable<String, Object>();
            props.put("clusterName", "testcluster");
            props.put("multicastAddress", "228.8.8.8");
            props.put("multicastPort", "7600");
            getOrCreateConfiguration("org.everit.osgi.cache.infinispan.component.CacheFactoryComponent",
                    props);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public void bindConfigAdmin(final ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    private String getOrCreateConfiguration(final String factoryPid, final Dictionary<String, Object> props)
            throws IOException,
            InvalidSyntaxException {
        Configuration[] configurations = configAdmin.listConfigurations("(service.factoryPid=" + factoryPid + ")");
        if ((configurations != null) && (configurations.length > 0)) {
            return configurations[0].getPid();
        }
        Configuration configuration = configAdmin.createFactoryConfiguration(factoryPid, null);
        configuration.update(props);
        return configuration.getPid();
    }
}
