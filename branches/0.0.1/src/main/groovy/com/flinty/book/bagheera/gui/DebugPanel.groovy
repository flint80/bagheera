package com.flinty.book.bagheera.gui

import groovy.swing.SwingBuilder

import java.awt.Color
import java.awt.Dimension
import java.awt.GridBagConstraints

import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextArea




/**
 * GUI part of debug module. There is no logic - only panels to read adn write debug information
 *
 */
class DebugPanel{


	def JPanel panel

	def JTextArea scriptArea

	def JTextArea resultArea

	DebugPanel(){
		def SwingBuilder builder = new SwingBuilder()
		panel = builder.panel(border: builder.lineBorder(color: Color.BLACK)){
			gridBagLayout()
			label('Выражение', constraints:builder.gbc(gridx:0,gridy:0, insets:[2, 2, 2, 2], anchor: GridBagConstraints.LINE_START, weightx: 1, fill: GridBagConstraints.HORIZONTAL))
			scrollPane(border: builder.lineBorder(color: Color.CYAN), constraints:builder.gbc(gridx:0,gridy:1, insets:[2, 2, 2, 2], weightx: 1, anchor: GridBagConstraints.LINE_START, fill: GridBagConstraints.HORIZONTAL), preferredSize: new Dimension(30,100), minimumSize: new Dimension(0,0), maximumSize: new Dimension(2000,100)){ scriptArea = textArea()	 }
			label('Результат', constraints:builder.gbc(gridx:0,gridy:2, insets:[2, 2, 2, 2], anchor: GridBagConstraints.LINE_START, weightx: 1, fill: GridBagConstraints.HORIZONTAL))
			scrollPane(border: builder.lineBorder(color: Color.CYAN), constraints:builder.gbc(gridx:0,gridy:3, insets:[2, 2, 2, 2], weightx: 1, weighty: 1, anchor: GridBagConstraints.LINE_START, fill: GridBagConstraints.BOTH), preferredSize: new Dimension(30,100), minimumSize: new Dimension(0,0), maximumSize: new Dimension(2000,100)){ resultArea = textArea()	 }
		}
	}

	JComponent getComponent(){
		return panel
	}

	String getScript(){
		scriptArea.getText()
	}


	void setResult(String value){
		resultArea.setText(value)
	}
}
