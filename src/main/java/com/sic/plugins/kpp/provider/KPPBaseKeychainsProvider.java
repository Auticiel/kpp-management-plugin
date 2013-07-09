
package com.sic.plugins.kpp.provider;

import com.sic.plugins.kpp.model.KPPKeychain;
import hudson.DescriptorExtensionList;
import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 *
 * An extension point for providing {@link KPPKeychain}.
 * @author mb
 */
public abstract class KPPBaseKeychainsProvider extends KPPBaseProvider implements ExtensionPoint {
    
    private List<KPPKeychain> keychains = new ArrayList<KPPKeychain>();
    
    /**
     * {@inherited}
     */
    @Override
    protected void merge() {
        List<KPPKeychain> keychainsFromFolder = loadKeychainsFromUploadFolder();
        keychains = mergedObjects(keychains, keychainsFromFolder);
    }
    
    /**
     * Updates keychains information.
     */
    @Override
    public void update() {
        getKeychains().clear();
        super.update();
    }
    
    private List<KPPKeychain> loadKeychainsFromUploadFolder() {
        List<KPPKeychain> ks = new ArrayList<KPPKeychain>();
        
        File[] keychainFiles = getFilesFromUploadDirectory(".keychain");
        for(File keychainFile : keychainFiles) {
            KPPKeychain keychain = new KPPKeychain(keychainFile.getName());
            if(StringUtils.isBlank(keychain.getFileName())) {
            break;
            }
            ks.add(keychain);
        }
        return ks;
    }
    
    /**
     * Get a list with all keychains.
     * 
     * @return all keychains.
     */
    public List<KPPKeychain> getKeychains() {
        return keychains;
    }

    /**
     * Get all the registered {@link KPPKeychain} descriptors.
     *
     * @return all the registered {@link KPPKeychain} descriptors.
     */
    public static DescriptorExtensionList<KPPKeychain, Descriptor<KPPKeychain>> allKeychainDescriptors() {
        return Hudson.getInstance().getDescriptorList(KPPKeychain.class);
    }
    
    /**
     * All regsitered {@link KPPBaseKeychainsProvider}s.
     */
    public static ExtensionList<KPPBaseKeychainsProvider> all() {
        return Hudson.getInstance().getExtensionList(KPPBaseKeychainsProvider.class);
    }
    
    /**
     * TODO: Refactor
     * Call this method to update keychains after save action.
     * This method updates keychain information and removes keychains from upload folder if they are deleted.
     * @param keychainsAfterSave 
     */
    public void updateKeychainsAfterSave(List<KPPKeychain>keychainsAfterSave) {
        List<KPPKeychain> ksCurrent = new ArrayList<KPPKeychain>(getKeychains());
        List<KPPKeychain> ksNew = new ArrayList<KPPKeychain>(keychainsAfterSave.size());
        
        for (KPPKeychain kS : keychainsAfterSave) {
            for (KPPKeychain kC : ksCurrent) {
                if (kC.equals(kS)) {
                    ksNew.add(kS);
                    ksCurrent.remove(kC);
                    break;
                }
            }
        }
        
        if (!ksCurrent.isEmpty()) {
            // delete keychains from filesystem
            final String ksFolderPath = getUploadDirectoryPath();
            File kFile;
            for (KPPKeychain k : ksCurrent) {
                kFile = new File(ksFolderPath + File.separator +k.getFileName());
                kFile.delete();
            }
        }
        
        keychains = ksNew;
    }
    
}