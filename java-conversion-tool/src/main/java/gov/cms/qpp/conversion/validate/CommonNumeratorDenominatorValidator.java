package gov.cms.qpp.conversion.validate;

import gov.cms.qpp.conversion.model.Node;
import gov.cms.qpp.conversion.model.TemplateId;
import gov.cms.qpp.conversion.model.ValidationError;

import java.util.List;

/**
 * Super class for AciNumeratorValidator and AciDenominatorValidator classes
 * Factored out common functionality
 */
public class CommonNumeratorDenominatorValidator extends NodeValidator {

	protected static String nodeName;
	protected static final String EMPTY_MISSING_XML =
			"ACI " + nodeName + " Node Aggregate is empty or missing";
	protected static final String INCORRECT_CHILD =
			"This " + nodeName + " Node does not have an Aggregate Count Node  \n\t%s";
	protected static final String INVALID_VALUE =
			"This ACI " + nodeName + " Node Aggregate Value has an invalid value %s  \n\t%s";
	protected static final String NO_CHILDREN =
			"This ACI " + nodeName + " Node does not have any child Nodes  \n\t%s";
	protected static final String TOO_MANY_CHILDREN =
			"This ACI " + nodeName + " Node has too many child Nodes  \n\t%s";
	protected static final String DENOMINATOR_CANNOT_BE_ZERO =
			"The ACI Denominator Aggregate Value can not be zero %s \n\t%s";

	/**
	 * internalValidateSameTemplateIdNodes allows for any cross node dependencies
	 * to be validated. AciNumerator does not have any cross node dependencies
	 * @param nodes List of Node
	 */
	@Override
	protected void internalValidateSameTemplateIdNodes(List<Node> nodes) {
		// no cross-node ACI Numerator validations required
	}

	/**
	 * internalValidateSingleNode inspects the node for certain validations.
	 * Will add validation errors if any exist
	 *
	 * @param node Node parsed xml fragment under consideration
	 */
	@Override
	protected void internalValidateSingleNode(Node node) {
		if (node == null) {
			this.addValidationError(new ValidationError(EMPTY_MISSING_XML));
			return;
		}
		List<Node> children = node.getChildNodes();

		if (children.isEmpty()) {
			this.addValidationError(new ValidationError(String.format(NO_CHILDREN, node.toString()), node.getPath()));
			return;
		}
		Node child = children.get(0);
		if (TemplateId.ACI_AGGREGATE_COUNT != child.getType()) {
			this.addValidationError(new ValidationError(String.format(INCORRECT_CHILD, node.toString()), node.getPath()));
			return;
		}
		if (children.size() > 1) {
			this.addValidationError(new ValidationError(String.format(TOO_MANY_CHILDREN, node.toString()), node.getPath()));
			return;
		}
		String value = child.getValue("aggregateCount");
		try {
			int val = Integer.parseInt(value);
			if (val < 0) {
				this.addValidationError(
						new ValidationError(String.format(INVALID_VALUE, value, node.toString()), child.getPath()));
			}
			if (AciDenominatorValidator.DENOMINATOR_NAME.equals(nodeName) && val == 0) {
				this.addValidationError(
						new ValidationError(String.format(DENOMINATOR_CANNOT_BE_ZERO, value, node.toString()), child.getPath()));
			}
		} catch (NumberFormatException nfe) {
			this.addValidationError(
					new ValidationError(String.format(INVALID_VALUE, value, node.toString())));
		}
	}
}