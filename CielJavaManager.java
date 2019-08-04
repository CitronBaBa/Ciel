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
import com.github.javaparser.resolution.declarations.*;

import java.time.Duration;
import java.util.*;
import java.io.*;
import java.util.Collections;
import javafx.scene.paint.*;

public class CielJavaManager implements Serializable
{   private Map<String,Etoile> classes = new HashMap<>();
    private Map<String,Set<String>> classDependents = new HashMap<>();

    private Map<String,double[]> interfaces = new HashMap<>();
    private Map<String,List<Etoile>> implementations = new HashMap<>();

    private final File parsePath = new File("./");
    private final File jarPath = new File("./FX/lib");  

    private boolean debugging = false;

    CielJavaManager()
    { ;
    }
    CielJavaManager(boolean debugging)
    {   this.debugging = debugging;
    }
    

    public Map<String,double[]> getInterfaces()
    {   return interfaces;
    }
    public Map<String,List<Etoile>> getImplementations()
    {   return implementations;
    }
    public Set<Etoile> getDependentEtoiles(String className)
    {   Set<Etoile> dependentEtoiles = new HashSet<>();
        Set<String> dependentClasses = classDependents.get(className);

        //no such className 
        if(dependentClasses == null) return null;

        for(String dependentName : dependentClasses)
        {   dependentEtoiles.add(classes.get(dependentName));
        }
        return dependentEtoiles;
    }

    public List<Etoile> readJavaFiles(List<File> targetFiles)
    {   List<ClassOrInterfaceDeclaration> javaClassDeclares = new ArrayList<>();
        List<Etoile> recordedEtoiles = new ArrayList<>();
        for(File file: targetFiles)
        {   readJavaFile(file,javaClassDeclares,recordedEtoiles);
        }
        for(ClassOrInterfaceDeclaration c : javaClassDeclares)
        {   recordInheritance(c);
            recordInnerClass(c);
        }

        //optianal operation
        for(File file: targetFiles)
        {   recordDependency(file);
        }

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
            {   double[] color = {Math.random(),Math.random(),Math.random(),1.0f};
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

    private void recordInnerClass(ClassOrInterfaceDeclaration c)
    {    // check parent is a class
         if(c.getParentNode().get().getClass()!=ClassOrInterfaceDeclaration.class)
         {   return ;
         }

         //whther itself is a interface 
         if(c.isInterface()) return;

         // already a child because of inheritance
         if(classes.get(c.getName().toString()).isSubStar())
         {   return ;
         }


         ClassOrInterfaceDeclaration parent = (ClassOrInterfaceDeclaration)c.getParentNode().get();
         Etoile parentEtoile = classes.get(parent.getName().toString());
         Etoile childEtoile = classes.get(c.getName().toString());
         childEtoile.becomeSubStar(parentEtoile);
         parentEtoile.addChild(childEtoile);
    }

// this should happens after recording classes
    private void recordDependency(File targetFile)
    {   JavaParserTypeSolver psolver = new JavaParserTypeSolver(parsePath.getAbsolutePath());
        ReflectionTypeSolver rsolver = new ReflectionTypeSolver();

        CombinedTypeSolver combinedSolver = new CombinedTypeSolver();
        combinedSolver.add(psolver);
        combinedSolver.add(rsolver);

        for(File jar : jarPath.listFiles())
        {   if(!jar.getPath().endsWith(".jar")) continue;
            try 
            {  JarTypeSolver jarSolver = new JarTypeSolver(jar.getPath()); 
                combinedSolver.add(jarSolver);
            }
            catch(Exception e) 
            {   System.out.println(e);
                e.printStackTrace(); 
            }
        }

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedSolver);
        StaticJavaParser.getConfiguration().setSymbolResolver(symbolSolver);
        CompilationUnit cu;
        try { cu = StaticJavaParser.parse(targetFile); }
        catch(Exception e) {  e.printStackTrace(); return;}

        List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);
        for(ClassOrInterfaceDeclaration c :classes)
        recordClassDependents(c);
    }

    private void recordClassDependents(ClassOrInterfaceDeclaration c)
    {   Set<String> finalDependents = new HashSet<>();
        // getFieldsDependents(c);
        Set<String> set1 = getVariablesDependents(c);
        Set<String> set2 = getMethodDeclarationDependents(c);
        Set<String> set3 =getMethodCallDependents(c);
        finalDependents.addAll(set1);
        finalDependents.addAll(set2);
        finalDependents.addAll(set3);
        
        String className = c.getName().toString();
        // a hack to remove itself, could be done in functions
        finalDependents.remove(className);
        classDependents.put(className,finalDependents);
        System.out.println(className+":  "+finalDependents);
    }

