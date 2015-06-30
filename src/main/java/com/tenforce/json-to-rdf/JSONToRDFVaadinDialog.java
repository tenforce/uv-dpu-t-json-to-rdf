package com.tenforce.json-to-rdf;

package tenforce.test;

import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.*;

import eu.unifiedviews.dpu.config.DPUConfigException;
import eu.unifiedviews.helpers.dpu.vaadin.dialog.AbstractDialog;

/**
 * DPU's configuration dialog. User can use this dialog to configure DPU
 * configuration.
 */
public class JSONToRDFVaadinDialog extends AbstractDialog<JSONToRDFConfig_V1> {

    private static final long serialVersionUID = -5668436075836909428L;

    private static final String COMMIT_SIZE_LABEL = "Commit size (0 = one file, one transaction, 1 = autocommit connection, n = commit every n triples)";


    private static final String VOCAB_LABEL = "Base URI for generated properties (this becomes a @vocab entry in the @context)";


    private final ObjectProperty<Integer> commitSize = new ObjectProperty<>(0);

    private final ObjectProperty<String> vocab = new ObjectProperty<>("");

    private ComboBox comboFailPolicy;

    private ComboBox comboOutputGraph;

    private TextField txtSymbolicName;

    public JSONToRDFVaadinDialog() {
        super(JSONToRDF.class);
    }

    @Override
    protected void buildDialogLayout() {
        // top-level component properties
        setSizeFull();

        Panel panel = new Panel();
        panel.setSizeFull();

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setMargin(false);
        mainLayout.setSpacing(true);
        mainLayout.setImmediate(false);
        mainLayout.setWidth("100%");
        mainLayout.setHeight("-1px");

        mainLayout.addComponent(new TextField(COMMIT_SIZE_LABEL, commitSize));

	mainLayout.addComponent(new TextField(VOCAB_LABEL, vocab));

        comboFailPolicy = new ComboBox("What to do if extraction on a single file fail:");
        comboFailPolicy.addItem(JSONToRDFConfig_V1.SKIP_CONTINUE_NEXT_FILE_ERROR_HANDLING);
        comboFailPolicy.setItemCaption(JSONToRDFConfig_V1.SKIP_CONTINUE_NEXT_FILE_ERROR_HANDLING, "Skip and continue");
        comboFailPolicy.addItem(JSONToRDFConfig_V1.STOP_EXTRACTION_ERROR_HANDLING);
        comboFailPolicy.setItemCaption(JSONToRDFConfig_V1.STOP_EXTRACTION_ERROR_HANDLING, "Stop execution of the pipeline");
        comboFailPolicy.setInvalidAllowed(false);
        comboFailPolicy.setNullSelectionAllowed(false);
        mainLayout.addComponent(comboFailPolicy);

        comboOutputGraph = new ComboBox("Policy for output symbolic name selection:");
        comboOutputGraph.addItem(JSONToRDFConfig_V1.USE_INPUT_SYMBOLIC_NAME);
        comboOutputGraph.setItemCaption(JSONToRDFConfig_V1.USE_INPUT_SYMBOLIC_NAME, "Use input file's symbolic name");
        comboOutputGraph.addItem(JSONToRDFConfig_V1.USE_FIXED_SYMBOLIC_NAME);
        comboOutputGraph.setItemCaption(JSONToRDFConfig_V1.USE_FIXED_SYMBOLIC_NAME, "Use single fixed symbolic name");
        comboOutputGraph.setInvalidAllowed(false);
        comboOutputGraph.setNullSelectionAllowed(false);
        comboOutputGraph.setImmediate(true);
        mainLayout.addComponent(comboOutputGraph);

        txtSymbolicName = new TextField("Fixed output symbolic name:");
        txtSymbolicName.setDescription("Symbolic name for output, if left blank an 'unique' symbolic is generated.");
        txtSymbolicName.setWidth("100%");
        txtSymbolicName.setInputPrompt("auto");
        txtSymbolicName.setNullSettingAllowed(true);
        txtSymbolicName.setNullRepresentation("");
        mainLayout.addComponent(txtSymbolicName);

        comboOutputGraph.addValueChangeListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                txtSymbolicName.setEnabled(JSONToRDFConfig_V1.USE_FIXED_SYMBOLIC_NAME.equals(event.getProperty().getValue()));
            }
        });

        panel.setContent(mainLayout);
        setCompositionRoot(panel);
    }

    @Override
    public void setConfiguration(JSONToRDFConfig_V1 conf) throws DPUConfigException {
        commitSize.setValue(conf.getCommitSize());
	vocab.setValue(conf.getVocab());
        comboFailPolicy.setValue(conf.getFatalErrorHandling());
        comboOutputGraph.setValue(conf.getOutputNaming());

        txtSymbolicName.setValue(conf.getOutputSymbolicName());
        txtSymbolicName.setEnabled(JSONToRDFConfig_V1.USE_FIXED_SYMBOLIC_NAME.equals(comboOutputGraph.getValue()));
    }

    @Override
    public JSONToRDFConfig_V1 getConfiguration() throws DPUConfigException {
        JSONToRDFConfig_V1 conf = new JSONToRDFConfig_V1();
	conf.setVocab(vocab.getValue());
        conf.setCommitSize(commitSize.getValue());
        conf.setFatalErrorHandling(comboFailPolicy.getValue().toString());
        conf.setOutputNaming(comboOutputGraph.getValue().toString());
        conf.setOutputSymbolicName(txtSymbolicName.getValue());
        return conf;
    }

}
