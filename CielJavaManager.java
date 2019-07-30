import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.*;
import com.github.javaparser.utils.*;
import com.github.javaparser.*;
import java.time.Duration;
import java.util.*;
import java.io.*;
import java.util.Collections;
import javafx.scene.paint.*;

public class CielJavaManager implements Serializable
{   private Map<String,Etoile> classes = new HashMap<>();
    private Map<String,double[]> interfaces = new HashMap<>(); 
    private Map<String,List<Etoile>> implementations = new HashMap<>();

    public List<Etoile> readJavaFiles(List<File> targetFiles)
    {   List<ClassOrInterfaceDeclaration> javaClassDeclares = new ArrayList<>();
        List<Etoile> recordedEtoiles = new ArrayList<>();
        for(File file: targetFiles)
        {   readJavaFile(file,javaClassDeclares,recordedEtoiles);
        }
        for(ClassOrInterfaceDeclaration c : javaClassDeclares)
        {   recordInheritance(c);    
        }
        System.out.println(implementations);
        return recordedEtoiles;
    }


    private void readJavaFile(File targetFile, List<ClassOrInterfaceDeclaration> javaClassDeclares, List<Etoile> recordedEtoiles)
    {   CompilationUnit compilationUnit;
        try
        {    compilationUnit = StaticJavaParser.parse(targetFile);
        }
        catch(Exception e) {  e.printStackTrace(); return;}
        List<ClassOrInterfaceDeclaration> classesFromCode = compilationUnit.findAll(ClassOrInterfaceDeclaration.class);
        for(ClassOrInterfaceDeclaration c : classesFromCode)
        {   Etoile newEtoile = recordEtoileFromJava(c);
            if(newEtoile!=null) recordedEtoiles.add(newEtoile);
        }
        javaClassDeclares.addAll(classesFromCode);
    }

    private Etoile recordEtoileFromJava(ClassOrInterfaceDeclaration c)
    {   if(c.isInterface()) return null;
        String className = c.getName().toString();

        // two identical classes situation
        if(classes.containsKey(className)) return null;

        Etoile newEtoile = new Etoile(className);
        newEtoile.setText(c.toString());
        this.classes.put(className,newEtoile);

        NodeList<ClassOrInterfaceType> implementeds = c.getImplementedTypes();
        for(ClassOrInterfaceType i: implementeds)
        {   String interfaceName = i.getName().toString();
            if(!interfaces.containsKey(interfaceName))
            {   double[] color = {255,0,0,1};
                interfaces.put(interfaceName,color);
                implementations.put(interfaceName,new ArrayList<Etoile>());
            }
            implementations.get(interfaceName).add(newEtoile);
        }  
        
        return newEtoile;
    }

    private void recordInheritance(ClassOrInterfaceDeclaration c)
    {   NodeList<ClassOrInterfaceType> extended = c.getExtendedTypes();
        if(extended.size()!=0)
        {   String parentClassName = extended.get(0).getName().toString();
            String childClassName = c.getName().toString(); 
            Etoile parentEtoile = classes.get(parentClassName);
            Etoile childEtoile = classes.get(childClassName);

            //his parent is not here
            if(parentEtoile==null) return;
            
            childEtoile.becomeSubStar(parentEtoile);
            parentEtoile.addChild(childEtoile);
        }
        
    }


}
