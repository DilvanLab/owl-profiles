package br.usp.icmc.java;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.protege.editor.owl.ui.view.AbstractOWLSelectionViewComponent;
import org.protege.editor.owl.model.OWLModelManager;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnknownOWLOntologyException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.profiles.OWLProfileReport;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWL2RLProfile;
import org.semanticweb.owlapi.profiles.OWL2ELProfile;
import org.semanticweb.owlapi.profiles.OWL2QLProfile;
import org.semanticweb.owlapi.profiles.OWLProfileViolation;

/**
 *
 * @author Franco Lamping
 */
public class CheckProfiles extends AbstractOWLSelectionViewComponent {

	private static final long serialVersionUID = 1L;
	private JTextPane areaDL, areaEL, areaQL, areaRL;
	private JLabel labelDL, labelEL, labelQL, labelRL, labelFormat, labelInv;
	private JLabel labelDL2, labelEL2, labelQL2, labelRL2;
	private JButton refresh;
	private OWLOntology ontology;
	
	// create the GUI
    public void initialiseView() throws Exception {
    	GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);
        
        // Labels
    	labelDL = new JLabel("DL Profile: "); labelDL.setFont(new Font("SansSerif", Font.BOLD, 11));
        labelEL = new JLabel("EL Profile: "); labelEL.setFont(new Font("SansSerif", Font.BOLD, 11));
        labelQL = new JLabel("QL Profile: "); labelQL.setFont(new Font("SansSerif", Font.BOLD, 11));
        labelRL = new JLabel("RL Profile: "); labelRL.setFont(new Font("SansSerif", Font.BOLD, 11));
        labelFormat = new JLabel("Format: "); labelInv = new JLabel("     ");
        
        // Result labels. Initiates ""
        labelDL2 = new JLabel(); labelDL2.setFont(new Font("SansSerif", Font.BOLD, 11));
        labelEL2 = new JLabel(); labelEL2.setFont(new Font("SansSerif", Font.BOLD, 11));
        labelQL2 = new JLabel(); labelQL2.setFont(new Font("SansSerif", Font.BOLD, 11));
        labelRL2 = new JLabel(); labelRL2.setFont(new Font("SansSerif", Font.BOLD, 11));

        // Area where the violations are going to be shown
        areaDL = new JTextPane(); areaDL.setEditable(false); areaDL.setFont(new Font("SansSerif", Font.PLAIN, 12));
        areaDL.setBorder(BorderFactory.createEtchedBorder()); 
        areaEL = new JTextPane(); areaEL.setEditable(false); areaEL.setFont(new Font("SansSerif", Font.PLAIN, 12));
        areaEL.setBorder(BorderFactory.createEtchedBorder());
        areaQL = new JTextPane(); areaQL.setEditable(false); areaQL.setFont(new Font("SansSerif", Font.PLAIN, 12));
        areaQL.setBorder(BorderFactory.createEtchedBorder());
        areaRL = new JTextPane(); areaRL.setEditable(false); areaRL.setFont(new Font("SansSerif", Font.PLAIN, 12));
        areaRL.setBorder(BorderFactory.createEtchedBorder());

        // Refresh button
        refresh = new JButton("Refresh");
    	refresh.addActionListener(new ActionListener() {    		 
            public void actionPerformed (ActionEvent e) {
            	try {
        			checkProfileDL();
        			checkProfileEL();
        			checkProfileQL();
        			checkProfileRL();
        		} catch (BadLocationException exc) {
        			exc.printStackTrace();
        		}
            }
        });
    	
    	// First row
    	c.anchor = GridBagConstraints.WEST;
        c.gridx = 0; c.gridy = 0; add(refresh, c);
       
        // Second row
        c.gridx = 0; c.gridy = 1; c.weighty = 0.025; add(labelDL, c); add(labelDL2, c);
        c.gridx = 1; add(labelInv, c);
        c.gridx = 2; add(labelEL, c); add(labelEL2, c);
        
        // Third row
        c.fill = GridBagConstraints.BOTH; c.weightx = 0.3; c.weighty = 0.3;
        c.gridx = 0; c.gridy = 2; add(new JScrollPane(areaDL), c); 
        c.gridx = 2; add(new JScrollPane(areaEL), c);

        // Fourth row
        c.fill = GridBagConstraints.NONE; c.weightx = 0; c.weighty = 0.025;
        c.gridx = 0; c.gridy = 3; add(labelQL, c);  add(labelQL2, c);
        c.gridx = 2; add(labelRL, c); add(labelRL2, c);
        
        // Fifth row
        c.fill = GridBagConstraints.BOTH; c.weightx = 0.3; c.weighty = 0.3;
        c.gridx = 0; c.gridy = 4; add(new JScrollPane(areaQL), c);
        c.gridx = 2; add(new JScrollPane(areaRL), c);
        
