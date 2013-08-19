package au.swin.ict.research.cs3.road.road4ws.core.util;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.java2wsdl.DefaultSchemaGenerator;
import org.apache.axis2.description.java2wsdl.TypeTable;
import org.apache.ws.commons.schema.XmlSchema;

import java.util.Collection;
import java.util.Map;

/**
 * Generates Schema for a Java Type
 */
public class POJO2SchemaGenerator extends DefaultSchemaGenerator {

    public POJO2SchemaGenerator(ClassLoader loader, String className,
	    String schematargetNamespace, String schematargetNamespacePrefix,
	    AxisService service) throws Exception {
	super(loader, className, schematargetNamespace,
		schematargetNamespacePrefix, service);
    }

    public Collection<XmlSchema> generateSchema() throws Exception {
	generateSchema(serviceClass);
	return schemaMap.values();
    }

    public Map<String, XmlSchema> getSchemaMap() {
	return schemaMap;
    }

    public TypeTable getTypeTable() {
	return typeTable;
    }
}
