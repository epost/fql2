package catdata.ide;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.text.html.*;

// The Simple Web Browser.
@SuppressWarnings({ "rawtypes", "serial" })
class MiniBrowser extends JFrame implements HyperlinkListener {
	// These are the buttons for iterating through the page list.
	private final JButton backButton, forwardButton;

	// Page location text field.
	private final JTextField locationTextField;

	// Editor pane for displaying pages.
	private final JEditorPane displayEditorPane;

	// Browser's list of pages that have been visited.
	private final ArrayList pageList = new ArrayList();

	// Constructor for Mini Web Browser.
    private MiniBrowser() {
		// Set application title.
		super("Mini Browser");

		// Set window size.
		setSize(800, 480);

		// Handle closing events.
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				actionExit();
			}
		});

		// Set up file menu.
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenuItem fileExitMenuItem = new JMenuItem("Close", KeyEvent.VK_X);
		fileExitMenuItem.addActionListener(e -> actionExit());
		fileMenu.add(fileExitMenuItem);
		menuBar.add(fileMenu);
		setJMenuBar(menuBar);

		// Set up button panel.
		JPanel buttonPanel = new JPanel();
		backButton = new JButton("< Back");
		backButton.addActionListener(e -> actionBack());
		backButton.setEnabled(false);
		buttonPanel.add(backButton);
		forwardButton = new JButton("Forward >");
		forwardButton.addActionListener(e -> actionForward());
		forwardButton.setEnabled(false);
		buttonPanel.add(forwardButton);
		locationTextField = new JTextField(35);
		locationTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					actionGo();
				}
			}
		});
		buttonPanel.add(locationTextField);
		JButton goButton = new JButton("GO");
		goButton.addActionListener(e -> actionGo());
		buttonPanel.add(goButton);

		// Set up page display.
		displayEditorPane = new JEditorPane();
		displayEditorPane.setContentType("text/html");
		displayEditorPane.setEditable(false);
		displayEditorPane.addHyperlinkListener(this);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(buttonPanel, BorderLayout.NORTH);
		getContentPane().add(new JScrollPane(displayEditorPane), BorderLayout.CENTER);
	}

	

	// Go back to the page viewed before the current page.
	private void actionBack() {
		URL currentUrl = displayEditorPane.getPage();

		int pageIndex = pageList.indexOf(currentUrl.toString());
		try {
			showPage(new URL((String) pageList.get(pageIndex - 1)), false);
		} catch (MalformedURLException e) {
		}
	}

	// Go forward to the page viewed after the current page.
	private void actionForward() {
		URL currentUrl = displayEditorPane.getPage();
		int pageIndex = pageList.indexOf(currentUrl.toString());
		try {
			showPage(new URL((String) pageList.get(pageIndex + 1)), false);
		} catch (MalformedURLException e) {
		}
	}

	// Load and show the page specified in the location text field.
	private void actionGo() {
		URL verifiedUrl = verifyUrl(locationTextField.getText());
		if (verifiedUrl != null) {
			showPage(verifiedUrl, true);
		} else {
			showError("Invalid URL");
		}
	}

	// Show dialog box with error message.
	private void showError(String errorMessage) {
		JOptionPane.showMessageDialog(this, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
	}

	// Verify URL format.
	private static URL verifyUrl(String url) {
		// Only allow HTTP URLs.
		if (!url.toLowerCase().startsWith("http://"))
			return null;

		// Verify format of URL.
		URL verifiedUrl = null;
		try {
			verifiedUrl = new URL(url);
		} catch (MalformedURLException e) {
			
		}

		return verifiedUrl;
	}

	/*
	 * Show the specified page and add it to the page list if specified.
	 */
	@SuppressWarnings("unchecked")
	private void showPage(URL pageUrl, boolean addToList) {
		// Show hour glass cursor while crawling is under way.
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		try {
			// Get URL of page currently being displayed.
			URL currentUrl = displayEditorPane.getPage();

			// Load and display specified page.
			displayEditorPane.setPage(pageUrl);

			// Get URL of new page being displayed.
		//	URL newUrl = displayEditorPane.getPage();

			// Add page to list if specified.
			if (addToList) {
				int listSize = pageList.size();
				if (listSize > 0) {
					int pageIndex = pageList.indexOf(currentUrl.toString());
					if (pageIndex < listSize - 1) {
						for (int i = listSize - 1; i > pageIndex; i--) {
							pageList.remove(i);
						}
					}
				}
				
				pageList.add(pageUrl.toString());
				
			}

			// Update location text field with URL of current page.
			
				locationTextField.setText(pageUrl.toString());
			
			// Update buttons based on the page being displayed.
			updateButtons();
		} catch (IOException e) {
			e.printStackTrace();
			showError("Unable to load page");
		} finally {
			// Return to default cursor.
			setCursor(Cursor.getDefaultCursor());
		}
	}

	/*
	 * Update back and forward buttons based on the page being displayed.
	 */
	private void updateButtons() {
//		if (pageList.size() < 2) {
			backButton.setEnabled(true);
			forwardButton.setEnabled(true);
	/*	} else {
			URL currentUrl = displayEditorPane.getPage();
			int pageIndex = pageList.indexOf(currentUrl.toString());
			backButton.setEnabled(pageIndex > 0);
			forwardButton.setEnabled(pageIndex < (pageList.size() - 1));
		} */
	}

	// Handle hyperlink's being clicked.
	@Override
	public void hyperlinkUpdate(HyperlinkEvent event) {
		EventType eventType = event.getEventType();
		if (Objects.equals(eventType, EventType.ACTIVATED)) {
			if (event instanceof HTMLFrameHyperlinkEvent) {
				HTMLFrameHyperlinkEvent linkEvent = (HTMLFrameHyperlinkEvent) event;
				HTMLDocument document = (HTMLDocument) displayEditorPane.getDocument();
				document.processHTMLFrameHyperlinkEvent(linkEvent);
			} else {
				showPage(event.getURL(), true);
			}
		}
	}

	// Run the Mini Browser.
	@SuppressWarnings("deprecation")
	public static void main(String... args)  {
		MiniBrowser browser = new MiniBrowser();
		browser.show();
	}
	
	// Exit this program.
		private static void actionExit() {
			System.exit(1);
		}
}