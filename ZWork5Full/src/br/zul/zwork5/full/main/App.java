package br.zul.zwork5.full.main;

import br.zul.zwork5.exception.ZUneditableFileException;
import br.zul.zwork5.io.ZFile;
import br.zul.zwork5.io.ZFileEdition;
import br.zul.zwork5.log.ZLogger;
import br.zul.zwork5.util.ZAppUtils;
import br.zul.zwork5.util.ZList;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author luizh
 */
class App {

    //==========================================================================
    //VARIÁVEIS
    //==========================================================================
    private final ZLogger logger;

    //==========================================================================
    //CONSTRUTORES
    //==========================================================================
    public App() {
        this.logger = new ZLogger(getClass());
    }

    //==========================================================================
    //MÉTODOS PÚBLICOS
    //==========================================================================
    ZFile getAppDirectory() {
        return ZAppUtils.getAppFile(getClass()).getParent();
    }

    ZFile getAppFile(){
        return ZAppUtils.getAppFile(getClass());
    }
    
    ZFile getPortableFile() {
        return getAppDirectory().getParent().getChild("ZWork5FullPortable.jar");
    }

    void printVersionInfo() {
        logger.info.println("ZWork5 version {0}", Const.VERSION);
    }

    void compilePortableVersion() throws ZUneditableFileException, IOException {
        getPortableFile().delete();
        ZFileEdition edition = getPortableFile().editFile(false);
        try (OutputStream os = edition.getOutputStream()){
            try (ZipOutputStream zos = new JarOutputStream(os)){
                for (ZFile lib:listLibraries()){
                    copyFiles(lib, zos);
                }
            }
            edition.commit();
        }
    }
    
    ZList<ZFile> listLibraries(){
        ZList<ZFile> result = getAppDirectory().getChild("lib")
                                               .listChildren()
                                               .map(f->(ZFile)f);
        result.add(0, getAppFile());
        return result;
    }

    private void copyFiles(ZFile lib, ZipOutputStream zos) throws IOException {
        logger.info.println("Processando a biblioteca: {0}...", lib.getPath());
        try (InputStream is=lib.getInputStream()){
            try (ZipInputStream zis = new ZipInputStream(is)){
                ZipEntry entryIn;
                while ((entryIn=zis.getNextEntry())!=null){
                    try {
                        ZipEntry entryOut = new ZipEntry(entryIn.getName());
                        zos.putNextEntry(entryOut);
                        logger.info.println("Transferindo arquivo: {0}...", entryIn.getName());
                        copy(zis, zos);
                    } catch (ZipException ex){
                        if (!logDuplicateEntry(ex, entryIn)){
                            throw ex;
                        }
                    }
                }
            }
        }
    }

    private void copy(ZipInputStream zis, ZipOutputStream zos) throws IOException {
        byte[] buffer = new byte[1024*9];
        int len;
        while ((len=zis.read(buffer))!=-1){
            zos.write(buffer, 0, len);
        }
    }

    private boolean logDuplicateEntry(ZipException ex, ZipEntry entryIn) {
        if (ex.getMessage().contains("duplicate entry:")){
            logger.info.println("Arquivo duplicado: {0}", entryIn.getName());
            return true;
        }
        return false;
    }
    
}
