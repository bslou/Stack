package application;
	
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.Paragraph;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.collection.ListModification;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;


public class Main extends Application {
	Scene programmingScene;
	private static final String[] KEYWORDS = new String[] {
            "alignas", "alignof", "and", "and_eq", "asm", "atomic_cancel",
            "atomic_commit", "atomica_noexcept", "auto", "bitand", "bitor", 
            "bool", "break", "case", "catch", "char", "char8_t", "char16_t",
            "char32_t", "cin", "class", "compl", "concept", "const", "consteval",
            "constexpr", "constinit", "const_cast", "continue", "cout", "co_await",
            "co_return", "co_yield", "decltype", "default", "delete", "do", 
            "double", "dynamic_cast", "else", "enum", "explicit", "export", 
            "extern", "false", "float", "for", "friend", "goto", "if", "inline", 
            "int", "long", "mutable", "namespace", "new", "noexcept", "not", "not_eq", 
            "nullptr", "operator", "or", "or_eq", "private", "protected", "public", 
            "reflexpr", "register", "reinterpret_cast", "requires", "return", "short", 
            "signed", "sizeof", "static", "static_assert", "static_cast", "std", "struct", 
            "switch", "synchronized", "template", "this", "thread_local", "throw", 
            "true", "try", "typedef", "typeid", "typename", "union", "unsigned", 
            "using", "virtual", "void", "volatile", "wchar_t", "while", "xor", "xor_eq"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String LEGE_PATTERN = "<([^\"\\\\]|\\\\.)*>";
    private static final String HASH_PATTERN = "#([A-Za-z])*";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/"   // for whole text processing (text blocks)
    		                          + "|" + "/\\*[^\\v]*" + "|" + "^\\h*\\*([^\\v]*|/)";  // for visible paragraph processing (line by line)

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
            + "|(?<HASH>" + HASH_PATTERN + ")"
            + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
            + "|(?<LEGE>" + LEGE_PATTERN + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    private static final String sampleCode = String.join("\n", new String[] {
        "#include <iostream>",
        "using namespace std;",
        "",
        "",
        "int main() {",
        "   cout << \"Hello World\";",
        "   return 0;",
        "}"
    });
    
    //change code below
    private static final String sampleHeaderCode = String.join("\n", new String[] {
            "class main{", 
            "public:",
            "   ",
            "",
            "}",
            "#endif"
        });
    
    private static final String sampleArduinoCode = String.join("\n", new String[] {
            "void setup(){",
            "   //put your setup code here (once)",
            "}",
            "",
            "void loop(){",
            "   //put your main code here (repeatedly)",
            "}",
        });
	
    Hashtable<String, String> mydict = new Hashtable<String, String>();
    ListView lov;
    ListView lom;
    TextField t;
    TextField t2;
    TextField t3;
    String pathOfFile;
    
	@Override
	public void start(Stage primaryStage) {
		//String sampleHeaderCode = "class " + t3.getText() + "{\npublic:\n\n}";
		CodeArea codeArea = new CodeArea();

        // add line numbers to the left of area
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.setContextMenu( new DefaultContextMenu() );

        codeArea.getVisibleParagraphs().addModificationObserver
        (
            new VisibleParagraphStyler<>( codeArea, this::computeHighlighting )
        );

        // auto-indent: insert previous line's indents on enter
        final Pattern whiteSpace = Pattern.compile( "^\\s+" );
        codeArea.addEventHandler( KeyEvent.KEY_PRESSED, KE ->
        {
            if ( KE.getCode() == KeyCode.ENTER ) {
            	int caretPosition = codeArea.getCaretPosition();
            	int currentParagraph = codeArea.getCurrentParagraph();
                Matcher m0 = whiteSpace.matcher( codeArea.getParagraph( currentParagraph-1 ).getSegments().get( 0 ) );
                if ( m0.find() ) Platform.runLater( () -> codeArea.insertText( caretPosition, m0.group() ) );
            }
        });

        //codeArea.setStyle("");
        
		
		File stack = new File("StackProjects");
		if (!(stack.exists())) {
			stack.mkdirs();
		}
		
		TabPane tabPane = new TabPane();
		
		
		ToolBar toolBar = new ToolBar();
		
		MenuButton mb = new MenuButton();
		mb.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/proj.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:25px; -fx-min-height:25px;");
		toolBar.getItems().add(mb);
		
		Button save = new Button();
		save.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/saveIt.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:25px; -fx-min-height:25px;");
		toolBar.getItems().addAll(save, new Separator());
		
		Button play = new Button();
        play.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/playBtn.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:25px; -fx-min-height:25px;");
        toolBar.getItems().add(play);
        
        
        
        Button stop = new Button();
        
        stop.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/terminate.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:25px; -fx-min-height:25px;");
        toolBar.getItems().addAll(stop, new Separator());
        
        Button undo = new Button();
        undo.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/icons8-undo-48.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:25px; -fx-min-height:25px;");
        toolBar.getItems().add(undo);
        
        
        Button redo = new Button();
        redo.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/icons8-redo-48.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:25px; -fx-min-height:25px;");
        toolBar.getItems().add(redo);
        
		TreeItem<String> rootItem = new TreeItem<String>();

		TreeView tv = new TreeView(rootItem);
		MenuItem entry1 = new MenuItem("New Item");
		
		Button cr = new Button("Add");
		cr.setOnAction(e -> {
			String enFile = lom.getSelectionModel().getSelectedItem().toString();
        	//System.out.println(enFile);
        	if (enFile == null) {
        		return;
        	}
        	
    	    String entered = t3.getText();
    	    
    	    if (entered == ""){
    	    	return;
    	    }
    	    if (!entered.matches("[A-Za-z_]*")) {
    	    	return;
    	    }
    	    
    	    if (enFile == "Source File") {
    	    	String[] filesLoop;
    	    	File doo = new File(pathOfFile);
    	    	filesLoop = doo.list();
    	    	//System.out.println(filesLoop);
    	    	boolean exists = false;
    	    	for (int i = 0; i < filesLoop.length; i++) {
    	    		if (filesLoop[i].equals("Source Files")) {
    	    			//System.out.println("tv");
    	    			File noop = new File(pathOfFile + "/Source Files");
    	    			String[] full = noop.list();
    	    			for (int m = 0; m < full.length; m++) {
    	    				if (full[m].equals(t3.getText() + ".cpp")) {
    	    					return;
    	    				}
    	    			}
    	    			TreeItem tone = new TreeItem(t3.getText() + ".cpp");
    	    			Label main = new Label();
    			    	main.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/cppFile.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");
    			    	tone.setGraphic(main);
    	    			rootItem.getChildren().get(i).getChildren().add(tone);
    	    			exists = true;
    	    			
    	    			File newF = new File(noop, t3.getText() + ".cpp");
    	    			try {
							newF.createNewFile();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
    	    			File myFoo = new File(pathOfFile + "/Source Files/" + t3.getText() + ".cpp");
    	        		FileWriter fooWriter;
    					try {
    						fooWriter = new FileWriter(myFoo, false);
    						fooWriter.write(sampleCode);
    		        		fooWriter.close();
    					} catch (IOException e1) {
    						// TODO Auto-generated catch block
    						e1.printStackTrace();
    					} 
    	    			break;
    	    		}
    	    		
    	    		
    	    	}
    	    	if (exists == false) {
    	    		TreeItem roo = new TreeItem("Source Files");
    	    		rootItem.getChildren().add(roo);
    	    		TreeItem tone = new TreeItem(t3.getText() + ".cpp");
	    			Label main = new Label();
			    	main.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/cppFile.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");
			    	tone.setGraphic(main);
			    	Label folder = new Label();
			    	folder.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/folder.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");

			    	roo.setGraphic(folder);
	    			roo.getChildren().add(tone);
	    			
	    			File newFo = new File(pathOfFile, "Source Files");
	    			newFo.mkdirs();
	    			File newF = new File(newFo, t3.getText() + ".cpp");
	    			try {
						newF.createNewFile();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	    			File myFoo = new File(pathOfFile + "/Source Files/" + t3.getText() + ".cpp");
	        		FileWriter fooWriter;
					try {
						fooWriter = new FileWriter(myFoo, false);
						fooWriter.write(sampleCode);
		        		fooWriter.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
			    	
    	    	}
    	    	CodeArea codeArea1 = new CodeArea();

	            // add line numbers to the left of area
	            codeArea1.setParagraphGraphicFactory(LineNumberFactory.get(codeArea1));
	            codeArea1.setContextMenu( new DefaultContextMenu() );

	            codeArea1.getVisibleParagraphs().addModificationObserver
	            (
	                new VisibleParagraphStyler<>( codeArea1, this::computeHighlighting )
	            );

	            // auto-indent: insert previous line's indents on enter
	            codeArea1.addEventHandler( KeyEvent.KEY_PRESSED, KE ->
	            {
	                if ( KE.getCode() == KeyCode.ENTER ) {
	                	int caretPosition = codeArea1.getCaretPosition();
	                	int currentParagraph = codeArea1.getCurrentParagraph();
	                    Matcher m0 = whiteSpace.matcher( codeArea1.getParagraph( currentParagraph-1 ).getSegments().get( 0 ) );
	                    if ( m0.find() ) Platform.runLater( () -> codeArea1.insertText( caretPosition, m0.group() ) );
	                }
	            });
	            codeArea1.replaceText(0, 0, sampleCode);
	    		Tab tab2 = new Tab(t3.getText() + ".cpp", new VirtualizedScrollPane<>(codeArea1));
	    		
	    		tabPane.getTabs().add(tab2);
    	    	
    	    }else if (enFile == "Header File") {
    	    	//header.jpg
    	    	String[] filesLoop;
    	    	File doo = new File(pathOfFile);
    	    	filesLoop = doo.list();
    	    	//System.out.println(filesLoop);
    	    	boolean exists = false;
    	    	for (int i = 0; i < filesLoop.length; i++) {
    	    		if (filesLoop[i].equals("Header Files")) {
    	    			//System.out.println("tv");
    	    			File noop = new File(pathOfFile + "/Header Files");
    	    			String[] full = noop.list();
    	    			for (int m = 0; m < full.length; m++) {
    	    				if (full[m].equals(t3.getText() + ".h")) {
    	    					return;
    	    				}
    	    			}
    	    			TreeItem tone = new TreeItem(t3.getText() + ".h");
    	    			Label main = new Label();
    			    	main.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/header.jpg'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");
    			    	tone.setGraphic(main);
    	    			rootItem.getChildren().get(i).getChildren().add(tone);
    	    			exists = true;
    	    			
    	    			File newF = new File(noop, t3.getText() + ".h");
    	    			try {
							newF.createNewFile();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
    	    			File myFoo = new File(pathOfFile + "/Header Files/" + t3.getText() + ".h");
    	        		FileWriter fooWriter;
    					try {
    						fooWriter = new FileWriter(myFoo, false);
    						fooWriter.write(sampleHeaderCode);
    		        		fooWriter.close();
    					} catch (IOException e1) {
    						// TODO Auto-generated catch block
    						e1.printStackTrace();
    					} 
    	    			break;
    	    		}
    	    		
    	    		
    	    	}
    	    	if (exists == false) {
    	    		TreeItem roo = new TreeItem("Header Files");
    	    		rootItem.getChildren().add(roo);
    	    		TreeItem tone = new TreeItem(t3.getText() + ".h");
	    			Label main = new Label();
			    	main.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/header.jpg'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");
			    	tone.setGraphic(main);
			    	Label folder = new Label();
			    	folder.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/folder.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");

			    	roo.setGraphic(folder);
	    			roo.getChildren().add(tone);
	    			
	    			File newFo = new File(pathOfFile, "Header Files");
	    			newFo.mkdirs();
	    			File newF = new File(newFo, t3.getText() + ".h");
	    			try {
						newF.createNewFile();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	    			File myFoo = new File(pathOfFile + "/Header Files/" + t3.getText() + ".h");
	        		FileWriter fooWriter;
					try {
						fooWriter = new FileWriter(myFoo, false);
						fooWriter.write(sampleHeaderCode);
		        		fooWriter.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
			    	
    	    	}
    	    	CodeArea codeArea1 = new CodeArea();

	            // add line numbers to the left of area
	            codeArea1.setParagraphGraphicFactory(LineNumberFactory.get(codeArea1));
	            codeArea1.setContextMenu( new DefaultContextMenu() );

	            codeArea1.getVisibleParagraphs().addModificationObserver
	            (
	                new VisibleParagraphStyler<>( codeArea1, this::computeHighlighting )
	            );

	            // auto-indent: insert previous line's indents on enter
	            codeArea1.addEventHandler( KeyEvent.KEY_PRESSED, KE ->
	            {
	                if ( KE.getCode() == KeyCode.ENTER ) {
	                	int caretPosition = codeArea1.getCaretPosition();
	                	int currentParagraph = codeArea1.getCurrentParagraph();
	                    Matcher m0 = whiteSpace.matcher( codeArea1.getParagraph( currentParagraph-1 ).getSegments().get( 0 ) );
	                    if ( m0.find() ) Platform.runLater( () -> codeArea1.insertText( caretPosition, m0.group() ) );
	                }
	            });
	            
	            codeArea1.replaceText(0, 0, sampleHeaderCode);
	    		Tab tab2 = new Tab(t3.getText() + ".h", new VirtualizedScrollPane<>(codeArea1));
	    		
	    		tabPane.getTabs().add(tab2);
    	    }else if (enFile == "Header and Source Files") {
    	    	
    	    	String[] filesLoop;
    	    	File doo = new File(pathOfFile);
    	    	filesLoop = doo.list();
    	    	//System.out.println(filesLoop);
    	    	boolean exists = false;
    	    	for (int i = 0; i < filesLoop.length; i++) {
    	    		if (filesLoop[i].equals("Source Files")) {
    	    			//System.out.println("tv");
    	    			File noop = new File(pathOfFile + "/Source Files");
    	    			String[] full = noop.list();
    	    			for (int m = 0; m < full.length; m++) {
    	    				if (full[m].equals(t3.getText() + ".cpp")) {
    	    					return;
    	    				}
    	    			}
    	    			TreeItem tone = new TreeItem(t3.getText() + ".cpp");
    	    			Label main = new Label();
    			    	main.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/cppFile.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");
    			    	tone.setGraphic(main);
    	    			rootItem.getChildren().get(i).getChildren().add(tone);
    	    			exists = true;
    	    			
    	    			File newF = new File(noop, t3.getText() + ".cpp");
    	    			try {
							newF.createNewFile();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
    	    			File myFoo = new File(pathOfFile + "/Source Files/" + t3.getText() + ".cpp");
    	        		FileWriter fooWriter;
    					try {
    						fooWriter = new FileWriter(myFoo, false);
    						fooWriter.write(sampleCode);
    		        		fooWriter.close();
    					} catch (IOException e1) {
    						// TODO Auto-generated catch block
    						e1.printStackTrace();
    					} 
    	    			break;
    	    		}
    	    		
    	    		
    	    	}
    	    	if (exists == false) {
    	    		TreeItem roo = new TreeItem("Source Files");
    	    		rootItem.getChildren().add(roo);
    	    		TreeItem tone = new TreeItem(t3.getText() + ".cpp");
	    			Label main = new Label();
			    	main.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/cppFile.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");
			    	tone.setGraphic(main);
			    	Label folder = new Label();
			    	folder.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/folder.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");

			    	roo.setGraphic(folder);
	    			roo.getChildren().add(tone);
	    			
	    			File newFo = new File(pathOfFile, "Source Files");
	    			newFo.mkdirs();
	    			File newF = new File(newFo, t3.getText() + ".cpp");
	    			try {
						newF.createNewFile();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	    			File myFoo = new File(pathOfFile + "/Source Files/" + t3.getText() + ".cpp");
	        		FileWriter fooWriter;
					try {
						fooWriter = new FileWriter(myFoo, false);
						fooWriter.write(sampleCode);
		        		fooWriter.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
			    	
    	    	}
    	    	CodeArea codeArea1 = new CodeArea();

	            // add line numbers to the left of area
	            codeArea1.setParagraphGraphicFactory(LineNumberFactory.get(codeArea1));
	            codeArea1.setContextMenu( new DefaultContextMenu() );

	            codeArea1.getVisibleParagraphs().addModificationObserver
	            (
	                new VisibleParagraphStyler<>( codeArea1, this::computeHighlighting )
	            );

	            // auto-indent: insert previous line's indents on enter
	            codeArea1.addEventHandler( KeyEvent.KEY_PRESSED, KE ->
	            {
	                if ( KE.getCode() == KeyCode.ENTER ) {
	                	int caretPosition = codeArea1.getCaretPosition();
	                	int currentParagraph = codeArea1.getCurrentParagraph();
	                    Matcher m0 = whiteSpace.matcher( codeArea1.getParagraph( currentParagraph-1 ).getSegments().get( 0 ) );
	                    if ( m0.find() ) Platform.runLater( () -> codeArea1.insertText( caretPosition, m0.group() ) );
	                }
	            });
	            codeArea1.replaceText(0, 0, sampleCode);
	    		Tab tab2 = new Tab(t3.getText() + ".cpp", new VirtualizedScrollPane<>(codeArea1));
	    		
	    		tabPane.getTabs().add(tab2);
    	    	
    	    	
    	    	
    	    	
    	    	
    	    	
    	    	
    	    	
    	    	
    	    	String[] filesLoop2;
    	    	File doo2 = new File(pathOfFile);
    	    	filesLoop2 = doo.list();
    	    	//System.out.println(filesLoop);
    	    	boolean exists2 = false;
    	    	for (int i = 0; i < filesLoop2.length; i++) {
    	    		if (filesLoop2[i].equals("Header Files")) {
    	    			//System.out.println("tv");
    	    			File noop2 = new File(pathOfFile + "/Header Files");
    	    			String[] full2 = noop2.list();
    	    			for (int m = 0; m < full2.length; m++) {
    	    				if (full2[m].equals(t3.getText() + ".h")) {
    	    					return;
    	    				}
    	    			}
    	    			TreeItem tone2 = new TreeItem(t3.getText() + ".h");
    	    			Label main2 = new Label();
    			    	main2.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/header.jpg'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");
    			    	tone2.setGraphic(main2);
    	    			rootItem.getChildren().get(i).getChildren().add(tone2);
    	    			exists2 = true;
    	    			
    	    			File newF2 = new File(noop2, t3.getText() + ".h");
    	    			try {
							newF2.createNewFile();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
    	    			File myFoo2 = new File(pathOfFile + "/Header Files/" + t3.getText() + ".h");
    	        		FileWriter fooWriter2;
    					try {
    						fooWriter2 = new FileWriter(myFoo2, false);
    						fooWriter2.write(sampleHeaderCode);
    		        		fooWriter2.close();
    					} catch (IOException e1) {
    						// TODO Auto-generated catch block
    						e1.printStackTrace();
    					} 
    	    			break;
    	    		}
    	    		
    	    		
    	    	}
    	    	if (exists2 == false) {
    	    		TreeItem roo2 = new TreeItem("Header Files");
    	    		rootItem.getChildren().add(roo2);
    	    		TreeItem tone2 = new TreeItem(t3.getText() + ".h");
	    			Label main2 = new Label();
			    	main2.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/header.jpg'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");
			    	tone2.setGraphic(main2);
			    	Label folder2 = new Label();
			    	folder2.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/folder.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");

			    	roo2.setGraphic(folder2);
	    			roo2.getChildren().add(tone2);
	    			
	    			File newFo2 = new File(pathOfFile, "Header Files");
	    			newFo2.mkdirs();
	    			File newF2 = new File(newFo2, t3.getText() + ".h");
	    			try {
						newF2.createNewFile();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	    			File myFoo2 = new File(pathOfFile + "/Header Files/" + t3.getText() + ".h");
	        		FileWriter fooWriter2;
					try {
						fooWriter2 = new FileWriter(myFoo2, false);
						fooWriter2.write(sampleHeaderCode);
		        		fooWriter2.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
			    	
    	    	}
    	    	CodeArea codeArea2 = new CodeArea();

	            // add line numbers to the left of area
	            codeArea2.setParagraphGraphicFactory(LineNumberFactory.get(codeArea2));
	            codeArea2.setContextMenu( new DefaultContextMenu() );

	            codeArea2.getVisibleParagraphs().addModificationObserver
	            (
	                new VisibleParagraphStyler<>( codeArea2, this::computeHighlighting )
	            );

	            // auto-indent: insert previous line's indents on enter
	            codeArea2.addEventHandler( KeyEvent.KEY_PRESSED, KE ->
	            {
	                if ( KE.getCode() == KeyCode.ENTER ) {
	                	int caretPosition = codeArea2.getCaretPosition();
	                	int currentParagraph = codeArea2.getCurrentParagraph();
	                    Matcher m0 = whiteSpace.matcher( codeArea2.getParagraph( currentParagraph-1 ).getSegments().get( 0 ) );
	                    if ( m0.find() ) Platform.runLater( () -> codeArea2.insertText( caretPosition, m0.group() ) );
	                }
	            });
	            codeArea2.replaceText(0, 0, sampleHeaderCode);
	    		Tab tab3 = new Tab(t3.getText() + ".h", new VirtualizedScrollPane<>(codeArea2));
	    		
	    		tabPane.getTabs().add(tab3);
    	    }else if (enFile == "Interface") {
    	    	
    	    }else if (enFile == "Class") {
    	    	
    	    }else if (enFile == "Text File") {
    	    	String[] filesLoop;
    	    	File doo = new File(pathOfFile);
    	    	filesLoop = doo.list();
    	    	boolean exists = false;
    	    	for (int i = 0; i < filesLoop.length; i++) {
    	    		if (filesLoop[i].equals("Text Files")) {
    	    			File noop = new File(pathOfFile + "/Text Files");
    	    			String[] full = noop.list();
    	    			for (int m = 0; m < full.length; m++) {
    	    				if (full[m].equals(t3.getText() + ".txt")) {
    	    					return;
    	    				}
    	    			}
    	    			TreeItem tone = new TreeItem(t3.getText() + ".txt");
    	    			Label main = new Label();
    			    	main.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/cppFile.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");
    			    	tone.setGraphic(main);
    	    			rootItem.getChildren().get(i).getChildren().add(tone);
    	    			exists = true;
    	    			File newF = new File(noop, t3.getText() + ".txt");
    	    			try {
							newF.createNewFile();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
    	    			File myFoo = new File(pathOfFile + "/Text Files/" + t3.getText() + ".txt");
    	        		FileWriter fooWriter;
    					try {
    						fooWriter = new FileWriter(myFoo, false);
    						fooWriter.write("");
    		        		fooWriter.close();
    					} catch (IOException e1) {
    						// TODO Auto-generated catch block
    						e1.printStackTrace();
    					} 
    	    			break;
    	    		}
    	    		
    	    		
    	    	}
    	    	if (exists == false) {
    	    		TreeItem roo = new TreeItem("Text Files");
    	    		rootItem.getChildren().add(roo);
    	    		TreeItem tone = new TreeItem(t3.getText() + ".txt");
	    			Label main = new Label();
			    	main.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/cppFile.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");
			    	tone.setGraphic(main);
			    	Label folder = new Label();
			    	folder.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/folder.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");

			    	roo.setGraphic(folder);
	    			roo.getChildren().add(tone);
	    			
	    			File newFo = new File(pathOfFile, "Text Files");
	    			newFo.mkdirs();
	    			File newF = new File(newFo, t3.getText() + ".txt");
	    			try {
						newF.createNewFile();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	    			File myFoo = new File(pathOfFile + "/Text Files/" + t3.getText() + ".txt");
	        		FileWriter fooWriter;
					try {
						fooWriter = new FileWriter(myFoo, false);
						fooWriter.write("");
		        		fooWriter.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
			    	
    	    	}
    	    	TextArea textarea1 = new TextArea();
    	    	textarea1.replaceText(0, 0, "");
    	    	
	    		Tab tab2 = new Tab(t3.getText() + ".txt", textarea1);
	    		
	    		tabPane.getTabs().add(tab2);
    	    }
    	    System.out.println(pathOfFile);
    	    ((Button) e.getTarget()).getScene().getWindow().hide();
		});
        Button ou = new Button("Cancel");
        ou.setOnAction(e -> {
        	((Button) e.getTarget()).getScene().getWindow().hide();
        });
		entry1.setOnAction(e -> {
			Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(primaryStage);
            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            BorderPane rr = new BorderPane();
            HBox hbo = new HBox();
            Label high = new Label("Add New Item");
            high.setFont(Font.font ("Verdana", 25));
            hbo.getChildren().add(high);
            hbo.setStyle("-fx-padding: 10 10 10 10;");
            hbo.setAlignment(Pos.CENTER);
            rr.setTop(hbo);
            lom = new ListView();
            lom.setStyle("-fx-background-insets: 0;");
            dialog.setTitle("New Item");
            
            if (pathOfFile.contains(".ino")) {
            	Image img1 = new Image("file:/Users/benjaminsloutsky/Downloads/icons8-arduino-96.png");

                
                
                lom.getItems().add("Arduino File");
                
                
                lom.setCellFactory(param -> new ListCell<String>() {
                    private ImageView imageView = new ImageView();
                    @Override
                    public void updateItem(String name, boolean empty) {
                        super.updateItem(name, empty);
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            if(name.equals("Arduino File")) {
                                imageView.setImage(img1);
                            }
                            setText(name);
                            setGraphic(imageView);
                        }
                    }
                });
                
                rr.setCenter(lom);
            }else {
            	Image img1 = new Image("file:/Users/benjaminsloutsky/Downloads/icons8-code-file-96.png");
                Image img2 = new Image("file:/Users/benjaminsloutsky/Downloads/icons8-header-96.png");
                Image img3 = new Image("file:/Users/benjaminsloutsky/Downloads/icons8-file-96.png");
                Image img4 = new Image("file:/Users/benjaminsloutsky/Downloads/icons8-class-96.png");
                Image img5 = new Image("file:/Users/benjaminsloutsky/Downloads/icons8-cloud-file-96.png");
                Image img6 = new Image("file:/Users/benjaminsloutsky/Downloads/icons8-txt-96.png");

                
                
                lom.getItems().add("Source File");
                lom.getItems().add("Header File");
                lom.getItems().add("Header and Source Files");
                lom.getItems().add("Interface");
                lom.getItems().add("Class");
                lom.getItems().add("Text File");
                
                
                lom.setCellFactory(param -> new ListCell<String>() {
                    private ImageView imageView = new ImageView();
                    @Override
                    public void updateItem(String name, boolean empty) {
                        super.updateItem(name, empty);
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            if(name.equals("Source File")) {
                                imageView.setImage(img1);
                            }
                            else if(name.equals("Header File")) {
                                imageView.setImage(img2);
                            }
                            else if(name.equals("Header and Source Files")) {
                                imageView.setImage(img3);
                            }
                            else if(name.equals("Interface")) {
                                imageView.setImage(img4);
                            }
                            else if(name.equals("Class")) {
                            	imageView.setImage(img5);
                            }
                            else if(name.equals("Text File")) {
                            	imageView.setImage(img6);
                            }
                            setText(name);
                            setGraphic(imageView);
                        }
                    }
                });
                
                rr.setCenter(lom);
            }
            
            
            
            HBox h = new HBox();
            Label l = new Label("Item Name:   ");
            t3 = new TextField();
            
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);     
            spacer.setMinWidth(Region.USE_PREF_SIZE);
            
            h.getChildren().add(l);
            h.getChildren().add(t3);
            HBox.setHgrow(l, Priority.ALWAYS);
            HBox.setHgrow(t3, Priority.ALWAYS);
            
            
            
            //h2.getChildren().add(spacer);
            
            
            
            HBox h3 = new HBox();
            
            h3.getChildren().add(cr);
            h3.getChildren().add(ou);
            
            h.setStyle("-fx-padding: 10 10 10 10;");
            h3.setStyle("-fx-padding: 10 10 10 10;");
            
            h.setAlignment(Pos.CENTER);
            h3.setAlignment(Pos.CENTER_RIGHT);
            
            
            
            VBox vb = new VBox();
            vb.getChildren().addAll(h, new Separator(), h3);
            vb.setAlignment(Pos.CENTER);
            rr.setBottom(vb);
            
            
            Scene sc = new Scene(rr, 500, 500);
            dialog.setScene(sc);
            dialog.show();
		});
		MenuItem entry6 = new MenuItem("Existing Item");
		MenuItem entry4 = new MenuItem("Cut");
		MenuItem entry2 = new MenuItem("Copy");
		MenuItem entry3 = new MenuItem("Paste");
		MenuItem entry5 = new MenuItem("Delete");

		tv.setContextMenu(new ContextMenu(entry1, entry6, new SeparatorMenuItem(), entry4, entry2, entry3, entry5));
		//mb.getItems().addAll(entry1, entry6, new SeparatorMenuItem(), entry4, entry2, entry3, entry5);
		
		tv.setPrefWidth(200);
		
		Accordion accordion = new Accordion();
		TextArea txt = new TextArea();
		
        TitledPane pane1 = new TitledPane("Console", txt);
        accordion.getPanes().add(pane1);
		
		BorderPane root2 = new BorderPane();
		
		root2.setCenter(tabPane);
		root2.setLeft(tv);
		root2.setTop(toolBar);
		root2.setBottom(accordion);
		
		BorderPane root = new BorderPane();
		ToolBar tb = new ToolBar();
		Button btn = new Button("New Project");
		//Stage dialog = new Stage();
		
		
		Button sa = new Button("Create");
        Button ca = new Button("Cancel");
        Button browse = new Button("Browse");
        browse.setOnAction(event -> {
        	DirectoryChooser fff = new DirectoryChooser();
        	File pat = fff.showDialog(primaryStage);
        	if (pat != null) {
        	    String fileAsString = pat.getAbsolutePath();
        	    t2.setText(fileAsString);
        	}
        });
        ca.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {               
               ((Button)t.getTarget()).getScene().getWindow().hide();              
            }
        });
        sa.setOnAction(event -> {
        	String proj = lov.getSelectionModel().getSelectedItem().toString();
        	System.out.println(proj);
        	if (proj == null) {
        		return;
        	}
        	
    	    String entered = t.getText();
    	    
    	    if (entered == ""){
    	    	return;
    	    }
    	    if (!entered.matches("[A-Za-z_]*")) {
    	    	return;
    	    }
	    	//File newF = new File("Stack Projects", entered);
    	    
	    	
	    	
	    	File n = new File("Untitled");
	    	
	    	programmingScene = new Scene(root2);	    	      
	    	programmingScene.getStylesheets().add(Main.class.getResource("application.css").toExternalForm());
	    	
	    	if (proj == "Console Project") {
	    		
	    		
		    	File f = new File(t2.getText());
		    	if (!f.exists()) {
		    		return;
		    	}
	    		n = new File(f, entered);
	    		if (!n.exists()) {
	    			n.mkdirs();
	    			File src = new File(n, "Source Files");
	    			src.mkdirs();
	    			File cpp = new File(src, "main.cpp");
	    			try {
						cpp.createNewFile();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	    			File myFoo = new File(cpp.getAbsolutePath());
	        		FileWriter fooWriter;
					try {
						fooWriter = new FileWriter(myFoo, false);
						fooWriter.write(sampleCode);
		        		fooWriter.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
	    		}else {
	    			return;
	    		}
	    	
	    		
		    	codeArea.replaceText(0, 0, sampleCode);
	    		Tab tab = new Tab("main.cpp", new VirtualizedScrollPane<>(codeArea));
				tabPane.getTabs().add(tab);
		    	Label folder = new Label();
		    	folder.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/folder.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");
		    	Label folder2 = new Label();
		    	folder2.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/folder.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");
		    	rootItem.setValue(entered);
		    	rootItem.setGraphic(folder);
		    	TreeItem ti = new TreeItem();
		    	ti.setValue("Source Files");
		    	ti.setGraphic(folder2);
		    	ti.setExpanded(true);
		    	rootItem.getChildren().add(ti);
		    	TreeItem cppFile = new TreeItem();
		    	Label main = new Label();
		    	main.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/cppFile.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");
		    	cppFile.setValue("main.cpp");
		    	cppFile.setGraphic(main);
		    	ti.getChildren().add(cppFile);
		    	rootItem.setExpanded(true);
		    	mydict.put("main.cpp", sampleCode);
		    	primaryStage.setTitle(entered + " - " + n.getAbsolutePath());
	    	}else if (proj == "Empty Project") {
	    		
	    		
		    	
	    		File f = new File(t2.getText());
		    	if (!f.exists()) {
		    		return;
		    	}
	    		n = new File(f, entered);
	    		if (!n.exists()) {
	    			n.mkdirs();
	    		}else {
	    			return;
	    		}
		    	
	    		
	    		
	    		Label folder = new Label();
		    	folder.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/folder.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");
		    	
		    	rootItem.setValue(entered);
		    	rootItem.setGraphic(folder);
		    	primaryStage.setTitle(entered + " - " + n.getAbsolutePath());
	    	}else if (proj == "Arduino Project") {
	    		
	    		File f = new File(t2.getText());
		    	if (!f.exists()) {
		    		return;
		    	}
	    		n = new File(f, entered + ".ino");
	    		if (!n.exists()) {
	    			try {
						n.createNewFile();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
	    		}else {
	    			return;
	    		}
	    		codeArea.replaceText(0, 0, sampleArduinoCode);
	    		
	    		rootItem.setValue(entered + ".ino");
	    		Label main = new Label();
		    	main.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/cppFile.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");
		    	
		    	rootItem.setGraphic(main);
	    		root2.setCenter(new VirtualizedScrollPane<>(codeArea));
	    		primaryStage.setTitle("Arduino Project");
	    	}
	    	//dialog.close();
	    	pathOfFile = n.getAbsolutePath();
	    	((Button)event.getTarget()).getScene().getWindow().hide();
	    	
	    	
	    	primaryStage.setScene(programmingScene);
	    	primaryStage.setMaximized(true); 
    	    
        });
        
        tv.setOnMouseClicked(e -> {
        	if (e.getClickCount() == 2 && ((TreeItem<String>) tv.getSelectionModel().getSelectedItem()).getValue().contains(".")) {
        		String finPath = "";
        		String name = "";
        		File ur = new File(pathOfFile);
        		String[] dist = ur.list();
        		String pathString = "";
        		for (int i = 0; i < dist.length; i++) {
        			File oo = new File(ur + "/" + dist[i]);
        			String[] hoo = oo.list();
        			for (int o = 0; o < hoo.length; o++) {
        				System.out.println(hoo[o]);
        				if (hoo[o].equals(((TreeItem<String>) tv.getSelectionModel().getSelectedItem()).getValue())) {
        					finPath = ur + "/" + dist[i] + "/" + hoo[o];
        					name = hoo[o];
        				}
        			}
        		}
        		System.out.println(finPath);
        		CodeArea codeArea1 = new CodeArea();

                // add line numbers to the left of area
                codeArea1.setParagraphGraphicFactory(LineNumberFactory.get(codeArea1));
                codeArea1.setContextMenu( new DefaultContextMenu() );

                codeArea1.getVisibleParagraphs().addModificationObserver
                (
                    new VisibleParagraphStyler<>( codeArea1, this::computeHighlighting )
                );

                // auto-indent: insert previous line's indents on enter
                codeArea1.addEventHandler( KeyEvent.KEY_PRESSED, KE ->
                {
                    if ( KE.getCode() == KeyCode.ENTER ) {
                    	int caretPosition = codeArea1.getCaretPosition();
                    	int currentParagraph = codeArea1.getCurrentParagraph();
                        Matcher m0 = whiteSpace.matcher( codeArea1.getParagraph( currentParagraph-1 ).getSegments().get( 0 ) );
                        if ( m0.find() ) Platform.runLater( () -> codeArea1.insertText( caretPosition, m0.group() ) );
                    }
                });
        		Tab tab2 = new Tab(name, codeArea1);
        		
        		//add if statement
        		
        		System.out.println("Not Exists " + tabPane.getTabs());
        		for (int i = 0; i < tabPane.getTabs().size(); i++) {
        			System.out.println(tabPane.getTabs().get(i).getText());
        			if (tabPane.getTabs().get(i).getText().equals(name)) {
        				return;
        			}
        		}
            	tabPane.getTabs().add(tab2);
        		
            	if (mydict.containsKey(name)) {
            		codeArea1.appendText(mydict.get(name));
            	}else {
            		File f = new File(finPath);
                	try (Scanner input = new Scanner(f)) {
                        while (input.hasNextLine()) {
                            codeArea1.appendText(input.nextLine() + "\n");
                        }
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                    }
            	}
                
            	
                	
            	
                	
                	
                
        	}
        	
        });
		btn.setOnAction(event -> {
			Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(primaryStage);
            Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
            BorderPane rr = new BorderPane();
            lov = new ListView();
            lov.setStyle("-fx-background-insets: 0;");
            dialog.setTitle("New Project");
            
            Image img1 = new Image("file:/Users/benjaminsloutsky/Downloads/console-96.png");
            Image img2 = new Image("file:/Users/benjaminsloutsky/Downloads/icons8-menu-squared-96.png");
            Image img3 = new Image("file:/Users/benjaminsloutsky/Downloads/icons8-maximize-window-96.png");
            //Image img4 = new Image("file:/Users/benjaminsloutsky/Downloads/icons8-menu-squared-96.png");
            //Image img5 = new Image("file:/Users/benjaminsloutsky/Downloads/icons8-file-96.png");
            
            
            lov.getItems().add("Console Project");
            lov.getItems().add("Empty Project");
            lov.getItems().add("Arduino Project");
//            lov.getItems().add("OpenGL Project");
//            lov.getItems().add("MakeFile Project");
            lov.setCellFactory(param -> new ListCell<String>() {
                private ImageView imageView = new ImageView();
                @Override
                public void updateItem(String name, boolean empty) {
                    super.updateItem(name, empty);
                    if (empty) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        if(name.equals("Console Project")) {
                            imageView.setImage(img1);
                        }
                        else if(name.equals("Empty Project")) {
                            imageView.setImage(img2);
                        }
                        else if(name.equals("Arduino Project")) {
                            imageView.setImage(img3);
                        }
//                        else if(name.equals("OpenGL Project")) {
//                            imageView.setImage(img4);
//                        }
//                        else if(name.equals("MakeFile Project")) {
//                        	imageView.setImage(img5);
//                        }
                        setText(name);
                        setGraphic(imageView);
                    }
                }
            });
            HBox dos = new HBox();
            Label labs = new Label("Select Project");
            dos.setAlignment(Pos.CENTER);
            labs.setFont(Font.font ("Verdana", 25));
            dos.getChildren().add(labs);
            rr.setTop(dos);
            rr.setCenter(lov);
            HBox h = new HBox();
            Label l = new Label("Project Name:   ");
            t = new TextField();
            
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);     
            spacer.setMinWidth(Region.USE_PREF_SIZE);
            
            h.getChildren().add(l);
            h.getChildren().add(t);
            HBox.setHgrow(l, Priority.ALWAYS);
            HBox.setHgrow(t, Priority.ALWAYS);
            
            Label l2 = new Label("Project Location:   ");
            t2 = new TextField();
            t2.setText(stack.getAbsolutePath());
            HBox.setHgrow(l2, Priority.ALWAYS);
            HBox.setHgrow(t2, Priority.ALWAYS);
            HBox.setHgrow(browse, Priority.ALWAYS);
            
            HBox h2 = new HBox();
            
            h2.getChildren().add(l2);
            h2.getChildren().add(t2);
            h2.getChildren().add(browse);
            
            //h2.getChildren().add(spacer);
            
            
            
            HBox h3 = new HBox();
            
            h3.getChildren().add(sa);
            h3.getChildren().add(ca);
            
            h.setStyle("-fx-padding: 10 10 10 10;");
            h2.setStyle("-fx-padding: 10 10 10 10;");
            h3.setStyle("-fx-padding: 10 10 10 10;");
            
            h.setAlignment(Pos.CENTER);
            h2.setAlignment(Pos.CENTER);
            h3.setAlignment(Pos.CENTER_RIGHT);
            
            
            
            VBox vb = new VBox();
            vb.getChildren().addAll(h, h2, new Separator(), h3);
            vb.setAlignment(Pos.CENTER);
            rr.setBottom(vb);
            Scene sc = new Scene(rr, 500, 500);
            dialog.setScene(sc);
            dialog.show();
			
        	
		});
		Button btn2 = new Button("Open Project");
		btn2.setOnAction(event -> {
			FileChooser fc = new FileChooser();
			fc.setTitle("Open Project");
			fc.showOpenDialog(primaryStage);
		});
		tb.getItems().add(btn);
		tb.getItems().add(btn2);
		ListView lv = new ListView();
		
		lv.setStyle("-fx-background-insets: 0; -fx-cursor: hand;");
		
		String[] name;
		if (stack.exists()) {
			name = stack.list();
			System.out.println(name.toString());
			for (int i = 0; i < name.length; i++) {
				System.out.println(name[i]);
				if (!(name[i].equals(".DS_Store"))) {
					lv.getItems().add("  " + name[i] + "\n  ~StackProjects/" + name[i]);
				}
			}
		}
		lv.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		    	
		        String str = "";
		        for (int i = 2; i < newValue.length(); i++) {
		        	if (newValue.charAt(i) == ' ' || newValue.charAt(i) == '\n') {
		        		break;
		        	}else {
		        		str += newValue.charAt(i);
		        	}
		        }
		        System.out.println(str);
		        File c = new File("StackProjects");
		        File r = new File(c, str);
		        if (str.contains(".ino")) {
		        	
					programmingScene = new Scene(root2);    	    	
	    	    	programmingScene.getStylesheets().add(Main.class.getResource("application.css").toExternalForm());
	    	    	primaryStage.setTitle(str + " - " + r.getAbsolutePath());
	    	    	primaryStage.setScene(programmingScene);
	    	    	primaryStage.setMaximized(true);
	    	    	String[] files;
	    	    	files = r.list();
	    	    	Label foldero = new Label();
			    	foldero.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/folder.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");

	    	    	rootItem.setValue(str);
	    	    	Label main = new Label();
			    	main.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/cppFile.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");

	    	    	rootItem.setGraphic(main);
	    	    
	    	    	
	    	    	
	    	    	
		        }else {
		        	
					programmingScene = new Scene(root2);    	    	
	    	    	programmingScene.getStylesheets().add(Main.class.getResource("application.css").toExternalForm());
	    	    	primaryStage.setTitle(str + " - " + r.getAbsolutePath());
	    	    	primaryStage.setScene(programmingScene);
	    	    	primaryStage.setMaximized(true);
	    	    	String[] files;
	    	    	files = r.list();
	    	    	Label foldero = new Label();
			    	foldero.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/folder.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");

	    	    	rootItem.setValue(str);
	    	    	rootItem.setGraphic(foldero);
	    	    
	    	    	
	    	    	
	    	    	
	    	    	for (int i = 0; i < files.length; i++) {
	    	    		System.out.println(files[i]);
	    	    		Label folder = new Label();
	    		    	folder.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/folder.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");

	    	    		TreeItem ti = new TreeItem();
	    	    		ti.setValue(files[i].toString());
	    	    		ti.setGraphic(folder);
	    	    		rootItem.getChildren().add(ti);
	    	    		File m = new File(r, files[i]);
	    	    		String[] gho = m.list();
	    	    		for (int o = 0; o < gho.length; o++) {
	    	    			TreeItem to = new TreeItem();
	    	    			Label main = new Label();
	    	    			if (gho[o].contains(".cpp")) {
	    	    				main.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/cppFile.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");

	    	    			}else if (gho[o].contains(".h")){
		    			    	main.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/header.jpg'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");
	    	    			}else if (gho[o].contains(".txt")) {
		    			    	main.setStyle("-fx-background-color: transparent;-fx-background-image: url('file:/Users/benjaminsloutsky/Downloads/txt.png'); -fx-background-size: 100% 100%; -fx-background-repeat: no-repeat; -fx-min-width:15px; -fx-min-height:15px;");
	    	    			}
	    	    			
	        	    		to.setValue(gho[o].toString());
	        	    		to.setGraphic(main);
	        	    		ti.getChildren().add(to);
	    	    		}
	    	    	}
		        }
		        pathOfFile = r.getAbsolutePath();

		    }
		});
		
		codeArea.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
		    @Override
		    public void handle(KeyEvent e) {
		        if (e.getCode() == KeyCode.TAB) {
		        	codeArea.insertText(codeArea.getCaretPosition(), "   ");
		            e.consume();
		        }
		    }
		});
		
		root.setCenter(lv);
		root.setTop(tb);
		Scene scene = new Scene(root,400,400);
		primaryStage.setScene(scene);
		primaryStage.setTitle("Welcome to Stack");
		primaryStage.show();
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	private void saveTextToFile(String text, File f) {
    	try {
            PrintWriter writer;
            writer = new PrintWriter(f);
            writer.println(text);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
		
	}
	private StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                    matcher.group("HASH") != null ? "hash" :
                    matcher.group("PAREN") != null ? "paren" :
                    matcher.group("BRACE") != null ? "brace" :
                    matcher.group("BRACKET") != null ? "bracket" :
                    matcher.group("LEGE") != null ? "lege" :
                    matcher.group("SEMICOLON") != null ? "semicolon" :
                    matcher.group("STRING") != null ? "string" :
                    matcher.group("COMMENT") != null ? "comment" :
                    null; /* never happens */ assert styleClass != null;
            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        return spansBuilder.create();
    }

    private class VisibleParagraphStyler<PS, SEG, S> implements Consumer<ListModification<? extends Paragraph<PS, SEG, S>>>
    {
        private final GenericStyledArea<PS, SEG, S> area;
        private final Function<String,StyleSpans<S>> computeStyles;
        private int prevParagraph, prevTextLength;

        public VisibleParagraphStyler( GenericStyledArea<PS, SEG, S> area, Function<String,StyleSpans<S>> computeStyles )
        {
            this.computeStyles = computeStyles;
            this.area = area;
        }

        @Override
        public void accept( ListModification<? extends Paragraph<PS, SEG, S>> lm )
        {
            if ( lm.getAddedSize() > 0 )
            {
                int paragraph = Math.min( area.firstVisibleParToAllParIndex() + lm.getFrom(), area.getParagraphs().size()-1 );
                String text = area.getText( paragraph, 0, paragraph, area.getParagraphLength( paragraph ) );

        	    if ( paragraph != prevParagraph || text.length() != prevTextLength )
        	    {
                    int startPos = area.getAbsolutePosition( paragraph, 0 );
                    Platform.runLater( () -> area.setStyleSpans( startPos, computeStyles.apply( text ) ) );
                    prevTextLength = text.length();
                    prevParagraph = paragraph;
        	    }
        	}
        }
    }

    private class DefaultContextMenu extends ContextMenu
    {
        private MenuItem fold, unfold, print;

        public DefaultContextMenu()
        {
            fold = new MenuItem( "Fold selected text" );
            fold.setOnAction( AE -> { hide(); fold(); } );

            unfold = new MenuItem( "Unfold from cursor" );
            unfold.setOnAction( AE -> { hide(); unfold(); } );

            print = new MenuItem( "Print" );
            print.setOnAction( AE -> { hide(); print(); } );

            getItems().addAll( fold, unfold, print );
        }

        /**
         * Folds multiple lines of selected text, only showing the first line and hiding the rest.
         */
        private void fold() {
            ((CodeArea) getOwnerNode()).foldSelectedParagraphs();
        }

        /**
         * Unfold the CURRENT line/paragraph if it has a fold.
         */
        private void unfold() {
            CodeArea area = (CodeArea) getOwnerNode();
            area.unfoldParagraphs( area.getCurrentParagraph() );
        }

        private void print() {
            System.out.println( ((CodeArea) getOwnerNode()).getText() );
        }
    }
}
