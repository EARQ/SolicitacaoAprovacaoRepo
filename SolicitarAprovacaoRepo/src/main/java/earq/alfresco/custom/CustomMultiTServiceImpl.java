package earq.alfresco.custom;

import net.sf.acegisecurity.Authentication;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.MultiTServiceImpl;
import org.alfresco.repo.tenant.TenantDomainMismatchException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.util.ParameterCheck;

/*
 * MT Service implementation
 *
 * Adapts names to be tenant specific or vice-versa, if MT is enabled (otherwise NOOP).
 *
 * author janv
 * since 3.0
 */
public class CustomMultiTServiceImpl extends MultiTServiceImpl {

	
    @Override
    public void checkDomainUser(String username)
    {
        ParameterCheck.mandatory("Username", username);

        if("earq_admin_aprovacao".equals(username)){
        	return;
        }
        
        String tenantDomain = getCurrentUserDomain();

        if (!tenantDomain.equals(DEFAULT_DOMAIN))
        {
            int idx2 = username.lastIndexOf(SEPARATOR);
            if ((idx2 > 0) && (idx2 < (username.length() - 1)))
            {
                String tenantUserDomain = username.substring(idx2 + 1);

                if ((tenantUserDomain == null) || (!tenantDomain.equalsIgnoreCase(tenantUserDomain)))
                {
                    throw new TenantDomainMismatchException(tenantDomain, tenantUserDomain);
                }
            }
            else
            {
                throw new TenantDomainMismatchException(tenantDomain, null);
            }
        }
    }

//    @Override
//    public StoreRef getBaseName(StoreRef storeRef)
//    {
//        if (storeRef == null)
//        {
//            return null;
//        }
//        
//		Authentication auth = AuthenticationUtil
//				.getRunAsAuthentication();
//		boolean noValid = false;
//
//		if (auth != null) {
//			String nameUser = AuthenticationUtil
//					.getRunAsAuthentication().getName();
//			if (nameUser != null) {
//				noValid = nameUser.indexOf("earq_admin_aprovacao") != -1;
//				if (noValid) {
//					return new StoreRef(storeRef.getProtocol(), "@clubeturismocampinas.com.br@SpacesStore");
//				}
//			}
//		}
//
//        return new StoreRef(storeRef.getProtocol(), getBaseName(storeRef.getIdentifier()));
//    }
////
////	
//    @Override
//    public NodeRef getName(NodeRef nodeRef)
//    {
//        if (nodeRef == null)
//        {
//            return null;
//        }
//        
//        
//		Authentication auth = AuthenticationUtil
//				.getRunAsAuthentication();
//		boolean noValid = false;
//
//		if (auth != null) {
//			String nameUser = AuthenticationUtil
//					.getRunAsAuthentication().getName();
//			if (nameUser != null) {
//				noValid = nameUser.indexOf("earq_admin_aprovacao") != -1;
//				if (noValid) {
//					  return new NodeRef(nodeRef.getStoreRef().getProtocol(), "@clubeturismocampinas.com.br@SpacesStore", nodeRef.getId());
//				}
//			}
//		}
//
//        return new NodeRef(nodeRef.getStoreRef().getProtocol(), getName(nodeRef.getStoreRef().getIdentifier()), nodeRef.getId());
//    } 
////
////	
//    @Override
//    public StoreRef getName(StoreRef storeRef)
//    {
//        if (storeRef == null)
//        {
//            return null;
//        }
//
//        
//		Authentication auth = AuthenticationUtil
//				.getRunAsAuthentication();
//		boolean noValid = false;
//
//		if (auth != null) {
//			String nameUser = AuthenticationUtil
//					.getRunAsAuthentication().getName();
//			if (nameUser != null) {
//				noValid = nameUser.indexOf("earq_admin_aprovacao") != -1;
//				if (noValid) {
//					 return new StoreRef(storeRef.getProtocol(), "@clubeturismocampinas.com.br@SpacesStore");
//				}
//			}
//		}
//
//        
//        return new StoreRef(storeRef.getProtocol(), getName(storeRef.getIdentifier()));
//    }
	
