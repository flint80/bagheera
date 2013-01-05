package com.flinty.book.bagheera.gui

import groovy.swing.SwingBuilder

import java.awt.Component
import java.awt.Dimension

import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JProgressBar
import javax.swing.JScrollPane
import javax.swing.border.EmptyBorder

import com.flinty.book.bagheera.common.FileInfo
import com.flinty.book.bagheera.common.Message
import com.flinty.book.bagheera.common.Utils
import com.flinty.book.bagheera.common.Message.MessageType
import com.flinty.book.bagheera.model.Environment
import com.flinty.book.bagheera.model.IContentProvider
import com.flinty.book.bagheera.model.IScript
import com.flinty.book.bagheera.model.IScriptCalback

/**
 * Class for rendering panel which is shown during execution of script which generates a book
 *
 */
class GeneratorPanelProvider implements IContentProvider{

	def JComponent component

	def List<JButton> buttons = new LinkedList<JButton>()

	def JButton stopButton

	def JButton resumeButton

	def JButton evaluateButton

	def JLabel messageLabel

	def JLabel fileLabel

	def FileInfo fileInfo

	def JProgressBar progressBar

	def JButton showButton

	def JPanel detailsPanel

	def JScrollPane messagePanel

	def JEditorPane messageTextPane

	def File resultFile

	def SwingBuilder builder = new SwingBuilder()

	def boolean workFinished = false

	def Thread workingThread

	def IScript script

	def List<Message> messages = []

	def boolean paused

	def DebugPanel debugPanel

	def Binding scriptBinding

	def lockHolder = new Object()

	GeneratorPanelProvider(FileInfo value){
		fileInfo = value
		def addComponent = {JComponent comp ->
			comp.setAlignmentX(Component.LEFT_ALIGNMENT)
			comp.setBorder(new EmptyBorder(5,5,0,5))
			return comp
		}
		component = builder.panel(minimumSize: new Dimension(0,0), maximumSize: new Dimension(2000,2000)){
			boxLayout(axis: BoxLayout.Y_AXIS)
			messageLabel = addComponent(label())
			progressBar = progressBar(minimum: 0, maximum: 100, value: 0, alignmentX: Component.LEFT_ALIGNMENT)
			addComponent(panel(){
				boxLayout(axis: BoxLayout.X_AXIS)
				fileLabel = label('Файл не создан')
				builder.panel(minimumSize: new Dimension(0,0), preferredSize: new Dimension(0,0), maximumSize: new Dimension(2000,0))
				showButton = button(text: 'Открыть', enabled: false, actionPerformed:{ openFile() })
			})
			detailsPanel = addComponent(panel(){ borderLayout() })
		}
		stopButton = Utils.createToolButton("Стоп", '/images/stop.png', { stopProcess() })
		stopButton.enabled = false
		resumeButton = Utils.createToolButton('Продолжить', '/images/run.gif', { resumeProcess() })
		resumeButton.enabled = false
		buttons.add(stopButton)
		buttons.add(resumeButton)
		evaluateButton = Utils.createToolButton('Выполнить', '/images/execute.png', { evaluateExpression() })
	}

	void openFile(){
		Runtime.getRuntime().exec("cmd /c \"${resultFile.getAbsolutePath()}\"".toString())
	}

	@Override
	public JComponent getContent() {
		return component
	}

	@Override
	public List<JButton> getToolbarButtons() {
		return buttons
	}

	void addAndShowMessage(Message msg){
		if(!messagePanel){
			def builder = new SwingBuilder()
			messagePanel = builder.scrollPane(){
				messageTextPane = editorPane(editable: false,contentType: 'text/html')
			}
		}
		messages.add(msg)
		StringBuilder sb = new StringBuilder('<html>')
		messages.each{ sb.append("<b>$it.type</b>: $it.message<br>") }
		messageTextPane.setText(sb.toString())
		detailsPanel.removeAll()
		detailsPanel.add(messagePanel)
		detailsPanel.revalidate()
		detailsPanel.repaint()
	}

