/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.rockeat.util;

import com.jpexs.asdec.SWF;
import com.jpexs.asdec.tags.DoABCTag;
import com.jpexs.asdec.tags.Tag;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 *
 * @author l.frattini
 */
public class ActionScriptUtils {

    public static void decompileSwf(String filename, String outdir) throws Exception {
        FileInputStream fis = new FileInputStream(filename);
        InputStream bis = new BufferedInputStream(fis);
        SWF swf = new SWF(bis);
        for (Tag t : swf.tags) {
            if (t instanceof DoABCTag) {
                DoABCTag tag = (DoABCTag) t;
                tag.abc.export(outdir, true);
            }
        }
    }
}
