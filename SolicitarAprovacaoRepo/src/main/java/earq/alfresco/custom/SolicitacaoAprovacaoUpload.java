package earq.alfresco.custom;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.tenant.MultiTServiceImpl;
import org.alfresco.repo.tenant.TenantContextHolder;
import org.alfresco.repo.tenant.TenantDomainMismatchException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.quickshare.InvalidSharedIdException;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.log4j.Logger;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.ScriptContent;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.servlet.FormData;
import org.springframework.extensions.webscripts.servlet.FormData.FormField;

public class SolicitacaoAprovacaoUpload extends DeclarativeWebScript {

	private Repository repository;
	private QuickShareService shareService;
	private ServiceRegistry registry;
	private static Logger logger = Logger
			.getLogger(SolicitacaoAprovacaoUpload.class);
	private static final String USER_PASS = "EARQ123!@#102225";

	public void setServiceRegistry(ServiceRegistry registry) {
		this.registry = registry;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setQuickShareService(QuickShareService shareService) {
		this.shareService = shareService;
	}

	@Override
	protected void executeScript(ScriptContent location,
			Map<String, Object> model) {

		super.executeScript(location, model);
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req,
			Status status, Cache cache) {

//		registry.getAuthenticationService().clearCurrentSecurityContext();
//		registry.getAuthenticationService().authenticate(
//				"earq_admin_aprovacao", "earq_admin_aprovacao".toCharArray());

//		registry.getAuthenticationService().
		
		String sharedID = null;
		InputStream inputStream = null;
		String mimetype = null;
		String uploadfilename = null;
		Map<String, Object> model = new HashMap<String, Object>();

		FormData form = (FormData) req.parseContent();

		for (FormField formField : form.getFields()) {

			if (formField.getIsFile()) {
				inputStream = formField.getInputStream();
				mimetype = formField.getMimetype();
				uploadfilename = formField.getFilename();
				logger.info("mimetype = " + mimetype);
				logger.info("isFile = " + uploadfilename);
			} else if ("sharedID".equals(formField.getName())) {
				sharedID = (String) formField.getValue();
			}
		}

		// synchronized (this) {

		// TransactionService txService = registry.getTransactionService();
		// UserTransaction tx = txService.getUserTransaction();
		//
		// try {
		// tx.begin();
		logger.info("sharedID = " + sharedID);

		Map<String, Object> map = null;
		
		
		
		try {
			String tenantNode = shareService.getTenantNodeRefFromSharedId(sharedID).getFirst();
			
			
			registry.getAuthenticationService().clearCurrentSecurityContext();
			
			
			//valida se o arquivo é de tenant ou root
			if(tenantNode!=null && !"".equals(tenantNode)){
				registry.getAuthenticationService().authenticate(
						"user@"+tenantNode, SolicitacaoAprovacaoUpload.USER_PASS.toCharArray());
			}
			else{
				registry.getAuthenticationService().authenticate(
						"user", SolicitacaoAprovacaoUpload.USER_PASS.toCharArray());
			}
			
//			TenantContextHolder.clearTenantDomain();
			TenantContextHolder.setTenantDomain(tenantNode);
//			new MultiTServiceImpl().checkDomain(name);
			
			logger.info("tenantNode = " + tenantNode);
			map = shareService.getMetaData(sharedID);

		} catch (InvalidSharedIdException | TenantDomainMismatchException e) {
			//e.printStackTrace();
				
			logger.info("Não foi possível buscar os dados do ShareID (" + sharedID +"). Mensagem:"+e.getMessage());
			
			String message = "Ops!";
			String message2 = " O arquivo n\u00E3o est\u00E1 mais dispon\u00EDvel para visualiza\u00e7\u00e3o ou foi descompartilhado.";
			String message3 = "Obrigado.";

			model.put("message", message);
			model.put("message2", message2);
			model.put("message3", message3);
			return model;

		}

		logger.info("map = " + map);

		Pair<String, NodeRef> tenantPair = shareService
				.getTenantNodeRefFromSharedId(sharedID);
		String tenant = tenantPair.getFirst();
		NodeRef nodeRef = tenantPair.getSecond();
		String originalFileName = (String) registry.getNodeService()
				.getProperty(nodeRef, ContentModel.PROP_NAME);

		logger.info("Tenant = " + tenant);
		logger.info("nodeRef = " + nodeRef);
		
		
		//Força a utilização do tenant
	
	
//		System.out.println(TenantUtil.getTenantDomain();
		
//		TenantUtil.runAsTenant(runAsWork, tenantDomain)
		

		NodeRef folder = registry.getNodeService().getPrimaryParent(nodeRef)
				.getParentRef();

		logger.info("folder = "
				+ registry.getNodeService().getProperty(folder,
						ContentModel.PROP_NAME));
		logger.info("type = " + registry.getNodeService().getType(nodeRef));

		String newFileName = getNewFileName(uploadfilename, originalFileName,
				folder);

		Map<QName, Serializable> props = new HashMap<QName, Serializable>(1);
		props.put(ContentModel.PROP_NAME, newFileName);
		System.out.println(newFileName);
		// FileInfo fileInfo =
		// registry.getFileFolderService().create(folder,
		// "TesteSASA.pdg", ContentModel.TYPE_CONTENT);

		// NodeRef newNode = registry
		// .getNodeService()
		// .createNode(
		// folder,
		// ContentModel.ASSOC_CONTAINS,
		// QName.createQName(
		// NamespaceService.CONTENT_MODEL_1_0_URI,
		// newFileName), ContentModel.TYPE_CONTENT,
		// props).getChildRef();

		NodeRef newNode = registry.getCopyService().copyAndRename(
				nodeRef,
				folder,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
						newFileName), false);

		registry.getNodeService().setProperties(newNode, props);
		// registry.getCopyService().

		ContentWriter writer = registry.getContentService().getWriter(newNode,
				ContentModel.PROP_CONTENT, true);
		// writer.setEncoding("UTF-8");
		writer.setMimetype(mimetype);
		// File
		// origFile=AbstractContentTransformerTest.loadQuickTestFile(ext);

		writer.putContent(inputStream);

		shareService.unshareContent(sharedID);

		// }

		String message = "Arquivo importado com sucesso.";
		String message2 = "Por quest\u00F5es de seguran\u00E7a, o arquivo enviado n\u00E3o est\u00E1 dispon\u00EDvel para visualiza\u00e7\u00e3o.";
		String message3 = "Obrigado.";

		model.put("message", message);
		model.put("message2", message2);
		model.put("message3", message3);

		return model;

	}

	private String getNewFileName(String uploadfilename,
			String originalFileName, NodeRef folder) {
		String complemento = " - Aprovado cliente";
		String newFileName = "";
		String extensao = originalFileName.substring(originalFileName
				.lastIndexOf("."));
		String arquivoSemExtensao = originalFileName.substring(0,
				originalFileName.lastIndexOf("."));

		logger.info("arquivoSemExtensao = " + arquivoSemExtensao);

		int fileCounter = 1;

		NodeRef novoArquivo = registry.getFileFolderService().searchSimple(
				folder, arquivoSemExtensao + complemento + extensao);

		if (novoArquivo == null) {
			logger.info("arquivo não existe");
			newFileName = arquivoSemExtensao + complemento + extensao;
		} else {
			fileCounter++;

			logger.info("arquivo existe");

			for (; fileCounter < 30; fileCounter++) {

				newFileName = arquivoSemExtensao + complemento + " "
						+ fileCounter + extensao;

				logger.info("Buscando por:" + newFileName);

				NodeRef tempFile = registry.getFileFolderService()
						.searchSimple(folder, newFileName);

				logger.info(tempFile);
				if (tempFile == null) {
					break;
				}

			}
		}

		return newFileName;

	}
}
