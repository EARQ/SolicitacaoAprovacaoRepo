package earq.alfresco.custom;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.quickshare.QuickShareDTO;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SendAsEmailActionExecuter extends ActionExecuterAbstractBase {
	private static Log logger = LogFactory
			.getLog(SendAsEmailActionExecuter.class);

	// Form parameters
	public static final String PARAM_EMAIL_TO_NAME = "to";
	public static final String PARAM_EMAIL_SUBJECT_NAME = "subject";
	public static final String PARAM_EMAIL_CC_NAME = "cc";
	public static final String PARAM_EMAIL_BODY_NAME = "body_text";

	private NodeService m_nodeService;
	private QuickShareService shareService;
	private QuickShareDTO quickShareDTO;

	public void setQuickShareService(QuickShareService shareService) {
		this.shareService = shareService;
	}

	private ActionService actionService;
	private ServiceRegistry serviceRegistry;

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setNodeService(NodeService nodeService) {
		m_nodeService = nodeService;
	}

	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {

		ParameterDefinitionImpl paramTo = new ParameterDefinitionImpl(
				PARAM_EMAIL_TO_NAME, DataTypeDefinition.TEXT, true,
				getParamDisplayLabel(PARAM_EMAIL_TO_NAME));

		paramList.add(paramTo);

		ParameterDefinitionImpl paramCC = new ParameterDefinitionImpl(
				PARAM_EMAIL_CC_NAME, DataTypeDefinition.TEXT, false,
				getParamDisplayLabel(PARAM_EMAIL_CC_NAME));

		paramList.add(paramCC);

		ParameterDefinitionImpl paramSubject = new ParameterDefinitionImpl(
				PARAM_EMAIL_SUBJECT_NAME, DataTypeDefinition.TEXT, true,
				getParamDisplayLabel(PARAM_EMAIL_SUBJECT_NAME));

		paramList.add(paramSubject);

		// paramList.add(new ParameterDefinitionImpl(PARAM_EMAIL_BODY_NAME,
		// DataTypeDefinition.TEXT, true,
		// getParamDisplayLabel(PARAM_EMAIL_BODY_NAME)));
	}

	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
		
		if (m_nodeService.exists(actionedUponNodeRef) == true) {
			// Get the email properties entered via Share Form
			String to = (String) action.getParameterValue(PARAM_EMAIL_TO_NAME);
			String subject = (String) action
					.getParameterValue(PARAM_EMAIL_SUBJECT_NAME);
			String cc = (String) action
					.getParameterValue(PARAM_EMAIL_CC_NAME);
			// String body = (String) action
			// .getParameterValue(PARAM_EMAIL_BODY_NAME);

			// sharefile
			quickShareDTO = shareService.shareContent(actionedUponNodeRef);

			// envia email
			Action mailAction = actionService
					.createAction(MailActionExecuter.NAME);

			mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT,
					subject);

			mailAction.setParameterValue(MailActionExecuter.PARAM_TO, to);

			mailAction.setParameterValue(MailActionExecuter.PARAM_CC, cc);

			mailAction.setParameterValue(MailActionExecuter.PARAM_FROM,
					"suporte@earqconsultoria.com");

			// TODO CRIAR ALTERNATIVA DE ENVIO SEM O TEMPLATE
			// mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT,
			// body);

			setTemplateModel(mailAction, actionedUponNodeRef);

			actionService.executeAction(mailAction, null);
		}

	}

	private void setTemplateModel(Action mailAction, NodeRef actionedUponNodeRef) {
		
		
		
		String link = "share/s/"+ quickShareDTO.getId() + "&ra="+actionedUponNodeRef.getId();
		
		String templatePATH = "PATH:\"/app:company_home/app:dictionary/app:email_templates/cm:aprovacao/cm:aprovacao-email.html.ftl\"";

		ResultSet resultSet = serviceRegistry.getSearchService().query(
				new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore"),
				SearchService.LANGUAGE_LUCENE, templatePATH);
		
		
		if (resultSet.length() == 0) {
			logger.warn("Template " + templatePATH + " not found.");

			String textoGenericoEmail = "<html><body>Ol\u00E1, <br/><br/><br/>Voc\u00EA possui um novo documento para aprova\u00E7\u00E3o. Favor acesso o link abaixo e siga as instru\u00E7\u00F5es."
					+ "<br/><br/><br/><br/><a href=\"http://www.ebravo.com.br/"+link+"\">DOCUMENTO</a>"
					+ "<br/><br/><br/><br/>Atenciosamente,"
					+ "<br/><br/><br/>eBravo</body></html>";

			mailAction.setParameterValue(MailActionExecuter.PARAM_TEXT,
					textoGenericoEmail);
			return;
		} else {
			logger.info("Encontrou template: " + templatePATH + " found.");
		}
		NodeRef template = resultSet.getNodeRef(0);
		mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE,
				template);
		// Define parameters for the model (set fields in the ftl like :
		// args.workflowTitle)
		Map<String, Serializable> templateArgs = new HashMap<String, Serializable>();
		
		
		templateArgs.put("link", link);
		Map<String, Serializable> templateModel = new HashMap<String, Serializable>();
		templateModel.put("args", (Serializable) templateArgs);
		mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL,
				(Serializable) templateModel);

	}
}