    private Set<String> getFieldsDependents(Node anyNode)
    {   Set<String> dependents = new HashSet<>();

        List<FieldDeclaration> fields = anyNode.findAll(FieldDeclaration.class);
        for(FieldDeclaration f : fields)
        {   for(VariableDeclarator vd: f.getVariables())
            try
            {    ResolvedType rtype = vd.getType().resolve();
                 String name = rtype.describe();
                 dependents.addAll(getDependentNames(rtype));
                    
                 if(debugging) 
                 {   System.out.println("Field type: " + name);
                     System.out.println(rtype.asReferenceType().typeParametersValues());
                     System.out.println(f + "  solved\n\n");
                 }
            }
            catch( Exception e)
            {    if(debugging) 
                 {   System.out.println(f + "  unsolved");
                     e.printStackTrace();
                     System.out.println("\n\n");
                 }
            }
        }
        return dependents;
    }

    private Set<String> getVariablesDependents(Node anyNode)
    {   Set<String> dependents = new HashSet<>();

        List<VariableDeclarator> variables = anyNode.findAll(VariableDeclarator.class);
        for(VariableDeclarator v : variables)
        {   try
            {    ResolvedType rtype = v.resolve().getType();
                 String name = rtype.describe();
                 dependents.addAll(getDependentNames(rtype));
            
                 if(debugging)
                 {   System.out.println("variable type: " + name);
                     if(rtype.isReferenceType())
                     {  System.out.println(rtype.asReferenceType().typeParametersValues());
                     }
                     System.out.println(v + "  solved\n\n");
                }
            }
            catch( Exception e)
            {    if(debugging)
                 {  System.out.println(v + "  unsolved");
                    e.printStackTrace();
                    System.out.println("\n\n");
                 }
            }
        }
        return dependents;
    }
    
    private Set<String> getMethodDeclarationDependents(ClassOrInterfaceDeclaration c)
    {   Set<String> dependents = new HashSet<>();
        try
        {   for(MethodUsage m : c.resolve().getAllMethods())
            {   dependents.addAll(getDependentNames(m));
                if(debugging) System.out.println("---------"+m.returnType().describe() + "---" + m.getName());
            }
        } catch(UnsolvedSymbolException e) 
        {   if(debugging)
            {   System.out.println(c.getName()+"-- unsolved"); e.printStackTrace();
            }
        }
        return dependents;
    }

    private Set<String> getMethodCallDependents(ClassOrInterfaceDeclaration c)
    {   Set<String> dependents = new HashSet<>();
        List<MethodCallExpr> calls = c.findAll(MethodCallExpr.class);
        for(MethodCallExpr mc : calls)
        {   try
            {   dependents.addAll(getDependentNames(mc.resolve()));
                if(debugging) System.out.println("---------"+mc.resolve().getReturnType().describe()+"----"+ mc.getName());
            }
            catch(Exception e ) 
            {   if(debugging)
                {   System.out.println(mc.getName()+"---unsolved"); e.printStackTrace();}
            }
        }
        return dependents;
    }

    private Set<String> getDependentNames(ResolvedMethodDeclaration rmd)
    {   Set<String> dependents = new HashSet<>();
        
        if(isRelevantDependent(rmd.getClassName())) dependents.add(rmd.getClassName());
        dependents.addAll(getDependentNames(rmd.getReturnType()));
        //params 
        for(int i=0; i<rmd.getNumberOfParams(); i++)
        {   ResolvedType rtype = rmd.getParam(i).getType();
            dependents.addAll(getDependentNames(rtype));
            if(debugging) System.out.println("p: "+getDependentNames(rtype));
        }
        
        return dependents;
    }

    private Set<String> getDependentNames(MethodUsage mu)
    {   return getDependentNames(mu.getDeclaration()); 
        // Set<String> dependents = new HashSet<>();
        // dependents.addAll(getDependentNames(mu.returnType()));
        // 
        // //params 
        // for(int i=0; i<mu.getNoParams(); i++)
        // {   ResolvedType rtype = mu.getParamType(i);
        //     dependents.addAll(getDependentNames(rtype));
        //     System.out.println("p: "+getDependentNames(rtype));
        // }
        // 
        // return dependents;
    }

    private Set<String> getDependentNames(ResolvedType rtype)
    {   Set<String> dependents = new HashSet<>();
        if(isRelevantDependent(rtype.describe()))
        dependents.add(rtype.describe());

        if(rtype.isArray())
        {   dependents.addAll(
                getDependentNames(
                    rtype.asArrayType().getComponentType()
            ));
        }
        
        if(rtype.isReferenceType())
        {   for(ResolvedType typeParam : rtype.asReferenceType().typeParametersValues())
            {   dependents.addAll(getDependentNames(typeParam));
            }
        }

        return dependents;
    }

    private boolean isRelevantDependent(String name)
    {   if(classes.containsKey(name))  return true;
        else return false;
    }

//debug note  :  constructer, enumeration un solved;
    public static void main(String[] args) 
    {   CielJavaManager newManager = new CielJavaManager();
        File root = new File("./");
        List<File> testFiles = new ArrayList<>();

        for(File f : root.listFiles())
        {   if(f.getPath().endsWith(".java"))
            testFiles.add(f);
        }
        newManager.readJavaFiles(testFiles); 
    }

}
