package com.stottlerhenke.simbionic.editor.gui;

import java.awt.Component;
import java.awt.Font;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Stream;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import com.stottlerhenke.simbionic.common.xmlConverters.model.Binding;
import com.stottlerhenke.simbionic.editor.SB_Binding;
import com.stottlerhenke.simbionic.editor.SB_Global;
import com.stottlerhenke.simbionic.editor.SB_Parameter;
import com.stottlerhenke.simbionic.editor.SB_Variable;
import com.stottlerhenke.simbionic.editor.SimBionicEditor;
import com.stottlerhenke.simbionic.editor.gui.api.EditorRegistry;
import com.stottlerhenke.simbionic.editor.gui.api.I_EditorListener;
import com.stottlerhenke.simbionic.editor.gui.api.I_ExpressionEditor;

/**
 * UI for the list of bindings.
 */
public class SB_BindingsTable extends JTable {

    protected SimBionicEditor _editor;

    protected List<SB_Binding> _bindings;

    protected JComboBox<String> _comboBox = new JComboBox<>();
    protected DefaultCellEditor _varCellEditor;
    
    protected SB_Autocomplete _expressionEditor;

    public SB_BindingsTable(SimBionicEditor editor) {
        _editor = editor;
        
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setRowHeight(21);

        ComboBoxRenderer renderer = new ComboBoxRenderer();
        //renderer.setPreferredSize(new Dimension(200, 130));
        _comboBox.setRenderer(renderer);
        //_comboBox.setMaximumRowCount(3);
        _comboBox.setFont(getFont());

        _varCellEditor =  new DefaultCellEditor(_comboBox);       

        _expressionEditor = _editor.createAutocomplete();
        DefaultCellEditor exprCellEditor = new DefaultCellEditor(_expressionEditor);
        setDefaultEditor(String.class, exprCellEditor);
    }

    protected void setBindings(SB_Polymorphism poly, List<SB_Binding> bindings,
            boolean insert) {
        _bindings = copyBindings(bindings);

        _comboBox.removeAllItems();
        // add locals
        DefaultMutableTreeNode locals = poly.getLocals();
        int size = locals.getChildCount();
        for (int i = 0; i < size; ++i) {
            SB_Variable local = (SB_Variable) ((DefaultMutableTreeNode) locals
                    .getChildAt(i)).getUserObject();
            _comboBox.addItem(local.getName());
        }
        // add parameters
        SB_ProjectBar projectBar = (SB_ProjectBar) ComponentRegistry.getProjectBar();
        SB_Catalog catalog = projectBar._catalog;
        DefaultMutableTreeNode params = catalog.findNode(poly._parent,
                catalog._behaviors);
        size = params.getChildCount();
        for (int i = 0; i < size; ++i) {
            SB_Parameter param = (SB_Parameter) ((DefaultMutableTreeNode) params
                    .getChildAt(i)).getUserObject();
            _comboBox.addItem(param.getName());
        }
        // add globals
        DefaultMutableTreeNode globals = catalog._globals;
        size = globals.getChildCount();
        for (int i = 0; i < size; ++i) {
            SB_Global global = (SB_Global) ((DefaultMutableTreeNode) globals
                    .getChildAt(i)).getUserObject();
            _comboBox.addItem(global.getName());
        }
        
        if (insert) {
           Binding bindingModel = new Binding();
           bindingModel.setVar(_comboBox.getItemAt(0));
           bindingModel.setExpr("");

           _bindings.add(new SB_Binding(bindingModel));

        }

        setModel(new SB_TableModel());
        getColumnModel().getColumn(0).setPreferredWidth(50);
        getColumnModel().getColumn(1).setPreferredWidth(250);

        getColumnModel().getColumn(0).setCellEditor(_varCellEditor);

    }

    static List<SB_Binding> copyBindings(List<SB_Binding> bindings) {
        List<SB_Binding> copy = new Vector<>();
        int size = bindings.size();
        for (int i = 0; i < size; ++i) {
            SB_Binding binding = (SB_Binding) bindings.get(i);
            Binding bindingModel = new Binding();
            bindingModel.setVar(new String(binding.getVar()));
            bindingModel.setExpr(new String(binding.getExpr()));
            copy.add(new SB_Binding(bindingModel));
        }
        return copy;
    }