	void stopProcess(){
		if(workingThread){
			script.interrupt()
		}
		workFinished = true
		progressBar.value = 100
		messageLabel.text = 'Выполнение прервано'
		addAndShowMessage(new Message(type: MessageType.ERROR, message:'Выполнение прервано'))
		stopButton.enabled = false
		resumeButton.enabled = false
		evaluateButton.visible = false
		Environment.contentManager.repaint()
	}
	void resumeProcess(){
		paused = false
		synchronized (lockHolder) {
			lockHolder.notify()
		}
	}
	void startProcess(){
		def binding = new Binding()
		script = Environment.scriptFactory.createScript(fileInfo.file, binding, new IScriptCalback(){

					@Override
					public void updateProgress(int progress, String message) {
						if(workFinished || paused){
							return
						}
						builder.edt{
							progressBar.value = progress
							messageLabel.text = message
							component.repaint()
						}
					}

					@Override
					public void showMessage(Message message) {
						if(workFinished || paused){
							return
						}
						builder.edt{ addAndShowMessage(message) }
					}

					@Override
					public void setOutputFile(File file) {
						if(workFinished || paused){
							return
						}
						builder.edt{
							resultFile = file
							fileLabel.text = file==null? 'файл не создан': file.name
							showButton.enabled = file != null
							component.repaint()
						}
					}

					@Override
					public void setWorkFinished() {
						if(workFinished || paused){
							return
						}
						builder.edt{
							workFinished = true
							showButton.enabled = resultFile != null
							stopButton.enabled = false
							progressBar.value = 100
							messageLabel.text = 'Операция завершена'
							Environment.contentManager.repaint()
						}
					}

					@Override
					public Map getParameters(List definitions){
						if(workFinished || paused){
							return
						}
						def editor = new ParametersEditor(fileInfo.file)
						builder.edt{
							resumeButton.enabled = true
							stopButton.enabled = true
							detailsPanel.removeAll()
							def JComponent comp = editor.createComponent(definitions)
							detailsPanel.add(comp)
							detailsPanel.revalidate()
							detailsPanel.repaint()
						}
						paused = true
						while(paused){
							synchronized (lockHolder) {
								lockHolder.wait()
							}
						}
						builder.edt{ resumeButton.enabled = false  }
						return editor.getValues()
					}

					@Override
					void debug(Binding aBinding){
						if(workFinished || paused){
							return
						}
						debugPanel = new DebugPanel()
						scriptBinding = aBinding
						builder.edt{
							resumeButton.enabled = true
							stopButton.enabled = true
							evaluateButton.enabled = true
							buttons.add(evaluateButton)
							detailsPanel.removeAll()
							detailsPanel.add(debugPanel.getComponent())
							detailsPanel.revalidate()
							detailsPanel.repaint()
							Environment.contentManager.repaint()
						}
						paused = true
						while(paused){
							synchronized (lockHolder) {
								lockHolder.wait()
							}
						}
						debugPanel = null
						scriptBinding = null
						builder.edt{
							resumeButton.enabled = false
							evaluateButton.enabled = false
							buttons.remove(evaluateButton)
							detailsPanel.removeAll()
							detailsPanel.add(messagePanel)
							detailsPanel.revalidate()
							detailsPanel.repaint()
							Environment.contentManager.repaint()
						}
					}
				})
		workingThread = Thread.startDaemon('script') { script.start() }
		workingThread.setDefaultUncaughtExceptionHandler({t,ex ->
			builder.edt{
				addAndShowMessage(new Message(type: MessageType.ERROR, message: 'произошла непредвиденная ошибка' + ex.getMessage()))
				stopButton.enabled = false
				resumeButton.enabled = false
				evaluateButton.enabled = false
				progressBar.value = 100
				messageLabel.text = 'Операция прервана'
				component.repaint()
				Environment.contentManager.repaint()
			}
		} as Thread.UncaughtExceptionHandler)
		stopButton.enabled = true
	}

	void evaluateExpression(){
		if(!debugPanel || !scriptBinding){
			return
		}
		try{
			def String script = debugPanel.getScript()
			def GroovyShell shell = new GroovyShell(scriptBinding)
			def result = shell.evaluate(script)
			debugPanel.setResult("Скрипт успешно выполнен, результат:\n${result? Utils.toJsonString(result): 'отсутствует'}".toString())
		} catch (Exception e) {
			debugPanel.setResult('Произошла ошибка: ' + e.getMessage())
		}
	}

	@Override
	public void updateContent() {
		//NOOP
	}

	@Override
	public boolean isNavigationAllowed() {
		return workFinished
	}
}