	@Override
	public String getBaseName(String name, boolean forceForNonTenant) {
		if (name == null) {
			return null;
		}

		int idx1 = name.indexOf(SEPARATOR);
		if (idx1 == 0) {
			int idx2 = name.indexOf(SEPARATOR, 1);
			String nameDomain = name.substring(1, idx2);
			String tenantDomain = getCurrentUserDomain();
			// String tenantDomain = nameDomain;

			// System.out.println("Aprovacao doc - CustomMultiTServiceImpl nameDomain = "+nameDomain);
			// System.out.println("Aprovacao doc - CustomMultiTServiceImpl  name = "+name);
			// System.out.println("Aprovacao doc - CustomMultiTServiceImpl tenantDomain = "+tenantDomain);
			// System.out.println("Aprovacao doc - CustomMultiTServiceImpl idx2 = "+idx2);
			// System.out.println("Aprovacao doc - CustomMultiTServiceImpl name.substring(idx2 + 1) = "+name.substring(idx2
			// + 1));

			// valid
			// if ((!tenantDomain.equals(DEFAULT_DOMAIN))
			// && (!tenantDomain.equals(nameDomain))) {
			// throw new AlfrescoRuntimeException(
			// "domain mismatch: expected = " + tenantDomain
			// + ", actual = " + nameDomain);
			// }

			if ((!tenantDomain.equals(DEFAULT_DOMAIN)) || (forceForNonTenant)) {

				Authentication auth = AuthenticationUtil
						.getRunAsAuthentication();
				boolean noValid = false;

				if (auth != null) {
					String nameUser = AuthenticationUtil
							.getRunAsAuthentication().getName();
					if (nameUser != null) {
						noValid = nameUser.indexOf("earq_admin_aprovacao") != -1;
//						if (noValid) {
//							System.out.println("EARQ - Ignora remove tenant");
//							System.out.println("nameUser = " + nameUser);
//							System.out.println(tenantDomain);
//						}
					}
				}

				if (!noValid) {
					// remove tenant domain
					name = name.substring(idx2 + 1);
				}

			}
		}

		return name;
	}

	@Override
	public String getName(String name) {
		if (name == null) {
			return null;
		}

		String tenantDomain = getCurrentUserDomain();

		// System.out.println("getSystemUserName = "+AuthenticationUtil.getSystemUserName());
		// System.out.println("getFullyAuthenticatedUser() = "+AuthenticationUtil.getFullyAuthenticatedUser());
		// if(AuthenticationUtil.getRunAsAuthentication()!=null){
		// System.out.println("getRunAsAuthentication().getName() = "+AuthenticationUtil.getRunAsAuthentication().getName());
		// System.out.println("getRunAsAuthentication().getRunAsAuthentication().getCredentials() = "+AuthenticationUtil.getRunAsAuthentication().getCredentials());
		// System.out.println("getRunAsAuthentication().getRunAsAuthentication().getCredentials() = "+AuthenticationUtil.getRunAsAuthentication().getName().indexOf("earq"));
		// }
		if (!tenantDomain.equals(DEFAULT_DOMAIN)) {
			int idx1 = name.indexOf(SEPARATOR);
			if (idx1 != 0) {
				// no tenant domain prefix, so add it
				// name = SEPARATOR + tenantDomain + SEPARATOR + name;

				// Authentication auth =
				// AuthenticationUtil.getRunAsAuthentication();
				// boolean noValid = false;
				//
				// if(auth!=null){
				// String nameUser =
				// AuthenticationUtil.getRunAsAuthentication().getName();
				// if(nameUser!=null){
				// noValid = nameUser.indexOf("earq_admin_aprovacao")!=-1;
				// if(noValid){
				// System.out.println("EARQ - Ignora remove tenant");
				// System.out.println("nameUser = "+nameUser);
				// }
				// }
				// }
				//
				// if(noValid){
				// name = SEPARATOR + "clubeturismocampinas.com.br" + SEPARATOR
				// + name;
				// }
				// else{
				name = SEPARATOR + tenantDomain + SEPARATOR + name;
				// }

			} else {
				int idx2 = name.indexOf(SEPARATOR, 1);
				String nameDomain = name.substring(1, idx2);

				if (!tenantDomain.equalsIgnoreCase(nameDomain)) {
					// throw new
					// AlfrescoRuntimeException("domain mismatch: expected = " +
					// tenantDomain + ", actual = " + nameDomain);
				}
			}
		}

		return name;
	}

}
