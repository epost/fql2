package catdata.opl;

import java.awt.GridLayout;
import java.util.function.Function;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import catdata.Pair;
import catdata.Unit;
import catdata.algs.kb.KBOptions;
import catdata.ide.Language;
import catdata.ide.Options;

public class OplOptions extends Options {

	private static final long serialVersionUID = 1L;

	@Override
	public String getName() {
		return Language.OPL.toString();
	}
	
	public boolean opl_unfailing = KBOptions.defaultOptions.unfailing;
	public int opl_iterations = KBOptions.defaultOptions.iterations;
	public boolean opl_require_const = false;
	public boolean opl_sort_cps = KBOptions.defaultOptions.sort_cps;
	public boolean opl_semantic_ac = KBOptions.defaultOptions.semantic_ac;
	public int opl_red_its = KBOptions.defaultOptions.red_its;
	public int opl_hom_its = 100000;
	public boolean opl_validate = true;
	public boolean opl_pretty = true;
	public boolean opl_reorder = true;
	public boolean opl_suppress_dom = true; 
	public boolean opl_horn = KBOptions.defaultOptions.horn;
	public boolean opl_query_check_eqs = true;
	public boolean opl_pushout_simpl = false;
	
	@Override
	public Pair<JComponent, Function<Unit, Unit>> display() {
		JPanel opl1 = new JPanel(new GridLayout(Options.biggestSize, 1));
		JPanel opl2 = new JPanel(new GridLayout(Options.biggestSize, 1));

		JSplitPane oplsplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		oplsplit.add(opl1);
		oplsplit.add(opl2);
		
		JCheckBox opl_validate_box = new JCheckBox("", opl_validate);
		JLabel opl_validate_label = new JLabel("Validate mappings:");
		opl1.add(opl_validate_label);
		opl2.add(opl_validate_box);
		
		JCheckBox opl_unfailing_box = new JCheckBox("", opl_unfailing);
		JLabel opl_unfailing_label = new JLabel("Allow unorientable equations:");
		opl1.add(opl_unfailing_label);
		opl2.add(opl_unfailing_box);
		
		JCheckBox opl_const_box = new JCheckBox("", opl_require_const);
		JLabel opl_const_label = new JLabel("Require a constant at each sort (false=dangerous):");
		opl2.add(opl_const_box);
		opl1.add(opl_const_label);
		
		JCheckBox opl_sort_box = new JCheckBox("", opl_sort_cps);
		JLabel opl_sort_label = new JLabel("Sort orientable critical pairs by length:");
		opl2.add(opl_sort_box);
		opl1.add(opl_sort_label);

		JCheckBox opl_reorder_box = new JCheckBox("", opl_reorder);
		JLabel opl_reorder_label = new JLabel("Reorder joins:");
		opl2.add(opl_reorder_box);
		opl1.add(opl_reorder_label);
		
		JCheckBox opl_semantic_ac_box = new JCheckBox("", opl_semantic_ac);
		JLabel opl_semantic_ac_label = new JLabel("Enable Semantic AC optimization in Knuth-Bendix:");
		opl2.add(opl_semantic_ac_box);
		opl1.add(opl_semantic_ac_label);


		JTextField opl_iterations_box = new JTextField(Integer.toString(opl_iterations), 12);
		JLabel opl_iterations_label = new JLabel("Knuth-Bendix timeout (ms)");
		opl2.add(wrap(opl_iterations_box));
		opl1.add(opl_iterations_label);
		
		JTextField opl_homit_box = new JTextField(Integer.toString(opl_hom_its), 12);
		JLabel opl_homit_label = new JLabel("Saturation timeout (ms)");
		opl2.add(wrap(opl_homit_box));
		opl1.add(opl_homit_label);
		
		JTextField opl_red_box = new JTextField(Integer.toString(opl_red_its), 12);
		JLabel opl_red_label = new JLabel("Reduction iterations maximum");
		opl2.add(wrap(opl_red_box));
		opl1.add(opl_red_label);
		
		JCheckBox opl_pretty_box = new JCheckBox("", opl_pretty);
		JLabel opl_pretty_label = new JLabel("Pretty Print terms:");
		opl2.add(opl_pretty_box);
		opl1.add(opl_pretty_label);
		
		JCheckBox opl_suppress_box = new JCheckBox("", opl_suppress_dom);
		JLabel opl_suppress_label = new JLabel("Supress instance domains:");
		opl2.add(opl_suppress_box);
		opl1.add(opl_suppress_label);
		
		JCheckBox opl_horn_box = new JCheckBox("", opl_horn);
		JLabel opl_horn_label = new JLabel("Allow implications in theories (dangerous, also can't check mappings):");
		opl2.add(opl_horn_box);
		opl1.add(opl_horn_label);
		
		JCheckBox opl_eqs_box = new JCheckBox("", opl_query_check_eqs);
		JLabel opl_eqs_label = new JLabel("Check that queries preserve equalities:");
		opl2.add(opl_eqs_box);
		opl1.add(opl_eqs_label);
		
		JCheckBox opl_simpl_box = new JCheckBox("", opl_pushout_simpl);
		JLabel opl_simpl_label = new JLabel("Simplify pushout schemas:");
		opl2.add(opl_simpl_box);
		opl1.add(opl_simpl_label);

		for (int i = 0; i < Options.biggestSize - size(); i++) {
			opl1.add(new JLabel());
			opl2.add(new JLabel());
		}
			
		Function<Unit, Unit> fn = new Function<Unit, Unit>() {

			@Override
			public Unit apply(Unit t) {
				try {
					int opl = opl_iterations;
					int opl_h = opl_hom_its;
					int opl_r = opl_red_its;
					try {
						opl = Integer.parseInt(opl_iterations_box.getText());
						opl_h = Integer.parseInt(opl_homit_box.getText());
						opl_r = Integer.parseInt(opl_red_box.getText());
					} catch (NumberFormatException nfe) {
					}
					opl_iterations = opl;
					opl_hom_its = opl_h;
					opl_red_its = opl_r;
				} catch (NumberFormatException nfe) {
				}
				
				opl_require_const = opl_const_box.isSelected();
				opl_sort_cps = opl_sort_box.isSelected();
				opl_unfailing = opl_unfailing_box.isSelected();
				opl_validate = opl_validate_box.isSelected();
				opl_pretty = opl_pretty_box.isSelected();
				opl_reorder = opl_reorder_box.isSelected();
				opl_suppress_dom = opl_suppress_box.isSelected();
				opl_horn = opl_horn_box.isSelected();
				opl_semantic_ac = opl_semantic_ac_box.isSelected();
				opl_query_check_eqs = opl_eqs_box.isSelected();
				opl_pushout_simpl = opl_simpl_box.isSelected();
			
				return new Unit();
			}
			
		};

		return new Pair<>(oplsplit, fn);
	}


	@Override
	public int size() {
		return 15;
	} 

}
