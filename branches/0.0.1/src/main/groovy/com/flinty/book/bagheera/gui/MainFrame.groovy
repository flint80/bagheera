package com.flinty.book.bagheera.gui



import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GraphicsEnvironment
import java.awt.Point
import java.awt.Rectangle


import groovy.swing.SwingBuilder

import javax.imageio.ImageIO
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JFrame
import javax.swing.JToolBar

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import com.flinty.book.bagheera.common.Utils
import com.flinty.book.bagheera.model.Environment
import com.flinty.book.bagheera.model.IContentManager
import com.flinty.book.bagheera.model.IContentProvider




/**
 * Main frame of the program: container for all content providers 
 */
class MainFrame{

	def JComponent contentPanel

	def JFrame frame
	def JToolBar toolBar
	def List<JButton> customButtons = []
	def List<JButton> rightSideButtons = []

	def LinkedList<IContentProvider> history = []

	def JButton backButton

	def JComponent glue = Box.createHorizontalGlue()

	def Log log = LogFactory.getLog(getClass().getName())

	MainFrame(){
		log.info('mainframe')

		def swingBuilder = new SwingBuilder()

		rightSideButtons << Utils.createToolButton('О программе', '/images/info.png', {
			Environment.contentManager.updateProvider(AboutPanelProvider.getInstance())
		} )

		def addMainToolbar = {
			toolBar = swingBuilder.toolBar(constraints: BorderLayout.NORTH, floatable: false,preferredSize: new Dimension(500, 50), maximumSize: new Dimension(32767, 50)){
				boxLayout(axis:BoxLayout.X_AXIS)
				button(Utils.createToolButtonParams('Выход', '/images/exit.png', {
					frame.dispose()
					System.exit(0)
				} ))
				backButton = button(Utils.createToolButtonParams('Назад', '/images/undo.png', { navigateToPreviousView() } ))
				backButton.enabled = false
				separator()
			}
		}

		def addContentPanel = {
			contentPanel = swingBuilder.panel(constraints: BorderLayout.CENTER){ borderLayout() }
		}

		def Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint()

		def int width = Environment.configurationManager.getIntProperty(getClass().getName() +'.width', 400)
		def int height = Environment.configurationManager.getIntProperty(getClass().getName() +'.height', 300)
		frame = swingBuilder.frame(title:"Багира",
				defaultCloseOperation:JFrame.EXIT_ON_CLOSE,
				bounds:new Rectangle(
				(int) (center.x - width/2),
				(int) (center.y - height/2),
				width,
				height),
				componentResized :{
					Environment.configurationManager.setIntProperty(MainFrame.class.getName() +'.width', (int) frame.getSize().width)
					Environment.configurationManager.setIntProperty(MainFrame.class.getName() +'.height',(int) frame.getSize().height)
				},
				windowClosing:{
					Environment.configurationManager.setIntProperty(MainFrame.class.getName() +'.width', (int) frame.getSize().width)
					Environment.configurationManager.setIntProperty(MainFrame.class.getName() +'.height', (int) frame.getSize().height)
				},
				iconImage: ImageIO.read(getClass().getResource('/images/bagheera.png'))) {
					addMainToolbar()
					addContentPanel()
				}
	}


	IContentManager createContentManager(){
		return new IContentManager(){

			void updateProvider(IContentProvider provider){
				if(history.size != 0 && history.last.class == provider.class){
					return
				}
				history <<provider
				backButton.enabled = history.size() > 1
				updateContent(provider)
			}

			void repaint(){
				if(history.size == 0){
					return
				}
				updateContent(history.last)
			}

			void updateContent(IContentProvider provider){
				provider.updateContent()
				contentPanel.removeAll()
				contentPanel<<provider.getContent()
				customButtons.each { toolBar.remove(it) }
				rightSideButtons.each { toolBar.remove(it) }
				toolBar.remove(glue)
				List<JButton> buttons= provider.getToolbarButtons()
				customButtons.clear()
				if(buttons){
					buttons.each {
						toolBar.add(it)
						customButtons << it
					}
				}
				toolBar.add(glue)
				rightSideButtons.each {
					toolBar.add(it)
					it.setEnabled(provider.isNavigationAllowed())
				}
				backButton.setEnabled(provider.isNavigationAllowed())
				contentPanel.revalidate()
				contentPanel.repaint()
				toolBar.revalidate()
				toolBar.repaint()
			}
		}
	}
	void navigateToPreviousView(){
		if(history.size > 1){
			IContentProvider prev = history[history.size - 2]
			history.removeLast()
			history.removeLast()
			Environment.contentManager.updateProvider(prev)
		}
	}
}

