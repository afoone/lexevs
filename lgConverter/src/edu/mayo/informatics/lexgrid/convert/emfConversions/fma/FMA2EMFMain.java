/*
 * Copyright: (c) 2004-2009 Mayo Foundation for Medical Education and 
 * Research (MFMER). All rights reserved. MAYO, MAYO CLINIC, and the
 * triple-shield Mayo logo are trademarks and service marks of MFMER.
 *
 * Except as contained in the copyright notice above, or as used to identify 
 * MFMER as the author of this software, the trade names, trademarks, service
 * marks, or product names of the copyright holder shall not be used in
 * advertising, promotion or otherwise in connection with this software without
 * prior written authorization of the copyright holder.
 * 
 * Licensed under the Eclipse Public License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 * 		http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package edu.mayo.informatics.lexgrid.convert.emfConversions.fma;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import org.LexGrid.LexBIG.Utility.logging.LgMessageDirectorIF;
import org.LexGrid.codingSchemes.CodingScheme;
import org.lexevs.logging.messaging.impl.CachingMessageDirectorImpl;
import org.lexevs.logging.messaging.impl.CommandLineMessageDirector;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;

/* @author <A HREF="mailto:dks02@mayo.edu">Deepak Sharma</A>
 * @author <A HREF="mailto:kanjamala.pradip@mayo.edu">Pradip Kanjamala</A>
 * @version subversion $Revision: 2917 $ checked in on $Date: 2006-06-19
 *          15:52:21 +0000 (Mon, 19 Jun 2006) $
 */

public class FMA2EMFMain {
    public static void main(String[] args) {
        LgMessageDirectorIF logger = new CachingMessageDirectorImpl(new CommandLineMessageDirector("FMA2EMFLogger"));
        if (args.length != 2) {
            System.out.println("Usage: FMA2EMFMain  <ProtegeFrames pprj uri>  <target lexgrid xml file uri>");

            System.out.println("\t target file name - place to put output XML.");

            System.exit(0);
        }

        System.out.println("=== Converting " + args[0] + " started at " + (new java.util.Date()) + " ===");
        try {

            URI input_uri = createURIFromString(args[0]);
            URI output_uri = createURIFromString(args[1]);

            FMA2EMFMain f2e = new FMA2EMFMain();
            CodingScheme cst = f2e.map(input_uri, logger);

            f2e.writeLexGridXML(cst, output_uri, logger);
        } catch (Exception ex) {
            logger.fatal(ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static URI createURIFromString(String str) throws Exception {
        String trimmed = str.trim();

        // Resolve to file, treating the string as either a
        // standard file path or URI.
        File f;
        if (!(f = new File(trimmed)).exists()) {
            // Check if we can find the file using the ClassLoader
            URL url = FMA2EMFMain.class.getClassLoader().getResource(trimmed);
            if (url != null) {
                return url.toURI();
            } else {
                // Check if the string is a uri string
                f = new File(new URI(trimmed.replace(" ", "%20")));
                // If we can't find the filename, see if we can get it using the
                // ClassLoader
                if (!f.exists()) {
                    throw new FileNotFoundException();
                }
            }
        }
        // Accomodate embedded spaces ...
        return new URI(f.toURI().toString().replace(" ", "%20"));
    }

    public CodingScheme map(Project proj, LgMessageDirectorIF messages) {
        CodingScheme csclass = null;
        KnowledgeBase kb_ = null;
        if (proj != null) {
            // System.out.println("Current Project=" + proj);
            kb_ = proj.getKnowledgeBase();
        }

        if ((proj != null) && (kb_ != null)) {
            try {
                // XMLMap mapping = getXMLMappings();

                FMA2EMFStaticMapHolders fmaCodeSys = new FMA2EMFStaticMapHolders();
                csclass = fmaCodeSys.getFMACodingScheme(kb_);

                if (csclass != null) {
                    FMA2EMFDynamicMapHolders fmaAttribs = new FMA2EMFDynamicMapHolders();
                    boolean processed = fmaAttribs.processFMA(csclass, kb_);

                    if (processed) {
                        fmaAttribs.populateSupportedProperties(csclass);
                        fmaAttribs.populateSupportedSources(csclass);
                        fmaAttribs.populateSupportedRepresentationalForms(csclass);
                        fmaAttribs.populateSupportedAssociations(csclass);
                        csclass.setApproxNumConcepts(new Long(fmaAttribs.getApproxNumberOfConcepts()));
                    }
                }
            } catch (Exception e) {
                messages.error("Failed in ProtegeFrames Mapping...");
                e.printStackTrace();
            }
        }
        return csclass;
    }

    public CodingScheme map(URI inFileName, LgMessageDirectorIF messages) {
        // URI projectURI =
        // URI.create("file:///C:/Work/Protege-Projects/ProtegeFrames.pprj");
        Collection errors = new ArrayList();

        Project proj = Project.loadProjectFromURI(inFileName, errors);
        System.out.println("Project name= " + proj.getName());
        return map(proj, messages);

    }

    void writeLexGridXML(CodingScheme codingScheme, URI output_filename, LgMessageDirectorIF messages) {
        throw new UnsupportedOperationException(); //TODO  Need to implement new 6.0 exporter 
//        LgXMLResourceImpl xml = null;
//
//        try {
//
//            xml = new LgXMLResourceImpl(org.eclipse.emf.common.util.URI.createURI(output_filename.toURL().toString()));
//            xml.getContents().add(codingScheme);
//
//            // Perform the save ...
//            xml.save();
//        }
//
//        catch (Exception e) {
//
//            messages.fatal("Failed - " + e.toString() + " see log file.");
//        }
    }

}