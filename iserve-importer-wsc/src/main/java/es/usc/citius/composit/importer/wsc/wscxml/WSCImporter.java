package es.usc.citius.composit.importer.wsc.wscxml;





import es.usc.citius.composit.importer.wsc.wscxml.model.XMLInstance;
import es.usc.citius.composit.importer.wsc.wscxml.model.services.XMLService;
import es.usc.citius.composit.importer.wsc.wscxml.model.services.XMLServices;
import uk.ac.open.kmi.iserve.commons.model.MessageContent;
import uk.ac.open.kmi.iserve.commons.model.Operation;
import uk.ac.open.kmi.iserve.commons.model.Service;
import uk.ac.open.kmi.iserve.sal.ServiceImporter;
import uk.ac.open.kmi.iserve.sal.exception.ImporterException;

import javax.xml.bind.JAXB;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports and transforms datasets from the Web Service Challenge 2008 (XML format)
 * User: Pablo Rodriguez Mier
 * Date: 7/18/13
 * Time: 5:04 PM
 */
public class WSCImporter implements ServiceImporter {

    public static final String DEFAULT_NAMESPACE_URI = "http://www.ws-challenge.org/WSC08Services";

    public WSCImporter(){

    }

    @Override
    public List<Service> transform(InputStream originalDescription) throws ImporterException {
        return transform(originalDescription, null);
    }

    @Override
    public List<Service> transform(InputStream originalDescription, String baseUri) throws ImporterException {
        // Deserialize
        XMLServices services = JAXB.unmarshal(originalDescription, XMLServices.class);
        // Create the services following the iserve-commons-vocabulary
        for(XMLService service : services.getServices()){
            URI srvURI = URI.create(DEFAULT_NAMESPACE_URI);
            Service modelService = new Service(srvURI);
            modelService.setSource(srvURI);
            modelService.setWsdlGrounding(srvURI);

            //modelService.setLabel();
            Operation operation = new Operation(URI.create(DEFAULT_NAMESPACE_URI + "/" + service.getName()));
            for(XMLInstance input : service.getInputs().getInstances()){
                MessageContent inputContent = new MessageContent(URI.create(DEFAULT_NAMESPACE_URI + "/" + input.getName()));
                //inputContent.
                operation.addInput(inputContent);
            }
            for(XMLInstance output : service.getOutputs().getInstances()){

            }
            modelService.addOperation(operation);
        }
        /*
        msmSvc.setSource(svcUri);
        msmSvc.setWsdlGrounding(svcUri);
        msmSvc.setLabel(qname.getLocalPart());

        // Add documentation
        addComment(wsdlSvc, msmSvc);

        addModelReferences(wsdlSvc, msmSvc);

        // Process Operations
        URI baseUri;
        uk.ac.open.kmi.iserve.commons.model.Operation msmOp;
        Port[] ports = wsdlSvc.getPorts();
        for (Port port : ports) {
            if (port.hasBinding()) {
                BindingOperation[] operations = port.findBinding().getOperations();
                for (BindingOperation operation : operations) {
                    msmOp = transform(operation,xplore  URIUtil.getNameSpace(svcUri), port.getName());
                    msmSvc.addOperation(msmOp);
                }

            }
        }*/

        return null;
    }

    @Override
    public List<Service> transform(File originalDescription) throws ImporterException {
        if (originalDescription != null && originalDescription.exists()) {
            // Open the file and transform it
            InputStream in = null;
            try {
                in = new FileInputStream(originalDescription);
                return transform(in);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new ImporterException("Unable to open input file", e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new ImporterException("Error while closing input file", e);
                    }
                }
            }
        }
        // Return an empty array if it could not be transformed.
        return new ArrayList<Service>();
    }

    @Override
    public List<Service> transform(File originalDescription, String baseUri) throws ImporterException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
