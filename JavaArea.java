import java.time.Duration;
import java.util.*;
import java.io.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Parent;
import javafx.scene.input.*;
import javafx.scene.Scene;
import javafx.scene.*;
import javafx.stage.Stage;
import javafx.fxml.Initializable;
import javafx.scene.shape.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.paint.Color;
import javafx.scene.effect.*;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.utils.*;
import com.github.javaparser.resolution.*;
import com.github.javaparser.*;
import com.github.javaparser.symbolsolver.*;
import com.github.javaparser.symbolsolver.javaparser.*;
import com.github.javaparser.symbolsolver.resolution.typesolvers.*;
import com.github.javaparser.resolution.types.*;
import com.github.javaparser.ast.expr.*;

// an text area with java syntax highlighting and line count 

public class JavaArea
{   private StackPane javaArea;
    private CodeArea codeArea;
    private int fontsize = 14;
    
    public void increaseFontSize()
    {   ++fontsize;
        codeArea.setStyle("-fx-font-size:"+fontsize+"px");
    }
    public void decreaseFontSize()
    {   --fontsize;
        codeArea.setStyle("-fx-font-size:"+fontsize+"px");
    }

    public Region getArea()
    {   return javaArea;
    }

    public String getCode()
    {   return codeArea.getText();
    }

    public void loadText(String text)
    {   codeArea.replaceText(0,codeArea.getLength(),text);
        codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
        //refresh should be added here
    }

    public JavaArea()
    {   codeArea = new CodeArea();
        
        // add line numbers to the left of area
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        // recompute the syntax highlighting 500 ms after user stops editing area
        Subscription cleanupWhenNoLongerNeedIt = codeArea

                // plain changes = ignore style changes that are emitted when syntax highlighting is reapplied
                // multi plain changes = save computation by not rerunning the code multiple times
                //   when making multiple changes (e.g. renaming a method at multiple parts in file)
                .multiPlainChanges()

                // do not emit an event until 500(50 for me) ms have passed since the last emission of previous stream
                .successionEnds(Duration.ofMillis(50))

                // run the following code block when previous stream emits an event
                .subscribe(ignore -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));

        // when no longer need syntax highlighting and wish to clean up memory leaks
        // run: `cleanupWhenNoLongerNeedIt.unsubscribe();`

        codeArea.replaceText(0, 0, sampleCode);
        
        javaArea = new StackPane(new VirtualizedScrollPane<>(codeArea));
        buttonSetUp();
    }
    
    private void buttonSetUp()
    {   Button btn = new Button("+");
        Button btn2 = new Button("-");
        btn.setOnAction(e->increaseFontSize());
        btn2.setOnAction(e->decreaseFontSize());
        AnchorPane anchorPane = new AnchorPane();
        btn2.widthProperty().addListener((obs,oldV,newV)->
        {   AnchorPane.setRightAnchor(btn,18+btn2.getWidth()+3);  
        });
        AnchorPane.setBottomAnchor(btn,25d);
        AnchorPane.setRightAnchor(btn2,18d);
        AnchorPane.setBottomAnchor(btn2,25d);
        anchorPane.getChildren().addAll(btn,btn2);
        anchorPane.setPickOnBounds(false);
        javaArea.getChildren().add(anchorPane);
    }


    private static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder
                = new StyleSpansBuilder<>();
        while(matcher.find()) {
            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                    matcher.group("PAREN") != null ? "paren" :
                    matcher.group("BRACE") != null ? "brace" :
                    matcher.group("BRACKET") != null ? "bracket" :
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

    private static final String[] KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<KEYWORD>" + KEYWORD_PATTERN + ")"
            + "|(?<PAREN>" + PAREN_PATTERN + ")"
            + "|(?<BRACE>" + BRACE_PATTERN + ")"
            + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
            + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
            + "|(?<STRING>" + STRING_PATTERN + ")"
            + "|(?<COMMENT>" + COMMENT_PATTERN + ")"
    );

    private static final String sampleCode = String.join("\n", new String[] {
       "package com.example;",
       "",
       "import java.util.*;",
       "",
       "public class Foo extends Bar implements Baz {",
       "    ",
       "    /*",
       "     * multi-line comment",
       "     */",
       "    public static void main(String[] args) {",
       "        // single-line comment",
       "        for(String arg: args) {",
       "            if(arg.length() != 0)",
       "                System.out.println(arg);",
       "            else",
       "                System.err.println(\"Warning: empty string as argument\");",
       "        }",
       "        Align.print(arg);",
       "    }",
       "",
       "}"
    });
}