    protected void insertBinding() {
        Binding bindingModel = new Binding();
        bindingModel.setVar(_comboBox.getItemAt(0));
        bindingModel.setExpr("");
        _bindings.add(new SB_Binding(bindingModel));
        revalidate();
        int row = _bindings.size() - 1;
        setRowSelectionInterval(row, row);
        repaint();
    }

    protected void deleteBinding() {
        int row = getSelectedRow();
        if (row < 0) return;
        _bindings.remove(row);
        revalidate();
        if (row != 0 && row == _bindings.size())
                setRowSelectionInterval(row - 1, row - 1);
        if (_bindings.isEmpty()) clearSelection();
        repaint();
    }

    protected void moveUp() {
        int row = getSelectedRow();
        if (row <= 0) return;
        _bindings.add(row - 1, _bindings.remove(row));
        setRowSelectionInterval(row - 1, row - 1);
        revalidate();
        repaint();
    }

    protected void moveDown() {
        int row = getSelectedRow();
        if (row == _bindings.size() - 1) return;
        _bindings.add(row + 1, _bindings.remove(row));
        setRowSelectionInterval(row + 1, row + 1);
        revalidate();
        repaint();
    }

    protected void addListenerToVarCellEditor(CellEditorListener l) {
        _varCellEditor.addCellEditorListener(l);
    }

    protected void addListenerToSelectionModel(ListSelectionListener l) {
        getSelectionModel().addListSelectionListener(l);
    }

    protected void setVarValue(){
    	final int row = getSelectedRow();
        if (row < 0 || row>=_bindings.size()) return;
        
        if (isEditing()) 
        	getCellEditor().stopCellEditing();

        SB_Binding binding = _bindings.get(row);
        String varName = binding.getVar();
        String typeName = getTypeForVariableName(varName)
                .orElseThrow(() -> new RuntimeException("It is assumed that the"
                        + " type for variable " + varName
                        + " is known by this point."));

        // since currently the Dialog does not allow
        // switching between the two (table and array) type of dialog
        // we need one for each
        I_ExpressionEditor setValueEditor = getSetValueCustomEditor(typeName);
        if (setValueEditor != null) {
            setValueEditor.editObject(binding.getExpr(), new I_EditorListener() {
        		public void editingCanceled(I_ExpressionEditor source) {}
        		
        		public void editingCompleted(I_ExpressionEditor source, String result) {
        			binding.setExpr(result);
        			repaint();
        		}
        	});
        } else {
        	editCellAt(row, 1);
        	getEditorComponent().requestFocus();
        }
    }

    protected I_ExpressionEditor getSetValueCustomEditor(String setValueType) {
        int row = getSelectedRow();
        return _editor.getEditorRegistry().getExpressionEditor(
        		EditorRegistry.EXPRESSION_TYPE_BINDING,
        		setValueType,
        		//typeManager.getTypeName(SB_VarType.getTypeFromInt(_setValueType)), 
        		((SB_Binding)_bindings.get(row)).getExpr());
    }

    // TODO: the combobox at the toolbar has more element than the binding.
    /**
     * update the display of 'Set Value' button so that it is enabled only when
     * the variable is of type array or table
     */
    public boolean enableSetValueButton(){
  
        final int row = getSelectedRow();
        if (row<0) {
        	return false;
        } else if(_bindings.size()<=row){
        	return false;
        } else {
            String varName = _bindings.get(row).getVar();
            //XXX: May not handle null varName well, but no check is done to
            //preserve old behavior.
            return getTypeForVariableName(varName)
                    .map(type -> getSetValueCustomEditor(type) != null)
                    .orElse(false);
        }
    }

