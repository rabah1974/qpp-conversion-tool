package gov.cms.qpp.conversion.decode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.jdom2.Element;

import gov.cms.qpp.conversion.model.Node;
import gov.cms.qpp.conversion.model.Registry;
import gov.cms.qpp.conversion.model.XmlDecoder;

/**
 * Top level Decoder for parsing into QPP format. Contains a map of child Decoders
 * that can Decode an element.
 */
public class QppXmlDecoder extends XmlInputDecoder {
 	
	protected static Registry<String, QppXmlDecoder> decoders = new Registry<>(XmlDecoder.class);

	public QppXmlDecoder() {}

	/**
	 * Iterates over the element to find all child elements. Finds any elements
	 * that match a templateId in the Decoder registry. If there are any matches,
	 * calls internalDecode with that Element on the Decoder class. Aggregates Nodes
	 * that are returned.
	 * 
	 */
	@Override
	protected Node decode(Element element, Node parentNode) {

		Node returnNode = parentNode;
		
		if (null == element) {
			return returnNode;
		}
		
		setNamespace(element, this);
		
		List<Element> childElements = element.getChildren();

		for (Element ele : childElements) {

			List<Element> chchildElements = ele.getChildren();

			for (Element eleele : chchildElements) {
				if ("templateId".equals(eleele.getName())) {
					String templateId = eleele.getAttributeValue("root");

					QppXmlDecoder childDecoder = decoders.get(templateId);

					if (null != childDecoder) {
						Node thisNode = new Node();
						thisNode.setId(templateId);
						
						setNamespace(eleele, childDecoder);
						
						Node childNodeValue = childDecoder.internalDecode(ele, thisNode);
	
						if (null != childNodeValue) {
							parentNode.addChildNode(childNodeValue);
	
							// recursively call decode(element, node) with this
							// child as parent
							decode(ele, childNodeValue);
						} else {
							// recursively call decode(element, node) with a
							// placeholder node as parent
	
							Node placeholderNode = new Node();
							placeholderNode.setId("placeholder");
	
							decode(ele, placeholderNode);
	
						}
					}
				}
			}
		}

		return returnNode;
	}

	@Override
	protected Node internalDecode(Element element, Node thisnode) {
		// this is the top level, so just return null
		return null;
	}
	
	@Override
	public Iterator<String> iterator() {
		List<String> validationMsgs = new ArrayList<>();
		
		for (Entry<String, List<String>> templateEntry : validations.entrySet()) {
			for (String msg : templateEntry.getValue()) {
				
				validationMsgs.add(templateEntry.getKey() + " - " + msg);
			}
		}
		
		return validationMsgs.iterator();
	}

	@Override
	public List<String> getValidations(String templateId) {
		return validations.get(templateId);
	}

	@Override
	public void addValidation(String templateId, String validation) {
		List<String> validationList = getValidations(templateId);
		
		if (null != validationList) {
			validations.put(templateId, validationList);
		}
		
		validationList.add(validation);
		
	}

	
}
