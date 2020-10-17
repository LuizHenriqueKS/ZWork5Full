package br.zul.zwork5.full.main;

import br.zul.zwork5.exception.ZUneditableFileException;
import br.zul.zwork5.log.ZLog;
import java.io.IOException;

/**
 *
 * @author luizh
 */
public class Main {

    public static void main(String[] args) throws ZUneditableFileException, IOException {
        App app = new App();
        if (app.getAppDirectory().getName().equals("dist")){
            app.compilePortableVersion();
        } else {
            app.printVersionInfo();
        }
    }
    
}