    /**
     * Looks for the name of 
     * 
     * This reproduces the behavior of the old implementation of
     * {@link #enableSetValueButton()}, where globals shadow parameters and
     * parameters shadow local variables. This behavior may not be desirable.
     * */
    private Optional<String> getTypeForVariableName(String varName) {

        //XXX: ComponentRegistry items
        SB_Catalog catalog = ComponentRegistry.getProjectBar()._catalog;
        SB_Polymorphism poly = ComponentRegistry.getContent()
                .getActiveCanvas()._poly;

        DefaultMutableTreeNode locals = poly.getLocals();
        Optional<SB_Variable> foundLocal
        = getChildVariableWithMatchingName(locals, varName);

        DefaultMutableTreeNode params = catalog.findNode(poly._parent,
                catalog._behaviors);
        Optional<SB_Variable> foundParam
        = getChildVariableWithMatchingName(params, varName);

        DefaultMutableTreeNode globals = catalog._globals;
        Optional<SB_Variable> foundGlobal
        = getChildVariableWithMatchingName(globals, varName);

        //XXX: Reproduces old implementation by checking globals, then params,
        //then locals, while traversing all elements of all three.
        return Stream.of(foundGlobal, foundParam, foundLocal)
                //XXX: Clearer in Java 9, where Optional#stream provides
                //a single method to make a stream out of the contents
                //of an Optional.
                .flatMap(opt -> opt.map(Stream::of).orElse(Stream.empty()))
                .findFirst()
                .map(variable -> variable.getType());

    }

    /**
     * XXX: Many assumptions are made about the parentNode
     * @param parentNode a DefaultMutableTreeNode that is assumed to have only
     * DefaultMutableTreeNode children that contain non-null SB_Variable user
     * objects (note that SB_Constant, SB_Global, and SB_Parameter are all
     * subclasses of SB_Variable.)
     * */
    private static Optional<SB_Variable> getChildVariableWithMatchingName(
            DefaultMutableTreeNode parentNode, String varName) {
        //XXX: This unchecked conversion replicates earlier behavior, which
        //assumed that all children of the locals node are
        //DefaultMutableTreeNode instances.
        Enumeration<DefaultMutableTreeNode> children = parentNode.children();
        return Collections.list(children).stream()
                .map(child -> (SB_Variable) child.getUserObject())
                .filter(var -> var.getName().equals(varName))
                //XXX: reduce used to simulate "find last" of former behavior.
                .reduce((a, b) -> b);
    }

    class SB_TableModel extends AbstractTableModel {

        final String[] columnNames = { "Variable", "Expression"};

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            if (_bindings != null)
                return _bindings.size();
            else
                return 0;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            SB_Binding binding = (SB_Binding) _bindings.get(row);
            if (col == 0)
                return binding.getVar();
            else
                return binding.getExpr();
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public void setValueAt(Object value, int row, int col) {
            if (_bindings.size() > row){
            SB_Binding binding = (SB_Binding) _bindings.get(row);
            if (col == 0) {
                if (value instanceof SB_Variable)
                    binding.setVar(((SB_Variable) value).getName());
                else if (value instanceof String)
                        binding.setVar((String) value);
            } else
                binding.setExpr((String) value);
            fireTableCellUpdated(row, col);
            }else{
                int i=0;
                int j=2;
            }
        }
    }

    static class ComboBoxRenderer extends JLabel
    implements ListCellRenderer<String> {

        private Font uhOhFont;

        public ComboBoxRenderer() {
            setOpaque(true);
            //setHorizontalAlignment(CENTER);
            //setVerticalAlignment(CENTER);
        }

        /**
         * XXX: Uses present approach of String instances as ComboBox contents;
         * will need refit if images are added (again?).
         * */
        @Override
        public Component getListCellRendererComponent(
                JList<? extends String> list, String value, int index,
                boolean isSelected, boolean cellHasFocus) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            //Set the icon and text. If icon was null, say so.
            String varName = (String) value;
            // RTH icons removed for now to fix combo box selection problem
//            Icon icon = var.getIcon();
            String text = varName;
//            setIcon(icon);
//            if (icon != null) {
                setText(text);
                setFont(list.getFont());
//            } else {
//                setUhOhText(text + " (no image available)", list.getFont());
//            }

            return this;
        }

        //Set the font and text when no image was found.
        protected void setUhOhText(String uhOhText, Font normalFont) {
            if (uhOhFont == null) { //lazily create this font
                uhOhFont = normalFont.deriveFont(Font.ITALIC);
            }
            setFont(uhOhFont);
            setText(uhOhText);
        }
    }

}