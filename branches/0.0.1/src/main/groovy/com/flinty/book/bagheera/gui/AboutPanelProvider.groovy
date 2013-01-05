package com.flinty.book.bagheera.gui

import java.awt.Desktop

import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JEditorPane
import javax.swing.JOptionPane
import javax.swing.JScrollPane
import javax.swing.event.HyperlinkEvent
import javax.swing.event.HyperlinkListener
import javax.swing.text.html.HTMLDocument
import javax.swing.text.html.HTMLFrameHyperlinkEvent

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import com.flinty.book.bagheera.common.Utils
import com.flinty.book.bagheera.model.IContentProvider


/**
 * GUI class representing About page with Html help content
 * It is a singleton as it has no state or specific related resources 
 */
class AboutPanelProvider implements IContentProvider{

	private static final Log log = LogFactory.getLog(AboutPanelProvider.class)
	private static AboutPanelProvider instance

	static getInstance() {
		if(!instance){
			instance = new AboutPanelProvider()
		}
		return instance
	}

	private final JComponent component

	private final HelpPane pane

	private final URL indexURL = new File('help/index.html').toURI().toURL()

	private final JButton homeButton = Utils.createToolButton('В начало', '/images/home.png', { pane.setPage(indexURL) })

	private AboutPanelProvider(){
		pane = new HelpPane()
		component = new JScrollPane(pane)
		pane.setPage(indexURL)
	}


	@Override
	public JComponent getContent() {
		return component
	}

	@Override
	public List<JButton> getToolbarButtons() {
		return [homeButton]
	}

	@Override
	public void updateContent() {
		pane.setPage(indexURL)
	}


	@Override
	public boolean isNavigationAllowed() {
		return true
	}

	/**
	 * Pane for representing Html content. It overrides standard 
	 * JEditorPane behavior to correctly handle html links transitions 
	 */
	static class HelpPane extends JEditorPane{

		HelpPane(){
			setEditable(false)
			setContentType('text/html')
			addHyperlinkListener(new HyperlinkListener() {

						public void hyperlinkUpdate(HyperlinkEvent e) {
							URL url = e.getURL()
							if(!url || e.getEventType() != HyperlinkEvent.EventType.ACTIVATED){
								return
							}
							if('http'.equals(url.getProtocol())){
								String urlStr = JOptionPane.showInputDialog(JOptionPane.getFrameForComponent(HelpPane.this), 'Открыть страничку в браузере?', url.toString())
								if(urlStr){
									try {
										Desktop.getDesktop().browse(URI.create(urlStr))
									} catch (Exception t) {
										JOptionPane.showMessageDialog(JOptionPane.getFrameForComponent(HelpPane.this), 'Не удалось открыть WEB-страницу', 'Ошибка', JOptionPane.ERROR_MESSAGE)
									}
								}
								return
							}
							if(!'file'.equals(url.getProtocol())){
								return
							}
							JEditorPane pane = (JEditorPane) e.getSource()
							if (e instanceof HTMLFrameHyperlinkEvent) {
								HTMLFrameHyperlinkEvent  evt = (HTMLFrameHyperlinkEvent)e
								HTMLDocument doc = (HTMLDocument)pane.getDocument()
								doc.processHTMLFrameHyperlinkEvent(evt)
							} else {
								try {
									pane.setPage(e.getURL())
								} catch (Throwable t) {
									AboutPanelProvider.log.error('unable to set page to ' + e.getURL(), t)
								}
							}
						}
					}
					)
		}



		protected InputStream getStream(URL url){
			if(!'file'.equals(url.getProtocol())){
				return super.getStream(page)
			}
			String content = url.getText('utf-8')
			if(!content){
				return new ByteArrayInputStream(new byte[0])
			}
			int idx = content.indexOf('<html')
			return new ByteArrayInputStream((idx != -1? content.substring(idx): content).getBytes('utf-8'))
		}
	}
}

