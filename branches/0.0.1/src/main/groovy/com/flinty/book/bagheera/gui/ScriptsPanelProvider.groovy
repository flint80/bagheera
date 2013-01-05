package com.flinty.book.bagheera.gui

import groovy.swing.SwingBuilder
import javax.swing.*

import com.flinty.book.bagheera.common.FileInfo
import com.flinty.book.bagheera.common.Utils
import com.flinty.book.bagheera.model.Environment
import com.flinty.book.bagheera.model.IContentProvider


import java.awt.*
import java.awt.event.MouseEvent
import java.util.List

/**
 * Panel representing a list of available user scripts
 * Panel has no state or related resources, so it is a singleton
 */
class ScriptsPanelProvider implements IContentProvider{

	private static ScriptsPanelProvider instance

	static getInstance() {
		if(!instance){
			instance = new ScriptsPanelProvider()
		}
		return instance
	}


	private final JComponent component

	private final List<JButton> buttons = new LinkedList<JButton>()

	private JList fileList

	private JButton startButton

	ScriptsPanelProvider(){
		def builder = new SwingBuilder()
		component = builder.scrollPane{
			fileList = list(cellRenderer:new FileInfoRenderer(), //
					mouseClicked:{MouseEvent event ->
						if (event.getClickCount() == 2) {
							int index = fileList.locationToIndex(event.getPoint())
							if(index != -1){
								processScript(fileList.selectedValue)
							}
						}},//
					selectionMode: ListSelectionModel.SINGLE_SELECTION,//
					valueChanged:{startButton.enabled = fileList.selectedIndex != -1 } )
		}
		startButton = Utils.createToolButton('Старт', '/images/run.gif', {
			if(fileList.selectedIndex != -1){
				processScript(fileList.selectedValue)
			}
		})
		startButton.enabled = false
		buttons.add(startButton)
	}

	void  processScript(FileInfo value){
		GeneratorPanelProvider content = new GeneratorPanelProvider(value)
		Environment.contentManager.updateProvider(content)
		content.startProcess()
	}

	@Override
	public JComponent getContent() {
		return component
	}

	@Override
	public List<JButton> getToolbarButtons() {
		return buttons
	}

	static class FileInfoRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) super.getListCellRendererComponent(list, value,
					index, isSelected, cellHasFocus)

			label.setVerticalAlignment(SwingConstants.TOP)
			label.setText(value?.title)
			return label
		}
	}

	@Override
	public void updateContent() {
		fileList.listData = Environment.userScriptsProvider.getFiles()
	}

	@Override
	public boolean isNavigationAllowed() {
		return true
	}
}

