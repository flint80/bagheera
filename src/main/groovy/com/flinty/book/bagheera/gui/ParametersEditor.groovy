package com.flinty.book.bagheera.gui

import groovy.swing.SwingBuilder

import java.awt.Color
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.text.DateFormat
import java.text.SimpleDateFormat

import javax.swing.JComponent
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.JTextField





/**
 * Class for rendering editors for user parameters queried in scripts
 * For now the following types are supported: int, text, date and datetime
 * For additional types one must create and register in static initializer a class implementing IParameterEditor
 */
class ParametersEditor {

	def static Map editorsRegistry = new HashMap()

	static {
		editorsRegistry.put('int', new IntegerParametersEditor())
		editorsRegistry.put('text', new TextParametersEditor())
		editorsRegistry.put('date', new DateParameterEditor())
		editorsRegistry.put('datetime', new DateTimeParameterEditor())
		editorsRegistry.put('content', new ContentParametersEditor())
	}

	def List editors = new LinkedList()

	final String scriptFileName

	ParametersEditor(File script){
		scriptFileName = script.getName()
	}

	JComponent createComponent(List parameters){
		def SwingBuilder builder = new SwingBuilder()
		return builder.panel(border: builder.lineBorder(color: Color.BLACK)){
			gridBagLayout()
			def int n = 0
			parameters.each {
				label(it['title']+":", constraints:builder.gbc(gridx:0,gridy:n, insets:[2, 2, 2, 2], anchor: GridBagConstraints.LINE_END))
				def IParameterEditor anEditor = editorsRegistry[it['type']]
				def Closure cl = anEditor.createComponent(gbc(gridx:1,gridy:n,anchor: GridBagConstraints.LINE_START,weightx:1, insets:[2, 2, 2, 2]))
				cl.delegate = delegate
				def JComponent component = cl.call()
				def value = it['value']
				anEditor.setData(component, value)
				editors<<[comp: component, type: it['type'], paramName: it['paramName']]
				n++
			}
			if(parameters){
				builder.panel(constraints:builder.gbc(gridx:0,gridy:n, weighty: 1))
			}
		}
	}



	Map getValues(){
		def Map result = [:]
		def Map defaultValues = [:]
		editors.each{
			def value = editorsRegistry[it['type']].getData(it['comp'])
			result[it['paramName']] = value
			defaultValues[it['paramName']] = value
		}
		return result
	}

	interface IParameterEditor{

		Closure createComponent(GridBagConstraints value)

		void setData(JComponent comp, Object value)

		Object getData(JComponent comp)

		String getFieldType()
	}

	static class IntegerParametersEditor implements IParameterEditor{

		Closure createComponent(GridBagConstraints value){
			return {
				return textField(preferredSize: new Dimension(200,30), minimumSize: new Dimension(200,30), constraints: value)
			}
		}

		void setData(JComponent comp, Object value){
			((JTextField) comp).setText(value == null? null : value.toString())
		}

		Object getData(JComponent comp){
			def value = ((JTextField) comp).getText()
			try{
				return value == null? Integer.valueOf(0): Integer.parseInt(value)
			} catch (Exception e) {
				throw new Exception('введено некорректное значение параметра ' + value)
			}
		}

		String getFieldType(){
			return 'int'
		}
	}
	static class TextParametersEditor implements IParameterEditor{

		Closure createComponent(GridBagConstraints value){
			return {
				return textField(preferredSize: new Dimension(200,30), minimumSize: new Dimension(200,30), constraints: value)
			}
		}

		void setData(JComponent comp, Object value){
			((JTextField) comp).setText(value)
		}

		Object getData(JComponent comp){
			return ((JTextField) comp).getText()
		}

		String getFieldType(){
			return 'text'
		}
	}
	static class ContentParametersEditor implements IParameterEditor{
		
		Closure createComponent(GridBagConstraints value){
			return {
				return scrollPane(preferredSize: new Dimension(400,200), minimumSize: new Dimension(4000,200), constraints: value){textArea()}
			}
		}
		
		void setData(JComponent comp, Object value){
			((JTextArea) ((JScrollPane) comp).getViewport().getView()).setText(value)
		}
		
		Object getData(JComponent comp){
			return ((JTextArea) ((JScrollPane) comp).getViewport().getView()).getText()
		}
		
		String getFieldType(){
			return 'content'
		}
	}
	
	static class DateParameterEditor implements IParameterEditor{

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd")

		Closure createComponent(GridBagConstraints value){
			return {
				return textField(preferredSize: new Dimension(200,30), minimumSize: new Dimension(200,30), constraints: value)
			}
		}

		void setData(JComponent comp, Object value){
			((JTextField) comp).setText(value == null? null: df.format(value))
		}

		Object getData(JComponent comp){
			def value = ((JTextField) comp).getText()
			try{
				return value == null? null: df.parse(value)
			} catch (Exception e) {
				throw new Exception('введено некорректное значение параметра ' + value)
			}
		}

		String getFieldType(){
			return 'date'
		}
	}
	static class DateTimeParameterEditor implements IParameterEditor{

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm")

		Closure createComponent(GridBagConstraints value){
			return {
				return textField(preferredSize: new Dimension(200,30), minimumSize: new Dimension(200,30), constraints: value)
			}
		}

		void setData(JComponent comp, Object value){
			((JTextField) comp).setText(value == null? null: df.format(value))
		}

		Object getData(JComponent comp){
			def value = ((JTextField) comp).getText()
			try{
				return value == null? null: df.parse(value)
			} catch (Exception e) {
				throw new Exception('введено некорректное значение параметра ' + value)
			}
		}

		String getFieldType(){
			return 'datetime'
		}
	}
}
