package edu.mayo.informatics.lexgrid.convert.directConversions.hl7.mif.vocabulary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MifVocabParserHandler extends DefaultHandler {

    private MifVocabularyModel vocabularyModel;  
    
    protected List<MifCodeSystem> codeSystems;
    protected List<MifCodeSystemVersion> codeSystemVersions;
    protected List<String> csvSupportedLanguages;
    protected List<MifConcept> concepts;
    protected List<MifConceptRelationship> conceptRelationships;
    protected List<MifConceptProperty> conceptProperties;
    protected List<MifConceptCode> conceptCodes;
    
    protected MifCodeSystem codeSystem;
    
    protected MifCodeSystemVersion codeSystemVersion;
    
    protected MifConcept concept;
    protected MifConceptRelationship conceptRelationship;
    protected MifConceptProperty conceptProperty;
    protected MifConceptCode conceptCode;
    protected MifPrintName conceptPrintName;
    protected MifSupportedConceptRelationship supportedConceptRelationship;
    
    // Collection below is the complete list of unique SupportedConceptRelationships found in the HL7 MIF Vocab XML file
    protected HashMap<String, MifSupportedConceptRelationship> supportedConceptRelationshipsMap;
    // Collection below is the complete list of unique SupportedConceptProperty items found in the HL7 MIF Vocab XML file
    protected HashMap<String, MifSupportedConceptProperty> supportedConceptPropertiesMap;
    
    protected boolean csvSupportedLanguageFlag;
    protected boolean conceptRelationshipFlag;
    
    // Debug/statisical data variables - used to get misc info about contents of the load
    // source file.  
    public int countOfMultipleCodeConcepts = 0;
    public int countOfCodeSystemVersions = 0;
    public int countOfCodeSystemVersionsWithNoConcepts = 0;
    
    final protected int START = 0,
            VOCABULARYMODEL = 1,
            CODESYSTEM = 2,
            CODESYSTEMVERSION = 3,
            CONCEPT = 4,
            CONCEPTRELATIONSHIP = 5,
            CONCEPTPROPERTY = 6,
            CONCEPTCODE = 7;
    
    protected int state = START;
    
//    protected MifVocabLeafElement currentElement;  // needed class for holding currentElement info - need to code this ???
    
    public MifVocabParserHandler() {
        super();
        vocabularyModel = new MifVocabularyModel();
    }                   

    public MifVocabularyModel getVocabularyModel() {
        return vocabularyModel;
    }

    public void setVocabularyModel(MifVocabularyModel vocabularyModel) {
        this.vocabularyModel = vocabularyModel;
    }

    public HashMap<String, MifSupportedConceptRelationship> getSupportedConceptRelationshipsMap() {
        return supportedConceptRelationshipsMap;
    }

    public void setSupportedConceptRelationshipsMap(
            HashMap<String, MifSupportedConceptRelationship> supportedConceptRelationshipsMap) {
        this.supportedConceptRelationshipsMap = supportedConceptRelationshipsMap;
    }

    public HashMap<String, MifSupportedConceptProperty> getSupportedConceptPropertiesMap() {
        return supportedConceptPropertiesMap;
    }

    public void setSupportedConceptPropertiesMap(HashMap<String, MifSupportedConceptProperty> supportedConceptPropertiesMap) {
        this.supportedConceptPropertiesMap = supportedConceptPropertiesMap;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        // VocabularyModel data
        if (qName.equalsIgnoreCase("vocabularyModel")) {
            //System.out.println("Start Element :" + qName);
            codeSystems = new ArrayList<MifCodeSystem>();
            supportedConceptRelationshipsMap = new HashMap<String, MifSupportedConceptRelationship>();
            supportedConceptPropertiesMap = new HashMap<String, MifSupportedConceptProperty>();
            
            getVocabularyModel().setXmlns(attributes.getValue("xmlns"));
            getVocabularyModel().setName(attributes.getValue("name"));
            getVocabularyModel().setTitle(attributes.getValue("title"));
            getVocabularyModel().setSchemaVersion(attributes.getValue("schemaVersion"));
            getVocabularyModel().setDefaultLanguage("en"); 
        }
        if (qName.equalsIgnoreCase("packageLocation")) {
            //System.out.println("Start Element :" + qName);
            getVocabularyModel().setCombinedId(attributes.getValue("combinedId"));
        }

        // CodeSystem data
        if (qName.equalsIgnoreCase("codeSystem")) {
            //System.out.println("Start Element :" + qName);
            codeSystem = new MifCodeSystem();
            // set parent VocabularyModel info
            codeSystem.setVmCombinedId(getVocabularyModel().getCombinedId());
            codeSystem.setVmSchemaVersion(getVocabularyModel().getSchemaVersion());
            // set CodeSystem attributes
            codeSystem.setName(attributes.getValue("name"));
            codeSystem.setTitle(attributes.getValue("title"));
            codeSystem.setCodeSystemId(attributes.getValue("codeSystemId"));           
        }
                        
        // CodeSystemVersion data
        if (qName.equalsIgnoreCase("releasedVersion")) {
            //System.out.println("Start Element :" + qName);
            codeSystemVersion = new MifCodeSystemVersion();
            concepts = new ArrayList<MifConcept>();
            csvSupportedLanguages = new ArrayList<String>();

            // set grandparent VocabularyModel info
            codeSystemVersion.setVmCombinedId(getVocabularyModel().getCombinedId());

            // set parent CodeSystem info
            codeSystemVersion.setCodeSystemId(codeSystem.getCodeSystemId());

            // set CodeSystemVersion attributes
            codeSystemVersion.setReleaseDate(attributes.getValue("releaseDate"));
//            codeSystemVersion.setPublisherVersionId(attributes.getValue("publisherVersionId"));
            state = CODESYSTEMVERSION;
        }
        if (qName.equalsIgnoreCase("supportedLanguage") && state == CODESYSTEMVERSION) {
            //System.out.println("Start Element :" + qName);
            csvSupportedLanguageFlag = true;
            // NOTE:  data is embedded within element and parser will generate a characters event from
            // which the data will be parsed out in the characters(char[], int, int) method of this handler class.
        }
        if (qName.equalsIgnoreCase("supportedConceptRelationship")) {
            String name = attributes.getValue("name");
            if (!supportedConceptRelationshipsMap.containsKey(name)) {
                // Not in map of all found supportedConceptRelationships so add it
                MifSupportedConceptRelationship scr = new MifSupportedConceptRelationship();
                scr.setName(name);
                scr.setRelationshipKind(attributes.getValue("relationshipKind"));
                scr.setInverseName(attributes.getValue("inverseName"));
                scr.setReflexivity(attributes.getValue("reflexivity"));
                scr.setSymmetry(attributes.getValue("symmetry"));
                scr.setTransitivity(attributes.getValue("transitivity"));
                supportedConceptRelationshipsMap.put(name, scr);
            }
        }
        if (qName.equalsIgnoreCase("supportedConceptProperty")) {
            String propertyName = attributes.getValue("propertyName");
            if (!supportedConceptPropertiesMap.containsKey(propertyName)) {
                // Not in map of all found supportedConceptProperties so add it
                MifSupportedConceptProperty scp = new MifSupportedConceptProperty();
                scp.setPropertyName(propertyName);
                scp.setType(attributes.getValue("type"));
                supportedConceptPropertiesMap.put(propertyName, scp);
            }
        }
        
        // Concept data
        if (qName.equalsIgnoreCase("concept")) {
            //System.out.println("Start Element :" + qName);
            concept = new MifConcept();
            conceptRelationships = new ArrayList<MifConceptRelationship>();
            conceptProperties = new ArrayList<MifConceptProperty>();
            conceptCodes = new ArrayList<MifConceptCode>();
            conceptPrintName = new MifPrintName();
            
            // set great grandparent VocabularyModel info
            concept.setVmCombinedId(getVocabularyModel().getCombinedId());

            // set grandparent CodeSystem info
            concept.setCsCodeSystemId(codeSystem.getCodeSystemId());

            // set parent CodeSystemVersion info
            concept.setCsvReleaseDate(codeSystemVersion.getReleaseDate());
            
            // set Concept info
            if (attributes.getValue("isSelectable") != null && attributes.getValue("isSelectable").equalsIgnoreCase("false")) {
                concept.setIsSelectable(false);
            }
            
        }
        
        // ConceptRelationship data
        if (qName.equalsIgnoreCase("conceptRelationship")) {
            //System.out.println("Start Element :" + qName);
            conceptRelationship = new MifConceptRelationship();
            
            // set great great grandparent VocabularyModel info
            conceptRelationship.setVmCombinedId(getVocabularyModel().getCombinedId());

            // set great grandparent CodeSystem info
            conceptRelationship.setCsCodeSystemId(codeSystem.getCodeSystemId());

            // set grandparent CodeSystemVersion info
            conceptRelationship.setCsvReleaseDate(codeSystemVersion.getReleaseDate());
            
            // set ConceptRelationship info
            conceptRelationship.setRelationshipName(attributes.getValue("relationshipName"));
        }
        if (qName.equalsIgnoreCase("targetConcept")) {
            //System.out.println("Start Element :" + qName);
            // set ConceptRelationship info
            conceptRelationship.setTargetConceptCode(attributes.getValue("code"));
        }

        // ConceptProperty data
        if (qName.equalsIgnoreCase("conceptProperty")) {
            //System.out.println("Start Element :" + qName);
            conceptProperty = new MifConceptProperty();
            
            // set great great grandparent VocabularyModel info
            conceptProperty.setVmCombinedId(getVocabularyModel().getCombinedId());

            // set great grandparent CodeSystem info
            conceptProperty.setCsCodeSystemId(codeSystem.getCodeSystemId());

            // set grandparent CodeSystemVersion info
            conceptProperty.setCsvReleaseDate(codeSystemVersion.getReleaseDate());
            
            // set conceptProperty info
            conceptProperty.setName(attributes.getValue("name"));
            conceptProperty.setValue(attributes.getValue("value"));
        }

        // Concept printName data
        if (qName.equalsIgnoreCase("printName")) {
            //System.out.println("Start Element :" + qName);
            conceptPrintName = new MifPrintName();
            
            // set concept printName attribute info
            conceptPrintName.setLanguage(attributes.getValue("language"));
            
            if ((attributes.getValue("preferredForLanguage") != null) && (attributes.getValue("preferredForLanguage").equalsIgnoreCase("true"))) {
                conceptPrintName.setPreferredForLanguage(true);
            }
            if ((attributes.getValue("preferredForLanguage") != null) && (attributes.getValue("preferredForLanguage").equalsIgnoreCase("false"))) {
                conceptPrintName.setPreferredForLanguage(false);
            }
            
            conceptPrintName.setText(attributes.getValue("text"));
        }

        // ConceptCode data
        if (qName.equalsIgnoreCase("code")) {
            //System.out.println("Start Element :" + qName);
            conceptCode = new MifConceptCode();
            
            // set great great grandparent VocabularyModel info
            conceptCode.setVmCombinedId(getVocabularyModel().getCombinedId());

            // set great grandparent CodeSystem info
            conceptCode.setCsCodeSystemId(codeSystem.getCodeSystemId());

            // set grandparent CodeSystemVersion info
            conceptCode.setCsvReleaseDate(codeSystemVersion.getReleaseDate());
            
            // set conceptCode attribute info
            conceptCode.setCode(attributes.getValue("code"));
            conceptCode.setStatus(attributes.getValue("status"));
        }
        
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("conceptRelationship")) {
            //System.out.println("End Element :" + qName); 
            conceptRelationships.add(conceptRelationship);
        }
        
        if (qName.equalsIgnoreCase("conceptProperty")) {
            //System.out.println("End Element :" + qName); 
            conceptProperties.add(conceptProperty);
        }
        
        if (qName.equalsIgnoreCase("printName")) {
            //System.out.println("End Element :" + qName); 
            concept.setPrintName(conceptPrintName);
        }
        
        if (qName.equalsIgnoreCase("code")) {
            //System.out.println("End Element :" + qName); 
            conceptCodes.add(conceptCode);
        }
       
        if (qName.equalsIgnoreCase("concept")) {
            //System.out.println("End Element :" + qName); 
            if (conceptProperties.size() == 0) {
                System.out.println("WARNING:  A Concept for CodeSystemVersion having CodeSystem name " + codeSystem.getName() 
                        + " and the CodeSystemVersion releaseDate " 
                        + codeSystemVersion.getReleaseDate() + " has " + conceptProperties.size() 
                        + " conceptProperty entries.");
            } else{
                boolean notFoundFlag = true;
                int count = 0;
                for (int i=0; i<conceptProperties.size(); i++) {
                    MifConceptProperty mcprop = conceptProperties.get(i);
                    if (mcprop.getName().equals("internalId")) {
                        notFoundFlag = false;
                        count += 1;
                    }
                }
                if (notFoundFlag) {
                    System.out.println("WARNING:  A Concept for CodeSystemVersion having CodeSystem name " + codeSystem.getName() 
                            + " and the CodeSystemVersion releaseDate " 
                            + codeSystemVersion.getReleaseDate() + " has no internalId type conceptProperty.");
                }
                if (count > 1) {
                    System.out.println("WARNING:  A Concept for CodeSystemVersion having CodeSystem name " + codeSystem.getName() 
                            + " and the CodeSystemVersion releaseDate " 
                            + codeSystemVersion.getReleaseDate() + " has " + count + " internalId type conceptProperties.");
                }
            }
            if (conceptCodes.size() > 1) {
                countOfMultipleCodeConcepts += 1;
            }
            concept.setConceptCodes(conceptCodes);
            concept.setConceptRelationships(conceptRelationships);
            concept.setConceptProperties(conceptProperties);
            concepts.add(concept);
        }

        if (qName.equalsIgnoreCase("releasedVersion")) {
            //System.out.println("End Element :" + qName); 
            /*
            if (csvSupportedLanguages.size() > 1) {
                System.out.println("WARNING:  CodeSystemVersion having CodeSystem name " + codeSystem.getName() 
                        + " and the CodeSystemVersion releaseDate has " + csvSupportedLanguages.size() 
                        + " supportedLanguage entries.");
            }
            */
            codeSystemVersion.setSupportedLanguages(csvSupportedLanguages);
            codeSystemVersion.setConcepts(concepts);
            codeSystem.getCodeSystemVersions().add(codeSystemVersion);
            // ensure ValueSetVersion's supportedLanguage element is not processed for CodeSystemVersion object
            state = START;
            csvSupportedLanguageFlag = false;
            
            // Debug/statisical data
            countOfCodeSystemVersions += 1;
            if (concepts.size() == 0) {
               countOfCodeSystemVersionsWithNoConcepts += 1;
            }
        }
        
        if (qName.equalsIgnoreCase("codeSystem")) {
            //System.out.println("End Element :" + qName); 
            /*
            if (codeSystem.getCodeSystemVersions().size() > 1) {
                System.out.println("WARNING:  CodeSystem having name " + codeSystem.getName() 
                        + " has " + codeSystem.getCodeSystemVersions().size() + " CodeSystemVersion objects.");
            }
            */
            codeSystems.add(codeSystem);
        }
        
        if (qName.equalsIgnoreCase("packageLocation")) {
            //System.out.println("End Element :" + qName);           
        }
        if (qName.equalsIgnoreCase("vocabularyModel")) {
            //System.out.println("End Element :" + qName);
            getVocabularyModel().setCodeSystems(codeSystems);
            getVocabularyModel().setSupportedConceptRelationshipsMap(supportedConceptRelationshipsMap);
            getVocabularyModel().setSupportedConceptPropertiesMap(supportedConceptPropertiesMap);
        }        
        
    }

    public void characters(char ch[], int start, int length) throws SAXException {

        if (csvSupportedLanguageFlag) {
            //System.out.println("CSV SupportedLanguage: " + new String(ch, start, length));
            csvSupportedLanguages.add(new String(ch, start, length));
            csvSupportedLanguageFlag = false;
        }
    }

    public static void main(String[] args) {
        MifVocabParserHandler mifVocabSaxHandler = new MifVocabParserHandler();
        SAXParser p = new SAXParser();
        p.setContentHandler(mifVocabSaxHandler);
        
        try {
            p.parse("C:\\temp_downloads_HL7\\DEFN=UV=VO=1189-20121121.coremif");
        } catch (Exception e) {
            e.printStackTrace();
        }

//        System.out.println(mifVocabSaxHandler.getVocabularyModel().getName());
//        System.out.println(mifVocabSaxHandler.getVocabularyModel().getTitle());
//        System.out.println(mifVocabSaxHandler.getVocabularyModel().getSchemaVersion());
//        System.out.println(mifVocabSaxHandler.getVocabularyModel().getCombinedId());
        
        MifVocabularyModel mvm = mifVocabSaxHandler.getVocabularyModel();

        System.out.println("Number of CodeSystems: " + mvm.getCodeSystems().size());
        
        int conceptCount = 0;
        @SuppressWarnings("rawtypes")
        List codeSystemList = mvm.getCodeSystems();
        for (int i=0; i<codeSystemList.size(); i++) {
            MifCodeSystem mifCodeSystem = (MifCodeSystem)codeSystemList.get(i);
            MifCodeSystemVersion mifCVS = (MifCodeSystemVersion)mifCodeSystem.getCodeSystemVersions().get(0);
            conceptCount += mifCVS.getConcepts().size();
        }
        
        System.out.println("Number of CodeSystemVersions(<releasedVersion> entries): " + mifVocabSaxHandler.countOfCodeSystemVersions);
        System.out.println("Number of CodeSystemVersions that do not have any Concepts: " + mifVocabSaxHandler.countOfCodeSystemVersionsWithNoConcepts);        
        System.out.println("Total number of Concepts: " + conceptCount);
        System.out.println("Total number of Concepts having more than one Code: " + mifVocabSaxHandler.countOfMultipleCodeConcepts);
        Set<String> keySet = mifVocabSaxHandler.getSupportedConceptRelationshipsMap().keySet();
        System.out.println("Total number of SupportedConceptRelationships: " + keySet.size());
        System.out.print("List of SupportedConceptRelationship names: ");
        //for (int i=0; i<keySet.size(); i++)
        for (String keyName : keySet) {
            System.out.print(keyName + " ");
        }
        System.out.println();
        Set<String> keySetProps = mifVocabSaxHandler.getSupportedConceptPropertiesMap().keySet();
        System.out.println("Total number of SupportedConceptProperties: " + keySetProps.size());
        
    }
    
}