        // Sixth row
        c.fill = GridBagConstraints.NONE; c.weightx = 0; c.weighty = 0;
        c.gridx = 0; c.gridy = 5; add(labelFormat, c);

        // Load the ontology
        ontology = loadOntology();
        
        // Check profiles and show violations if exist
        checkProfileDL(); checkProfileEL();
		checkProfileQL(); checkProfileRL();
    }

    protected OWLObject updateView() {
    	return null;
	}
    
    public void disposeView() {
    }
    
    private OWLOntology loadOntology() throws OWLOntologyCreationException {
    	// Get hold of an ontology manager
    	OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    	
		OWLModelManager modelManager = getOWLModelManager() ;
		OWLOntology ontology = modelManager.getActiveOntology();
		
		System.out.println("Ontology Loaded...");
		System.out.println("Ontology: " + ontology.getOntologyID());
		try {
	        System.out.println("Format: " + manager.getOntologyFormat(ontology));
	        labelFormat.setText("Format: " + 
	        ((manager.getOntologyFormat(ontology)==null) ? "no format found" :  manager.getOntologyFormat(ontology)));
		} catch (UnknownOWLOntologyException e) {
			labelFormat.setText("Format: (error) " + e);
		}        
        return ontology;
	}
	
    private void checkProfileDL () throws BadLocationException {
		OWL2DLProfile profileDL = new OWL2DLProfile();
		OWLProfileReport reportDL = profileDL.checkOntology(this.ontology);
		
		areaDL.setText("");
		StyleContext sc = StyleContext.getDefaultStyleContext(); 
	    AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, java.awt.Color.BLACK);
		StyledDocument doc = areaDL.getStyledDocument();
		
		if (reportDL.isInProfile()) {
			labelDL2.setText("                    YES"); labelDL2.setForeground(new Color(21, 121, 9));
		} else {
			labelDL2.setText("                    NO"); labelDL2.setForeground(new Color(255, 0, 0));
			for (OWLProfileViolation v : reportDL.getViolations()) {
				doc.insertString(doc.getLength(), v.toString() + "\n\n", aset);
			}
		}
		areaDL.setCaretPosition(0);
	}
    
    private void checkProfileEL () throws BadLocationException {
		OWL2ELProfile profileEL = new OWL2ELProfile();
		OWLProfileReport reportEL = profileEL.checkOntology(this.ontology);
		
		areaEL.setText("");
		StyleContext sc = StyleContext.getDefaultStyleContext(); 
	    AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, java.awt.Color.BLACK);
		StyledDocument doc = areaEL.getStyledDocument();
		
		if (reportEL.isInProfile()) {
			labelEL2.setText("                    YES"); labelEL2.setForeground(new Color(21, 121, 9));
		} else {
			labelEL2.setText("                    NO"); labelEL2.setForeground(new Color(255, 0, 0));
			for (OWLProfileViolation v : reportEL.getViolations()) {
				doc.insertString(doc.getLength(), v.toString() + "\n\n", aset);
			}
		}
		areaEL.setCaretPosition(0);
	}
    
    private void checkProfileQL () throws BadLocationException {
		OWL2QLProfile profileQL = new OWL2QLProfile();
		OWLProfileReport reportQL = profileQL.checkOntology(this.ontology);
		
		areaQL.setText("");
		StyleContext sc = StyleContext.getDefaultStyleContext(); 
	    AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, java.awt.Color.BLACK);
		StyledDocument doc = areaQL.getStyledDocument();
		
		if (reportQL.isInProfile()) {
			labelQL2.setText("                    YES"); labelQL2.setForeground(new Color(21, 121, 9));
		} else {
			labelQL2.setText("                    NO"); labelQL2.setForeground(new Color(255, 0, 0));
			for (OWLProfileViolation v : reportQL.getViolations()) {
				doc.insertString(doc.getLength(), v.toString() + "\n\n", aset);
			}
		}
		areaQL.setCaretPosition(0);
	}
    
    private void checkProfileRL () throws BadLocationException {
		OWL2RLProfile profileRL = new OWL2RLProfile();
		OWLProfileReport reportRL = profileRL.checkOntology(this.ontology);
		
		areaRL.setText("");
		StyleContext sc = StyleContext.getDefaultStyleContext(); 
	    AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, java.awt.Color.BLACK);
		StyledDocument doc = areaRL.getStyledDocument();
		
		if (reportRL.isInProfile()) {
			labelRL2.setText("                    YES"); labelRL2.setForeground(new Color(21, 121, 9));
		} else {
			labelRL2.setText("                    NO"); labelRL2.setForeground(new Color(255, 0, 0));
			for (OWLProfileViolation v : reportRL.getViolations()) {
				doc.insertString(doc.getLength(), v.toString() + "\n\n", aset);
			}
		}
		areaRL.setCaretPosition(0);
	}
    
}